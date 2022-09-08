package com.zhangteng.httputils.http

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.zhangteng.httputils.config.EncryptConfig
import com.zhangteng.httputils.interceptor.*
import com.zhangteng.httputils.interceptor.CallBackInterceptor.CallBack
import com.zhangteng.httputils.utils.RetrofitServiceProxyHandler
import com.zhangteng.utils.SSLUtils
import com.zhangteng.utils.SSLUtils.getSslSocketFactory
import com.zhangteng.utils.SSLUtils.sslSocketFactory
import com.zhangteng.utils.getDiskCacheDir
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.File
import java.io.InputStream
import java.lang.reflect.Proxy
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Function
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * Created by swing on 2018/4/24.
 */
class SingleHttpUtils private constructor() {
    private var baseUrl: String? = null
    private var dns: Dns? = null
    private var cache: Cache? = null
    private var readTimeout: Long = 0
    private var writeTimeout: Long = 0
    private var connectTimeout: Long = 0
    private var sslParams: SSLUtils.SSLParams? = null
    private val converterFactories: MutableList<Converter.Factory>
    private val adapterFactories: MutableList<CallAdapter.Factory>

    /**
     * description: 拦截器集合,按照优先级从小到大排序
     */
    private val priorityInterceptors: TreeSet<PriorityInterceptor>

    /**
     * description: 网络拦截器集合,按照优先级从小到大排序
     */
    private val networkInterceptors: TreeSet<PriorityInterceptor>

    /**
     * description 设置网络baseUrl
     *
     * @param baseUrl 接口前缀
     */
    fun setBaseUrl(baseUrl: String?): SingleHttpUtils {
        this.baseUrl = baseUrl
        return this
    }

    /**
     * 局部设置Converter.Factory,默认GsonConverterFactory.create()
     */
    fun addConverterFactory(factory: Converter.Factory?): SingleHttpUtils {
        if (factory != null) {
            converterFactories.add(factory)
        }
        return this
    }

    /**
     * 局部设置CallAdapter.Factory,默认FlowCallAdapterFactory.create()、CoroutineCallAdapterFactory.create()
     */
    fun addCallAdapterFactory(factory: CallAdapter.Factory?): SingleHttpUtils {
        if (factory != null) {
            adapterFactories.add(factory)
        }
        return this
    }

    /**
     * description 设置域名解析服务器
     *
     * @param dns 域名解析服务器
     */
    fun setDns(dns: Dns?): SingleHttpUtils {
        this.dns = dns
        return this
    }

