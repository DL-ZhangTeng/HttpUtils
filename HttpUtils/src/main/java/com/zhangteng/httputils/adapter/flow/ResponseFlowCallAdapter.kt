package com.zhangteng.httputils.adapter.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Response
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ResponseFlowCallAdapter<R>(private val responseBodyType: R) :
    CallAdapter<R, Flow<Response<R>>> {
    override fun responseType() = responseBodyType as Type

    override fun adapt(call: Call<R>): Flow<Response<R>> = flow {
        suspendCancellableCoroutine<Response<R>> { continuation ->
            continuation.invokeOnCancellation {
                call.cancel()
            }
            try {
                val response = call.execute()
                continuation.resume(response)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }.let {
            emit(it)
        }
    }
}