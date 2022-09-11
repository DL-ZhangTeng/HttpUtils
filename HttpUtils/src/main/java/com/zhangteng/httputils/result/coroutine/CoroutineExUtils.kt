package com.zhangteng.httputils.result.coroutine

import android.app.Dialog
import com.zhangteng.httputils.lifecycle.addHttpUtilsDisposable
import com.zhangteng.httputils.lifecycle.isInterruptByLifecycle
import com.zhangteng.utils.IException
import com.zhangteng.utils.IResponse
import kotlinx.coroutines.*
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

/**
 * 不过滤请求结果
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param block 请求体
 * @param success 成功回调
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param mProgressDialog 显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> CoroutineContext.launchGo(
    block: suspend () -> T,
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    withContext(Dispatchers.Main) {
        mProgressDialog?.show()
    }

    addHttpUtilsDisposable(tag)

    try {
        withContext(Dispatchers.IO) {
            block()
        }.also {
            if (!this@launchGo.isInterruptByLifecycle(tag)) {
                success(it)
            }
        }
    } catch (e: Throwable) {
        if (!this@launchGo.isInterruptByLifecycle(tag)) {
            error(IException.handleException(e))
        }
    } finally {
        if (!this@launchGo.isInterruptByLifecycle(tag)) {
            mProgressDialog?.dismiss()
            complete()
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
 * @param mProgressDialog 是否显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> CoroutineContext.launchGoIResponse(
    block: suspend () -> IResponse<T>,
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    withContext(Dispatchers.Main) {
        mProgressDialog?.show()
    }

    addHttpUtilsDisposable(tag)

    try {
        withContext(Dispatchers.IO) {
            block().let {
                if (it.isSuccess()) it.getResult()
                else
                    throw IException(it.getMsg(), it.getCode())
            }
        }.also {
            if (!this@launchGoIResponse.isInterruptByLifecycle(tag)) {
                success(it)
            }
        }
    } catch (e: Throwable) {
        if (!this@launchGoIResponse.isInterruptByLifecycle(tag)) {
            error(IException.handleException(e))
        }
    } finally {
        if (!this@launchGoIResponse.isInterruptByLifecycle(tag)) {
            mProgressDialog?.dismiss()
            complete()
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
 * @param mProgressDialog 显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
fun <T> CoroutineScope.launchGo(
    block: suspend CoroutineScope.() -> T,
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    launch {
        withContext(Dispatchers.Main) {
            mProgressDialog?.show()
        }

        //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
        if (this !is Closeable) {
            addHttpUtilsDisposable(tag)
        }

        try {
            withContext(Dispatchers.IO) {
                block()
            }.also {
                if (!this@launchGo.isInterruptByLifecycle(tag)) {
                    success(it)
                }
            }
        } catch (e: Throwable) {
            if (!this@launchGo.isInterruptByLifecycle(tag)) {
                error(IException.handleException(e))
            }
        } finally {
            if (!this@launchGo.isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                complete()
            }
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
 * @param mProgressDialog 是否显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
fun <T> CoroutineScope.launchGoIResponse(
    block: suspend CoroutineScope.() -> IResponse<T>,
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    launch {
        withContext(Dispatchers.Main) {
            mProgressDialog?.show()
        }

        //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
        if (this !is Closeable) {
            addHttpUtilsDisposable(tag)
        }

        try {
            withContext(Dispatchers.IO) {
                block().let {
                    if (it.isSuccess()) it.getResult()
                    else
                        throw IException(it.getMsg(), it.getCode())
                }
            }.also {
                if (!this@launchGoIResponse.isInterruptByLifecycle(tag)) {
                    success(it)
                }
            }
        } catch (e: Throwable) {
            if (!this@launchGoIResponse.isInterruptByLifecycle(tag)) {
                error(IException.handleException(e))
            }
        } finally {
            if (!this@launchGoIResponse.isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                complete()
            }
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
 * @param mProgressDialog 显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
fun <T> CoroutineScope.launchGoDeferred(
    block: CoroutineScope.() -> Deferred<T>,
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    launch {
        withContext(Dispatchers.Main) {
            mProgressDialog?.show()
        }

        //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
        if (this !is Closeable) {
            addHttpUtilsDisposable(tag)
        }

        try {
            withContext(Dispatchers.IO) {
                block().await()
            }.also {
                if (!this@launchGoDeferred.isInterruptByLifecycle(tag)) {
                    success(it)
                }
            }
        } catch (e: Throwable) {
            if (!this@launchGoDeferred.isInterruptByLifecycle(tag)) {
                error(IException.handleException(e))
            }
        } finally {
            if (!this@launchGoDeferred.isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                complete()
            }
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
 * @param mProgressDialog 是否显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
fun <T> CoroutineScope.launchGoDeferredIResponse(
    block: CoroutineScope.() -> Deferred<IResponse<T>>,
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    launch {
        withContext(Dispatchers.Main) {
            mProgressDialog?.show()
        }

        //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
        if (this !is Closeable) {
            addHttpUtilsDisposable(tag)
        }

        try {
            withContext(Dispatchers.IO) {
                block().await().let {
                    if (it.isSuccess()) it.getResult()
                    else
                        throw IException(it.getMsg(), it.getCode())
                }
            }.also {
                if (!this@launchGoDeferredIResponse.isInterruptByLifecycle(tag)) {
                    success(it)
                }
            }
        } catch (e: Throwable) {
            if (!this@launchGoDeferredIResponse.isInterruptByLifecycle(tag)) {
                error(IException.handleException(e))
            }
        } finally {
            if (!this@launchGoDeferredIResponse.isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                complete()
            }
        }
    }
}

/**
 * 不过滤请求结果
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param success 成功回调
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param mProgressDialog 显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> Deferred<T>.deferredGo(
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    withContext(Dispatchers.Main) {
        mProgressDialog?.show()
    }

    addHttpUtilsDisposable(tag)

    try {
        withContext(Dispatchers.IO) {
            await()
        }.also {
            if (!this@deferredGo.isInterruptByLifecycle(tag)) {
                success(it)
            }
        }
    } catch (e: Throwable) {
        if (!this@deferredGo.isInterruptByLifecycle(tag)) {
            error(IException.handleException(e))
        }
    } finally {
        if (!this@deferredGo.isInterruptByLifecycle(tag)) {
            mProgressDialog?.dismiss()
            complete()
        }
    }
}

/**
 * 过滤请求结果，其他全抛异常
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param success 成功回调
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param mProgressDialog 是否显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> Deferred<IResponse<T>>.deferredGoIResponse(
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    withContext(Dispatchers.Main) {
        mProgressDialog?.show()
    }

    addHttpUtilsDisposable(tag)

    try {
        withContext(Dispatchers.IO) {
            await().let {
                if (it.isSuccess()) it.getResult()
                else
                    throw IException(it.getMsg(), it.getCode())
            }
        }.also {
            if (!this@deferredGoIResponse.isInterruptByLifecycle(tag)) {
                success(it)
            }
        }
    } catch (e: Throwable) {
        if (!this@deferredGoIResponse.isInterruptByLifecycle(tag)) {
            error(IException.handleException(e))
        }
    } finally {
        if (!this@deferredGoIResponse.isInterruptByLifecycle(tag)) {
            mProgressDialog?.dismiss()
            complete()
        }
    }
}