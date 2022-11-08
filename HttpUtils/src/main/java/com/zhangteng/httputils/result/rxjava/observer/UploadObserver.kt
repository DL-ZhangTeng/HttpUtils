package com.zhangteng.httputils.result.rxjava.observer

import android.annotation.SuppressLint
import com.zhangteng.httputils.fileload.upload.ISliceFile
import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.result.callback.UploadCallBack
import com.zhangteng.utils.IException
import com.zhangteng.utils.ILoadingView
import com.zhangteng.utils.IResponse
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * description: Observer上传回调
 * author: Swing
 * date: 2022/11/8
 */
abstract class UploadObserver<T : ISliceFile, R : IResponse<T>>(
    currentNum: Int = 1,
    allNum: Int = 1,
    iLoadingView: ILoadingView? = null
) : UploadCallBack<T, R, Disposable>(currentNum, allNum, iLoadingView), Observer<R> {

    override fun onSubscribe(d: Disposable) {
        doOnSubscribe(d)
    }

    override fun onNext(o: R) {
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

    @SuppressLint("CheckResult")
    override fun onSuccess(t: R) {
        Observable
            .just(t)
            .subscribeOn(Schedulers.io())
            .subscribe {
                if (t.isSuccess()) {
                    if (t.getResult().isFileExists() == true) {
                        onSuccess(
                            currentNum,
                            allNum,
                            100f,
                            true,
                            t.getResult().getSourcePath(),
                            t.getResult().getSourceId()
                        )
                    } else {
                        onSuccess(
                            currentNum,
                            allNum,
                            0f,
                            false,
                            t.getResult().getSourcePath(),
                            t.getResult().getSourceId()
                        )
                    }
                } else {
                    onSuccess(
                        currentNum,
                        allNum,
                        0f,
                        false,
                        t.getResult().getSourcePath(),
                        t.getResult().getSourceId()
                    )
                }
            }
    }
}