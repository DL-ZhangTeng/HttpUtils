package com.zhangteng.httputils.http

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.zhangteng.httputils.adapter.coroutine.CoroutineCallAdapterFactory
import com.zhangteng.httputils.adapter.flow.FlowCallAdapterFactory
import com.zhangteng.httputils.config.EncryptConfig
import com.zhangteng.httputils.interceptor.*
import com.zhangteng.httputils.interceptor.CallBackInterceptor.CallBack
import com.zhangteng.httputils.utils.RetrofitServiceProxyHandler
import com.zhangteng.utils.LruCache
import com.zhangteng.utils.SSLUtils
import com.zhangteng.utils.SSLUtils.getSslSocketFactory
import com.zhangteng.utils.SSLUtils.sslSocketFactory
import com.zhangteng.utils.getDiskCacheDir
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStream
import java.lang.reflect.Proxy
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * Created by swing on 2018/4/24.
 */
class GlobalHttpUtils private constructor() {
    /**
     * description: 全局okhttpBuilder，保证使用一个网络实例
     */
    private val okhttpBuilder: OkHttpClient.Builder = OkHttpClient.Builder()

    /**
     * description: 全局retrofitBuilder，保证使用一个网络实例
     */
    private val retrofitBuilder: Retrofit.Builder = Retrofit.Builder()

    /**
     * description: 全局okHttpClient，保证使用一个网络实例
     */
    private var okHttpClient: OkHttpClient? = null

    /**
     * description: 全局retrofit，保证使用一个网络实例，当retrofit构建完成后无法修改全局baseUrl
     */
    var retrofit: Retrofit? = null
        get() {
            if (okHttpClient == null) {
                for (priorityInterceptor in priorityInterceptors) {
                    okhttpBuilder.addInterceptor(priorityInterceptor)
                }
                for (priorityInterceptor in networkInterceptors) {
                    okhttpBuilder.addNetworkInterceptor(priorityInterceptor)
                }
                okHttpClient = okhttpBuilder.build()
            }
            if (field == null) {
                if (retrofitBuilder.callAdapterFactories().isEmpty()) {
                    retrofitBuilder.addCallAdapterFactory(CoroutineCallAdapterFactory.create())
                    retrofitBuilder.addCallAdapterFactory(FlowCallAdapterFactory.create())

                    if (HttpUtils.instance.isRxjava2) {
                        retrofitBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    }
                }
                if (retrofitBuilder.converterFactories().isEmpty()) {
                    retrofitBuilder.addConverterFactory(GsonConverterFactory.create())
                }
                field = retrofitBuilder.client(okHttpClient!!).build()
            }
            return field
        }
        private set

    /**
     * description: Lru缓存
     */
    private var mRetrofitServiceCache: LruCache<String, Any>? = null

    /**
     * description: 拦截器集合,按照优先级从小到大排序
     */
    private val priorityInterceptors: TreeSet<PriorityInterceptor> =
        TreeSet { o: PriorityInterceptor, r: PriorityInterceptor ->
            o.priority.compareTo(r.priority)
        }

    /**
     * description: 网络拦截器集合,按照优先级从小到大排序
     */
    private val networkInterceptors: TreeSet<PriorityInterceptor> =
        TreeSet { o: PriorityInterceptor, r: PriorityInterceptor ->
            o.priority.compareTo(r.priority)
        }

    /**
     * description 设置网络baseUrl
     *
     * @param baseUrl 接口前缀
     */
    fun setBaseUrl(baseUrl: String): GlobalHttpUtils {
        retrofitBuilder.baseUrl(baseUrl)
        return this
    }

    /**
     * description 设置Converter.Factory,默认GsonConverterFactory.create()
     */
    fun addConverterFactory(factory: Converter.Factory): GlobalHttpUtils {
        retrofitBuilder.addConverterFactory(factory)
        return this
    }

    /**
     * description 设置CallAdapter.Factory,默认FlowCallAdapterFactory.create()、CoroutineCallAdapterFactory.create()、RxJava2CallAdapterFactory.create()
     */
    fun addCallAdapterFactory(factory: CallAdapter.Factory): GlobalHttpUtils {
        retrofitBuilder.addCallAdapterFactory(factory)
        return this
    }

