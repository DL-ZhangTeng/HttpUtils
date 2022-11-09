package com.zhangteng.httputils.gsonadapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.math.BigDecimal

/**
 * description: BigDecimal 类型解析适配器
 * author: Swing
 * date: 2022/10/31
 */
class BigDecimalTypeAdapter : TypeAdapter<BigDecimal?>() {
    @Throws(IOException::class)
    override fun read(jsonReader: JsonReader): BigDecimal? {
        return when (jsonReader.peek()) {
            JsonToken.NUMBER, JsonToken.STRING -> {
                val result = jsonReader.nextString()
                if (result == null || "" == result) {
                    BigDecimal(0)
                } else BigDecimal(result)
            }
            JsonToken.NULL -> {
                jsonReader.nextNull()
                null
            }
            else -> {
                jsonReader.skipValue()
                null
            }
        }
    }

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: BigDecimal?) {
        out.value(value)
    }
}