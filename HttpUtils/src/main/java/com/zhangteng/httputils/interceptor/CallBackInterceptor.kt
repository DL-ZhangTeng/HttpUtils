package com.zhangteng.httputils.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * description: 网络请求前后回调函数
 * author: Swing
 * date: 2022/9/1
 */
class CallBackInterceptor(private val callBack: CallBack?) : PriorityInterceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return if (callBack != null) {
            //在请求服务器之前拿到
            val request = callBack.onHttpRequest(chain, chain.request())
            val response: Response = chain.proceed(request)
            //这里可以比客户端提前一步拿到服务器返回的结果
            callBack.onHttpResponse(chain, response)
        } else {
            chain.proceed(chain.request())
        }
    }

    /**
     * 晚于[CacheInterceptor] [HeaderInterceptor] [AddCookieInterceptor]执行
     * 早于[SignInterceptor] [HttpLoggingProxyInterceptor] [EncryptionInterceptor]执行
     */
    override val priority: Int
        get() = 3

    /**
     * 处理 Http 请求和响应结果的处理类
     */
    interface CallBack {
        /**
         * 这里可以先客户端一步拿到每一次 Http 请求的结果
         *
         * @param chain    [okhttp3.Interceptor.Chain]
         * @param response [Response]
         * @return [Response]
         */
        fun onHttpResponse(chain: Interceptor.Chain, response: Response): Response

        /**
         * 这里可以在请求服务器之前拿到 [Request]
         *
         * @param chain   [okhttp3.Interceptor.Chain]
         * @param request [Request]
         * @return [Request]
         */
        fun onHttpRequest(chain: Interceptor.Chain, request: Request): Request
    }
}