    /**
     * description 设置域名解析服务器
     *
     * @param dns 域名解析服务器
     */
    fun setDns(dns: Dns): GlobalHttpUtils {
        okhttpBuilder.dns(dns)
        return this
    }

    /**
     * description 添加单个请求头公共参数，当okHttpClient构建完成后依旧可以新增全局请求头参数，可随时添加修改公共请求头
     *
     * @param key   请求头 key
     * @param value 请求头 value
     */
    fun addHeader(key: String?, value: Any?): GlobalHttpUtils {
        var headerInterceptor: Interceptor? = null
        for (interceptor in priorityInterceptors) {
            if (interceptor is HeaderInterceptor) {
                headerInterceptor = interceptor
                headerInterceptor.headerMaps?.set(key, value)
            }
        }
        if (headerInterceptor == null) {
            val headerMaps: MutableMap<String?, Any?> = HashMap()
            headerMaps[key] = value
            priorityInterceptors.add(HeaderInterceptor(headerMaps))
        }
        return this
    }

    /**
     * description 设置请求头公共参数，当okHttpClient构建完成后无法新增全局请求头参数
     *
     * @param headerMaps 请求头设置的静态参数
     */
    fun setHeaders(headerMaps: MutableMap<String?, Any?>?): GlobalHttpUtils {
        priorityInterceptors.add(HeaderInterceptor(headerMaps))
        return this
    }

    /**
     * description 动态设置请求头，如token等需要根据登录状态实时变化的请求头参数
     *
     * @param headersFunction 请求头设置的函数式参数，如token等需要根据登录状态实时变化的请求头参数
     */
    fun setHeaders(headersFunction: (MutableMap<String?, Any?>) -> MutableMap<String?, Any?>): GlobalHttpUtils {
        priorityInterceptors.add(HeaderInterceptor(headersFunction))
        return this
    }

    /**
     * description 设置请求头公共参数
     *
     * @param headerMaps      请求头设置的静态参数
     * @param headersFunction 请求头设置的函数式参数，如token等需要根据登录状态实时变化的请求头参数
     */
    fun setHeaders(
        headerMaps: MutableMap<String?, Any?>?,
        headersFunction: (MutableMap<String?, Any?>) -> MutableMap<String?, Any?>
    ): GlobalHttpUtils {
        priorityInterceptors.add(HeaderInterceptor(headerMaps, headersFunction))
        return this
    }

    /**
     * description 开启网络日志
     *
     * @param isShowLog 是否
     */
    fun setLog(isShowLog: Boolean): GlobalHttpUtils {
        if (isShowLog) {
            val loggingInterceptor =
                HttpLoggingInterceptor { message: String? -> Log.i("HttpUtils", message!!) }
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val proxyInterceptor = HttpLoggingProxyInterceptor(loggingInterceptor)
            priorityInterceptors.add(proxyInterceptor)
        }
        return this
    }

    /**
     * description 开启网络日志
     *
     * @param logger 自定义日志打印类
     */
    fun setLog(logger: HttpLoggingInterceptor.Logger): GlobalHttpUtils {
        val loggingInterceptor = HttpLoggingInterceptor(logger)
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val proxyInterceptor = HttpLoggingProxyInterceptor(loggingInterceptor)
        priorityInterceptors.add(proxyInterceptor)
        return this
    }

    /**
     * description 设置网络缓存，有网时使用网络默认缓存策略，无网时使用强制缓存策略，默认缓存文件路径Environment.getExternalStorageDirectory() + "/RxHttpUtilsCache"，缓存文件大小1024 * 1024
     *
     * @param isCache 是否开启缓存
     */
    fun setCache(isCache: Boolean): GlobalHttpUtils {
        if (isCache) {
            val cacheInterceptor = CacheInterceptor()
            val file: File = File(
                HttpUtils.Companion.instance.context
                    .getDiskCacheDir() + "/RxHttpUtilsCache"
            )
            val cache = Cache(file, 1024 * 1024)
            priorityInterceptors.add(cacheInterceptor)
            networkInterceptors.add(cacheInterceptor)
            okhttpBuilder.cache(cache)
        }
        return this
    }

