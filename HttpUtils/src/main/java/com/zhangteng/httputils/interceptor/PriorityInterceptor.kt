package com.zhangteng.httputils.interceptor

import okhttp3.Interceptor

/**
 * description: 拦截器优先级接口，优先级越小越早被添加0-9预留给框架
 * author: Swing
 * date: 2022/9/2
 */
interface PriorityInterceptor : Interceptor {
    /**
     * description 自定义拦截器Priority必须>=10
     * Interceptor添加顺序: [CacheInterceptor] [HeaderInterceptor] [AddCookieInterceptor] [CallBackInterceptor] [SignInterceptor] [HttpLoggingProxyInterceptor] [EncryptionInterceptor]
     * NetworkInterceptor添加顺序: [CacheInterceptor] [SaveCookieInterceptor] [DecryptionInterceptor]
     */
    val priority: Int
        get() = 10
}