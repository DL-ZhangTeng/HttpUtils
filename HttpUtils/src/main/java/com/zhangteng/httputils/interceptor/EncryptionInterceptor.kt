package com.zhangteng.httputils.interceptor

import android.text.TextUtils
import com.google.gson.JsonParser
import com.zhangteng.httputils.config.EncryptConfig
import com.zhangteng.httputils.config.SPConfig
import com.zhangteng.httputils.http.GlobalHttpUtils
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.utils.DiskLruCacheUtils
import com.zhangteng.utils.AESUtils.encrypt
import com.zhangteng.utils.AESUtils.key
import com.zhangteng.utils.RSAUtils.encryptByPublicKey
import com.zhangteng.utils.getFromSP
import com.zhangteng.utils.putToSP
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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
                HttpUtils.instance.context
                    .putToSP(SPConfig.FILE_NAME, EncryptConfig.SECRET, "")
                DiskLruCacheUtils.remove(EncryptConfig.publicKeyUrl)
                DiskLruCacheUtils.flush()
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
     * description 保证加密时优先执行且避免特殊情况需要广域晚于解密之前执行的Interceptor因此返回Integer.MAX_VALUE - 1
     */
    override val priority: Int
        get() = Int.MAX_VALUE - 1

    /**
     * 构建加密请求
     *
     * @param request 原请求
     */
    @Throws(IOException::class)
    protected fun buildRequest(request: Request): Request? {
        if (TextUtils.isEmpty(
                HttpUtils.instance.context
                    .getFromSP(SPConfig.FILE_NAME, EncryptConfig.SECRET, "") as CharSequence
            )
        ) {
            val secretResponse: Response = GlobalHttpUtils.instance.getOkHttpClient()
                .newCall(Request.Builder().url(EncryptConfig.publicKeyUrl!!).build()).execute()
            if (secretResponse.code == 200) {
                try {
                    val secretResponseString = secretResponse.body?.string()
                    val jsonObject = JsonParser().parse(secretResponseString).asJsonObject
                    val jsonElement = jsonObject["result"].asJsonObject["publicKey"]
                    HttpUtils.instance.context
                        .putToSP(SPConfig.FILE_NAME, EncryptConfig.SECRET, jsonElement.asString)
                } catch (exception: NullPointerException) {
                    return null
                }
            } else {
                return null
            }
        }
        val aesRequestKey = key
        val requestBuilder: Request.Builder = request.newBuilder()
        requestBuilder.removeHeader(EncryptConfig.SECRET)
        try {
            requestBuilder.addHeader(
                EncryptConfig.SECRET,
                encryptByPublicKey(
                    aesRequestKey,
                    HttpUtils.instance.context.getFromSP(
                        SPConfig.FILE_NAME,
                        EncryptConfig.SECRET,
                        EncryptConfig.publicKey
                    ) as String
                )
            )
        } catch (e: Exception) {
            return null
        }
        if (METHOD_GET == request.method) {
            val url = request.url.toUrl().toString()
            val paramsBuilder = url.substring(url.indexOf("?") + 1)
            try {
                val encryptParams =
                    encrypt(paramsBuilder, aesRequestKey, aesRequestKey.substring(0, 16))
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
                                    val encryptParams = encrypt(
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
                                encrypt(paramsRaw, aesRequestKey, aesRequestKey.substring(0, 16))
                            requestBuilder.post(
                                RequestBody.create(
                                    requestBody.contentType(),
                                    encryptParams
                                )
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