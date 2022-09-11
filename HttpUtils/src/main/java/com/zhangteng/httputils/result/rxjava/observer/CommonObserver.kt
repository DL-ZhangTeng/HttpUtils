package com.zhangteng.httputils.result.rxjava.observer

import android.app.Dialog
import androidx.lifecycle.LifecycleOwner
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.lifecycle.HttpLifecycleEventObserver
import com.zhangteng.httputils.lifecycle.HttpLifecycleEventObserver.Companion.isLifecycleDestroy
import com.zhangteng.httputils.result.rxjava.observer.base.BaseObserver
import com.zhangteng.utils.IException
import com.zhangteng.utils.i
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
    }

    override fun doOnError(iException: IException) {
        if (isInterrupt()) return
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
        if (!isHideToast) {
            HttpUtils.instance.context.showShortToast(iException.message)
        }
        onFailure(iException)
    }

    override fun doOnCompleted() {
        if (isInterrupt()) return
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    override fun doOnNext(t: T) {
        if (isInterrupt()) return
        onSuccess(t)
    }

    /**
     * description: 是否中断后续程序
     */
    private fun isInterrupt(): Boolean {
        if (tag is LifecycleOwner && isLifecycleDestroy(tag as LifecycleOwner?)) {
            //页面销毁状态取消网络请求
            //观察者会清理全部请求
            disposable = null
            return true
        }
        if (disposable != null) {
            //主动取消并清理请求集合
            try {
                HttpUtils.instance.cancelSingleRequest(disposable!!)
            } catch (e: IllegalStateException) {
                e.message.i("cancelSingleRequest")
            } finally {
                disposable = null
            }
        }
        return false
    }

    init {
        if (tag is LifecycleOwner) {
            HttpLifecycleEventObserver.bind(tag as LifecycleOwner)
        }
    }
}