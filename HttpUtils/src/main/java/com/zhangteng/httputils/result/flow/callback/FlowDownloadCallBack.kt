package com.zhangteng.httputils.result.flow.callback

import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.result.callback.DownloadCallBack
import com.zhangteng.httputils.utils.DownloadManager
import com.zhangteng.utils.IException
import com.zhangteng.utils.ILoadingView
import com.zhangteng.utils.ThreadPoolUtils
import okhttp3.ResponseBody
import java.io.File
import kotlin.coroutines.CoroutineContext

/**
 * description: Flow下载回调
 * author: Swing
 * date: 2022/9/13
 */
abstract class FlowDownloadCallBack(
    fileName: String? = null,
    iLoadingView: ILoadingView? = null
) : DownloadCallBack<CoroutineContext>(fileName, iLoadingView) {

    override fun doOnError(iException: IException) {
        super.doOnError(iException)
        disposable.cancelSingleRequest()
    }

    override fun doOnCompleted() {
        super.doOnCompleted()
        disposable.cancelSingleRequest()
    }

    override fun onSuccess(t: ResponseBody) {
        ThreadPoolUtils.instance.addExecuteTask {
            DownloadManager.Builder()
                .apply {
                    progressListener = object : DownloadManager.ProgressListener {
                        override fun start() {

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

                        override fun onComplete(file: File) {

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