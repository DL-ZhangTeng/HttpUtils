package com.zhangteng.httputils.result.flow.callback

import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.result.callback.CommonCallBack
import com.zhangteng.utils.IException
import com.zhangteng.utils.IStateView
import kotlin.coroutines.CoroutineContext

/**
 * description: Flow回调接口
 * author: Swing
 * date: 2022/9/12
 */
abstract class FlowCallBack<T>(
    iStateView: IStateView? = null
) : CommonCallBack<T, CoroutineContext>(iStateView) {

    override fun doOnError(iException: IException) {
        super.doOnError(iException)
        disposable.cancelSingleRequest()
    }

    override fun doOnCompleted() {
        super.doOnCompleted()
        disposable.cancelSingleRequest()
    }
}