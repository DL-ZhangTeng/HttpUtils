package com.zhangteng.httputils.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.lang.Boolean

/**
 * description: String 类型解析适配器
 * author: Swing
 * date: 2022/10/31
 */
class StringTypeAdapter : TypeAdapter<String?>() {
    @Throws(IOException::class)
    override fun read(jsonReader: JsonReader): String? {
        return when (jsonReader.peek()) {
            JsonToken.STRING, JsonToken.NUMBER -> jsonReader.nextString()
            JsonToken.BOOLEAN -> Boolean.toString(jsonReader.nextBoolean())
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
    override fun write(out: JsonWriter, value: String?) {
        out.value(value)
    }
}