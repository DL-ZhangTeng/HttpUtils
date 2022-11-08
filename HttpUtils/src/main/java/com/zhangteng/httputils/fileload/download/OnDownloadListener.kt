package com.zhangteng.httputils.fileload.download

import java.io.File

/**
 * Created by swing on 2018/4/24.
 */
interface OnDownloadListener {
    fun start()

    /**
     * 下载进度监听
     *
     * @param bytesRead     已经下载文件的大小
     * @param contentLength 文件的大小
     * @param progress      当前进度，-2:后端返回得文件长度<=0；-1：任务被阻塞;0：任务待开始;100：下载成功
     * @param done          是否下载完成，false：下载中；true：下载成功
     * @param filePath      文件路径
     */
    fun onDownload(
        bytesRead: Long,
        contentLength: Long,
        progress: Float,
        done: Boolean,
        filePath: String?
    )

    fun onComplete(file: File)

    fun onError(e: Exception)
}