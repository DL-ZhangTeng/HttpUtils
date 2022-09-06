package com.zhangteng.httputils.interceptor

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import com.zhangteng.httputils.http.HttpUtils
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * 缓存数据，如果无网络且有缓存有数据直接读取缓存数据，只针对get请求
 * Created by swing on 2018/4/24.
 */
class CacheInterceptor : PriorityInterceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        //如果没有网络，则启用 FORCE_CACHE
        if (!isNetworkConnected) {
            request = request.newBuilder()
                .cacheControl(CacheControl.FORCE_CACHE)
                .build()
        }
        val originalResponse: Response = chain.proceed(request)
        return if (isNetworkConnected) {
            //有网的时候读接口上的@Headers里的配置
            val cacheControl = request.cacheControl.toString()
            originalResponse.newBuilder()
                .header("Cache-Control", cacheControl)
                .removeHeader("Pragma")
                .build()
        } else {
            originalResponse.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=3600")
                .removeHeader("Pragma")
                .build()
        }
    }

    override val priority: Int
        get() = 0

    companion object {
        val isNetworkConnected: Boolean
            get() {
                val context: Context? = HttpUtils.instance.context
                if (context != null) {
                    val mConnectivityManager =
                        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    @SuppressLint("MissingPermission") val mNetworkInfo =
                        mConnectivityManager.activeNetworkInfo
                    if (mNetworkInfo != null) {
                        return mNetworkInfo.isAvailable
                    }
                }
                return true
            }
    }
}