    /**
     * description 设置网络缓存，有网时使用网络默认缓存策略，无网时使用强制缓存策略
     *
     * @param isCache 是否开启缓存
     * @param path    缓存文件路径
     * @param maxSize 缓存文件大小
     */
    fun setCache(isCache: Boolean, path: String, maxSize: Long): GlobalHttpUtils {
        if (isCache) {
            val cacheInterceptor = CacheInterceptor()
            val file = File(path)
            val cache = Cache(file, maxSize)
            priorityInterceptors.add(cacheInterceptor)
            networkInterceptors.add(cacheInterceptor)
            okhttpBuilder.cache(cache)
        }
        return this
    }

    /**
     * description 设置Cookie
     *
     * @param saveCookie 是否设置Cookie
     */
    fun setCookie(saveCookie: Boolean): GlobalHttpUtils {
        if (saveCookie) {
            priorityInterceptors.add(AddCookieInterceptor())
            networkInterceptors.add(SaveCookieInterceptor())
        }
        return this
    }

    /**
     * description 设置网络请求前后回调函数
     *
     * @param callBack 网络回调类
     */
    fun setHttpCallBack(callBack: CallBack?): GlobalHttpUtils {
        if (callBack != null) {
            priorityInterceptors.add(CallBackInterceptor(callBack))
        }
        return this
    }

    /**
     * description 网络请求加签
     * 1、身份验证：是否是我规定的那个人
     * 2、防篡改：是否被第三方劫持并篡改参数
     * 3、防重放：是否重复请求
     *
     * @param appKey 验签时前后端匹配的appKey，前后端一致即可
     */
    fun setSign(appKey: String?): GlobalHttpUtils {
        priorityInterceptors.add(SignInterceptor(appKey))
        return this
    }

    /**
     * description 数据加解密
     * 数据加密，防止信息截取，具体加解密方案参考https://blog.csdn.net/duoluo9/article/details/105214983?spm=1001.2014.3001.5501
     *
     * @param publicKeyUrl rsa公钥失效后重新请求秘钥的接口
     * @param publicKey    rsa公钥
     */
    fun setEnAndDecryption(publicKeyUrl: HttpUrl?, publicKey: String?): GlobalHttpUtils {
        EncryptConfig.publicKeyUrl = publicKeyUrl
        EncryptConfig.publicKey = publicKey!!
        priorityInterceptors.add(EncryptionInterceptor())
        networkInterceptors.add(DecryptionInterceptor())
        return this
    }

    /**
     * description 添加拦截器
     *
     * @param interceptor 带优先级的拦截器
     */
    fun addInterceptor(interceptor: PriorityInterceptor): GlobalHttpUtils {
        priorityInterceptors.add(interceptor)
        return this
    }

    /**
     * description 添加拦截器
     *
     * @param interceptors 带优先级的拦截器
     */
    fun addInterceptors(interceptors: List<PriorityInterceptor>?): GlobalHttpUtils {
        priorityInterceptors.addAll(interceptors!!)
        return this
    }

    /**
     * description 添加网络拦截器
     *
     * @param interceptor 带优先级的拦截器
     */
    fun addNetworkInterceptor(interceptor: PriorityInterceptor): GlobalHttpUtils {
        networkInterceptors.add(interceptor)
        return this
    }

    /**
     * description 添加网络拦截器
     *
     * @param interceptors 带优先级的拦截器
     */
    fun addNetworkInterceptors(interceptors: List<PriorityInterceptor>?): GlobalHttpUtils {
        networkInterceptors.addAll(interceptors!!)
        return this
    }

    /**
     * description 超时时间
     *
     * @param second 秒
     */
    fun setReadTimeOut(second: Long): GlobalHttpUtils {
        okhttpBuilder
            .readTimeout(second, TimeUnit.SECONDS)
        return this
    }

    /**
     * description 超时时间
     *
     * @param second 秒
     */
    fun setWriteTimeOut(second: Long): GlobalHttpUtils {
        okhttpBuilder
            .writeTimeout(second, TimeUnit.SECONDS)
        return this
    }

    /**
     * description 超时时间
     *
     * @param second 秒
     */
    fun setConnectionTimeOut(second: Long): GlobalHttpUtils {
        okhttpBuilder
            .connectTimeout(second, TimeUnit.SECONDS)
        return this
    }

