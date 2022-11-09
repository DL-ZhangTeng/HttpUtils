package com.zhangteng.httputils.gsonadapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.math.BigDecimal

/**
 * description: int / Integer 类型解析适配器
 * author: Swing
 * date: 2022/10/31
 */
class IntegerTypeAdapter : TypeAdapter<Int?>() {
    @Throws(IOException::class)
    override fun read(jsonReader: JsonReader): Int? {
        return when (jsonReader.peek()) {
            JsonToken.NUMBER -> {
                return try {
                    jsonReader.nextInt()
                } catch (e: NumberFormatException) {
                    // 带小数点则会抛出这个异常
                    jsonReader.nextDouble().toInt()
                }
            }
            JsonToken.STRING -> {
                val result = jsonReader.nextString()
                return if (result == null || "" == result) {
                    0
                } else try {
                    result.toInt()
                } catch (e: NumberFormatException) {
                    BigDecimal(result).toFloat().toInt()
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
    override fun write(out: JsonWriter, value: Int?) {
        out.value(value)
    }
}