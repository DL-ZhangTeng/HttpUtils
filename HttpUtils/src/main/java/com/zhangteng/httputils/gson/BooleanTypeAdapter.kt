package com.zhangteng.httputils.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

/**
 * description: boolean / Boolean 类型解析适配器
 * author: Swing
 * date: 2022/10/31
 */
class BooleanTypeAdapter : TypeAdapter<Boolean?>() {
    @Throws(IOException::class)
    override fun read(jsonReader: JsonReader): Boolean? {
        return when (jsonReader.peek()) {
            JsonToken.BOOLEAN -> jsonReader.nextBoolean()
            JsonToken.STRING -> java.lang.Boolean.parseBoolean(jsonReader.nextString())
            JsonToken.NUMBER -> jsonReader.nextInt() != 0
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
    override fun write(out: JsonWriter, value: Boolean?) {
        out.value(value)
    }
}