# Retrofit网络加载库二次封装支持RxJava与Flow-HttpUtils
HttpUtils是Retrofit网络加载库二次封装支持RxJava与Flow，包含网络加载动画、activity销毁自动取消请求、网络缓存、公共参数、RSA+AES加密等
[GitHub仓库地址](https://github.com/DL-ZhangTeng/HttpUtils)
## 引入

### gradle
```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

implementation 'com.github.DL-ZhangTeng:HttpUtils:2.0.0'
    //库所使用的三方
    implementation 'androidx.lifecycle:lifecycle-common:2.4.0'
    implementation 'androidx.lifecycle:lifecycle-runtime:2.4.0'
    implementation 'androidx.lifecycle:lifecycle-extensions:2.2.0'
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1"
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.retrofit2:converter-scalars:2.8.1'
    implementation 'com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'

    //如果不需要rxjava不用导入
    implementation 'io.reactivex.rxjava2:rxjava:2.2.21'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
    implementation 'com.squareup.retrofit2:adapter-rxjava2:2.9.0'

    implementation 'com.github.DL-ZhangTeng:Utils:2.0.1'
```

## 属性
| 属性名                    | 描述                                                                                                                                                                                                                                                                  |
|------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| setBaseUrl             | ConfigGlobalHttpUtils()全局的BaseUrl；ConfigSingleInstance()单独设置BaseUrl                                                                                                                                                                                                 |
| addCallAdapterFactory  | 设置CallAdapter.Factory,默认FlowCallAdapterFactory.create()、CoroutineCallAdapterFactory.create()、RxJava2CallAdapterFactory.create()                                                                                                                                     |
| addConverterFactory    | 设置Converter.Factory,默认GsonConverterFactory.create()                                                                                                                                                                                                                 |
| setDns                 | 自定义域名解析                                                                                                                                                                                                                                                             |
| setCache               | 开启缓存策略                                                                                                                                                                                                                                                              |
| addHeader              | 全局的单个请求头信息                                                                                                                                                                                                                                                          |
| setHeaders             | 全局的请求头信息，设置静态请求头：更新请求头时不需要重新设置，对Map元素进行移除添加即可；设置动态请求头：如token等需要根据登录状态实时变化的请求头参数，最小支持api 24                                                                                                                                                                          |
| setHttpCallBack        | 设置网络请求前后回调函数 onHttpResponse:可以先客户端一步拿到每一次Http请求的结果 onHttpRequest:可以在请求服务器之前拿到                                                                                                                                                                                       |
| setSign                | 全局验签，appKey与后端匹配即可，具体规则参考：https://blog.csdn.net/duoluo9/article/details/105214983                                                                                                                                                                                   |
| setEnAndDecryption     | 全局加解密(AES+RSA)。1、公钥请求路径HttpUrl.get(BuildConfig.HOST + "/getPublicKey")；2、公钥响应结果{"result": {"publicKey": ""},"message": "查询成功!","status": 100}                                                                                                                       |
| setCookie              | 全局持久话cookie,保存本地每次都会携带在header中                                                                                                                                                                                                                                      |
| addInterceptor         | 添加拦截器(继承PriorityInterceptor重写getPriority方法自定义顺序，自定义拦截器Priority必须>=10)                                                                                                                                                                                               |
| addInterceptors        | 添加拦截器(继承PriorityInterceptor重写getPriority方法自定义顺序，自定义拦截器Priority必须>=10)                                                                                                                                                                                               |
| addNetworkInterceptor  | 添加网络拦截器(继承PriorityInterceptor重写getPriority方法自定义顺序，自定义拦截器Priority必须>=10)                                                                                                                                                                                             |
| addNetworkInterceptors | 添加网络拦截器(继承PriorityInterceptor重写getPriority方法自定义顺序，自定义拦截器Priority必须>=10)                                                                                                                                                                                             |
| setSslSocketFactory    | 全局ssl证书认证。1、信任所有证书,不安全有风险，setSslSocketFactory()；2、使用预埋证书，校验服务端证书（自签名证书），setSslSocketFactory(getAssets().open("your.cer"))；3、使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书），setSslSocketFactory(getAssets().open("your.bks"), "123456", getAssets().open("your.cer")) |
| setReadTimeOut         | 全局超时配置                                                                                                                                                                                                                                                              |
| setWriteTimeOut        | 全局超时配置                                                                                                                                                                                                                                                              |
| setConnectionTimeOut   | 全局超时配置                                                                                                                                                                                                                                                              |
| setLog                 | 全局是否打开请求log日志                                                                                                                                                                                                                                                       |

## 使用

### 初始化
```kotlin
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
            .addConverterFactory(GsonConverterFactory.create())
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
    }
}
```

### ICallBack回调（更多请求方式请参考MainActivity）
```kotlin
    fun deferredGo_ICallBack() {
        GlobalScope.launch {
            HttpUtils.instance.ConfigGlobalHttpUtils()
                .createService(Api::class.java)
                .getHomeListByDeferred(0)
                .deferredGo(object :
                    DeferredCallBack<BaseResult<HomeListBean>>(
                        this@MainActivity
                    ) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        FailOverGson.failOverGson.toJson(iException).e("deferredGo_ICallBack")
                    }

                    override fun onSuccess(t: BaseResult<HomeListBean>) {
                        FailOverGson.failOverGson.toJson(t).e("deferredGo_ICallBack")
                    }
                })
        }
    }

    fun deferredGoIResponse_ICallBack() {
        GlobalScope.launch {
            HttpUtils.instance.ConfigGlobalHttpUtils()
                .createService(Api::class.java)
                .getHomeListByDeferred(0)
                .deferredGoIResponse(object :
                    DeferredCallBack<IResponse<HomeListBean>>(
                        this@MainActivity
                    ) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        FailOverGson.failOverGson.toJson(iException).e("deferredGoIResponse_ICallBack")
                    }

                    override fun onSuccess(t: IResponse<HomeListBean>) {
                        FailOverGson.failOverGson.toJson(t).e("deferredGoIResponse_ICallBack")
                    }
                })
        }
    }

    fun flowGo_ICallBack() {
        GlobalScope.launch {
            HttpUtils.instance.ConfigGlobalHttpUtils()
                .createService(Api::class.java)
                .getHomeListByFlow(0)
                .flowGo(object :
                    FlowCallBack<BaseResult<HomeListBean>>(this@MainActivity) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        FailOverGson.failOverGson.toJson(iException).e("flowGo_ICallBack")
                    }

                    override fun onSuccess(t: BaseResult<HomeListBean>) {
                        FailOverGson.failOverGson.toJson(t).e("flowGo_ICallBack")
                    }
                })
        }
    }

    fun flowGoIResponse_ICallBack() {
        GlobalScope.launch {
            HttpUtils.instance.ConfigGlobalHttpUtils()
                .createService(Api::class.java)
                .getHomeListByFlow(0)
                .flowGoIResponse(object :
                    FlowCallBack<IResponse<HomeListBean>>(this@MainActivity) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        FailOverGson.failOverGson.toJson(iException).e("flowGoIResponse_ICallBack")
                    }

                    override fun onSuccess(t: IResponse<HomeListBean>) {
                        FailOverGson.failOverGson.toJson(t).e("flowGoIResponse_ICallBack")
                    }
                })
        }
    }

    fun observableGoCompose() {
        HttpUtils.instance
            .ConfigGlobalHttpUtils()
            .createService(Api::class.java)
            .getHomeListByObservable(0)
            //页面销毁自动取消请求
            .compose(LifecycleObservableTransformer(this@MainActivity))
            //自动处理网络加载中动画
            .compose(ProgressDialogObservableTransformer(this@MainActivity))
            .subscribe(object : CommonObserver<IResponse<HomeListBean>>() {
                override fun onFailure(iException: IException?) {
                    FailOverGson.failOverGson.toJson(iException).e("rxjavaGo")
                }

                override fun onSuccess(t: IResponse<HomeListBean>) {
                    FailOverGson.failOverGson.toJson(t).e("rxjavaGo")
                }
            })
    }

    fun observableGoObserver() {
        HttpUtils.instance
            .ConfigGlobalHttpUtils()
            .createService(Api::class.java)
            .getHomeListByObservable(0)
            //页面销毁自动取消请求
            //自动处理网络加载中动画
            .subscribe(object : CommonObserver<IResponse<HomeListBean>>(this@MainActivity) {
                override fun onFailure(iException: IException?) {
                    FailOverGson.failOverGson.toJson(iException).e("rxjavaGo")
                }

                override fun onSuccess(t: IResponse<HomeListBean>) {
                    FailOverGson.failOverGson.toJson(t).e("rxjavaGo")
                }
            })
    }

//手动取消网络请求
//        HttpUtils.instance.cancelSingleRequest(any);
//        HttpUtils.instance.cancelAllRequest();
```

## 混淆
-keep public class com.zhangteng.**.*{ *; }

## 历史版本
| 版本     | 更新                                       | 更新时间              |
|--------|------------------------------------------|-------------------|
| v2.0.0 | Retrofit网络加载库二次封装支持RxJava与Flow-HttpUtils | 2022/9/15 at 0:17 |

## 赞赏
如果您喜欢HttpUtils，或感觉HttpUtils帮助到了您，可以点右上角“Star”支持一下，您的支持就是我的动力，谢谢

## 联系我
邮箱：763263311@qq.com/ztxiaoran@foxmail.com

## License
Copyright (c) [2020] [Swing]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
