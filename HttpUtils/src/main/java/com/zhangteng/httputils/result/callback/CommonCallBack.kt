package com.zhangteng.httputils.result.callback

import android.app.Dialog
import androidx.lifecycle.LifecycleOwner
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.lifecycle.HttpLifecycleEventObserver
import com.zhangteng.httputils.lifecycle.isInterruptByLifecycle
import com.zhangteng.httputils.result.callback.interfaces.ICallBack
import com.zhangteng.utils.IException
import com.zhangteng.utils.showShortToast

/**
 * description: 下载回调
 * author: Swing
 * date: 2022/9/13
 */
abstract class CommonCallBack<T, D>(
    protected var mProgressDialog: Dialog? = null,
    protected var tag: Any? = null
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
        if (tag == null) {
            HttpUtils.instance.addDisposable(d as Any)
        } else {
            HttpUtils.instance.addDisposable(d as Any, tag)
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
    }

    override fun doOnCompleted() {
        if (isInterruptByLifecycle(tag)) return
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    override fun doOnNext(t: T) {
        if (isInterruptByLifecycle(tag)) return
        onSuccess(t)
    }
}