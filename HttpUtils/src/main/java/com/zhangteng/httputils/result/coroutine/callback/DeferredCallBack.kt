package com.zhangteng.httputils.result.coroutine.callback

import android.app.Dialog
import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.result.callback.CommonCallBack
import kotlinx.coroutines.Deferred

/**
 * description: 协程回调
 * author: Swing
 * date: 2022/9/12
 */
abstract class DeferredCallBack<T>(
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) : CommonCallBack<T, Deferred<T>>(mProgressDialog, tag) {

    override fun doOnCompleted() {
        super.doOnCompleted()
        disposable.cancelSingleRequest()
    }
}