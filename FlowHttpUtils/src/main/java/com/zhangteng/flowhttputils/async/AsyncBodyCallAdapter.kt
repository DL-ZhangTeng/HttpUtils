package com.zhangteng.flowhttputils.async

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.*
import java.lang.reflect.Type

class AsyncBodyCallAdapter<T>(
    private val responseType: T
) : CallAdapter<T, Deferred<T>> {

    override fun responseType() = responseType as Type

    override fun adapt(call: Call<T>): Deferred<T> {
        val deferred = CompletableDeferred<T>()

        deferred.invokeOnCompletion {
            if (deferred.isCancelled) {
                call.cancel()
            }
        }

        call.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) {
                deferred.completeExceptionally(t)
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    deferred.complete(response.body()!!)
                } else {
                    deferred.completeExceptionally(HttpException(response))
                }
            }
        })

        return deferred
    }
}