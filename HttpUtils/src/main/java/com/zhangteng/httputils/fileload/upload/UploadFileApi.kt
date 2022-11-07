package com.zhangteng.httputils.fileload.upload

import com.zhangteng.httputils.utils.UploadManager
import com.zhangteng.utils.IResponse
import io.reactivex.Observable
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by swing on 2018/4/24.
 */
interface UploadFileApi {
    /**
     * 上传
     *
     * @param uploadUrl 地址
     * @param file      文件
     * @return ResponseBody
     */
    @Multipart
    @POST
    fun uploadFileByDeferred(
        @Url uploadUrl: String,
        @Part file: MultipartBody.Part
    ): Deferred<ResponseBody>

    /**
     * 上传
     *
     * @param uploadUrl 地址
     * @param file      文件
     * @return ResponseBody
     */
    @Multipart
    @POST
    fun uploadFileByFlow(
        @Url uploadUrl: String,
        @Part file: MultipartBody.Part
    ): Flow<ResponseBody>

    /**
     * 上传多个文件
     *
     * @param uploadUrl 地址
     * @param files      文件
     * @return ResponseBody
     */
    @Multipart
    @POST
    fun uploadFilesByDeferred(
        @Url uploadUrl: String,
        @Part files: List<MultipartBody.Part>
    ): Deferred<ResponseBody>

    /**
     * 上传多个文件
     *
     * @param uploadUrl 地址
     * @param files      文件
     * @return ResponseBody
     */
    @Multipart
    @POST
    fun uploadFilesByFlow(
        @Url uploadUrl: String,
        @Part files: List<MultipartBody.Part>
    ): Flow<ResponseBody>
}

/**
 * Created by swing on 2018/4/24.
 */
interface UploadFileObservableApi {

    /**
     * 上传
     *
     * @param uploadUrl 地址
     * @param file      文件
     * @return ResponseBody
     */
    @Multipart
    @POST
    fun uploadFileByObservable(
        @Url uploadUrl: String,
        @Part file: MultipartBody.Part
    ): Observable<ResponseBody>

    /**
     * 上传多个文件
     *
     * @param uploadUrl 地址
     * @param files      文件
     * @return ResponseBody
     */
    @Multipart
    @POST
    fun uploadFilesByObservable(
        @Url uploadUrl: String,
        @Part files: List<MultipartBody.Part>
    ): Observable<ResponseBody>
}

/**
 * description: 分片上传
 * author: Swing
 * date: 2022/11/7
 */
interface UploadFileSliceApi {

    /**
     * 上传
     *
     * @param uploadUrl 地址
     * @param file      文件
     * @param busType   文件类型
     * @param checkSum  文件md5
     * @param slice     切片编号
     * @param slices    切片总数
     * @param sliceSize 分片大小
     * @return UploadEntity
     */
    @Multipart
    @POST
    fun <T : UploadManager.ISliceFile, R : IResponse<T>> uploadFile(
        @Url uploadUrl: String?,
        @Part file: MultipartBody.Part,
        @Query("busType") busType: String?,
        @Query("checkSum") checkSum: String?,
        @Query("slice") slice: Int?,
        @Query("slices") slices: Int?,
        @Query("sliceSize") sliceSize: Long?,
    ): Call<R>

    /**
     * 文件校验
     * @param busType      文件类型
     * @param checkSum     文件md5
     * @param fileName     文件名
     * @param fileSize     文件大小
     * @return CheckFileEntity
     */
    @GET
    fun <T : UploadManager.ISliceFile, R : IResponse<T>> checkFile(
        @Url checkUrl: String?,
        @Query("busType") busType: String?,
        @Query("checkSum") checkSum: String?,
        @Query("fileName") fileName: String?,
        @Query("fileSize") fileSize: Long?
    ): Call<R>
}