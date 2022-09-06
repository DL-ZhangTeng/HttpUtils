package com.zhangteng.httputils.http

import android.app.Application
import android.content.Context
import com.zhangteng.httputils.config.SPConfig
import com.zhangteng.httputils.fileload.download.DownloadRetrofit
import com.zhangteng.httputils.fileload.upload.UploadRetrofit
import com.zhangteng.utils.getFromSPToSet
import io.reactivex.disposables.Disposable

/**
 * Created by swing on 2018/4/24.
 */
class HttpUtils private constructor() {
    /**
     * description 获取全局context
     */
    val context: Context?
        get() {
            checkInitialize()
            return Companion.context
        }

    /**
     * description 全局网络请求工具
     */
    fun ConfigGlobalHttpUtils(): GlobalHttpUtils? {
        return GlobalHttpUtils.instance
    }

    /**
     * description 单个网络请求工具
     */
    fun ConfigSingleInstance(): SingleHttpUtils? {
        return SingleHttpUtils.instance
    }

    /**
     * description 上传请求工具
     */
    fun UploadRetrofit(): UploadRetrofit? {
        return UploadRetrofit.instance
    }

    /**
     * description 下载请求工具
     */
    fun DownloadRetrofit(): DownloadRetrofit? {
        return DownloadRetrofit.instance
    }

    val cookie: HashSet<String>?
        get() = Companion.context.getFromSPToSet(
            SPConfig.FILE_NAME,
            SPConfig.COOKIE,
            HashSet()
        ) as HashSet<String>?

    /**
     * description 添加可处理对象集合
     *
     * @param disposable 可取消的对象
     */
    fun addDisposable(disposable: Disposable?) {
        if (disposables != null) {
            disposables!![disposable] = null
        }
    }

    /**
     * description 添加可处理对象集合，可使用标记tag取消请求，配合生命周期监听可以在页面销毁时自动取消全部请求
     *
     * @param disposable 可取消的对象
     * @param tag        标记
     */
    fun addDisposable(disposable: Disposable?, tag: Any?) {
        if (disposables != null) {
            disposables!![disposable] = tag
        }
    }

    /**
     * description 清除所有请求
     */
    fun cancelAllRequest() {
        if (disposables != null) {
            for (disposable in disposables!!.keys) {
                disposable!!.dispose()
            }
            disposables!!.clear()
        }
    }

    /**
     * description 取消单个请求，且单个请求结束后需要移除disposables时也可使用本方法
     *
     * @param disposable 可取消的对象
     */
    fun cancelSingleRequest(disposable: Disposable?) {
        if (disposable != null && !disposable.isDisposed) {
            disposable.dispose()
        }
        if (disposables != null) {
            disposables!!.remove(disposable)
        }
    }

    /**
     * @param tag 单个请求的标识（推荐使用Activity/Fragment.this）,多个请求可以使用同一个tag，取消请求时会同时取消
     * @description 通过tag取消请求，tag可以通过CommonObserver/LifecycleObservableTransformer创建时传入
     */
    fun cancelSingleRequest(tag: Any?) {
        if (tag != null && disposables != null) {
            val set: MutableSet<MutableMap.MutableEntry<Disposable?, Any?>> = disposables!!.entries
            val iterator = set.iterator()
            while (iterator.hasNext()) {
                val (disposable, value) = iterator.next()
                if (tag == value && !disposable!!.isDisposed) {
                    disposable.dispose()
                }
                if (disposable!!.isDisposed) {
                    iterator.remove()
                }
            }
        }
    }

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            HttpUtils()
        }
        private var context: Application? = null
        private var disposables: HashMap<Disposable?, Any?>? = null

        /**
         * description 初始化context
         */
        @kotlin.jvm.JvmStatic
        fun init(app: Application?) {
            context = app
        }

        private fun checkInitialize() {
            if (context == null) {
                throw ExceptionInInitializerError("请先在全局Application中调用 HttpUtils.init() 初始化！")
            }
        }
    }

    init {
        disposables = HashMap()
    }
}