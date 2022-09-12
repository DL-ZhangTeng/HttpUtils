package com.zhangteng.httputils.result.flow.interfaces

import com.zhangteng.httputils.result.interfaces.ISubscriber
import kotlin.coroutines.CoroutineContext

/**
 * description: Flow回调接口
 * author: Swing
 * date: 2022/9/12
 */
interface IFlowObserver<T> : ISubscriber<T> {
    fun doOnSubscribe(d: CoroutineContext)
}