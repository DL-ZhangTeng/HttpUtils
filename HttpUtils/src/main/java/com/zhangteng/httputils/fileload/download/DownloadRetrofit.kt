package com.zhangteng.httputils.fileload.download

import android.text.TextUtils
import com.zhangteng.httputils.http.HttpUtils
import io.reactivex.Observable
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit

/**
 * Created by swing on 2018/4/24.
 */
class DownloadRetrofit private constructor() {
    private var mRetrofit: Retrofit? = null
    private val builder: Retrofit.Builder = Retrofit.Builder().apply {
        //默认使用全局配置
        HttpUtils.instance.ConfigGlobalHttpUtils().retrofit?.callAdapterFactories()?.forEach {
            addCallAdapterFactory(it)
        }
        //默认使用全局配置
        HttpUtils.instance.ConfigGlobalHttpUtils().retrofit?.converterFactories()?.forEach {
            addConverterFactory(it)
        }
        //默认使用全局baseUrl
        baseUrl(
            HttpUtils.instance.ConfigGlobalHttpUtils().retrofit?.baseUrl() ?: "".toHttpUrl()
        )
        //默认使用全局配置
        client(HttpUtils.instance.ConfigGlobalHttpUtils().getOkHttpClient())
    }
    val retrofit: Retrofit?
        get() {
            if (mRetrofit == null) {
                mRetrofit = builder.build()
            }
            return mRetrofit
        }

    /**
     * description 自定义baseUrl
     *
     * @param baseUrl 公共url
     * @return DownloadRetrofit
     */
    fun setBaseUrl(baseUrl: String?): DownloadRetrofit {
        if (!TextUtils.isEmpty(baseUrl)) {
            builder.baseUrl(baseUrl!!)
        }
        return this
    }

    /**
     * description 自定义baseUrl
     *
     * @param baseUrl 公共url
     * @return DownloadRetrofit
     */
    fun setBaseUrl(baseUrl: HttpUrl?): DownloadRetrofit {
        if (baseUrl != null) {
            builder.baseUrl(baseUrl)
        }
        return this
    }

    /**
     * description 设置Converter.Factory
     *
     * @param factory Converter.Factory
     * @return DownloadRetrofit
     */
    fun addConverterFactory(factory: Converter.Factory?): DownloadRetrofit {
        if (factory != null) {
            builder.addConverterFactory(factory)
        }
        return this
    }

    /**
     * description 设置CallAdapter.Factory
     *
     * @param factory CallAdapter.Factory
     * @return DownloadRetrofit
     */
    fun addCallAdapterFactory(factory: CallAdapter.Factory?): DownloadRetrofit {
        if (factory != null) {
            builder.addCallAdapterFactory(factory)
        }
        return this
    }

    /**
     * description 自定义网络请求client
     *
     * @param client 网络请求client
     * @return DownloadRetrofit
     */
    fun setOkHttpClient(client: OkHttpClient?): DownloadRetrofit {
        if (client != null) {
            builder.client(client)
        }
        return this
    }

    /**
     * description 下载文件 默认使用全据配置，如需自定义可用DownloadRetrofit初始化
     *
     * @param fileUrl 文件网络路径
     * @return Deferred<ResponseBody>
     */
    fun downloadFileByDeferred(fileUrl: String?): Deferred<ResponseBody> {
        return instance
            .retrofit!!
            .create(DownloadApi::class.java)
            .downloadFileByDeferred(fileUrl)
    }

    /**
     * description 下载文件 默认使用全据配置，如需自定义可用DownloadRetrofit初始化
     *
     * @param fileUrl 文件网络路径
     * @return Flow<ResponseBody>
     */
    fun downloadFileByFlow(fileUrl: String?): Flow<ResponseBody> {
        return instance
            .retrofit!!
            .create(DownloadApi::class.java)
            .downloadFileByFlow(fileUrl)

    }

    /**
     * description 下载文件 默认使用全据配置，如需自定义可用DownloadRetrofit初始化
     *
     * @param fileUrl 文件网络路径
     * @return Observable<ResponseBody>
     */
    fun downloadFileByObservable(fileUrl: String?): Observable<ResponseBody> {
        return instance
            .retrofit!!
            .create(DownloadObservableApi::class.java)
            .downloadFileByObservable(fileUrl)
    }

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            DownloadRetrofit()
        }
    }
}