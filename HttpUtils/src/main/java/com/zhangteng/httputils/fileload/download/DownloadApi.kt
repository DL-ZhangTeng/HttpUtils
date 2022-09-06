package com.zhangteng.httputils.fileload.download

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

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
    fun downloadFile(@Url fileUrl: String?): Observable<ResponseBody?>
}