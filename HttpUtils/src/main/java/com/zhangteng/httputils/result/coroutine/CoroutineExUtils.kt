package com.zhangteng.httputils.result.coroutine

import android.app.Dialog
import androidx.lifecycle.LifecycleOwner
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.lifecycle.HttpLifecycleEventObserver
import com.zhangteng.httputils.lifecycle.HttpLifecycleEventObserver.Companion.isLifecycleDestroy
import com.zhangteng.utils.IException
import com.zhangteng.utils.IResponse
import kotlinx.coroutines.*
import java.io.Closeable

/**
 * 不过滤请求结果
 * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程
 * @param block 请求体
 * @param error 失败回调
 * @param complete  完成回调（无论成功失败都会调用）
 * @param mProgressDialog 显示加载框
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期
 */
fun CoroutineScope.launchGo(
    block: suspend CoroutineScope.() -> Unit,
    error: suspend CoroutineScope.(IException) -> Unit,
    complete: suspend CoroutineScope.() -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    mProgressDialog?.show()
    var job: Job? = null
    job = launch {
        handleException(
            withContext(Dispatchers.IO) { block },
            {
                if (isInterrupt(job, tag)) return@handleException
                error(it)
            }
        ) {
            if (isInterrupt(job, tag)) return@handleException
            mProgressDialog?.dismiss()
            complete()
        }
    }
    //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
    if (this !is Closeable) {
        HttpUtils.instance.addDisposable(job, tag)
    }
    //bind生命周期组件自动取消请求
    if (tag != null && tag is LifecycleOwner) {
        HttpLifecycleEventObserver.bind(tag)
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
fun <T> CoroutineScope.launchOnlyResult(
    block: suspend CoroutineScope.() -> IResponse<T>,
    success: (T) -> Unit,
    error: (IException) -> Unit,
    complete: () -> Unit = {},
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) {
    mProgressDialog?.show()
    var job: Job? = null
    job = launch {
        handleException(
            {
                withContext(Dispatchers.IO) {
                    block().let {
                        if (it.isSuccess()) it.getResult()
                        else
                            throw IException(it.getMsg(), it.getCode())
                    }
                }.also {
                    if (isInterrupt(job, tag)) return@handleException
                    success(it)
                }
            },
            {
                if (isInterrupt(job, tag)) return@handleException
                error(it)
            }
        ) {
            if (isInterrupt(job, tag)) return@handleException
            mProgressDialog?.dismiss()
            complete()
        }
    }
    //如果不是可取消的域，可取消的域暂时只有viewModelScope，viewModelScope会自动取消协程
    if (this !is Closeable) {
        HttpUtils.instance.addDisposable(job, tag)
    }
    //bind生命周期组件自动取消请求
    if (tag != null && tag is LifecycleOwner) {
        HttpLifecycleEventObserver.bind(tag)
    }
}

/**
 * 异常统一处理
 */
suspend fun handleException(
    block: suspend CoroutineScope.() -> Unit,
    error: suspend CoroutineScope.(IException) -> Unit,
    complete: suspend CoroutineScope.() -> Unit
) {
    coroutineScope {
        try {
            block()
        } catch (e: Throwable) {
            error(IException.handleException(e))
        } finally {
            complete()
        }
    }
}

/**
 * description: 是否中断后续程序
 */
private fun isInterrupt(job: Job?, tag: Any?): Boolean {
    if (tag is LifecycleOwner && isLifecycleDestroy(tag as LifecycleOwner?)) {
        //页面销毁状态取消网络请求
        //观察者会清理全部请求
        return true
    }
    if (job != null) {
        //主动取消并清理请求集合
        //如果是viewModelScope域下HttpUtils未添加job因此取消请求代码不生效（viewModelScope自动取消请求）
        HttpUtils.instance.cancelSingleRequest(job)
    }
    return false
}