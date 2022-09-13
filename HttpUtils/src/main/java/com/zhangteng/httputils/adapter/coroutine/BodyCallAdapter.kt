package com.zhangteng.httputils.adapter.coroutine

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.*
import java.lang.reflect.Type

class BodyCallAdapter<T>(
    private val isAsync: Boolean,
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

        if (isAsync) {
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
        } else {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    deferred.complete(response.body()!!)
                } else {
                    deferred.completeExceptionally(HttpException(response))
                }
            } catch (e: Exception) {
                deferred.completeExceptionally(e)
            }
        }

        return deferred
    }
}