    /**
     * description 添加单个请求头公共参数，当okHttpClient构建完成后依旧可以新增全局请求头参数，可随时添加修改公共请求头
     *
     * @param key   请求头 key
     * @param value 请求头 value
     */
    fun addHeader(key: String?, value: Any?): SingleHttpUtils {
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
    fun setHeaders(headerMaps: MutableMap<String?, Any?>?): SingleHttpUtils {
        priorityInterceptors.add(HeaderInterceptor(headerMaps))
        return this
    }

    /**
     * description 动态设置请求头，如token等需要根据登录状态实时变化的请求头参数，最小支持api 24
     *
     * @param headersFunction 请求头设置的函数式参数，如token等需要根据登录状态实时变化的请求头参数
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun setHeaders(headersFunction: Function<Map<String?, Any?>?, MutableMap<String?, Any?>>?): SingleHttpUtils {
        priorityInterceptors.add(HeaderInterceptor(headersFunction))
        return this
    }

    /**
     * description 设置请求头公共参数，最小支持api 24
     *
     * @param headerMaps      请求头设置的静态参数
     * @param headersFunction 请求头设置的函数式参数，如token等需要根据登录状态实时变化的请求头参数
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun setHeaders(
        headerMaps: MutableMap<String?, Any?>?,
        headersFunction: Function<Map<String?, Any?>?, MutableMap<String?, Any?>>?
    ): SingleHttpUtils {
        priorityInterceptors.add(HeaderInterceptor(headerMaps, headersFunction))
        return this
    }

    /**
     * description 开启网络日志
     *
     * @param isShowLog 是否
     */
    fun setLog(isShowLog: Boolean): SingleHttpUtils {
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
    fun setLog(logger: HttpLoggingInterceptor.Logger): SingleHttpUtils {
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
    fun setCache(isCache: Boolean): SingleHttpUtils {
        if (isCache) {
            val cacheInterceptor = CacheInterceptor()
            val file: File = File(
                HttpUtils.instance.context
                    .getDiskCacheDir() + "/RxHttpUtilsCache"
            )
            cache = Cache(file, 1024 * 1024)
            priorityInterceptors.add(cacheInterceptor)
            networkInterceptors.add(cacheInterceptor)
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
    fun setCache(isCache: Boolean, path: String?, maxSize: Long): SingleHttpUtils {
        if (isCache) {
            val cacheInterceptor = CacheInterceptor()
            val file = File(path)
            cache = Cache(file, maxSize)
            priorityInterceptors.add(cacheInterceptor)
            networkInterceptors.add(cacheInterceptor)
        }
        return this
    }

    /**
     * description 设置Cookie
     *
     * @param saveCookie 是否设置Cookie
     */
    fun setCookie(saveCookie: Boolean): SingleHttpUtils {
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
    fun setHttpCallBack(callBack: CallBack?): SingleHttpUtils {
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
    fun setSign(appKey: String?): SingleHttpUtils {
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
    fun setEnAndDecryption(publicKeyUrl: HttpUrl?, publicKey: String?): SingleHttpUtils {
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
    fun addInterceptor(interceptor: PriorityInterceptor): SingleHttpUtils {
        priorityInterceptors.add(interceptor)
        return this
    }

    /**
     * description 添加拦截器
     *
     * @param interceptors 带优先级的拦截器
     */
    fun addInterceptors(interceptors: List<PriorityInterceptor>?): SingleHttpUtils {
        priorityInterceptors.addAll(interceptors!!)
        return this
    }

    /**
     * description 添加网络拦截器
     *
     * @param interceptor 带优先级的拦截器
     */
    fun addNetworkInterceptor(interceptor: PriorityInterceptor): SingleHttpUtils {
        networkInterceptors.add(interceptor)
        return this
    }

    /**
     * description 添加网络拦截器
     *
     * @param interceptors 带优先级的拦截器
     */
    fun addNetworkInterceptors(interceptors: List<PriorityInterceptor>?): SingleHttpUtils {
        networkInterceptors.addAll(interceptors!!)
        return this
    }

    /**
     * description 超时时间
     *
     * @param readTimeout 秒
     */
    fun setReadTimeOut(readTimeout: Long): SingleHttpUtils {
        this.readTimeout = readTimeout
        return this
    }

    /**
     * description 超时时间
     *
     * @param writeTimeout 秒
     */
    fun setWriteTimeOut(writeTimeout: Long): SingleHttpUtils {
        this.writeTimeout = writeTimeout
        return this
    }

    /**
     * description 超时时间
     *
     * @param connectTimeout 秒
     */
    fun setConnectionTimeOut(connectTimeout: Long): SingleHttpUtils {
        this.connectTimeout = connectTimeout
        return this
    }

    /**
     * description 信任所有证书,不安全有风险
     */
    fun setSslSocketFactory(): SingleHttpUtils {
        sslParams = sslSocketFactory
        return this
    }

    /**
     * description 使用预埋证书，校验服务端证书（自签名证书）
     *
     * @param certificates 证书
     */
    fun setSslSocketFactory(vararg certificates: InputStream?): SingleHttpUtils {
        sslParams = getSslSocketFactory(*certificates)
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
    ): SingleHttpUtils {
        sslParams = getSslSocketFactory(bksFile, password, *certificates)
        return this
    }

    /**
     * description 动态代理方式获取RetrofitService
     *
     * @param cls 网络接口
     */
    fun <K> createService(cls: Class<K>): K {
        return Proxy.newProxyInstance(
            cls.classLoader, arrayOf<Class<*>>(cls),
            RetrofitServiceProxyHandler(singleRetrofitBuilder.build(), cls)
        ) as K
    }//获取全局的对象重新设置//获取全局的对象重新设置

    /**
     * description 单个RetrofitBuilder
     */
    private val singleRetrofitBuilder: Retrofit.Builder
        private get() {
            val singleRetrofitBuilder = Retrofit.Builder()
            if (converterFactories.isEmpty()) {
                //获取全局的对象重新设置
                val listConverterFactory: List<Converter.Factory> =
                    GlobalHttpUtils.instance.retrofit!!.converterFactories()
                for (factory in listConverterFactory) {
                    singleRetrofitBuilder.addConverterFactory(factory)
                }
            } else {
                for (converterFactory in converterFactories) {
                    singleRetrofitBuilder.addConverterFactory(converterFactory)
                }
            }
            if (adapterFactories.isEmpty()) {
                //获取全局的对象重新设置
                val listAdapterFactory: List<CallAdapter.Factory> =
                    GlobalHttpUtils.instance.retrofit!!.callAdapterFactories()
                for (factory in listAdapterFactory) {
                    singleRetrofitBuilder.addCallAdapterFactory(factory)
                }
            } else {
                for (adapterFactory in adapterFactories) {
                    singleRetrofitBuilder.addCallAdapterFactory(adapterFactory)
                }
            }
            if (baseUrl == null || baseUrl!!.isEmpty()) {
                singleRetrofitBuilder.baseUrl(
                    GlobalHttpUtils.instance.retrofit!!.baseUrl()
                )
            } else {
                singleRetrofitBuilder.baseUrl(baseUrl)
            }
            singleRetrofitBuilder.client(singleOkHttpBuilder.build())
            clearParams()
            return singleRetrofitBuilder
        }

    /**
     * description 获取单个 OkHttpClient.Builder
     */
    private val singleOkHttpBuilder: OkHttpClient.Builder
        private get() {
            val singleOkHttpBuilder = OkHttpClient.Builder()
            singleOkHttpBuilder.retryOnConnectionFailure(true)
            if (dns != null) {
                singleOkHttpBuilder.dns(dns!!)
            }
            if (cache != null) {
                singleOkHttpBuilder.cache(cache)
            }
            for (priorityInterceptor in priorityInterceptors) {
                singleOkHttpBuilder.addInterceptor(priorityInterceptor)
            }
            for (priorityInterceptor in networkInterceptors) {
                singleOkHttpBuilder.addNetworkInterceptor(priorityInterceptor)
            }
            singleOkHttpBuilder.readTimeout(
                if (readTimeout > 0) readTimeout else 10,
                TimeUnit.SECONDS
            )
            singleOkHttpBuilder.writeTimeout(
                if (writeTimeout > 0) writeTimeout else 10,
                TimeUnit.SECONDS
            )
            singleOkHttpBuilder.connectTimeout(
                if (connectTimeout > 0) connectTimeout else 10,
                TimeUnit.SECONDS
            )
            if (sslParams != null) {
                singleOkHttpBuilder.sslSocketFactory(
                    Objects.requireNonNull<SSLSocketFactory>(
                        sslParams!!.sSLSocketFactory
                    ), Objects.requireNonNull<X509TrustManager>(sslParams!!.trustManager)
                )
            }
            return singleOkHttpBuilder
        }

    private fun clearParams() {
        baseUrl = null
        dns = null
        cache = null
        readTimeout = 0
        writeTimeout = 0
        connectTimeout = 0
        sslParams = null
        converterFactories.clear()
        adapterFactories.clear()
        priorityInterceptors.clear()
        networkInterceptors.clear()
    }

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            SingleHttpUtils()
        }
    }

    init {
        converterFactories = ArrayList()
        adapterFactories = ArrayList()
        priorityInterceptors = TreeSet { o: PriorityInterceptor, r: PriorityInterceptor ->
            Integer.compare(
                o.priority,
                r.priority
            )
        }
        networkInterceptors = TreeSet { o: PriorityInterceptor, r: PriorityInterceptor ->
            Integer.compare(
                o.priority,
                r.priority
            )
        }
    }
}