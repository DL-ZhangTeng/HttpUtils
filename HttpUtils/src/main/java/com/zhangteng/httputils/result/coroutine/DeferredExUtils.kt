package com.zhangteng.httputils.result.coroutine

import com.zhangteng.httputils.lifecycle.addHttpUtilsDisposable
import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.lifecycle.isInterruptByLifecycle
import com.zhangteng.httputils.result.callback.interfaces.ICallBack
import com.zhangteng.utils.IException
import com.zhangteng.utils.IResponse
import com.zhangteng.utils.ILoadingView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/**
 * 不过滤请求结果
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param block 请求体
 * @param success 成功回调
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param iLoadingView 显示加载框，如果是LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> launchGo(
    block: suspend () -> T,
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    iLoadingView: ILoadingView? = null
) {
    withContext(Dispatchers.Main) {
        iLoadingView?.showProgressDialog()
    }

    coroutineContext.addHttpUtilsDisposable(iLoadingView)

    try {
        withContext(Dispatchers.IO) {
            block()
        }.also {
            if (!isInterruptByLifecycle(iLoadingView)) {
                withContext(Dispatchers.Main) {
                    success(it)
                }
            }
        }
    } catch (e: Throwable) {
        if (!isInterruptByLifecycle(iLoadingView)) {
            withContext(Dispatchers.Main) {
                error(IException.handleException(e))
            }
        }
    } finally {
        if (!isInterruptByLifecycle(iLoadingView)) {
            withContext(Dispatchers.Main) {
                iLoadingView?.dismissProgressDialog()
                complete()
            }
            coroutineContext.cancelSingleRequest()
        }
    }
}

/**
 * 过滤请求结果，其他全抛异常
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param block 请求体
 * @param success 成功回调
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param iLoadingView 显示加载框，如果是LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> launchGoIResponse(
    block: suspend () -> IResponse<T>,
    success: (IResponse<T>) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    iLoadingView: ILoadingView? = null
) {
    withContext(Dispatchers.Main) {
        iLoadingView?.showProgressDialog()
    }

    coroutineContext.addHttpUtilsDisposable(iLoadingView)

    try {
        withContext(Dispatchers.IO) {
            block().let {
                if (it.isSuccess()) it
                else
                    throw IException(it.getMsg(), it.getCode())
            }
        }.also {
            if (!isInterruptByLifecycle(iLoadingView)) {
                withContext(Dispatchers.Main) {
                    success(it)
                }
            }
        }
    } catch (e: Throwable) {
        if (!isInterruptByLifecycle(iLoadingView)) {
            withContext(Dispatchers.Main) {
                error(IException.handleException(e))
            }
        }
    } finally {
        if (!isInterruptByLifecycle(iLoadingView)) {
            withContext(Dispatchers.Main) {
                iLoadingView?.dismissProgressDialog()
                complete()
            }
            coroutineContext.cancelSingleRequest()
        }
    }
}

/**
 * 不过滤请求结果
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param block 请求体
 * @param success 成功回调
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param iLoadingView 显示加载框，如果是LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> launchGoDeferred(
    block: CoroutineScope.() -> Deferred<T>,
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    iLoadingView: ILoadingView? = null
) {
    withContext(Dispatchers.Main) {
        iLoadingView?.showProgressDialog()
    }

    coroutineContext.addHttpUtilsDisposable(iLoadingView)

    try {
        withContext(Dispatchers.IO) {
            block().await()
        }.also {
            if (!isInterruptByLifecycle(iLoadingView)) {
                withContext(Dispatchers.Main) {
                    success(it)
                }
            }
        }
    } catch (e: Throwable) {
        if (!isInterruptByLifecycle(iLoadingView)) {
            withContext(Dispatchers.Main) {
                error(IException.handleException(e))
            }
        }
    } finally {
        if (!isInterruptByLifecycle(iLoadingView)) {
            withContext(Dispatchers.Main) {
                iLoadingView?.dismissProgressDialog()
                complete()
            }
            coroutineContext.cancelSingleRequest()
        }
    }
}

/**
 * 过滤请求结果，其他全抛异常
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param block 请求体
 * @param success 成功回调
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param iLoadingView 显示加载框，如果是LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> launchGoDeferredIResponse(
    block: CoroutineScope.() -> Deferred<IResponse<T>>,
    success: (IResponse<T>) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    iLoadingView: ILoadingView? = null
) {
    withContext(Dispatchers.Main) {
        iLoadingView?.showProgressDialog()
    }

    coroutineContext.addHttpUtilsDisposable(iLoadingView)

    try {
        withContext(Dispatchers.IO) {
            block().await().let {
                if (it.isSuccess()) it
                else
                    throw IException(it.getMsg(), it.getCode())
            }
        }.also {
            if (!isInterruptByLifecycle(iLoadingView)) {
                withContext(Dispatchers.Main) {
                    success(it)
                }
            }
        }
    } catch (e: Throwable) {
        if (!isInterruptByLifecycle(iLoadingView)) {
            withContext(Dispatchers.Main) {
                error(IException.handleException(e))
            }
        }
    } finally {
        if (!isInterruptByLifecycle(iLoadingView)) {
            withContext(Dispatchers.Main) {
                iLoadingView?.dismissProgressDialog()
                complete()
            }
            coroutineContext.cancelSingleRequest()
        }
    }
}

/**
 * 不过滤请求结果
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param success 成功回调
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param iLoadingView 显示加载框，如果是LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> Deferred<T>.deferredGo(
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    iLoadingView: ILoadingView? = null
) {
    withContext(Dispatchers.Main) {
        iLoadingView?.showProgressDialog()
    }

    addHttpUtilsDisposable(iLoadingView)

    try {
        withContext(Dispatchers.IO) {
            await()
        }.also {
            if (!isInterruptByLifecycle(iLoadingView)) {
                withContext(Dispatchers.Main) {
                    success(it)
                }
            }
        }
    } catch (e: Throwable) {
        if (!isInterruptByLifecycle(iLoadingView)) {
            withContext(Dispatchers.Main) {
                error(IException.handleException(e))
            }
        }
    } finally {
        if (!isInterruptByLifecycle(iLoadingView)) {
            withContext(Dispatchers.Main) {
                iLoadingView?.dismissProgressDialog()
                complete()
            }
            coroutineContext.cancelSingleRequest()
        }
    }
}

/**
 * 过滤请求结果，其他全抛异常
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param success 成功回调
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param iLoadingView 显示加载框，如果是LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> Deferred<IResponse<T>>.deferredGoIResponse(
    success: (IResponse<T>) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    iLoadingView: ILoadingView? = null
) {
    withContext(Dispatchers.Main) {
        iLoadingView?.showProgressDialog()
    }

    addHttpUtilsDisposable(iLoadingView)

    try {
        withContext(Dispatchers.IO) {
            await().let {
                if (it.isSuccess()) it
                else
                    throw IException(it.getMsg(), it.getCode())
            }
        }.also {
            if (!isInterruptByLifecycle(iLoadingView)) {
                withContext(Dispatchers.Main) {
                    success(it)
                }
            }
        }
    } catch (e: Throwable) {
        if (!isInterruptByLifecycle(iLoadingView)) {
            withContext(Dispatchers.Main) {
                error(IException.handleException(e))
            }
        }
    } finally {
        if (!isInterruptByLifecycle(iLoadingView)) {
            withContext(Dispatchers.Main) {
                iLoadingView?.dismissProgressDialog()
                complete()
            }
            coroutineContext.cancelSingleRequest()
        }
    }
}

/**
 * 不过滤请求结果
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param observer 网络回调类，处理了弹窗与生命周期销毁自动取消请求
 */
