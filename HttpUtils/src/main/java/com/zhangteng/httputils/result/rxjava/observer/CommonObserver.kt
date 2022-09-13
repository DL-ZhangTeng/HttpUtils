package com.zhangteng.httputils.result.rxjava.observer

import android.app.Dialog
import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.result.callback.CommonCallBack
import com.zhangteng.utils.IException
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * Created by swing on 2018/4/24.
 */
abstract class CommonObserver<T : Any>(
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) : CommonCallBack<T, Disposable>(mProgressDialog, tag), Observer<T> {

    override fun onSubscribe(d: Disposable) {
        doOnSubscribe(d)
    }

    override fun onNext(o: T) {
        doOnNext(o)
    }

    override fun onError(e: Throwable) {
        doOnError(IException.handleException(e))
    }

    override fun onComplete() {
        doOnCompleted()
    }

    override fun doOnError(iException: IException) {
        super.doOnError(iException)
        disposable.cancelSingleRequest()
    }

    override fun doOnCompleted() {
        super.doOnCompleted()
        disposable.cancelSingleRequest()
    }

    override fun doOnNext(t: T) {
        super.doOnNext(t)
        disposable.cancelSingleRequest()
    }
}