package com.zhangteng.httputils.calladapter.flow

import kotlinx.coroutines.flow.Flow
import retrofit2.CallAdapter
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @GET("/user")
 * fun getUser(): Flow<User>
 */
class FlowCallAdapterFactory private constructor(private val isAsync: Boolean) :
    CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Flow::class.java) return null

        if (returnType !is ParameterizedType) {
            throw IllegalStateException("the flow type must be parameterized as Flow<Foo>!")
        }

        val flowableType = getParameterUpperBound(0, returnType)
        val rawFlowableType = getRawType(flowableType)

        return if (rawFlowableType == Response::class.java) {
            if (flowableType !is ParameterizedType) {
                throw IllegalStateException("the response type must be parameterized as Response<Foo>!")
            }
            val responseBodyType = getParameterUpperBound(0, flowableType)
            ResponseFlowCallAdapter(isAsync, responseBodyType)
        } else {
            BodyFlowCallAdapter(isAsync, flowableType)
        }
    }

    companion object {
        @JvmStatic
        fun create(isAsync: Boolean = false) = FlowCallAdapterFactory(isAsync)
    }
}