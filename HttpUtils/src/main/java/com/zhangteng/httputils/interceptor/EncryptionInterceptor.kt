package com.zhangteng.httputils.interceptor

import android.text.TextUtils
import android.util.Log
import com.google.gson.JsonParser
import com.zhangteng.httputils.config.EncryptConfig
import com.zhangteng.httputils.config.SPConfig
import com.zhangteng.httputils.http.GlobalHttpUtils
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.utils.DiskLruCacheUtils
import com.zhangteng.utils.AESUtils
import com.zhangteng.utils.RSAUtils
import com.zhangteng.utils.getFromSP
import com.zhangteng.utils.putToSP
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * 添加加密拦截器
 * Created by Swing on 2019/10/20.
 */
class EncryptionInterceptor : PriorityInterceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val headers = request.headers
        if (headers.names()
                .contains(EncryptConfig.SECRET) && "true" == headers[EncryptConfig.SECRET]
        ) {
            var secretRequest: Request? =
                buildRequest(request) ?: return getErrorSecretResponse(request)
            var secretResponse: Response = chain.proceed(secretRequest!!)
            val secretResponseBody = secretResponse.body
            val secretResponseStr = secretResponseBody?.string() ?: ""
            val jsonObject = JsonParser().parse(
                secretResponseStr.substring(
                    0,
                    secretResponseStr.lastIndexOf("}") + 1
                )
            ).asJsonObject
            val jsonElement = jsonObject["status"]
            if (jsonElement != null && !jsonElement.isJsonArray
                && !jsonElement.isJsonObject
                && !jsonElement.isJsonNull
                && EncryptConfig.SECRET_ERROR.toString() == jsonElement.asString
            ) {
                //服务端响应加解密错误状态码时清除本地秘钥
                HttpUtils.instance.context
                    .putToSP(EncryptConfig.SECRET, "", SPConfig.FILE_NAME)
                DiskLruCacheUtils.remove(EncryptConfig.publicKeyUrl)
                DiskLruCacheUtils.flush()
                //重新发起加密请求
                secretRequest = buildRequest(request)
                if (secretRequest == null) {
                    return getErrorSecretResponse(request)
                }
                secretResponse = chain.proceed(secretRequest)
            } else {
                val mediaType =
                    if (secretResponseBody != null) secretResponseBody.contentType() else "application/json;charset=UTF-8".toMediaTypeOrNull()
                val newResonseBody = secretResponseStr.toResponseBody(mediaType)
                secretResponse = secretResponse.newBuilder().body(newResonseBody).build()
            }
            return secretResponse
        }
        return chain.proceed(request)
    }

    /**
     * description 保证加密时较晚执行且避免特殊情况需要加密之后执行的Interceptor因此返回Integer.MAX_VALUE - 1
     */
    override val priority: Int
        get() = Int.MAX_VALUE - 1

    /**
     * 构建加密请求
     *
     * 1、客户端与服务端交换秘钥
     * 2、客户端生成AES秘钥
     * 3、客户端使用RSA秘钥加密AES秘钥并放入请求头
     * 4、客户端使用AES秘钥加密请求数据放入请求体
     * 5、服务端使用RSA秘钥解密请求头密文，获取AES秘钥
     * 6、服务端使用AES秘钥解密请求体获取请求数据
     * 7、服务端返回处理结果(使用AES加密数据放入响应体)
     * 8、客户端使用AES秘钥解密响应体获取响应数据
     *
     * @param request 原请求
     */
    @Throws(IOException::class)
    protected fun buildRequest(request: Request): Request? {
        //1、客户端与服务端交换秘钥
        exchangeSecretKey()

        //2、客户端生成AES秘钥
        val aesRequestKey = AESUtils.key
        //3、客户端使用RSA秘钥加密AES秘钥并放入请求头
        val requestBuilder: Request.Builder = request.newBuilder()
        try {
            requestBuilder.removeHeader(EncryptConfig.SECRET)
            val keyPair = HttpUtils.instance.context.getFromSP(
                EncryptConfig.SECRET,
                "",
                SPConfig.FILE_NAME,
            ) as String?
            //如果本地未存储客户端私钥则使用默认服务器公钥交换数据
            val m = if (keyPair.isNullOrEmpty()) {
                RSAUtils.encryptByPublicKey(aesRequestKey, EncryptConfig.publicKey)
            } else {
                RSAUtils.encryptByPrivateKey(aesRequestKey, keyPair)
            }
            requestBuilder.addHeader(EncryptConfig.SECRET, m)
            //用AES秘钥的密文作为key，AES秘钥作为value添加到请求头中，在解密拦截器中取出使用
            requestBuilder.addHeader(m, aesRequestKey)
        } catch (e: Exception) {
            return null
        }
        //4、客户端使用AES秘钥加密请求数据放入请求体
        if (METHOD_GET == request.method) {
            val url = request.url.toUrl().toString()
            val paramsBuilder = url.substring(url.indexOf("?") + 1)
            try {
                val encryptParams =
                    AESUtils.encrypt(paramsBuilder, aesRequestKey, aesRequestKey.substring(0, 16))
                requestBuilder.url(url.substring(0, url.indexOf("?")) + "?" + encryptParams)
            } catch (e: Exception) {
                return null
            }
        } else if (METHOD_POST == request.method) {
            val requestBody = request.body
            if (requestBody != null && aesRequestKey.length >= 16) {
                if (requestBody is FormBody) {
                    val formBody = request.body as FormBody?
                    val bodyBuilder = FormBody.Builder()
                    try {
                        if (formBody != null) {
                            for (i in 0 until formBody.size) {
                                val value = formBody.encodedValue(i)
                                if (!TextUtils.isEmpty(value)) {
                                    val encryptParams = AESUtils.encrypt(
                                        value,
                                        aesRequestKey,
                                        aesRequestKey.substring(0, 16)
                                    )
                                    bodyBuilder.addEncoded(formBody.encodedName(i), encryptParams)
                                }
                            }
                            requestBuilder.post(bodyBuilder.build())
                        }
                    } catch (e: Exception) {
                        return null
                    }
                } else {
                    val buffer = Buffer()
                    requestBody.writeTo(buffer)
                    var charset = StandardCharsets.UTF_8
                    val contentType = requestBody.contentType()
                    if (contentType != null) {
                        charset = contentType.charset()
                    }
                    val paramsRaw = buffer.readString(charset ?: Charset.defaultCharset())
                    if (!TextUtils.isEmpty(paramsRaw)) {
                        try {
                            val encryptParams =
                                AESUtils.encrypt(
                                    paramsRaw,
                                    aesRequestKey,
                                    aesRequestKey.substring(0, 16)
                                )
                            requestBuilder.post(
                                encryptParams
                                    .toRequestBody(requestBody.contentType())
                            )
                        } catch (e: Exception) {
                            return null
                        }
                    }
                }
            }
        }
        return requestBuilder.build()
    }

    /**
     * 客户端与服务端交换秘钥
     */
    protected fun exchangeSecretKey() {
        val localKeyPair = HttpUtils.instance.context.getFromSP(
            EncryptConfig.SECRET,
            "",
            SPConfig.FILE_NAME
        ) as String?
        if (localKeyPair.isNullOrEmpty()) {
            //无服务端公钥获取接口时不交换秘钥
            if (EncryptConfig.publicKeyUrl == null) {
                Log.i("EncryptionInterceptor", "未设置服务端公钥获取接口")
                return
            }
            //获取服务端公钥sPubKey
            val secretResponse: Response = GlobalHttpUtils.instance.okHttpClient
                .newCall(Request.Builder().url(EncryptConfig.publicKeyUrl!!).build()).execute()
            if (secretResponse.code == 200) {
                try {
                    val secretResponseString = secretResponse.body?.string()
                    val result = JsonParser().parse(secretResponseString).asJsonObject
                    val sPubKey = result["result"].asJsonObject["publicKey"].asString
                    HttpUtils.instance.context
                        .putToSP(EncryptConfig.SECRET, sPubKey, SPConfig.FILE_NAME)
                } catch (exception: NullPointerException) {
                    Log.i("EncryptionInterceptor", "秘钥交换失败")
                    return
                }
            } else {
                Log.i("EncryptionInterceptor", "秘钥交换失败")
                return
            }
        }
    }

    /**
     * 获取加密失败响应
     */
    protected fun getErrorSecretResponse(request: Request): Response {
        val failureResponse = Response.Builder()
        failureResponse.request(request)
        failureResponse.body(
            String.format(
                "{\"message\": \"移动端加密失败\",\"status\": %s}",
                EncryptConfig.SECRET_ERROR
            )
                .toResponseBody("application/json;charset=UTF-8".toMediaTypeOrNull())
        )
        return failureResponse.build()
    }

    companion object {
        const val METHOD_GET = "GET"
        const val METHOD_POST = "POST"
    }
}