package com.zhangteng.httputils.fileload.upload

import android.text.TextUtils
import com.zhangteng.httputils.http.HttpUtils
import io.reactivex.Observable
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.File

/**
 * Created by swing on 2018/4/24.
 */
class UploadRetrofit private constructor() {
    private val builder: Retrofit.Builder = Retrofit.Builder().apply {
        //默认使用全局配置
        HttpUtils.instance.ConfigGlobalHttpUtils().retrofit.callAdapterFactories().forEach {
            addCallAdapterFactory(it)
        }
        //默认使用全局配置
        HttpUtils.instance.ConfigGlobalHttpUtils().retrofit.converterFactories().forEach {
            addConverterFactory(it)
        }
        //默认使用全局baseUrl
        baseUrl(
            HttpUtils.instance.ConfigGlobalHttpUtils().retrofit.baseUrl() ?: "".toHttpUrl()
        )
        //默认使用全局配置
        client(HttpUtils.instance.ConfigGlobalHttpUtils().okHttpClient)
    }

    val retrofit: Retrofit by lazy {
        builder.build()
    }

    /**
     * description 自定义baseUrl
     *
     * @param baseUrl 公共url
     * @return UploadRetrofit
     */
    fun setBaseUrl(baseUrl: String?): UploadRetrofit {
        if (!TextUtils.isEmpty(baseUrl)) {
            builder.baseUrl(baseUrl!!)
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
        if (baseUrl != null) {
            builder.baseUrl(baseUrl)
        }
        return this
    }

    /**
     * description 设置Converter.Factory
     *
     * @param factory Converter.Factory
     * @return UploadRetrofit
     */
    fun addConverterFactory(factory: Converter.Factory?): UploadRetrofit {
        if (factory != null) {
            builder.addConverterFactory(factory)
        }
        return this
    }

    /**
     * description 设置CallAdapter.Factory
     *
     * @param factory CallAdapter.Factory
     * @return UploadRetrofit
     */
    fun addCallAdapterFactory(factory: CallAdapter.Factory?): UploadRetrofit {
        if (factory != null) {
            builder.addCallAdapterFactory(factory)
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
        if (client != null) {
            builder.client(client)
        }
        return this
    }

    /**
     * description 上传文件 默认使用全据配置，如需自定义可用UploadRetrofit初始化
     *
     * @param uploadUrl 后台url
     * @param fieldName 后台接收图片流的参数名
     * @param filePath  文件路径
     * @return Deferred<ResponseBody>
     */
    fun uploadFileByDeferred(
        uploadUrl: String,
        filePath: String,
        fieldName: String = "uploaded_file"
    ): Deferred<ResponseBody> {
        val file = File(filePath)
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData(fieldName, file.name, requestFile)
        return instance
            .retrofit
            .create(UploadFileApi::class.java)
            .uploadFileByDeferred(uploadUrl, body)
    }

    /**
     * description 上传文件 默认使用全据配置，如需自定义可用UploadRetrofit初始化
     *
     * @param uploadUrl 后台url
     * @param fieldName 后台接收图片流的参数名
     * @param filePath  文件路径
     * @return Flow<ResponseBody>
     */
    fun uploadFileByFlow(
        uploadUrl: String,
        filePath: String,
        fieldName: String = "uploaded_file"
    ): Flow<ResponseBody> {
        val file = File(filePath)
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData(fieldName, file.name, requestFile)
        return instance
            .retrofit
            .create(UploadFileApi::class.java)
            .uploadFileByFlow(uploadUrl, body)
    }

    /**
     * description 上传文件 默认使用全据配置，如需自定义可用UploadRetrofit初始化
     *
     * @param uploadUrl 后台url
     * @param fieldName 后台接收图片流的参数名
     * @param filePath  文件路径
     * @return Observable<ResponseBody>
     */
    fun uploadFileByObservable(
        uploadUrl: String,
        filePath: String,
        fieldName: String = "uploaded_file"
    ): Observable<ResponseBody> {
        val file = File(filePath)
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData(fieldName, file.name, requestFile)
        return instance
            .retrofit
            .create(UploadFileObservableApi::class.java)
            .uploadFileByObservable(uploadUrl, body)
    }

    /**
     * description 上传文件 默认使用全据配置，如需自定义可用UploadRetrofit初始化
     *
     * @param uploadUrl 后台url
     * @param filePaths 文件路径
     * @return Deferred<ResponseBody>
     */
    fun uploadFilesByDeferred(
        uploadUrl: String,
        filePaths: List<String>
    ): Deferred<ResponseBody> {
        val fieldNames: MutableList<String> = ArrayList()
        for (i in filePaths.indices) {
            fieldNames.add("uploaded_file$i")
        }
        return uploadFilesByDeferred(uploadUrl, fieldNames, filePaths)
    }

    /**
     * description 上传文件 默认使用全据配置，如需自定义可用UploadRetrofit初始化
     *
     * @param uploadUrl 后台url
     * @param filePaths 文件路径
     * @return Flow<ResponseBody>
     */
    fun uploadFilesByFlow(uploadUrl: String, filePaths: List<String>): Flow<ResponseBody> {
        val fieldNames: MutableList<String> = ArrayList()
        for (i in filePaths.indices) {
            fieldNames.add("uploaded_file$i")
        }
        return uploadFilesByFlow(uploadUrl, fieldNames, filePaths)
    }

    /**
     * description 上传文件 默认使用全据配置，如需自定义可用UploadRetrofit初始化
     *
     * @param uploadUrl 后台url
     * @param filePaths 文件路径
     * @return Observable<ResponseBody>
     */
    fun uploadFilesByObservable(
        uploadUrl: String,
        filePaths: List<String>
    ): Observable<ResponseBody> {
        val fieldNames: MutableList<String> = ArrayList()
        for (i in filePaths.indices) {
            fieldNames.add("uploaded_file$i")
        }
        return uploadFilesByObservable(uploadUrl, fieldNames, filePaths)
    }

    /**
     * description 上传文件 默认使用全据配置，如需自定义可用UploadRetrofit初始化
     *
     * @param uploadUrl  后台url
     * @param fieldNames 后台接收图片流的参数名
     * @param filePaths  文件路径
     * @return Deferred<ResponseBody>
     */
    fun uploadFilesByDeferred(
        uploadUrl: String,
        fieldNames: List<String>,
        filePaths: List<String>
    ): Deferred<ResponseBody> {
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
            .retrofit
            .create(UploadFileApi::class.java)
            .uploadFilesByDeferred(uploadUrl, parts)
    }

    /**
     * description 上传文件 默认使用全据配置，如需自定义可用UploadRetrofit初始化
     *
     * @param uploadUrl  后台url
     * @param fieldNames 后台接收图片流的参数名
     * @param filePaths  文件路径
     * @return Flow<ResponseBody>
     */
    fun uploadFilesByFlow(
        uploadUrl: String,
        fieldNames: List<String>,
        filePaths: List<String>
    ): Flow<ResponseBody> {
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
            .retrofit
            .create(UploadFileApi::class.java)
            .uploadFilesByFlow(uploadUrl, parts)
    }

    /**
     * description 上传文件 默认使用全据配置，如需自定义可用UploadRetrofit初始化
     *
     * @param uploadUrl  后台url
     * @param fieldNames 后台接收图片流的参数名
     * @param filePaths  文件路径
     * @return Observable<ResponseBody>
     */
    fun uploadFilesByObservable(
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
            .retrofit
            .create(UploadFileObservableApi::class.java)
            .uploadFilesByObservable(uploadUrl, parts)
    }

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            UploadRetrofit()
        }
    }
}