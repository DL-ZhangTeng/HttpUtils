package com.zhangteng.httputils.calladapter.flow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.*
import java.lang.reflect.Type
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.intercepted
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class BodyFlowCallAdapter<R>(private val isAsync: Boolean, private val responseBodyType: R) :
    CallAdapter<R, Flow<R>> {
    override fun responseType() = responseBodyType as Type

    override fun adapt(call: Call<R>): Flow<R> =
        if (isAsync)
            flow {
                try {
                    suspendCancellableCoroutine<R> { continuation ->
                        continuation.invokeOnCancellation {
                            call.cancel()
                        }
                        call.enqueue(object : Callback<R> {
                            override fun onResponse(call: Call<R>, response: Response<R>) {
                                if (response.isSuccessful) {
                                    continuation.resume(response.body()!!)
                                } else {
                                    continuation.resumeWithException(HttpException(response))
                                }
                            }

                            override fun onFailure(call: Call<R>, t: Throwable) {
                                continuation.resumeWithException(t)
                            }
                        })
                    }.let {
                        emit(it)
                    }
                } catch (e: Exception) {
                    suspendCoroutineUninterceptedOrReturn<Nothing> { continuation ->
                        Dispatchers.Default.dispatch(continuation.context) {
                            continuation.intercepted().resumeWithException(e)
                        }
                        COROUTINE_SUSPENDED
                    }
                }
            }
        else
            flow {
                suspendCancellableCoroutine<R> { continuation ->
                    continuation.invokeOnCancellation {
                        call.cancel()
                    }
                    try {
                        val response = call.execute()
                        if (response.isSuccessful) {
                            continuation.resume(response.body()!!)
                        } else {
                            continuation.resumeWithException(HttpException(response))
                        }
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }.let {
                    emit(it)
                }
            }
}