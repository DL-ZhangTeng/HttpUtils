package com.zhangteng.httputils.fileload.upload

import android.text.TextUtils
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.transformer.ProgressDialogObservableTransformer
import io.reactivex.Observable
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

/**
 * Created by swing on 2018/4/24.
 */
class UploadRetrofit private constructor() {
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
     * @return UploadRetrofit
     */
    fun setBaseUrl(baseUrl: String?): UploadRetrofit {
        if (TextUtils.isEmpty(baseUrl)) {
            builder.baseUrl(
                HttpUtils.instance.ConfigGlobalHttpUtils()!!.retrofit!!.baseUrl()
            )
        } else {
            builder.baseUrl(baseUrl ?: "")
        }
        return this
    }

    /**
     * description 自定义baseUrl
     *
     * @param baseUrl 公共url
     * @return UploadRetrofit
     */
    fun setBaseUrl(baseUrl: HttpUrl?): UploadRetrofit {
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
     * @return UploadRetrofit
     */
    fun addConverterFactory(factory: Converter.Factory?): UploadRetrofit {
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
     * @return UploadRetrofit
     */
    fun addCallAdapterFactory(factory: CallAdapter.Factory?): UploadRetrofit {
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
     * @return UploadRetrofit
     */
    fun setOkHttpClient(client: OkHttpClient?): UploadRetrofit {
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
            UploadRetrofit()
        }

        /**
         * description 上传文件 默认使用全据配置，如需自定义可用UploadRetrofit初始化
         *
         * @param uploadUrl 后台url
         * @param filePath  文件路径
         * @return Observable<ResponseBody>
         * */
        fun uploadFile(uploadUrl: String, filePath: String): Observable<ResponseBody?> {
            return uploadFile(uploadUrl, "uploaded_file", filePath)
        }

        /**
         * description 上传文件 默认使用全据配置，如需自定义可用UploadRetrofit初始化
         *
         * @param uploadUrl 后台url
         * @param fieldName 后台接收图片流的参数名
         * @param filePath  文件路径
         * @return Observable<ResponseBody>
         */
        fun uploadFile(
            uploadUrl: String,
            fieldName: String?,
            filePath: String
        ): Observable<ResponseBody?> {
            val file = File(filePath)
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData(fieldName ?: "", file.name, requestFile)
            return instance
                .retrofit!!
                .create(UploadFileApi::class.java)
                .uploadFile(uploadUrl, body)
        }

        /**
         * description 上传文件 默认使用全据配置，如需自定义可用UploadRetrofit初始化
         *
         * @param uploadUrl 后台url
         * @param filePaths 文件路径
         * @return Observable<ResponseBody>
         */
        fun uploadFiles(uploadUrl: String, filePaths: List<String>): Observable<ResponseBody> {
            val fieldNames: MutableList<String> = ArrayList()
            for (i in filePaths.indices) {
                fieldNames.add("uploaded_file$i")
            }
            return uploadFiles(uploadUrl, fieldNames, filePaths)
        }

        /**
         * description 上传文件 默认使用全据配置，如需自定义可用UploadRetrofit初始化
         *
         * @param uploadUrl  后台url
         * @param fieldNames 后台接收图片流的参数名
         * @param filePaths  文件路径
         * @return Observable<ResponseBody>
         */
        fun uploadFiles(
            uploadUrl: String,
            fieldNames: List<String>,
            filePaths: List<String>
        ): Observable<ResponseBody> {
            val builder: MultipartBody.Builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
            for (i in filePaths.indices) {
                val file = File(filePaths[i])
                val imageBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                //"uploaded_file"+i 后台接收图片流的参数名
                builder.addFormDataPart(fieldNames[i], file.name, imageBody)
            }
            val parts: List<MultipartBody.Part> = builder.build().parts
            return instance
                .retrofit!!
                .create(UploadFileApi::class.java)
                .uploadFiles(uploadUrl, parts)
                .compose(ProgressDialogObservableTransformer())
        }
    }

    init {
        builder = Retrofit.Builder() //默认使用全局配置
            .addCallAdapterFactory(
                HttpUtils.instance.ConfigGlobalHttpUtils()!!.retrofitBuilder
                    .callAdapterFactories().get(0)
            ) //默认使用全局配置
            .addConverterFactory(
                HttpUtils.instance.ConfigGlobalHttpUtils()!!.retrofitBuilder
                    .converterFactories().get(0)
            ) //默认使用全局baseUrl
            .baseUrl(
                HttpUtils.instance.ConfigGlobalHttpUtils()!!.retrofitBuilder
                    .build().baseUrl()
            ) //默认使用全局配置
            .client(HttpUtils.instance.ConfigGlobalHttpUtils()!!.getOkHttpClient())
    }
}