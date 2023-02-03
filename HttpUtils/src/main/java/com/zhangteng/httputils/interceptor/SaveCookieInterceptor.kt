package com.zhangteng.httputils.interceptor

import android.net.ParseException
import com.zhangteng.httputils.config.SPConfig
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.utils.putToSP
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.*

/**
 * Created by swing on 2018/4/24.
 */
class SaveCookieInterceptor : PriorityInterceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse: Response = chain.proceed(chain.request())
        //这里获取请求返回的cookie
        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            val cookies = HashSet<String>()
            for (header in originalResponse.headers("Set-Cookie")) {
                cookies.add(header)
            }
            HttpUtils.instance.context
                .putToSP(SPConfig.COOKIE, cookies, SPConfig.FILE_NAME)
        }
        //获取服务器相应时间--用于计算倒计时的时间差
        if (!originalResponse.header("Date")!!.isEmpty()) {
            val date = dateToStamp(originalResponse.header("Date"))
            HttpUtils.instance.context
                .putToSP(SPConfig.DATE, date, SPConfig.FILE_NAME)
        }
        return originalResponse
    }

    override val priority: Int
        get() = 2

    companion object {
        /**
         * 将时间转换为时间戳
         *
         * @param s date
         * @return long
         * @throws android.net.ParseException
         */
        @Throws(ParseException::class)
        fun dateToStamp(s: String?): Long {
            //转换为标准时间对象
            val date = Date(s)
            val calendar = Calendar.getInstance()
            calendar.time = date
            return calendar.timeInMillis
        }
    }
}