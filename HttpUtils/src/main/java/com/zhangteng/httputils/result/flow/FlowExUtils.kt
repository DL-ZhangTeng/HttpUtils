package com.zhangteng.httputils.result.flow

import android.app.Dialog
import com.zhangteng.httputils.lifecycle.addHttpUtilsDisposable
import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.lifecycle.isInterruptByLifecycle
import com.zhangteng.utils.IException
import com.zhangteng.utils.IResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import java.io.Closeable

/**
 * 无请求结果
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param block 请求体
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
    flow { emit(block()) }
        .flowOn(Dispatchers.IO)
        .onStart {
            mProgressDialog?.show()

            currentCoroutineContext().addHttpUtilsDisposable(tag)
        }
        .onCompletion {
            if (!isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                complete()
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .catch {
            if (!isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                error(IException.handleException(it))
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .flowOn(Dispatchers.Main)
        .collect {
            success(it)
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
suspend fun <T> launchGoIResponse(
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

            currentCoroutineContext().addHttpUtilsDisposable(tag)
        }
        .onCompletion {
            if (!isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                complete()
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .catch {
            if (!isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                error(IException.handleException(it))
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .flowOn(Dispatchers.Main)
        .collect {
            if (!isInterruptByLifecycle(tag)) {
                if (it.isSuccess()) {
                    success(it)
                } else {
                    error(IException.handleException(IException(it.getMsg(), it.getCode())))
                }
            }
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
suspend fun <T> launchGoFlow(
    block: () -> Flow<T>,
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    block()
        .flowOn(Dispatchers.IO)
        .onStart {
            mProgressDialog?.show()

            //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
            if (this !is Closeable) {
                currentCoroutineContext().addHttpUtilsDisposable(tag)
            }
        }
        .onCompletion {
            if (!isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                complete()
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .catch {
            if (!isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                error(IException.handleException(it))
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .flowOn(Dispatchers.Main)
        .collect {
            if (!isInterruptByLifecycle(tag)) {
                success(it)
            }
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
suspend fun <T> launchGoFlowIResponse(
    block: () -> Flow<IResponse<T>>,
    success: (IResponse<T>) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    block()
        .flowOn(Dispatchers.IO)
        .onStart {
            mProgressDialog?.show()

            //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
            if (this !is Closeable) {
                currentCoroutineContext().addHttpUtilsDisposable(tag)
            }
        }
        .onCompletion {
            if (!isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                complete()
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .catch {
            if (!isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                error(IException.handleException(it))
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .flowOn(Dispatchers.Main)
        .collect {
            if (!isInterruptByLifecycle(tag)) {
                if (it.isSuccess()) {
                    success(it)
                } else {
                    error(IException.handleException(IException(it.getMsg(), it.getCode())))
                }
            }
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

            currentCoroutineContext().addHttpUtilsDisposable(tag)
        }
        .onCompletion {
            if (!isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                complete()
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .catch {
            if (!isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                error(IException.handleException(it))
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .flowOn(Dispatchers.Main)
        .collect {
            if (!isInterruptByLifecycle(tag)) {
                success(it)
            }
        }
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

            currentCoroutineContext().addHttpUtilsDisposable(tag)
        }
        .onCompletion {
            if (!isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                complete()
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .catch {
            if (!isInterruptByLifecycle(tag)) {
                mProgressDialog?.dismiss()
                error(IException.handleException(it))
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .flowOn(Dispatchers.Main)
        .collect {
            if (!isInterruptByLifecycle(tag)) {
                if (it.isSuccess()) {
                    success(it)
                } else {
                    error(IException.handleException(IException(it.getMsg(), it.getCode())))
                }
            }
        }
}

/**
 * 无请求结果
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param observer 网络回调类，处理了弹窗与生命周期销毁自动取消请求
 */
suspend fun <T> Flow<T>.flowGo(observer: FlowObserver<T>) {
    flowOn(Dispatchers.IO)
        .onStart {
            observer.doOnSubscribe(currentCoroutineContext())
        }
        .onCompletion {
            observer.doOnCompleted()
        }
        .catch {
            observer.doOnError(IException.handleException(it))
        }
        .flowOn(Dispatchers.Main)
        .collect {
            observer.doOnNext(it)
        }
}

/**
 * 过滤IResponse.isSuccess()结果，其他全抛异常
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param observer 网络回调类，处理了弹窗与生命周期销毁自动取消请求
 */
suspend fun <T> Flow<IResponse<T>>.flowGoIResponse(observer: FlowObserver<IResponse<T>>) {
    flowOn(Dispatchers.IO)
        .onStart {
            observer.doOnSubscribe(currentCoroutineContext())
        }
        .onCompletion {
            observer.doOnCompleted()
        }
        .catch {
            observer.doOnError(IException.handleException(it))
        }
        .flowOn(Dispatchers.Main)
        .collect {
            if (it.isSuccess()) {
                observer.doOnNext(it)
            } else {
                observer.doOnError(
                    IException.handleException(
                        IException(
                            it.getMsg(),
                            it.getCode()
                        )
                    )
                )
            }
        }
}