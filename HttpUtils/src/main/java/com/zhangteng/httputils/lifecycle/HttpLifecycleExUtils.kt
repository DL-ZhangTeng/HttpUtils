package com.zhangteng.httputils.lifecycle

import androidx.lifecycle.LifecycleOwner
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.utils.i
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * bind生命周期组件自动取消请求
 * 网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程，不需要调用此方法
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期，需要手动取消job
 */
suspend fun Job?.addHttpUtilsDisposable(tag: Any?) {
    if (this != null) {
        HttpUtils.instance.addDisposable(this, tag)
    }
    //bind生命周期组件自动取消请求
    if (tag != null && tag is LifecycleOwner) {
        withContext(Dispatchers.Main) {
            HttpLifecycleEventObserver.bind(tag)
        }
    }
}

/**
 * bind生命周期组件自动取消请求
 * 网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程，不需要调用此方法
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期，需要手动取消job
 */
suspend fun CoroutineContext?.addHttpUtilsDisposable(tag: Any?) {
    if (this != null) {
        HttpUtils.instance.addDisposable(this, tag)
    }
    //bind生命周期组件自动取消请求
    if (tag != null && tag is LifecycleOwner) {
        withContext(Dispatchers.Main) {
            HttpLifecycleEventObserver.bind(tag)
        }
    }
}

/**
 * bind生命周期组件自动取消请求
 * 网络请求都在 viewModelScope 域中启动，当页面销毁时会自动调用ViewModel的  #onCleared 方法取消所有协程，不需要调用此方法
 * @param tag LifecycleOwner生命周期结束关闭请求的tag，添加非LifecycleOwner类型的tag无法绑定生命周期，需要手动取消job
 */
suspend fun CoroutineScope?.addHttpUtilsDisposable(tag: Any?) {
    if (this != null) {
        HttpUtils.instance.addDisposable(this, tag)
    }
    //bind生命周期组件自动取消请求
    if (tag != null && tag is LifecycleOwner) {
        withContext(Dispatchers.Main) {
            HttpLifecycleEventObserver.bind(tag)
        }
    }
}

/**
 * description: 是否因组件生命周期Destroy中断后续程序
 */
fun Job?.isInterruptByLifecycle(tag: Any?): Boolean {
    if (tag is LifecycleOwner && HttpLifecycleEventObserver.isLifecycleDestroy(tag as LifecycleOwner?)) {
        //页面销毁状态取消网络请求
        //观察者会清理全部请求
        return true
    }
    if (this != null) {
        //主动取消并清理请求集合
        try {
            HttpUtils.instance.cancelSingleRequest(this)
        } catch (e: IllegalStateException) {
            e.message.i("cancelSingleRequest")
        }
    }
    return false
}

/**
 * description: 是否因组件生命周期Destroy中断后续程序
 */
fun CoroutineContext?.isInterruptByLifecycle(tag: Any?): Boolean {
    if (tag is LifecycleOwner && HttpLifecycleEventObserver.isLifecycleDestroy(tag as LifecycleOwner?)) {
        //页面销毁状态取消网络请求
        //观察者会清理全部请求
        return true
    }
    if (this != null) {
        //主动取消并清理请求集合
        try {
            HttpUtils.instance.cancelSingleRequest(this)
        } catch (e: IllegalStateException) {
            e.message.i("cancelSingleRequest")
        }
    }
    return false
}

/**
 * description: 是否因组件生命周期Destroy中断后续程序
 */
fun CoroutineScope?.isInterruptByLifecycle(tag: Any?): Boolean {
    if (tag is LifecycleOwner && HttpLifecycleEventObserver.isLifecycleDestroy(tag as LifecycleOwner?)) {
        //页面销毁状态取消网络请求
        //观察者会清理全部请求
        return true
    }
    if (this != null) {
        //主动取消并清理请求集合
        try {
            HttpUtils.instance.cancelSingleRequest(this)
        } catch (e: IllegalStateException) {
            e.message.i("cancelSingleRequest")
        }
    }
    return false
}