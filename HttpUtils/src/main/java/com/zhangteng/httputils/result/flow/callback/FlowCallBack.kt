package com.zhangteng.httputils.result.flow.callback

import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.result.callback.CommonCallBack
import com.zhangteng.utils.IException
import com.zhangteng.utils.ILoadingView
import kotlin.coroutines.CoroutineContext

/**
 * description: Flow回调接口
 * author: Swing
 * date: 2022/9/12
 */
abstract class FlowCallBack<T>(
    iLoadingView: ILoadingView? = null
) : CommonCallBack<T, CoroutineContext>(iLoadingView) {

    override fun doOnError(iException: IException) {
        super.doOnError(iException)
        disposable.cancelSingleRequest()
    }

    override fun doOnCompleted() {
        super.doOnCompleted()
        disposable.cancelSingleRequest()
    }
}