package com.zhangteng.app

import android.app.Application
import com.zhangteng.httputils.adapter.coroutine.CoroutineCallAdapterFactory
import com.zhangteng.httputils.adapter.flow.FlowCallAdapterFactory
import com.zhangteng.httputils.gson.FailOverGson
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.utils.R
import com.zhangteng.utils.StateViewHelper
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class HttpUtilsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        HttpUtils.init(this)
        HttpUtils.instance
            .ConfigGlobalHttpUtils()
            //全局的BaseUrl
            .setBaseUrl("https://www.wanandroid.com/")
            //设置CallAdapter.Factory,默认FlowCallAdapterFactory.create()、CoroutineCallAdapterFactory.create()、RxJava2CallAdapterFactory.create()
            .addCallAdapterFactory(CoroutineCallAdapterFactory.create())
            .addCallAdapterFactory(FlowCallAdapterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            //设置Converter.Factory,默认GsonConverterFactory.create()
            .addConverterFactory(GsonConverterFactory.create(FailOverGson.failOverGson))
            //设置自定义域名解析
            //.setDns(HttpDns.getInstance())
            //开启缓存策略
            .setCache(true)
            //全局的单个请求头信息
            //.addHeader("Authorization", "Bearer ")
            //全局的静态请求头信息
            //.setHeaders(headersMap)
            //全局的请求头信息，需要Android
            //.setHeaders(headersMap) { headers ->
            //    headers.apply {
            //        this["version"] = BuildConfig.VERSION_CODE
            //        this["os"] = "android"
            //        val isLogin = BuildConfig.DEBUG
            //        if (isLogin) {
            //            this["Authorization"] = "Bearer " + "token"
            //        } else {
            //            this.remove("Authorization")
            //        }
            //    }
            //}
            //全局的动态请求头信息
            .setHeaders { headers ->
                headers.apply {
                    this["version"] = BuildConfig.VERSION_CODE
                    this["os"] = "android"
                    val isLogin = BuildConfig.DEBUG
                    if (isLogin) {
                        this["Authorization"] = "Bearer " + "token"
                    } else {
                        this.remove("Authorization")
                    }
                }
            }
            //.setHttpCallBack(object : CallBack {
            //    override fun onHttpResponse(
            //        chain: Interceptor.Chain,
            //        response: Response
            //    ): Response {
            //        //这里可以先客户端一步拿到每一次 Http 请求的结果
            //        val body: ResponseBody? = response.newBuilder().build().body
            //        val source = body?.source()
            //        try {
            //            source?.request(Long.MAX_VALUE) // Buffer the entire body.
            //        } catch (e: IOException) {
            //            e.printStackTrace()
            //        }
            //        val buffer: Buffer? = source?.buffer
            //        var charset: Charset = StandardCharsets.UTF_8
            //        val contentType: MediaType? = body?.contentType()
            //        if (contentType != null) {
            //            charset = contentType.charset(charset)!!
            //        }
            //        buffer?.readString(charset).e()
            //        return response
            //    }
            //
            //    override fun onHttpRequest(chain: Interceptor.Chain, request: Request): Request {
            //        //这里可以在请求服务器之前拿到
            //        FailOverGson.failOverGson.toJson(request.headers).e()
            //        val body: RequestBody? = request.body
            //        try {
            //            body?.contentLength().toString().e()
            //        } catch (e: IOException) {
            //            e.printStackTrace()
            //        }
            //        return request
            //    }
            //})
            //全局持久话cookie,保存本地每次都会携带在header中
            .setCookie(false)
            //全局ssl证书认证
            //信任所有证书,不安全有风险
            .setSslSocketFactory()
            //使用预埋证书，校验服务端证书（自签名证书）
            //.setSslSocketFactory(getAssets().open("your.cer"))
            //使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
            //.setSslSocketFactory(getAssets().open("your.bks"), "123456", getAssets().open("your.cer"))
            //全局超时配置
            .setReadTimeOut(10)
            //全局超时配置
            .setWriteTimeOut(10)
            //全局超时配置
            .setConnectionTimeOut(10)
            //全局是否打开请求log日志
            .setLog(true)


        //状态View初始化
        StateViewHelper.apply {
            //状态View图标
            defaultIcon = R.mipmap.icon_default
            noNetIcon = R.mipmap.icon_default_nonet
            timeOutIcon = R.mipmap.icon_default_timeout
            emptyIcon = R.mipmap.icon_default_empty
            unknownIcon = R.mipmap.icon_default_unknown
            noLoginIcon = R.mipmap.icon_default_nologin

            //状态View提示文本
            defaultText = ""
            noNetText = "无网络"
            timeOutText = "请求超时"
            emptyText = "暂无内容~"
            unknownText = "数据错误"
            noLoginText = "未登录"

            //状态View重试文本
            defaultAgainText = ""
            noNetAgainText = "点击重试"
            timeOutAgainText = "点击重试"
            emptyAgainText = ""
            unknownAgainText = ""
            noLoginAgainText = "去登录"

            //加载中图标
            loadingImage = R.drawable.loading1

            //加载中文本
            loadingText = "加载中..."

            //加载中布局
            loadingLayout = R.layout.layout_dialog_progress
        }
    }
}