package com.zhangteng.httputils.result.rxjava.observer

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.lifecycle.LifecycleOwner
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.lifecycle.HttpLifecycleEventObserver
import com.zhangteng.httputils.lifecycle.HttpLifecycleEventObserver.Companion.isLifecycleDestroy
import com.zhangteng.httputils.result.rxjava.observer.base.BaseObserver
import com.zhangteng.httputils.utils.DownloadManager
import com.zhangteng.utils.IException
import com.zhangteng.utils.showShortToast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import java.io.IOException

/**
 * Created by swing on 2018/4/24.
 */
abstract class DownloadObserver(
    private var fileName: String? = null,
    private var mProgressDialog: Dialog? = null,
    private var tag: Any? = null
) : BaseObserver<ResponseBody>() {
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

    @SuppressLint("CheckResult")
    override fun doOnNext(t: ResponseBody) {
        if (isInterrupt()) return
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
            HttpUtils.instance.cancelSingleRequest(disposable!!)
            disposable = null
        }
        return false
    }

    init {
        if (tag is LifecycleOwner) {
            HttpLifecycleEventObserver.bind(tag as LifecycleOwner)
        }
    }
}