suspend fun <T> Deferred<T>.deferredGo(observer: ICallBack<T, Deferred<T>>) {
    withContext(Dispatchers.Main) {
        observer.doOnSubscribe(this@deferredGo)
    }

    try {
        withContext(Dispatchers.IO) {
            await()
        }.also {
            withContext(Dispatchers.Main) {
                observer.doOnNext(it)
            }
        }
    } catch (e: Throwable) {
        withContext(Dispatchers.Main) {
            observer.doOnError(IException.handleException(e))
        }
    } finally {
        withContext(Dispatchers.Main) {
            observer.doOnCompleted()
        }
    }
}

/**
 * 过滤请求结果，其他全抛异常
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param observer 网络回调类，处理了弹窗与生命周期销毁自动取消请求
 */
suspend fun <T> Deferred<IResponse<T>>.deferredGoIResponse(observer: ICallBack<IResponse<T>, Deferred<IResponse<T>>>) {
    withContext(Dispatchers.Main) {
        observer.doOnSubscribe(this@deferredGoIResponse)
    }

    try {
        withContext(Dispatchers.IO) {
            await().let {
                if (it.isSuccess()) it
                else
                    throw IException(it.getMsg(), it.getCode())
            }
        }.also {
            withContext(Dispatchers.Main) {
                observer.doOnNext(it)
            }
        }
    } catch (e: Throwable) {
        withContext(Dispatchers.Main) {
            observer.doOnError(IException.handleException(e))
        }
    } finally {
        withContext(Dispatchers.Main) {
            observer.doOnCompleted()
        }
    }
}