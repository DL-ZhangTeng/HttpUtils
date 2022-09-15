package com.zhangteng.httputils.result.callback

import androidx.lifecycle.LifecycleOwner
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.lifecycle.HttpLifecycleEventObserver
import com.zhangteng.httputils.lifecycle.isInterruptByLifecycle
import com.zhangteng.httputils.result.callback.interfaces.ICallBack
import com.zhangteng.utils.IException
import com.zhangteng.utils.ILoadingView
import com.zhangteng.utils.showShortToast

/**
 * description: 下载回调
 * author: Swing
 * date: 2022/9/13
 */
abstract class CommonCallBack<T, D>(
    protected var iLoadingView: ILoadingView? = null
) : ICallBack<T, D> {

    protected var disposable: D? = null

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

    override fun doOnSubscribe(d: D) {
        disposable = d
        if (iLoadingView == null) {
            HttpUtils.instance.addDisposable(d as Any)
        } else {
            HttpUtils.instance.addDisposable(d as Any, iLoadingView)
        }
        if (iLoadingView is LifecycleOwner) {
            HttpLifecycleEventObserver.bind(iLoadingView as LifecycleOwner)
        }
        iLoadingView?.showProgressDialog()
    }

    override fun doOnError(iException: IException) {
        if (isInterruptByLifecycle(iLoadingView)) return
        iLoadingView?.dismissProgressDialog()
        if (!isHideToast()) {
            HttpUtils.instance.context.showShortToast(iException.message)
        }
        onFailure(iException)
    }

    override fun doOnCompleted() {
        if (isInterruptByLifecycle(iLoadingView)) return
        iLoadingView?.dismissProgressDialog()
    }

    override fun doOnNext(t: T) {
        if (isInterruptByLifecycle(iLoadingView)) return
        onSuccess(t)
    }
}