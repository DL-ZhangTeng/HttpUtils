package com.zhangteng.httputils.calladapter.coroutine

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class ResponseCallAdapter<T>(
    private val isAsync: Boolean,
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

        if (isAsync) {
            call.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    deferred.completeExceptionally(t)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    deferred.complete(response)
                }
            })
        } else {
            try {
                val response = call.execute()
                deferred.complete(response)
            } catch (e: Exception) {
                deferred.completeExceptionally(e)
            }
        }

        return deferred
    }
}