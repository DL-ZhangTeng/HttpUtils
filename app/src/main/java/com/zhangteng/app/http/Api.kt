package com.zhangteng.app.http

import com.zhangteng.app.http.entity.HomeListBean
import io.reactivex.Observable
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Path

interface Api {

    /**
     * 项目列表
     * @param page 页码，从0开始
     */
    @GET("article/listproject/{page}/json")
    fun getHomeListByObservable(@Path("page") page: Int): Observable<BaseResult<HomeListBean>>

    /**
     * 项目列表
     * @param page 页码，从0开始
     */
    @GET("article/listproject/{page}/json")
    fun getHomeListByDeferred(@Path("page") page: Int): Deferred<BaseResult<HomeListBean>>

    /**
     * 项目列表
     * @param page 页码，从0开始
     */
    @GET("article/listproject/{page}/json")
    fun getHomeListByFlow(@Path("page") page: Int): Flow<BaseResult<HomeListBean>>

    /**
     * 项目列表
     * @param page 页码，从0开始
     */
    @GET("article/listproject/{page}/json")
    suspend fun getHomeList(@Path("page") page: Int): BaseResult<HomeListBean>
}