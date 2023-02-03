package com.zhangteng.httputils.interceptor

import android.util.Log
import com.zhangteng.httputils.config.SPConfig
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.utils.getFromSPToSet
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Created by swing on 2018/4/24.
 */
class AddCookieInterceptor : PriorityInterceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()
        val preferences = HttpUtils.instance.context.getFromSPToSet(
            SPConfig.COOKIE,
            HashSet<String>(),
            SPConfig.FILE_NAME
        ) as HashSet<String>
        if (preferences != null) {
            for (cookie in preferences) {
                builder.addHeader("Cookie", cookie)
                Log.v("RxHttpUtils", "Adding Header Cookie : $cookie")
            }
        }
        return chain.proceed(builder.build())
    }

    override val priority: Int
        get() = 2
}