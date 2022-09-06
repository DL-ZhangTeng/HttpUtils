package com.zhangteng.httputils.interceptor

import android.text.TextUtils
import com.google.gson.JsonParser
import com.zhangteng.utils.MD5Util.md5Decode32
import okhttp3.*
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * 添加签名拦截器
 * Created by Swing on 2019/10/20.
 */
class SignInterceptor(private val appKey: String?) : PriorityInterceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val requestBuilder: Request.Builder = request.newBuilder()
        val urlBuilder: HttpUrl.Builder = request.url.newBuilder()
        val params: MutableMap<String, Any?> = TreeMap()
        if (METHOD_GET == request.method) {
            val httpUrl: HttpUrl = urlBuilder.build()
            val paramKeys = httpUrl.queryParameterNames
            for (key in paramKeys) {
                val value = httpUrl.queryParameter(key)
                if (!TextUtils.isEmpty(value)) params[key] = value
            }
        } else if (METHOD_POST == request.method) {
            if (request.body is FormBody) {
                val formBody = request.body as FormBody?
                for (i in 0 until formBody!!.size) {
                    params[formBody.encodedName(i)] = formBody.encodedValue(i)
                }
            } else {
                val requestBody = request.body
                val buffer = Buffer()
                requestBody!!.writeTo(buffer)
                var charset = StandardCharsets.UTF_8
                val contentType = requestBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset()
                }
                val paramJson = buffer.readString(charset ?: Charset.defaultCharset())
                val jsonObject = JsonParser().parse(paramJson).asJsonObject
                for (key in jsonObject.keySet()) {
                    val jsonElement = jsonObject[key]
                    if (jsonElement != null && !jsonElement.isJsonArray && !jsonElement.isJsonObject && !jsonElement.isJsonNull) {
                        val value = jsonElement.asString
                        if (!TextUtils.isEmpty(value)) params[key] = value
                    }
                }
            }
        }
        val sign = StringBuilder()
        sign.append(appKey)
        for (key in params.keys) {
            sign.append(key).append(params[key])
        }
        val _timestamp = System.currentTimeMillis()
        sign.append("_timestamp").append(_timestamp)
        sign.append(appKey)
        requestBuilder.addHeader("_timestamp", _timestamp.toString())
        requestBuilder.addHeader("_sign", md5Decode32(sign.toString()))
        return chain.proceed(requestBuilder.build())
    }

    override val priority: Int
        get() = 4

    companion object {
        private const val METHOD_GET = "GET"
        private const val METHOD_POST = "POST"
    }
}