package com.zhangteng.httputils.result.coroutine.callback

import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.result.callback.DownloadCallBack
import com.zhangteng.httputils.utils.DownloadManager
import com.zhangteng.utils.IException
import com.zhangteng.utils.ILoadingView
import com.zhangteng.utils.ThreadPoolUtils
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import java.io.File

/**
 * description: Deferred下载回调
 * author: Swing
 * date: 2022/9/13
 */
abstract class DeferredDownloadCallBack(
    fileName: String? = null,
    iLoadingView: ILoadingView? = null
) : DownloadCallBack<Deferred<ResponseBody>>(fileName, iLoadingView) {

    override fun doOnCompleted() {
        super.doOnCompleted()
        disposable.cancelSingleRequest()
    }

    override fun onSuccess(t: ResponseBody) {
        ThreadPoolUtils.instance.addExecuteTask {
            DownloadManager.Builder()
                .apply {
                    progressListener = object : DownloadManager.ProgressListener {
                        override fun onComplete(file: File) {

                        }

                        override fun onProgress(
                            bytesRead: Long,
                            contentLength: Long,
                            progress: Float,
                            done: Boolean,
                            filePath: String?
                        ) {
                            onSuccess(
                                bytesRead,
                                contentLength,
                                progress,
                                done,
                                filePath
                            )
                        }

                        override fun onError(e: Exception) {
                            doOnError(IException.handleException(e))
                        }
                    }
                }
                .build()
                .saveFile(t, fileName)
        }
    }
}