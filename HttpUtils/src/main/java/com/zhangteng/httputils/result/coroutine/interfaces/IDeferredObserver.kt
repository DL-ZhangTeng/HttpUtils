package com.zhangteng.httputils.result.coroutine.interfaces

import com.zhangteng.httputils.result.interfaces.ISubscriber
import kotlinx.coroutines.Deferred

/**
 * description: 协程回调接口
 * author: Swing
 * date: 2022/9/12
 */
interface IDeferredObserver<T> : ISubscriber<T> {
    fun doOnSubscribe(d: Deferred<T>)
}