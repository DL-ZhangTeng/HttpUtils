package com.zhangteng.httputils.fileload.download

import android.annotation.SuppressLint
import android.app.Dialog
import com.zhangteng.utils.IException.Companion.handleException
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import java.io.IOException

/**
 * Created by swing on 2018/4/24.
 */
abstract class DownloadObserver : BaseDownloadObserver {
    private var fileName: String
    private var mProgressDialog: Dialog? = null

    constructor(fileName: String) {
        this.fileName = fileName
    }

    constructor(fileName: String, mProgressDialog: Dialog?) {
        this.fileName = fileName
        this.mProgressDialog = mProgressDialog
    }

    /**
     * 获取disposable 在onDestroy方法中取消订阅disposable.dispose()
     *
     * @param d Disposable
     */
    protected abstract fun getDisposable(d: Disposable?)

    /**
     * 失败回调
     *
     * @param errorMsg errorMsg
     */
    protected abstract fun onFailure(errorMsg: String?)
    /**
     * 成功回调
     *
     * @param filePath filePath
     */
    /**
     * 成功回调
     *
     * @param bytesRead     已经下载文件的大小
     * @param contentLength 文件的大小
     * @param progress      当前进度
     * @param done          是否下载完成
     * @param filePath      文件路径
     */
    protected abstract fun onSuccess(
        bytesRead: Long,
        contentLength: Long,
        progress: Float,
        done: Boolean,
        filePath: String?
    )

    override fun doOnError(e: Throwable?) {
        onFailure(handleException(e!!).message)
    }

    override fun onSubscribe(d: Disposable) {
        getDisposable(d)
    }

    @SuppressLint("CheckResult")
    override fun onNext(responseBody: ResponseBody) {
        Observable
            .just(responseBody)
            .subscribeOn(Schedulers.io())
            .subscribe {
                try {
                    DownloadManager().saveFile(it, fileName, object : ProgressListener {
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
                    doOnError(e)
                }
            }
    }

    override fun onComplete() {}
}