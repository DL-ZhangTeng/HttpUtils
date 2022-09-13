package com.zhangteng.httputils.fileload.upload

import io.reactivex.Observable
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

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