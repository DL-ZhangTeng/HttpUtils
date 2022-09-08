package com.zhangteng.httputils.result.rxjava.observer

import android.app.Dialog
import androidx.lifecycle.LifecycleOwner
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.lifecycle.HttpLifecycleEventObserver
import com.zhangteng.httputils.result.rxjava.observer.base.BaseObserver
import com.zhangteng.utils.IException
import com.zhangteng.utils.showShortToast
import io.reactivex.disposables.Disposable

/**
 * Created by swing on 2018/4/24.
 */
abstract class CommonObserver<T>(
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

    override fun doOnSubscribe(d: Disposable?) {
        disposable = d
        if (tag == null) {
            HttpUtils.instance.addDisposable(d)
        } else {
            HttpUtils.instance.addDisposable(d, tag)
        }
    }

    override fun doOnError(iException: IException) {
        if (disposable != null) {
            HttpUtils.instance.cancelSingleRequest(disposable)
            disposable = null
        }
        if (isTargetDestroy) return
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
        if (!isHideToast) {
            HttpUtils.instance.context.showShortToast(iException.message)
        }
        onFailure(iException)
    }

    override fun doOnCompleted() {
        if (disposable != null) {
            HttpUtils.instance.cancelSingleRequest(disposable)
            disposable = null
        }
        if (isTargetDestroy) return
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    override fun doOnNext(t: T) {
        if (isTargetDestroy) return
        onSuccess(t)
    }

    /**
     * description 目标是否销毁
     */
    private val isTargetDestroy: Boolean
        get() = (tag != null && tag is LifecycleOwner
                && !HttpLifecycleEventObserver.isLifecycleActive(tag as LifecycleOwner?))

    init {
        if (tag is LifecycleOwner) {
            HttpLifecycleEventObserver.bind(tag as LifecycleOwner)
        }
    }
}