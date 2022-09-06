package com.zhangteng.httputils.utils

import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Retrofit
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * @className: RetrofitServiceProxyHandler
 * @description: 延迟Observable
 * @author: Swing
 * @date: 2021/5/10
 */
class RetrofitServiceProxyHandler(
    private val mRetrofit: Retrofit?,
    private val mServiceClass: Class<*>
) : InvocationHandler {
    private var mRetrofitService: Any? = null

    @Throws(Throwable::class)
    override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any {
        if (method.returnType == Observable::class.java) {
            // 如果方法返回值是 Observable 的话，则包一层再返回，
            // 只包一层 defer 由外部去控制耗时方法以及网络请求所处线程，
            // 如此对原项目的影响为 0，且更可控。
            return Observable.defer {
                method.invoke(
                    retrofitService, *args.orEmpty()
                ) as Observable<*>
            }
        } else if (method.returnType == Single::class.java) {
            // 如果方法返回值是 Single 的话，则包一层再返回。
            return Single.defer {
                method.invoke(
                    retrofitService, *args.orEmpty()
                ) as Single<*>
            }
        }

        // 返回值不是 Observable 或 Single 的话不处理。
        return method.invoke(retrofitService, *args.orEmpty())
    }

    private val retrofitService: Any?
        private get() {
            if (mRetrofitService == null) {
                mRetrofitService = mRetrofit!!.create(mServiceClass)
            }
            return mRetrofitService
        }
}