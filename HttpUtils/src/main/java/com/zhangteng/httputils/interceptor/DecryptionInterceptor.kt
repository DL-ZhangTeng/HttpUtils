package com.zhangteng.httputils.interceptor

import com.zhangteng.httputils.config.EncryptConfig
import com.zhangteng.utils.AESUtils
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
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
        val request = chain.request()
        val m = request.header(EncryptConfig.SECRET)
        if (m.isNullOrEmpty()) {
            //请求头中无加密的必要数据时跳过解密
            return chain.proceed(request)
        } else {
            val aesResponseKey = request.header(m)
            //移除请求头中的明文秘钥
            val builder: Request.Builder = request.newBuilder().removeHeader(m)
            val response: Response = chain.proceed(builder.build())
            if (!response.isSuccessful || response.code != 200) {
                return response
            }
            val responseBuilder: Response.Builder = response.newBuilder()
            val responseBody = response.body
            return try {
                val mediaType =
                    if (responseBody != null) responseBody.contentType() else "application/json;charset=UTF-8".toMediaTypeOrNull()
                val responseStr = responseBody?.string() ?: ""
                val rawResponseStr = aesResponseKey?.let {
                    AESUtils.decrypt(
                        responseStr,
                        it,
                        aesResponseKey.substring(0, 16)
                    )
                }
                responseBuilder.body(ResponseBody.create(mediaType, rawResponseStr ?: responseStr))
                responseBuilder.build()
            } catch (e: Exception) {
                responseBuilder.body(getErrorSecretResponse(e))
                responseBuilder.build()
            }
        }
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