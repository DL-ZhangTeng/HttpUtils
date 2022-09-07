package com.zhangteng.flowhttputils.sync

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.*
import java.lang.reflect.Type

class BodyCallAdapter<T>(
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
        return deferred
    }
}