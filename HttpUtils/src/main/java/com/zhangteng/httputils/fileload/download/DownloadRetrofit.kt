package com.zhangteng.httputils.fileload.download

import android.text.TextUtils
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.transformer.ProgressDialogObservableTransformer
import io.reactivex.Observable
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by swing on 2018/4/24.
 */
class DownloadRetrofit private constructor() {
    private var mRetrofit: Retrofit? = null
    private val builder: Retrofit.Builder
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
        if (TextUtils.isEmpty(baseUrl)) {
            builder.baseUrl(
                HttpUtils.instance.ConfigGlobalHttpUtils()!!.retrofit!!.baseUrl()
            )
        } else {
            builder.baseUrl(baseUrl)
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
        if (baseUrl == null) {
            builder.baseUrl(
                HttpUtils.instance.ConfigGlobalHttpUtils()!!.retrofit!!.baseUrl()
            )
        } else {
            builder.baseUrl(baseUrl)
        }
        return this
    }

    /**
     * description 设置Converter.Factory,传null时默认GsonConverterFactory.create()
     *
     * @param factory Converter.Factory
     * @return DownloadRetrofit
     */
    fun addConverterFactory(factory: Converter.Factory?): DownloadRetrofit {
        if (factory != null) {
            builder.addConverterFactory(factory)
        } else {
            builder.addConverterFactory(GsonConverterFactory.create())
        }
        return this
    }

    /**
     * description 设置CallAdapter.Factory,传null时默认RxJava2CallAdapterFactory.create()
     *
     * @param factory CallAdapter.Factory
     * @return DownloadRetrofit
     */
    fun addCallAdapterFactory(factory: CallAdapter.Factory?): DownloadRetrofit {
        if (factory != null) {
            builder.addCallAdapterFactory(factory)
        } else {
            builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
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
        if (client == null) {
            builder.client(
                HttpUtils.instance.ConfigGlobalHttpUtils()!!.getOkHttpClient()
            )
        } else {
            builder.client(client)
        }
        return this
    }

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            DownloadRetrofit()
        }

        /**
         * description 下载文件 默认使用全据配置，如需自定义可用DownloadRetrofit初始化
         *
         * @param fileUrl 文件网络路径
         * @return Observable<ResponseBody>
        </ResponseBody> */
        fun downloadFile(fileUrl: String?): Observable<ResponseBody> {
            return instance
                .retrofit!!
                .create(DownloadApi::class.java)
                .downloadFile(fileUrl)
                .compose(ProgressDialogObservableTransformer())
        }
    }

    init {
        builder = Retrofit.Builder() //默认使用全局配置
            .addCallAdapterFactory(
                HttpUtils.instance.ConfigGlobalHttpUtils()!!.retrofitBuilder
                    .callAdapterFactories()[0]
            ) //默认使用全局配置
            .addConverterFactory(
                HttpUtils.instance.ConfigGlobalHttpUtils()!!.retrofitBuilder
                    .converterFactories()[0]
            ) //默认使用全局baseUrl
            .baseUrl(
                HttpUtils.instance.ConfigGlobalHttpUtils()!!.retrofitBuilder
                    .build().baseUrl()
            ) //默认使用全局配置
            .client(HttpUtils.instance.ConfigGlobalHttpUtils()!!.getOkHttpClient())
    }
}