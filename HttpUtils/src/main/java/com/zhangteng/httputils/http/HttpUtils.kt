package com.zhangteng.httputils.http

import android.app.Application
import android.content.Context
import com.zhangteng.httputils.config.SPConfig
import com.zhangteng.httputils.fileload.download.DownloadRetrofit
import com.zhangteng.httputils.fileload.upload.UploadRetrofit
import com.zhangteng.utils.getFromSPToSet
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Job

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
    fun ConfigGlobalHttpUtils(): GlobalHttpUtils {
        return GlobalHttpUtils.instance
    }

    /**
     * description 单个网络请求工具
     */
    fun ConfigSingleInstance(): SingleHttpUtils {
        return SingleHttpUtils.instance
    }

    /**
     * description 上传请求工具
     */
    fun UploadRetrofit(): UploadRetrofit {
        return UploadRetrofit.instance
    }

    /**
     * description 下载请求工具
     */
    fun DownloadRetrofit(): DownloadRetrofit {
        return DownloadRetrofit.instance
    }

    fun getCookie(): HashSet<String>? {
        return Companion.context.getFromSPToSet(
            SPConfig.FILE_NAME,
            SPConfig.COOKIE,
            HashSet()
        ) as HashSet<String>?
    }

    /**
     * description 添加可处理对象集合，可使用标记tag取消请求，配合生命周期监听可以在页面销毁时自动取消全部请求
     *
     * @param disposable 可取消的对象
     * @param tag        标记
     */
    fun addDisposable(disposable: Any, tag: Any? = null) {
        disposables[disposable] = tag
    }

    /**
     * description 取消单个请求，且单个请求结束后需要移除disposables时也可使用本方法
     *
     * @param any 1、可取消的对象 Disposable或者Job
     *            2、单个请求的标识（推荐使用Activity/Fragment.this）,多个请求可以使用同一个tag，取消请求时会同时取消。通过tag取消请求，tag可以通过CommonObserver/LifecycleObservableTransformer创建时传入
     */
    fun cancelSingleRequest(any: Any) {
        if (any is Job) {//如果是Job直接取消
            if (!any.isCancelled && disposables.containsKey(any)) {
                any.cancel()
                disposables.remove(any)
            }
        } else if (any is Disposable) {//如果是Disposable直接取消
            if (!any.isDisposed && disposables.containsKey(any)) {
                any.dispose()
                disposables.remove(any)
            }
        } else {//其它情况按照tag处理
            val set: MutableSet<MutableMap.MutableEntry<Any, Any?>> = disposables.entries
            val iterator = set.iterator()
            while (iterator.hasNext()) {
                val (disposable, value) = iterator.next()
                if (disposable is Job) {//如果tag对应的可取消类型是Job
                    if (any == value && !disposable.isCancelled) {
                        disposable.cancel()
                    }
                    iterator.remove()
                } else if (disposable is Disposable) {//如果tag对应的可取消类型是Disposable
                    if (any == value && !disposable.isDisposed) {
                        disposable.dispose()
                    }
                    iterator.remove()
                }
            }
        }
    }

    /**
     * description 清除所有请求
     */
    fun cancelAllRequest() {
        for (disposable in disposables.keys) {
            if (disposable is Job && !disposable.isCancelled) {//如果是Job直接取消
                disposable.cancel()
            } else if (disposable is Disposable && !disposable.isDisposed) {//如果是Disposable直接取消
                disposable.dispose()
            }
        }
        disposables.clear()
    }

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            HttpUtils()
        }
        private var context: Application? = null
        private var disposables: HashMap<Any, Any?> = HashMap()

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
}