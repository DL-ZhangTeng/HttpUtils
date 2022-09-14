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

    //noinspection GradleDynamicVersion
    implementation 'com.github.DL-ZhangTeng:Utils:2.0.+'
```

## 属性
属性名| 描述
--- | -----
setBaseUrl| ConfigGlobalHttpUtils()全局的BaseUrl；ConfigSingleInstance()单独设置BaseUrl
addCallAdapterFactory| 设置CallAdapter.Factory,默认FlowCallAdapterFactory.create()、CoroutineCallAdapterFactory.create()、RxJava2CallAdapterFactory.create()
addConverterFactory| 设置Converter.Factory,默认GsonConverterFactory.create()
setDns| 自定义域名解析
setCache| 开启缓存策略
addHeader| 全局的单个请求头信息
setHeaders| 全局的请求头信息，设置静态请求头：更新请求头时不需要重新设置，对Map元素进行移除添加即可；设置动态请求头：如token等需要根据登录状态实时变化的请求头参数，最小支持api 24
setHttpCallBack| 设置网络请求前后回调函数 onHttpResponse:可以先客户端一步拿到每一次Http请求的结果 onHttpRequest:可以在请求服务器之前拿到
setSign| 全局验签，appKey与后端匹配即可，具体规则参考：https://blog.csdn.net/duoluo9/article/details/105214983
setEnAndDecryption| 全局加解密(AES+RSA)。1、公钥请求路径HttpUrl.get(BuildConfig.HOST + "/getPublicKey")；2、公钥响应结果{"result": {"publicKey": ""},"message": "查询成功!","status": 100}
setCookie|全局持久话cookie,保存本地每次都会携带在header中
addInterceptor|添加拦截器(继承PriorityInterceptor重写getPriority方法自定义顺序，自定义拦截器Priority必须>=10)
addInterceptors|添加拦截器(继承PriorityInterceptor重写getPriority方法自定义顺序，自定义拦截器Priority必须>=10)
addNetworkInterceptor|添加网络拦截器(继承PriorityInterceptor重写getPriority方法自定义顺序，自定义拦截器Priority必须>=10)
addNetworkInterceptors|添加网络拦截器(继承PriorityInterceptor重写getPriority方法自定义顺序，自定义拦截器Priority必须>=10)
setSslSocketFactory| 全局ssl证书认证。1、信任所有证书,不安全有风险，setSslSocketFactory()；2、使用预埋证书，校验服务端证书（自签名证书），setSslSocketFactory(getAssets().open("your.cer"))；3、使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书），setSslSocketFactory(getAssets().open("your.bks"), "123456", getAssets().open("your.cer"))
setReadTimeOut|全局超时配置
setWriteTimeOut|全局超时配置
setConnectionTimeOut| 全局超时配置
setLog| 全局是否打开请求log日志

## 使用
```java
 public class MainApplication extends Application {
     private static MainApplication mainApplication;
     private final Map<String, Object> headersMap = new HashMap<>();

     public static MainApplication getInstance() {
         return mainApplication;
     }

    @Override
    public void onCreate() {
        super.onCreate();
        HttpUtils.init(this);
        HttpUtils.getInstance()
                .ConfigGlobalHttpUtils()
                //全局的BaseUrl
                .setBaseUrl("http://**/")
                //设置CallAdapter.Factory,默认RxJavaCallAdapterFactory.create()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                //设置Converter.Factory,默认GsonConverterFactory.create()
                .addConverterFactory(GsonConverterFactory.create())
                //设置自定义域名解析
                .setDns(HttpDns.getInstance())
                //开启缓存策略
                .setCache(true)
                //全局的单个请求头信息
                .addHeader("Authorization", "Bearer ")
                //全局的静态请求头信息
                //.setHeaders(headersMap)
                //全局的请求头信息
                //.setHeaders(headersMap, headers -> {
                //  if (headers == null) {
                //      headers = new HashMap<>();
                //  }
                //  boolean isLogin = BuildConfig.DEBUG;
                //  if (isLogin) {
                //      headers.put("Authorization", "Bearer " + "token");
                //  } else {
                //      headers.remove("Authorization");
                //  }
                //  return headers;
                //})
                //全局的动态请求头信息
                .setHeaders(headers -> {
                    if (headers == null) {
                        headers = new HashMap<>();
                    }
                    headers.put("version", BuildConfig.VERSION_CODE);
                    headers.put("os", "android");

                    boolean isLogin = BuildConfig.DEBUG;
                    if (isLogin) {
                        headers.put("Authorization", "Bearer " + "token");
                    } else {
                        headers.remove("Authorization");
                    }
                    return headers;
                })
                .setHttpCallBack(new CallBackInterceptor.CallBack() {
                    @NonNull
                    @Override
                    public Response onHttpResponse(@NonNull Interceptor.Chain chain, @NonNull Response response) {
                        //这里可以先客户端一步拿到每一次 Http 请求的结果
                        ResponseBody body = response.newBuilder().build().body();
                        BufferedSource source = body.source();
                        try {
                            source.request(Long.MAX_VALUE); // Buffer the entire body.
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Buffer buffer = source.getBuffer();
                        Charset charset = StandardCharsets.UTF_8;
                        MediaType contentType = body.contentType();
                        if (contentType != null) {
                            charset = contentType.charset(charset);
                        }
                        LogUtilsKt.e(buffer.readString(charset));
                        return response;
                    }

                    @NonNull
                    @Override
                    public Request onHttpRequest(@NonNull Interceptor.Chain chain, @NonNull Request request) {
                        //这里可以在请求服务器之前拿到
                        LogUtilsKt.e(new Gson().toJson(request.headers()));
                        RequestBody body = request.body();
                        if (body != null) {
                            try {
                                LogUtilsKt.e(String.valueOf(body.contentLength()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        return request;
                    }
                })
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
                .setLog(true);
    }
}
```

```java
//使用生命周期监听自动取消请求、加载中动画自动处理（LifecycleObservableTransformer、ProgressDialogObservableTransformer）
 HttpUtils.getInstance()
                .ConfigGlobalHttpUtils()
                .createService(ApiService.class)
                .loginPwd("admin", "admin")
                .compose(new LifecycleObservableTransformer<>(MainActivity.this))
                .compose(new ProgressDialogObservableTransformer<>(mProgressDialog))
                .subscribe(new BaseObserver<BaseResponse<LoginBean>>() {
                    @Override
                    public void doOnSubscribe(Disposable d) {

                    }

                    @Override
                    public void doOnError(IException iException) {

                    }

                    @Override
                    public void doOnNext(BaseResponse<LoginBean> loginBeanBaseResponse) {

                    }

                    @Override
                    public void doOnCompleted() {

                    }
                });
                
//使用生命周期监听自动取消请求、加载中动画自动处理（CommonObserver方案）
//        HttpUtils.getInstance()
//                .ConfigSingleInstance()
//                .setBaseUrl("https://**/")
//                .createService(ApiService.class)
//                .loginPwd("admin", "admin")
//                .subscribeOn(Schedulers.io())
//                .doOnSubscribe(disposable -> mProgressDialog.show())
//                .subscribeOn(AndroidSchedulers.mainThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new CommonObserver<BaseResponse<LoginBean>>(mProgressDialog) {
//                    @Override
//                    protected void onFailure(IException iException) {
//
//                    }
//
//                    @Override
//                    protected void onSuccess(BaseResponse<LoginBean> loginBeanBaseResponse) {
//                        ToastUtilsKt.showShortToast(MainActivity.this, loginBeanBaseResponse.getMsg());
//                    }
//                });

//手动取消网络请求
//        HttpUtils.getInstance().cancelSingleRequest(this);
//        HttpUtils.getInstance().cancelSingleRequest(Disposable);
//        HttpUtils.getInstance().cancelAllRequest();
```

## 混淆
-keep public class com.zhangteng.**.*{ *; }
## 历史版本
版本| 更新| 更新时间
-------- | ----- | -----
v2.0.0|Retrofit网络加载库二次封装支持RxJava与Flow-HttpUtils|2022/9/15 at 0:17

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
