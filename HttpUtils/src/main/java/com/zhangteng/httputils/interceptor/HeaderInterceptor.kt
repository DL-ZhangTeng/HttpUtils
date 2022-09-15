package com.zhangteng.httputils.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Created by swing on 2018/4/24.
 */
class HeaderInterceptor : PriorityInterceptor {
    var headerMaps: MutableMap<String?, Any?>? = null
        private set
    private var headersFunction: ((MutableMap<String?, Any?>) -> MutableMap<String?, Any?>)? = null

    /**
     * description 设置请求头公共参数
     *
     * @param headerMaps 请求头设置的静态参数
     */
    constructor(headerMaps: MutableMap<String?, Any?>?) {
        this.headerMaps = headerMaps
    }

    /**
     * description 动态设置请求头，如token等需要根据登录状态实时变化的请求头参数
     *
     * @param headersFunction 请求头设置的函数式参数，如token等需要根据登录状态实时变化的请求头参数
     */
    constructor(headersFunction: (MutableMap<String?, Any?>) -> MutableMap<String?, Any?>) {
        this.headersFunction = headersFunction
    }

    /**
     * description 设置请求头公共参数
     *
     * @param headerMaps      请求头设置的静态参数
     * @param headersFunction 请求头设置的函数式参数，如token等需要根据登录状态实时变化的请求头参数
     */
    constructor(
        headerMaps: MutableMap<String?, Any?>?,
        headersFunction: (MutableMap<String?, Any?>) -> MutableMap<String?, Any?>
    ) {
        this.headerMaps = headerMaps
        this.headersFunction = headersFunction
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request.Builder = chain.request().newBuilder()
        if (headersFunction != null) {
            if (headerMaps == null) {
                headerMaps = HashMap()
            }
            headerMaps = headersFunction!!.invoke(headerMaps!!)
        }
        if (headerMaps != null && headerMaps!!.isNotEmpty()) {
            for ((key, value) in headerMaps!!) {
                request.addHeader(key ?: "", value.toString())
            }
        }
        return chain.proceed(request.build())
    }

    override val priority: Int
        get() = 1
}