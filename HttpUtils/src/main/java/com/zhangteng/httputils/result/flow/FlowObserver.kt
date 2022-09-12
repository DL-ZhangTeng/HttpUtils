package com.zhangteng.httputils.result.flow

import android.app.Dialog
import androidx.lifecycle.LifecycleOwner
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.lifecycle.HttpLifecycleEventObserver
import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.lifecycle.isInterruptByLifecycle
import com.zhangteng.httputils.result.flow.interfaces.IFlowObserver
import com.zhangteng.utils.IException
import com.zhangteng.utils.showShortToast
import kotlin.coroutines.CoroutineContext

/**
 * description: Flow回调接口
 * author: Swing
 * date: 2022/9/12
 */
abstract class FlowObserver<T>(
    private var mProgressDialog: Dialog? = null,
    private var tag: Any? = null
) : IFlowObserver<T> {
    private var disposable: CoroutineContext? = null

    protected open fun isHideToast(): Boolean {
        return false
    }

    /**
     * 失败回调
     *
     * @param iException 错误信息
     */
    protected abstract fun onFailure(iException: IException?)

    /**
     * 成功回调
     *
     * @param t 数据
     */
    protected abstract fun onSuccess(t: T)

    override fun doOnSubscribe(d: CoroutineContext) {
        disposable = d
        if (tag == null) {
            HttpUtils.instance.addDisposable(d)
        } else {
            HttpUtils.instance.addDisposable(d, tag)
        }
        if (tag is LifecycleOwner) {
            HttpLifecycleEventObserver.bind(tag as LifecycleOwner)
        }
        if (mProgressDialog != null && !mProgressDialog!!.isShowing) {
            mProgressDialog!!.show()
        }
    }

    override fun doOnError(iException: IException) {
        if (isInterruptByLifecycle(tag)) return
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
        if (!isHideToast()) {
            HttpUtils.instance.context.showShortToast(iException.message)
        }
        onFailure(iException)
        disposable.cancelSingleRequest()
    }

    override fun doOnCompleted() {
        if (isInterruptByLifecycle(tag)) return
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
        disposable.cancelSingleRequest()
    }

    override fun doOnNext(t: T) {
        if (isInterruptByLifecycle(tag)) return
        onSuccess(t)
        disposable.cancelSingleRequest()
    }
}