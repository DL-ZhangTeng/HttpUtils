package com.zhangteng.httputils.result.rxjava.observer

import android.annotation.SuppressLint
import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.result.callback.DownloadCallBack
import com.zhangteng.httputils.utils.DownloadManager
import com.zhangteng.utils.IException
import com.zhangteng.utils.IStateView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import java.io.IOException

/**
 * Created by swing on 2018/4/24.
 */
abstract class DownloadObserver(
    fileName: String? = null,
    iStateView: IStateView? = null
) : DownloadCallBack<Disposable>(fileName, iStateView),
    Observer<ResponseBody> {

    override fun onSubscribe(d: Disposable) {
        doOnSubscribe(d)
    }

    override fun onNext(o: ResponseBody) {
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
    override fun onSuccess(t: ResponseBody) {
        Observable
            .just(t)
            .subscribeOn(Schedulers.io())
            .subscribe {
                try {
                    DownloadManager().saveFile(
                        it,
                        fileName,
                        object : DownloadManager.ProgressListener {
                            override fun onResponseProgress(
                                bytesRead: Long,
                                contentLength: Long,
                                progress: Int,
                                done: Boolean,
                                filePath: String?
                            ) {
                                Observable
                                    .just(progress)
                                    .distinctUntilChanged()
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe {
                                        onSuccess(
                                            bytesRead,
                                            contentLength,
                                            progress.toFloat(),
                                            done,
                                            filePath
                                        )
                                    }
                            }
                        })
                } catch (e: IOException) {
                    doOnError(IException.handleException(e))
                }
            }
    }
}