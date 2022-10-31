package com.zhangteng.httputils.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.math.BigDecimal

/**
 * description: long / Long 类型解析适配器
 * author: Swing
 * date: 2022/10/31
 */
class LongTypeAdapter : TypeAdapter<Long?>() {
    @Throws(IOException::class)
    override fun read(jsonReader: JsonReader): Long? {
        return when (jsonReader.peek()) {
            JsonToken.NUMBER -> {
                return try {
                    jsonReader.nextLong()
                } catch (e: NumberFormatException) {
                    // 带小数点则会抛出这个异常
                    BigDecimal(jsonReader.nextString()).toLong()
                }
            }
            JsonToken.STRING -> {
                val result = jsonReader.nextString()
                return if (result == null || "" == result) {
                    0L
                } else try {
                    result.toLong()
                } catch (e: NumberFormatException) {
                    BigDecimal(result).toLong()
                }
            }
            JsonToken.NULL -> {
                jsonReader.nextNull()
                null
            }
            else -> {
                jsonReader.skipValue()
                throw IllegalArgumentException()
            }
        }
    }

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Long?) {
        out.value(value)
    }
}