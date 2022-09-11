package com.zhangteng.httputils.result.flow

import android.app.Dialog
import com.zhangteng.httputils.lifecycle.addHttpUtilsDisposable
import com.zhangteng.httputils.lifecycle.isInterruptByLifecycle
import com.zhangteng.utils.IException
import com.zhangteng.utils.IResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.Closeable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * 无请求结果
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param block 请求体
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
    flow { emit(block()) }
        .flowOn(Dispatchers.IO)
        .onStart {
            mProgressDialog?.show()
        }
        .onCompletion {
            if (!this@launchGo.isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                complete()
            }
        }
        .catch {
            if (!this@launchGo.isInterruptByLifecycle(tag)) {
                error(IException.handleException(it))
            }
        }
        .flowOn(Dispatchers.Main)
        .collect {
            success(it)
        }

    //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
    if (this !is Closeable) {
        addHttpUtilsDisposable(tag)
    }
}

/**
 * 过滤IResponse.isSuccess()结果，其他全抛异常
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
    success: (IResponse<T>) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    flow { emit(block()) }
        .flowOn(Dispatchers.IO)
        .onStart {
            mProgressDialog?.show()
        }
        .onCompletion {
            if (!this@launchGoIResponse.isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                complete()
            }
        }
        .catch {
            if (!this@launchGoIResponse.isInterruptByLifecycle(tag)) {
                error(IException.handleException(it))
            }
        }
        .flowOn(Dispatchers.Main)
        .collect {
            if (!this@launchGoIResponse.isInterruptByLifecycle(tag)) {
                if (it.isSuccess()) {
                    success(it)
                } else {
                    error(IException.handleException(IException(it.getMsg(), it.getCode())))
                }
            }
        }

    //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
    if (this !is Closeable) {
        addHttpUtilsDisposable(tag)
    }
}

/**
 * 无请求结果
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param block 请求体
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param mProgressDialog 显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
fun <T> CoroutineScope.launchGo(
    block: suspend () -> T,
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    launch {
        flow { emit(block()) }
            .flowOn(Dispatchers.IO)
            .onStart {
                mProgressDialog?.show()
            }
            .onCompletion {
                if (!this@launchGo.isInterruptByLifecycle(tag)) {
                    mProgressDialog?.dismiss()
                    complete()
                }
            }
            .catch {
                if (!this@launchGo.isInterruptByLifecycle(tag)) {
                    error(IException.handleException(it))
                }
            }
            .flowOn(Dispatchers.Main)
            .collect {
                success(it)
            }
    }
    //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
    if (this !is Closeable) {
        addHttpUtilsDisposable(tag)
    }
}

/**
 * 过滤IResponse.isSuccess()结果，其他全抛异常
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param block 请求体
 * @param success 成功回调
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param mProgressDialog 是否显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
fun <T> CoroutineScope.launchGoIResponse(
    block: suspend () -> IResponse<T>,
    success: (IResponse<T>) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    launch {
        flow { emit(block()) }
            .flowOn(Dispatchers.IO)
            .onStart {
                mProgressDialog?.show()
            }
            .onCompletion {
                if (!this@launchGoIResponse.isInterruptByLifecycle(tag)) {
                    mProgressDialog?.dismiss()
                    complete()
                }
            }
            .catch {
                if (!this@launchGoIResponse.isInterruptByLifecycle(tag)) {
                    error(IException.handleException(it))
                }
            }
            .flowOn(Dispatchers.Main)
            .collect {
                if (!this@launchGoIResponse.isInterruptByLifecycle(tag)) {
                    if (it.isSuccess()) {
                        success(it)
                    } else {
                        error(IException.handleException(IException(it.getMsg(), it.getCode())))
                    }
                }
            }
    }

    //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
    if (this !is Closeable) {
        addHttpUtilsDisposable(tag)
    }
}

/**
 * 无请求结果
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param block 请求体
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param mProgressDialog 显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
fun <T> CoroutineScope.launchGoFlow(
    block: () -> Flow<T>,
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    launch {
        block()
            .flowOn(Dispatchers.IO)
            .onStart {
                mProgressDialog?.show()
            }
            .onCompletion {
                if (!this@launchGoFlow.isInterruptByLifecycle(tag)) {
                    mProgressDialog?.dismiss()
                    complete()
                }
            }
            .catch {
                if (!this@launchGoFlow.isInterruptByLifecycle(tag)) {
                    error(IException.handleException(it))
                }
            }
            .flowOn(Dispatchers.Main)
            .collect {
                if (!this@launchGoFlow.isInterruptByLifecycle(tag)) {
                    success(it)
                }
            }
    }

    //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
    if (this !is Closeable) {
        addHttpUtilsDisposable(tag)
    }
}

/**
 * 过滤IResponse.isSuccess()结果，其他全抛异常
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param block 请求体
 * @param success 成功回调
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param mProgressDialog 是否显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
fun <T> CoroutineScope.launchGoFlowIResponse(
    block: () -> Flow<IResponse<T>>,
    success: (IResponse<T>) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    launch {
        block()
            .flowOn(Dispatchers.IO)
            .onStart {
                mProgressDialog?.show()
            }
            .onCompletion {
                if (!this@launchGoFlowIResponse.isInterruptByLifecycle(tag)) {
                    mProgressDialog?.dismiss()
                    complete()
                }
            }
            .catch {
                if (!this@launchGoFlowIResponse.isInterruptByLifecycle(tag)) {
                    error(IException.handleException(it))
                }
            }
            .flowOn(Dispatchers.Main)
            .collect {
                if (!this@launchGoFlowIResponse.isInterruptByLifecycle(tag)) {
                    if (it.isSuccess()) {
                        success(it)
                    } else {
                        error(IException.handleException(IException(it.getMsg(), it.getCode())))
                    }
                }
            }
    }

    //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
    if (this !is Closeable) {
        addHttpUtilsDisposable(tag)
    }
}

/**
 * 无请求结果
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param mProgressDialog 显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> Flow<T>.flowGo(
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    flowOn(Dispatchers.IO)
        .onStart {
            mProgressDialog?.show()
        }
        .onCompletion {
            if (!kotlin.coroutines.coroutineContext.isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                complete()
            }
        }
        .catch {
            if (!kotlin.coroutines.coroutineContext.isInterruptByLifecycle(tag)) {
                error(IException.handleException(it))
            }
        }
        .flowOn(Dispatchers.Main)
        .collect {
            if (!coroutineContext.isInterruptByLifecycle(tag)) {
                success(it)
            }
        }

    coroutineContext.addHttpUtilsDisposable(tag)
}

/**
 * 过滤IResponse.isSuccess()结果，其他全抛异常
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param success 成功回调
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param mProgressDialog 是否显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> Flow<IResponse<T>>.flowGoIResponse(
    success: (IResponse<T>) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    flowOn(Dispatchers.IO)
        .onStart {
            mProgressDialog?.show()
        }
        .onCompletion {
            if (!kotlin.coroutines.coroutineContext.isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                complete()
            }
        }
        .catch {
            if (!kotlin.coroutines.coroutineContext.isInterruptByLifecycle(tag)) {
                error(IException.handleException(it))
            }
        }
        .flowOn(Dispatchers.Main)
        .collect {
            if (!coroutineContext.isInterruptByLifecycle(tag)) {
                if (it.isSuccess()) {
                    success(it)
                } else {
                    error(IException.handleException(IException(it.getMsg(), it.getCode())))
                }
            }
        }

    coroutineContext.addHttpUtilsDisposable(tag)
}