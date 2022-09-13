package com.zhangteng.httputils.result.flow.callback

import android.annotation.SuppressLint
import android.app.Dialog
import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.result.callback.DownloadCallBack
import com.zhangteng.httputils.utils.DownloadManager
import com.zhangteng.utils.IException
import com.zhangteng.utils.ThreadPoolUtils
import okhttp3.ResponseBody
import java.io.IOException
import kotlin.coroutines.CoroutineContext

/**
 * description: Flow下载回调
 * author: Swing
 * date: 2022/9/13
 */
abstract class FlowDownloadCallBack(
    fileName: String? = null,
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) : DownloadCallBack<CoroutineContext>(fileName, mProgressDialog, tag) {

    override fun doOnError(iException: IException) {
        super.doOnError(iException)
        disposable.cancelSingleRequest()
    }

    override fun doOnCompleted() {
        super.doOnCompleted()
        disposable.cancelSingleRequest()
    }

    @SuppressLint("CheckResult")
    override fun doOnNext(t: ResponseBody) {
        super.doOnNext(t)
        disposable.cancelSingleRequest()
    }

    override fun onSuccess(t: ResponseBody) {
        ThreadPoolUtils.instance.addExecuteTask {
            try {
                DownloadManager().saveFile(
                    t,
                    fileName,
                    object : DownloadManager.ProgressListener {
                        override fun onResponseProgress(
                            bytesRead: Long,
                            contentLength: Long,
                            progress: Int,
                            done: Boolean,
                            filePath: String?
                        ) {
                            onSuccess(
                                bytesRead,
                                contentLength,
                                progress.toFloat(),
                                done,
                                filePath
                            )
                        }
                    })
            } catch (e: IOException) {
                doOnError(IException.handleException(e))
            }
        }
    }
}