package com.zhangteng.httputils.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

/**
 * description: float / Float 类型解析适配器
 * author: Swing
 * date: 2022/10/31
 */
class FloatTypeAdapter : TypeAdapter<Float?>() {
    @Throws(IOException::class)
    override fun read(jsonReader: JsonReader): Float? {
        return when (jsonReader.peek()) {
            JsonToken.NUMBER -> jsonReader.nextDouble().toFloat()
            JsonToken.STRING -> {
                val result = jsonReader.nextString()
                if (result == null || "" == result) {
                    0f
                } else result.toFloat()
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
    override fun write(out: JsonWriter, value: Float?) {
        out.value(value)
    }
}