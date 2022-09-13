package com.zhangteng.httputils.http

import android.app.Application
import android.content.Context
import com.zhangteng.httputils.config.SPConfig
import com.zhangteng.httputils.fileload.download.DownloadRetrofit
import com.zhangteng.httputils.fileload.upload.UploadRetrofit
import com.zhangteng.utils.getFromSPToSet
import com.zhangteng.utils.i
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

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
     * description rxjava2是否可用，导入rxjava相关类自动变为true
     */
    val isRxjava2: Boolean
        get() {
            return Companion.isRxjava2
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
    @Throws(IllegalStateException::class)
    fun cancelSingleRequest(any: Any) {
        when (any) {
            is Job -> {//如果是Job直接取消
                if (!any.isCancelled && disposables.containsKey(any)) {
                    disposables.remove(any)
                    any.cancel(CancellationException(cancelMsg))
                }
            }
            is CoroutineContext -> {//如果是CoroutineContext直接取消
                if (any.isActive && disposables.containsKey(any)) {
                    disposables.remove(any)
                    any.cancel(CancellationException(cancelMsg))
                }
            }
            is CoroutineScope -> {//如果是CoroutineScope直接取消
                if (any.isActive && disposables.containsKey(any)) {
                    disposables.remove(any)
                    any.cancel(CancellationException(cancelMsg))
                }
            }
            is Disposable -> {//如果是Disposable直接取消
                if (!any.isDisposed && disposables.containsKey(any)) {
                    disposables.remove(any)
                    any.dispose()
                }
            }
            else -> {//其它情况按照tag处理
                val set: MutableSet<MutableMap.MutableEntry<Any, Any?>> = disposables.entries
                val iterator = set.iterator()
                while (iterator.hasNext()) {
                    val (disposable, value) = iterator.next()
                    when (disposable) {
                        is Job -> {//如果tag对应的可取消类型是Job
                            iterator.remove()
                            if (any == value && !disposable.isCancelled) {
                                disposable.cancel(CancellationException(cancelMsg))
                            }
                        }
                        is CoroutineContext -> {//如果tag对应的可取消类型是CoroutineContext
                            iterator.remove()
                            if (any == value && disposable.isActive) {
                                disposable.cancel(CancellationException(cancelMsg))
                            }
                        }
                        is CoroutineScope -> {//如果tag对应的可取消类型是CoroutineScope
                            iterator.remove()
                            if (any == value && disposable.isActive) {
                                disposable.cancel(CancellationException(cancelMsg))
                            }
                        }
                        is Disposable -> {//如果tag对应的可取消类型是Disposable
                            iterator.remove()
                            if (any == value && !disposable.isDisposed) {
                                disposable.dispose()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * description 清除所有请求
     */
    @Throws(IllegalStateException::class)
    fun cancelAllRequest() {
        for (disposable in disposables.keys) {
            if (disposable is Job && !disposable.isCancelled) {//如果是Job直接取消
                disposable.cancel(CancellationException(cancelMsg))
            } else if (disposable is CoroutineContext && disposable.isActive) {//如果是CoroutineContext直接取消
                disposable.cancel(CancellationException(cancelMsg))
            } else if (disposable is CoroutineScope && disposable.isActive) {//如果是CoroutineScope直接取消
                disposable.cancel(CancellationException(cancelMsg))
            } else if (disposable is Disposable && !disposable.isDisposed) {//如果是Disposable直接取消
                disposable.dispose()
            }
        }
        disposables.clear()
    }

    companion object {
        private const val cancelMsg: String = "请求完成或组件生命周期结束后主动取消请求"

        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            HttpUtils()
        }

        private var context: Application? = null
        private var isRxjava2: Boolean = true
        private var disposables: HashMap<Any, Any?> = HashMap()

        /**
         * description 初始化context
         */
        @kotlin.jvm.JvmStatic
        fun init(app: Application?) {
            context = app
            try {
                Class.forName("io.reactivex.Single")
                Class.forName("io.reactivex.Observable")
                Class.forName("retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory")
            } catch (e: ClassNotFoundException) {
                isRxjava2 = false
                "未导入rxjava2".i(GlobalHttpUtils::class.java.name)
            }
        }

        private fun checkInitialize() {
            if (context == null) {
                throw ExceptionInInitializerError("请先在全局Application中调用 HttpUtils.init() 初始化！")
            }
        }
    }
}