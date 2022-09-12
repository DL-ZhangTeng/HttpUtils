package com.zhangteng.httputils.result.rxjava.observer

import android.app.Dialog
import androidx.lifecycle.LifecycleOwner
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.lifecycle.HttpLifecycleEventObserver
import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.lifecycle.isInterruptByLifecycle
import com.zhangteng.httputils.result.rxjava.observer.base.BaseObserver
import com.zhangteng.utils.IException
import com.zhangteng.utils.showShortToast
import io.reactivex.disposables.Disposable

/**
 * Created by swing on 2018/4/24.
 */
abstract class CommonObserver<T : Any>(
    private var mProgressDialog: Dialog? = null,
    private var tag: Any? = null
) : BaseObserver<T>() {
    private var disposable: Disposable? = null

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

    override fun doOnSubscribe(d: Disposable) {
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