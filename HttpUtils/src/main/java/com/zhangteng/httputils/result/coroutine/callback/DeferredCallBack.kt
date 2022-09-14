package com.zhangteng.httputils.result.coroutine.callback

import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.result.callback.CommonCallBack
import com.zhangteng.utils.IStateView
import kotlinx.coroutines.Deferred

/**
 * description: 协程回调
 * author: Swing
 * date: 2022/9/12
 */
abstract class DeferredCallBack<T>(
    iStateView: IStateView? = null
) : CommonCallBack<T, Deferred<T>>(iStateView) {

    override fun doOnCompleted() {
        super.doOnCompleted()
        disposable.cancelSingleRequest()
    }
}