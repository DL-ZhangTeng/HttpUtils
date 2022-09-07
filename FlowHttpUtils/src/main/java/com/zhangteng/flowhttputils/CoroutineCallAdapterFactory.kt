package com.zhangteng.flowhttputils

import com.zhangteng.flowhttputils.async.AsyncBodyCallAdapter
import com.zhangteng.flowhttputils.async.AsyncResponseCallAdapter
import com.zhangteng.flowhttputils.sync.BodyCallAdapter
import com.zhangteng.flowhttputils.sync.ResponseCallAdapter
import kotlinx.coroutines.Deferred
import retrofit2.CallAdapter
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @GET("/user")
 * fun getUser(): Deferred<User>
 */
class CoroutineCallAdapterFactory private constructor(private val async: Boolean) :
    CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Deferred::class.java) {
            return null
        }
        if (returnType !is ParameterizedType) {
            throw IllegalStateException(
                "Deferred return type must be parameterized as Deferred<Foo> or Deferred<out Foo>"
            )
        }
        val responseType = getParameterUpperBound(0, returnType)

        val rawDeferredType = getRawType(responseType)
        return if (rawDeferredType == Response::class.java) {
            if (responseType !is ParameterizedType) {
                throw IllegalStateException(
                    "Response must be parameterized as Response<Foo> or Response<out Foo>"
                )
            }
            val responseBodyType = getParameterUpperBound(0, responseType)
            createResponseCallAdapter(async, responseBodyType)
        } else {
            createBodyCallAdapter(async, responseType)
        }
    }

    companion object {
        @JvmStatic
        fun create(async: Boolean = false) = CoroutineCallAdapterFactory(async)
    }
}

private fun createResponseCallAdapter(async: Boolean, responseBodyType: Type) =
    if (async)
        AsyncResponseCallAdapter(responseBodyType)
    else
        ResponseCallAdapter(responseBodyType)

private fun createBodyCallAdapter(async: Boolean, responseBodyType: Type) =
    if (async)
        AsyncBodyCallAdapter(responseBodyType)
    else
        BodyCallAdapter(responseBodyType)
