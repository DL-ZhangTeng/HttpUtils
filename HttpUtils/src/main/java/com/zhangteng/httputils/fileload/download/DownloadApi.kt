package com.zhangteng.httputils.fileload.download

import io.reactivex.Observable
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by swing on 2018/4/24.
 */
interface DownloadApi {
    /**
     * 大文件官方建议用 @Streaming 来进行注解，不然会出现IO异常，小文件可以忽略不注入
     *
     * @param fileUrl 地址
     * @return ResponseBody
     */
    @Streaming
    @GET
    fun downloadFileByDeferred(@Url fileUrl: String?): Deferred<ResponseBody>

    /**
     * 大文件官方建议用 @Streaming 来进行注解，不然会出现IO异常，小文件可以忽略不注入
     *
     * @param fileUrl 地址
     * @return ResponseBody
     */
    @Streaming
    @GET
    fun downloadFileByFlow(@Url fileUrl: String?): Flow<ResponseBody>
}

/**
 * Created by swing on 2018/4/24.
 */
interface DownloadObservableApi {
    /**
     * 大文件官方建议用 @Streaming 来进行注解，不然会出现IO异常，小文件可以忽略不注入
     *
     * @param fileUrl 地址
     * @return ResponseBody
     */
    @Streaming
    @GET
    fun downloadFileByObservable(@Url fileUrl: String?): Observable<ResponseBody>
}

/**
 * description: 断点续传
 * author: Swing
 * date: 2022/11/7
 */
interface DownloadRangeApi {
    /**
     * 大文件官方建议用 @Streaming 来进行注解，不然会出现IO异常，小文件可以忽略不注入
     * Content-Type: application/octet-stream 流方式获取响应
     * Accept-Encoding: identity  请求文件本身，不进行压缩，保证content-length可以获取到
     *
     * @param fileUrl 地址
     * @return ResponseBody
     */
    @Streaming
    @Headers("Content-Type: application/octet-stream", "Accept-Encoding: identity")
    @GET
    fun downloadFile(@Url fileUrl: String?): Call<ResponseBody>

    /**
     * 大文件官方建议用 @Streaming 来进行注解，不然会出现IO异常，小文件可以忽略不注入
     * Content-Type: application/octet-stream 流方式获取响应
     * Accept-Encoding: identity  请求文件本身，不进行压缩，保证content-length可以获取到
     *
     * @param fileUrl 地址
     * @param range 按照固定位置下载文件
     *              Range: bytes=0-499表示第0~499字节范围的内容。
     *              Range: bytes=500-999表示第500~999字节范围的内容。
     *              Range: bytes=-500表示最后500字节的内容。
     *              Range: bytes=500-表示从第500字节开始到文件结束部分的内容。
     *              Range: bytes=0-表示第一个字节到最后一个字节，即完整的文件内容。
     * @return ResponseBody
     */
    @Streaming
    @Headers("Content-Type: application/octet-stream", "Accept-Encoding: identity")
    @GET
    fun downloadFileByRange(
        @Url fileUrl: String?,
        @Header("Range") range: String?
    ): Call<ResponseBody>
}