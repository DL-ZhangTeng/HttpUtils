package com.zhangteng.httputils.interceptor

import android.text.TextUtils
import com.zhangteng.httputils.config.EncryptConfig
import com.zhangteng.httputils.config.SPConfig
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.utils.AESUtils
import com.zhangteng.utils.RSAUtils
import com.zhangteng.utils.getFromSP
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

/**
 * 添加解密拦截器
 * Created by Swing on 2019/10/20.
 */
class DecryptionInterceptor : PriorityInterceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        if (!response.isSuccessful || response.code != 200) {
            return response
        }
        val responseBuilder: Response.Builder = response.newBuilder()
        val responseBody = response.body
        val responseHeaders = response.headers
        for (name in responseHeaders.names()) {
            if (EncryptConfig.SECRET.contains(name) && !TextUtils.isEmpty(responseHeaders[name])) {
                return try {
                    val encryptKey = responseHeaders[name]
                    val keyPair = HttpUtils.instance.context.getFromSP(
                        EncryptConfig.SECRET,
                        "",
                        SPConfig.FILE_NAME,
                    ) as String?
                    //如果本地未存储客户端私钥则使用默认服务器公钥交换数据
                    val aesResponseKey = if (keyPair.isNullOrEmpty()) {
                        RSAUtils.decryptByPublicKey(encryptKey, EncryptConfig.publicKey)
                    } else {
                        RSAUtils.decryptByPrivateKey(encryptKey, keyPair)
                    }
                    val mediaType =
                        if (responseBody != null) responseBody.contentType() else "application/json;charset=UTF-8".toMediaTypeOrNull()
                    val responseStr = responseBody?.string() ?: ""
                    val rawResponseStr =
                        AESUtils.decrypt(
                            responseStr,
                            aesResponseKey,
                            aesResponseKey.substring(0, 16)
                        )
                    responseBuilder.body(ResponseBody.create(mediaType, rawResponseStr))
                    responseBuilder.build()
                } catch (e: Exception) {
                    responseBuilder.body(getErrorSecretResponse(e))
                    responseBuilder.build()
                }
            }
        }
        return response
    }

    /**
     * description 保证解密时优先执行且避免特殊情况需要早于解密之前执行的NetworkInterceptor因此返回Integer.MAX_VALUE - 1
     */
    override val priority: Int
        get() = Int.MAX_VALUE - 1

    /**
     * 获取解密失败响应
     */
    protected fun getErrorSecretResponse(e: Exception): ResponseBody {
        return String.format(
            "{\"message\": \"移动端解密失败%s\",\"status\": %s}",
            e.message,
            EncryptConfig.SECRET_ERROR
        )
            .toResponseBody("application/json;charset=UTF-8".toMediaTypeOrNull())
    }
}