package com.zhangteng.httputils.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class HttpLoggingProxyInterceptor(private val httpLoggingInterceptor: HttpLoggingInterceptor) :
    PriorityInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            httpLoggingInterceptor.intercept(chain)
        } catch (e: Exception) {
            chain.proceed(chain.request())
        }
    }

    override val priority: Int
        get() = 5
}