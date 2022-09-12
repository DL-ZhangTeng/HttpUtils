package com.zhangteng.httputils.result.coroutine

import android.app.Dialog
import com.zhangteng.httputils.lifecycle.addHttpUtilsDisposable
import com.zhangteng.httputils.lifecycle.isInterruptByLifecycle
import com.zhangteng.utils.IException
import com.zhangteng.utils.IResponse
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
 * @param mProgressDialog 显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> launchGo(
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

    coroutineContext.addHttpUtilsDisposable(tag)

    try {
        withContext(Dispatchers.IO) {
            block()
        }.also {
            if (!isInterruptByLifecycle(tag)) {
                success(it)
            }
        }
    } catch (e: Throwable) {
        if (!isInterruptByLifecycle(tag)) {
            error(IException.handleException(e))
        }
    } finally {
        if (!isInterruptByLifecycle(tag)) {
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
suspend fun <T> launchGoIResponse(
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

    coroutineContext.addHttpUtilsDisposable(tag)

    try {
        withContext(Dispatchers.IO) {
            block().let {
                if (it.isSuccess()) it.getResult()
                else
                    throw IException(it.getMsg(), it.getCode())
            }
        }.also {
            if (!isInterruptByLifecycle(tag)) {
                success(it)
            }
        }
    } catch (e: Throwable) {
        if (!isInterruptByLifecycle(tag)) {
            error(IException.handleException(e))
        }
    } finally {
        if (!isInterruptByLifecycle(tag)) {
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
suspend fun <T> launchGoDeferred(
    block: CoroutineScope.() -> Deferred<T>,
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    withContext(Dispatchers.Main) {
        mProgressDialog?.show()
    }

    coroutineContext.addHttpUtilsDisposable(tag)

    try {
        withContext(Dispatchers.IO) {
            block().await()
        }.also {
            if (!isInterruptByLifecycle(tag)) {
                success(it)
            }
        }
    } catch (e: Throwable) {
        if (!isInterruptByLifecycle(tag)) {
            error(IException.handleException(e))
        }
    } finally {
        if (!isInterruptByLifecycle(tag)) {
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
suspend fun <T> launchGoDeferredIResponse(
    block: CoroutineScope.() -> Deferred<IResponse<T>>,
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    withContext(Dispatchers.Main) {
        mProgressDialog?.show()
    }

    coroutineContext.addHttpUtilsDisposable(tag)

    try {
        withContext(Dispatchers.IO) {
            block().await().let {
                if (it.isSuccess()) it.getResult()
                else
                    throw IException(it.getMsg(), it.getCode())
            }
        }.also {
            if (!isInterruptByLifecycle(tag)) {
                success(it)
            }
        }
    } catch (e: Throwable) {
        if (!isInterruptByLifecycle(tag)) {
            error(IException.handleException(e))
        }
    } finally {
        if (!isInterruptByLifecycle(tag)) {
            mProgressDialog?.dismiss()
            complete()
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
            if (!isInterruptByLifecycle(tag)) {
                success(it)
            }
        }
    } catch (e: Throwable) {
        if (!isInterruptByLifecycle(tag)) {
            error(IException.handleException(e))
        }
    } finally {
        if (!isInterruptByLifecycle(tag)) {
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
            if (!isInterruptByLifecycle(tag)) {
                success(it)
            }
        }
    } catch (e: Throwable) {
        if (!isInterruptByLifecycle(tag)) {
            error(IException.handleException(e))
        }
    } finally {
        if (!isInterruptByLifecycle(tag)) {
            mProgressDialog?.dismiss()
            complete()
        }
    }
}