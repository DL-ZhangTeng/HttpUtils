package com.zhangteng.flowhttputils.sync

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Response
import java.lang.reflect.Type

class ResponseCallAdapter<T>(
    private val responseType: T
) : CallAdapter<T, Deferred<Response<T>>> {

    override fun responseType() = responseType as Type

    override fun adapt(call: Call<T>): Deferred<Response<T>> {
        val deferred = CompletableDeferred<Response<T>>()

        deferred.invokeOnCompletion {
            if (deferred.isCancelled) {
                call.cancel()
            }
        }

        try {
            val response = call.execute()
            deferred.complete(response)
        } catch (e: Exception) {
            deferred.completeExceptionally(e)
        }
        return deferred
    }
}