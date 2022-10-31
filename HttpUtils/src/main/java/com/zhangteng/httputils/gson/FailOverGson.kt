package com.zhangteng.httputils.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.TypeAdapters
import java.math.BigDecimal

/**
 * description: 返回一个兼容数据的Gson对象
 * author: Swing
 * date: 2022/10/31
 */
object FailOverGson {
    val failOverGson: Gson = newGsonBuilder().create()

    /**
     * 创建 Gson 构建对象
     */
    private fun newGsonBuilder(): GsonBuilder {
        val gsonBuilder = GsonBuilder()
        return gsonBuilder
            .registerTypeAdapterFactory(
                TypeAdapters.newFactory(
                    String::class.java,
                    StringTypeAdapter()
                )
            )
            .registerTypeAdapterFactory(
                TypeAdapters.newFactory(
                    Boolean::class.javaPrimitiveType,
                    Boolean::class.java,
                    BooleanTypeAdapter()
                )
            )
            .registerTypeAdapterFactory(
                TypeAdapters.newFactory(
                    Int::class.javaPrimitiveType,
                    Int::class.java,
                    IntegerTypeAdapter()
                )
            )
            .registerTypeAdapterFactory(
                TypeAdapters.newFactory(
                    Long::class.javaPrimitiveType,
                    Long::class.java,
                    LongTypeAdapter()
                )
            )
            .registerTypeAdapterFactory(
                TypeAdapters.newFactory(
                    Float::class.javaPrimitiveType,
                    Float::class.java,
                    FloatTypeAdapter()
                )
            )
            .registerTypeAdapterFactory(
                TypeAdapters.newFactory(
                    Double::class.javaPrimitiveType,
                    Double::class.java,
                    DoubleTypeAdapter()
                )
            )
            .registerTypeAdapterFactory(
                TypeAdapters.newFactory(
                    BigDecimal::class.java,
                    BigDecimalTypeAdapter()
                )
            )
    }
}