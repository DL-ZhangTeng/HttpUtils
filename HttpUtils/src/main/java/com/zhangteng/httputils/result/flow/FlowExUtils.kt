package com.zhangteng.httputils.result.flow

import com.zhangteng.httputils.lifecycle.addHttpUtilsDisposable
import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.lifecycle.isInterruptByLifecycle
import com.zhangteng.httputils.result.callback.interfaces.ICallBack
import com.zhangteng.utils.IException
import com.zhangteng.utils.IResponse
import com.zhangteng.utils.ILoadingView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

/**
 * 无请求结果
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param block 请求体
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
    flow { emit(block()) }
        .flowOn(Dispatchers.IO)
        .onStart {
            iLoadingView?.showProgressDialog()

            currentCoroutineContext().addHttpUtilsDisposable(iLoadingView)
        }
        .onCompletion {
            if (!isInterruptByLifecycle(iLoadingView)) {
                iLoadingView?.dismissProgressDialog()
                complete()
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .catch {
            if (!isInterruptByLifecycle(iLoadingView)) {
                iLoadingView?.dismissProgressDialog()
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
 * @param iLoadingView 显示加载框，如果是LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> launchGoIResponse(
    block: suspend () -> IResponse<T>,
    success: (IResponse<T>) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    iLoadingView: ILoadingView? = null
) {
    flow { emit(block()) }
        .flowOn(Dispatchers.IO)
        .onStart {
            iLoadingView?.showProgressDialog()

            currentCoroutineContext().addHttpUtilsDisposable(iLoadingView)
        }
        .onCompletion {
            if (!isInterruptByLifecycle(iLoadingView)) {
                iLoadingView?.dismissProgressDialog()
                complete()
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .catch {
            if (!isInterruptByLifecycle(iLoadingView)) {
                iLoadingView?.dismissProgressDialog()
                error(IException.handleException(it))
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .flowOn(Dispatchers.Main)
        .collect {
            if (!isInterruptByLifecycle(iLoadingView)) {
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
 * @param iLoadingView 显示加载框，如果是LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> launchGoFlow(
    block: () -> Flow<T>,
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    iLoadingView: ILoadingView? = null
) {
    block()
        .flowOn(Dispatchers.IO)
        .onStart {
            iLoadingView?.showProgressDialog()

            //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
            if (this !is Closeable) {
                currentCoroutineContext().addHttpUtilsDisposable(iLoadingView)
            }
        }
        .onCompletion {
            if (!isInterruptByLifecycle(iLoadingView)) {
                iLoadingView?.dismissProgressDialog()
                complete()
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .catch {
            if (!isInterruptByLifecycle(iLoadingView)) {
                iLoadingView?.dismissProgressDialog()
                error(IException.handleException(it))
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .flowOn(Dispatchers.Main)
        .collect {
            if (!isInterruptByLifecycle(iLoadingView)) {
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
 * @param iLoadingView 显示加载框，如果是LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> launchGoFlowIResponse(
    block: () -> Flow<IResponse<T>>,
    success: (IResponse<T>) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    iLoadingView: ILoadingView? = null
) {
    block()
        .flowOn(Dispatchers.IO)
        .onStart {
            iLoadingView?.showProgressDialog()

            //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
            if (this !is Closeable) {
                currentCoroutineContext().addHttpUtilsDisposable(iLoadingView)
            }
        }
        .onCompletion {
            if (!isInterruptByLifecycle(iLoadingView)) {
                iLoadingView?.dismissProgressDialog()
                complete()
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .catch {
            if (!isInterruptByLifecycle(iLoadingView)) {
                iLoadingView?.dismissProgressDialog()
                error(IException.handleException(it))
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .flowOn(Dispatchers.Main)
        .collect {
            if (!isInterruptByLifecycle(iLoadingView)) {
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
 * @param iLoadingView 显示加载框，如果是LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> Flow<T>.flowGo(
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    iLoadingView: ILoadingView? = null
) {
    flowOn(Dispatchers.IO)
        .onStart {
            iLoadingView?.showProgressDialog()

            currentCoroutineContext().addHttpUtilsDisposable(iLoadingView)
        }
        .onCompletion {
            if (!isInterruptByLifecycle(iLoadingView)) {
                iLoadingView?.dismissProgressDialog()
                complete()
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .catch {
            if (!isInterruptByLifecycle(iLoadingView)) {
                iLoadingView?.dismissProgressDialog()
                error(IException.handleException(it))
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .flowOn(Dispatchers.Main)
        .collect {
            if (!isInterruptByLifecycle(iLoadingView)) {
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
 * @param iLoadingView 显示加载框，如果是LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
suspend fun <T> Flow<IResponse<T>>.flowGoIResponse(
    success: (IResponse<T>) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    iLoadingView: ILoadingView? = null
) {
    flowOn(Dispatchers.IO)
        .onStart {
            iLoadingView?.showProgressDialog()

            currentCoroutineContext().addHttpUtilsDisposable(iLoadingView)
        }
        .onCompletion {
            if (!isInterruptByLifecycle(iLoadingView)) {
                iLoadingView?.dismissProgressDialog()
                complete()
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .catch {
            if (!isInterruptByLifecycle(iLoadingView)) {
                iLoadingView?.dismissProgressDialog()
                error(IException.handleException(it))
                currentCoroutineContext().cancelSingleRequest()
            }
        }
        .flowOn(Dispatchers.Main)
        .collect {
            if (!isInterruptByLifecycle(iLoadingView)) {
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
suspend fun <T> Flow<T>.flowGo(observer: ICallBack<T, CoroutineContext>) {
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
suspend fun <T> Flow<IResponse<T>>.flowGoIResponse(observer: ICallBack<IResponse<T>, CoroutineContext>) {
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