package com.zhangteng.httputils.utils

import android.os.Environment
import android.util.Log
import androidx.work.*
import com.zhangteng.httputils.http.HttpUtils
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * Created by swing on 2018/4/24.
 */
class DownloadManager private constructor(var builder: Builder) {

    /**
     * description 使用WorkManager方式开启下载，实现了断点续传下载、断网重连下载
     */
    fun start() {
        if (builder.downloadUrl.isNullOrEmpty()) {
            Log.d("DownloadManager", "downloadUrl is null,This is illegal")
            return
        }

        //构建下载任务
        val data = Data.Builder()
        data.putString(DownloadWorker.DOWNLOAD_WORKER_REQUEST_URL, builder.downloadUrl)
        //传入要存储的根目录，文件名字会根据downloadUrl进行截取
        data.putString(DownloadWorker.DOWNLOAD_WORKER_FILE_PATH, builder.downloadPath)

        val requestBuilder =
            OneTimeWorkRequest.Builder(DownloadWorker::class.java).setInputData(data.build())

        if (builder.isNetworkReconnect) {
            //网络连接正常时执行
            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            requestBuilder.setConstraints(constraints)
        }
        val request = requestBuilder.build()

        val manager = WorkManager.getInstance(HttpUtils.instance.context!!)
        manager.getWorkInfoByIdLiveData(request.id)
            .observeForever { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        val filePath =
                            workInfo.outputData.getString(DownloadWorker.DOWNLOAD_WORKER_FILE_PATH)
                        val progress =
                            workInfo.outputData.getFloat(
                                DownloadWorker.DOWNLOAD_WORKER_PROGRESS,
                                100f
                            )
                        val completed =
                            workInfo.outputData.getLong(
                                DownloadWorker.DOWNLOAD_WORKER_COMPLETED,
                                0L
                            )
                        val totalSize =
                            workInfo.outputData.getLong(DownloadWorker.DOWNLOAD_WORKER_TOTAL, 0L)
                        if (filePath.isNullOrEmpty()) {
                            builder.progressListener?.onError(Exception("下载失败，请稍后重试"))
                            Log.i("DownloadManager", "下载失败，请稍后重试")
                        } else {
                            builder.progressListener?.onProgress(
                                completed,
                                totalSize,
                                progress,
                                completed != totalSize || progress != 100f,
                                filePath
                            )
                            builder.progressListener?.onComplete(File(filePath))
                            Log.i("DownloadManager", "正在下载：进度$progress 完成$completed 大小$totalSize")
                            Log.i("DownloadManager", "下载完成")
                        }
                    }
                    WorkInfo.State.RUNNING -> {
                        val filePath =
                            workInfo.progress.getString(DownloadWorker.DOWNLOAD_WORKER_FILE_PATH)
                        val progress =
                            workInfo.progress.getFloat(DownloadWorker.DOWNLOAD_WORKER_PROGRESS, 0f)
                        val completed =
                            workInfo.progress.getLong(DownloadWorker.DOWNLOAD_WORKER_COMPLETED, 0L)
                        val totalSize =
                            workInfo.progress.getLong(DownloadWorker.DOWNLOAD_WORKER_TOTAL, 0L)
                        if (!filePath.isNullOrEmpty()) {
                            builder.progressListener?.onProgress(
                                completed,
                                totalSize,
                                progress,
                                completed == totalSize && progress == 100f,
                                filePath
                            )
                            Log.i("DownloadManager", "正在下载：进度$progress 完成$completed 大小$totalSize")
                        }
                    }
                    WorkInfo.State.ENQUEUED -> {
                        val filePath =
                            workInfo.outputData.getString(DownloadWorker.DOWNLOAD_WORKER_FILE_PATH)
                        val totalSize =
                            workInfo.outputData.getLong(DownloadWorker.DOWNLOAD_WORKER_TOTAL, 0L)
                        builder.progressListener?.onProgress(
                            0L,
                            totalSize,
                            0f,
                            false,
                            filePath
                        )
                        Log.i("DownloadManager", "等待下载")
                    }
                    WorkInfo.State.BLOCKED -> {
                        val filePath =
                            workInfo.outputData.getString(DownloadWorker.DOWNLOAD_WORKER_FILE_PATH)
                        val totalSize =
                            workInfo.outputData.getLong(DownloadWorker.DOWNLOAD_WORKER_TOTAL, 0L)
                        if (!filePath.isNullOrEmpty()) {
                            builder.progressListener?.onProgress(
                                0L,
                                totalSize,
                                -1f,
                                false,
                                filePath
                            )
                            Log.i("DownloadManager", "下载阻塞")
                        }
                    }
                    WorkInfo.State.CANCELLED -> {
                        builder.progressListener?.onError(Exception("取消下载"))
                        Log.i("DownloadManager", "取消下载")
                    }
                    WorkInfo.State.FAILED -> {
                        builder.progressListener?.onError(Exception("下载失败，请稍后重试"))
                        Log.i("DownloadManager", "下载失败，请稍后重试")
                    }
                }
            }
        manager.enqueue(request)
    }

    /**
     * 保存文件
     * 文件默认保存在getExternalFilesDir(null).toString() + File.separator + "download" + File.separator
     * 如果自定义文件保存目录可使用DownloadManager(builder)设置保存地址
     *
     * @param response     ResponseBody 接口返回实体
     * @param destFileName 文件名（包括文件后缀）
     */
    fun saveFile(response: ResponseBody, destFileName: String?): File? {
        var inputStream: InputStream? = null
        var fos: FileOutputStream? = null
        return try {
            val destFileDir: String = builder.downloadPath!!
            val buf = ByteArray(2048)
            var len: Int
            val contentLength: Long = response.contentLength()
            var sum: Long = 0
            val dir = File(destFileDir)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, destFileName ?: UUID.randomUUID().toString())
            inputStream = response.byteStream()
            fos = FileOutputStream(file)
            while (inputStream.read(buf).also { len = it } != -1) {
                sum += len.toLong()
                fos.write(buf, 0, len)
                val finalSum = sum
                builder.progressListener?.onProgress(
                    finalSum,
                    contentLength,
                    finalSum * 1.0f / contentLength * 100,
                    finalSum == contentLength,
                    file.absolutePath
                )
            }
            fos.flush()
            builder.progressListener?.onComplete(file)
            file
        } catch (e: IOException) {
            builder.progressListener?.onError(e)
            null
        } finally {
            response.close()
            inputStream?.close()
            fos?.close()
        }
    }

    class Builder {
        /**
         * description: 默认的下载存储目录
         */
        var downloadPath: String? = null

        /**
         * description: 文件的url，断点续传下载参数
         */
        var downloadUrl: String? = null

        /**
         * description: 是否需要断网重连 默认不重连，断点续传下载参数
         */
        var isNetworkReconnect: Boolean = false

        /**
         * description: 进度回调
         */
        var progressListener: ProgressListener? = null

        fun build(): DownloadManager {
            if (downloadPath.isNullOrEmpty()) {
                downloadPath = if (HttpUtils.instance.context == null) {
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + File.separator
                } else {
                    HttpUtils.instance.context!!.getExternalFilesDir(null)!!.absolutePath + File.separator + "download" + File.separator
                }
            }
            return DownloadManager(this)
        }

    }

    /**
     * Created by swing on 2018/4/24.
     */
    interface ProgressListener {
        fun onComplete(file: File)

        /**
         * 下载进度监听
         *
         * @param bytesRead     已经下载文件的大小
         * @param contentLength 文件的大小
         * @param progress      当前进度，-2:后端返回得文件长度<=0；-1：任务被阻塞;0：任务待开始;100：下载成功
         * @param done          是否下载完成，false：下载中；true：下载成功
         * @param filePath      文件路径
         */
        fun onProgress(
            bytesRead: Long,
            contentLength: Long,
            progress: Float,
            done: Boolean,
            filePath: String?
        )

        fun onError(e: Exception)
    }
}