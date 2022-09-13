package com.zhangteng.httputils.result.coroutine.callback

import android.app.Dialog
import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.result.callback.DownloadCallBack
import com.zhangteng.httputils.utils.DownloadManager
import com.zhangteng.utils.IException
import com.zhangteng.utils.ThreadPoolUtils
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import java.io.IOException

/**
 * description: Deferred下载回调
 * author: Swing
 * date: 2022/9/13
 */
abstract class DeferredDownloadCallBack(
    fileName: String? = null,
    mProgressDialog: Dialog? = null,
    tag: Any? = null
) : DownloadCallBack<Deferred<ResponseBody>>(fileName, mProgressDialog, tag) {

    override fun doOnCompleted() {
        super.doOnCompleted()
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