    /**
     * description 信任所有证书,不安全有风险
     */
    fun setSslSocketFactory(): GlobalHttpUtils {
        val sslParams: SSLUtils.SSLParams = sslSocketFactory
        okhttpBuilder.sslSocketFactory(
            Objects.requireNonNull<SSLSocketFactory>(sslParams.sSLSocketFactory),
            Objects.requireNonNull<X509TrustManager>(sslParams.trustManager)
        )
        return this
    }

    /**
     * description 使用预埋证书，校验服务端证书（自签名证书）
     *
     * @param certificates 证书
     */
    fun setSslSocketFactory(vararg certificates: InputStream?): GlobalHttpUtils {
        val sslParams: SSLUtils.SSLParams = getSslSocketFactory(*certificates)
        okhttpBuilder.sslSocketFactory(
            Objects.requireNonNull<SSLSocketFactory>(sslParams.sSLSocketFactory),
            Objects.requireNonNull<X509TrustManager>(sslParams.trustManager)
        )
        return this
    }

    /**
     * description 使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
     *
     * @param bksFile      bks证书
     * @param password     密码
     * @param certificates 证书
     */
    fun setSslSocketFactory(
        bksFile: InputStream?,
        password: String?,
        vararg certificates: InputStream?
    ): GlobalHttpUtils {
        val sslParams: SSLUtils.SSLParams = getSslSocketFactory(bksFile, password, *certificates)
        okhttpBuilder.sslSocketFactory(
            Objects.requireNonNull<SSLSocketFactory>(sslParams.sSLSocketFactory),
            Objects.requireNonNull<X509TrustManager>(sslParams.trustManager)
        )
        return this
    }

    /**
     * description 获取RetrofitService，如果已被创建则添加缓存，下次直接从缓存中获取RetrofitService
     *
     * @param cls 网络接口
     */
    fun <K> createService(cls: Class<K>): K {
        if (mRetrofitServiceCache == null) {
            try {
                val activityManager =
                    HttpUtils.instance.context!!.getSystemService(
                        Context.ACTIVITY_SERVICE
                    ) as ActivityManager
                val targetMemoryCacheSize =
                    (activityManager.memoryClass * MAX_SIZE_MULTIPLIER * 1024).toInt()
                if (targetMemoryCacheSize < MAX_SIZE) {
                    cache_size = targetMemoryCacheSize
                }
            } catch (exception: ExceptionInInitializerError) {
                cache_size = MAX_SIZE
            }
            mRetrofitServiceCache = LruCache(cache_size)
        }
        var retrofitService = mRetrofitServiceCache!![cls.canonicalName] as K
        if (retrofitService == null) {
            retrofitService = Proxy.newProxyInstance(
                cls.classLoader, arrayOf<Class<*>>(cls),
                RetrofitServiceProxyHandler(retrofit, cls)
            ) as K
            mRetrofitServiceCache!!.put(cls.canonicalName, retrofitService)
        }
        return retrofitService
    }

    /**
     * description 动态代理方式获取RetrofitService
     *
     * @param cls 网络接口
     */
    fun <K> createServiceNoCache(cls: Class<K>): K {
        return Proxy.newProxyInstance(
            cls.classLoader, arrayOf<Class<*>>(cls),
            RetrofitServiceProxyHandler(retrofit, cls)
        ) as K
    }

    /**
     * description 全局 okhttpBuilder
     */
    fun getOkHttpClientBuilder(): OkHttpClient.Builder {
        return okhttpBuilder
    }

    /**
     * description 全局 okHttpClient，当okHttpClient构建完成后无法新增全局参数
     */
    fun getOkHttpClient(): OkHttpClient {
        if (okHttpClient == null) {
            for (priorityInterceptor in priorityInterceptors) {
                okhttpBuilder.addInterceptor(priorityInterceptor)
            }
            for (priorityInterceptor in networkInterceptors) {
                okhttpBuilder.addNetworkInterceptor(priorityInterceptor)
            }
            okHttpClient = okhttpBuilder.build()
        }
        return okHttpClient!!
    }

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            GlobalHttpUtils()
        }

        /**
         * description: 最大缓存数量
         */
        private const val MAX_SIZE = 150

        /**
         * description: 缓存扩容因数
         */
        private const val MAX_SIZE_MULTIPLIER = 0.002f

        /**
         * description: 缓存数量
         */
        private var cache_size = MAX_SIZE
    }

}