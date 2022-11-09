package com.zhangteng.httputils.fileload.download

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import kotlin.math.max

/**
 * description: 下载任务
 * author: Swing
 * date: 2022/11/5
 */
class DownloadWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val inputData = inputData
        val url: String? = inputData.getString(DOWNLOAD_WORKER_REQUEST_URL)
        val path = inputData.getString(DOWNLOAD_WORKER_FILE_PATH)
        if (url.isNullOrEmpty()) return Result.failure()
        if (path.isNullOrEmpty()) return Result.failure()
        //创建存储目录
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val fileName = url.substring(url.lastIndexOf("/") + 1)
        val file = File(path, fileName)

        //如果文件不存在，说明是第一次下载，创建一个新的文件,并开始下载
        return if (!file.exists()) {
            file.createNewFile()
            startDownload(url, file)
        } else {
            continuousDownload(url, file)
        }
    }

    /**
     * description 普通下载任务开始
     * @param url 请求路径
     * @param file 本地下载文件
     */
    private fun startDownload(url: String, file: File): Result {
        val response = DownloadRetrofit.instance
            .retrofit
            .create(DownloadRangeApi::class.java)
            .downloadFile(url)
            .execute()
        if (!response.isSuccessful) {
            Log.i("DownloadWorker", "startDownload: 下载失败,正在准备重试")
            return Result.retry()
        }

        //获取文件总长度
        val totalSize = response.body()?.contentLength() ?: -1

        if (totalSize <= 0L) {
            return Result.retry()
        }

        val fos = FileOutputStream(file)
        val inputStream = response.body()!!.byteStream()
        val buf = ByteArray(1024)
        var len: Int
        var index: Long = 0
        while (inputStream.read(buf).also { len = it } != -1) {
            fos.write(buf, 0, len)
            if (index % 1024 == 0L) {
                //每1m通知一次进度
                setProgressAsync(getData(file, totalSize))
            }
            index++
        }
        fos.flush()
        return Result.success(getData(file, totalSize))
    }

    /**
     * description 续传下载任务开始
     * @param url 请求路径
     * @param file 本地下载文件
     */
    private fun continuousDownload(
        url: String,
        file: File,
    ): Result {
        //如果文件已存在,获取文件的长度
        val startIndex = file.length() //获取文件长度

        //文件异常，删除已经存储的文件，重新创建一个文件
        if (startIndex < 0) {
            file.delete()
            file.createNewFile()
            Log.i("DownloadWorker", "文件异常,正在准备重试")
            return Result.retry()
        }

        val response = DownloadRetrofit.instance
            .retrofit
            .create(DownloadRangeApi::class.java)
            .downloadFileByRange(url, "bytes=$startIndex-")
            .execute()

        //获取文件总长度,content-length: 0
        val totalSizeLength = response.body()?.contentLength() ?: -1
        //获取响应头中文件大小,content-range: bytes 0-499/1000
        val contentRange = response.headers()["content-range"] ?: "bytes */${totalSizeLength}"
        val totalSizeRange = contentRange.substring(contentRange.indexOf("/") + 1).toLong()

        //文件总长度
        val totalSize = max(totalSizeLength, totalSizeRange)

        //已下载文件长度等于服务端响应文件长度，直接返回成功
        if (startIndex == totalSize) {
            return Result.success(getData(file, totalSize))
        }
        //文件异常，删除已经存储的文件，重新创建一个文件
        if (startIndex > totalSize) {
            file.delete()
            file.createNewFile()
            Log.i("DownloadWorker", "文件异常,正在准备重试")
            return Result.retry()
        }

        //请求成功响应 206；请求失败响应 416
        if (response.code() == HttpURLConnection.HTTP_PARTIAL) {
            var randomAccessFile: RandomAccessFile? = null
            try {
                randomAccessFile = RandomAccessFile(file, "rwd")
                randomAccessFile.seek(startIndex)
                val inputStream = response.body()!!.byteStream()
                val buffer = ByteArray(1024)
                var len: Int
                var index: Long = 0
                while (inputStream.read(buffer).also { len = it } != -1) {
                    randomAccessFile.write(buffer, 0, len)
                    if (index % 1024 == 0L) {
                        //每1m通知一次进度
                        setProgressAsync(getData(file, totalSize))
                    }
                    index++
                }
                return Result.success(getData(file, totalSize))
            } catch (e: Exception) {
                Log.i("DownloadWorker", "断点下载失败,正在准备重试")
                return Result.retry()
            } finally {
                randomAccessFile?.close()
            }
        } else {
            //文件大小异常，删除文件重新下载
            file.delete()
            file.createNewFile()
            return Result.retry()
        }
    }

    /**
     * description 获取Data
     * @param file 已下载文件
     * @param totalSize 文件总长度
     */
    private fun getData(file: File, totalSize: Long): Data {
        val builder: Data.Builder = Data.Builder()
        builder.putString(DOWNLOAD_WORKER_FILE_PATH, file.absolutePath)
        if (file.exists()) {
            val completed = file.length()
            Log.i("DownloadWorker", "正在下载：完成$completed 大小$totalSize")
            if (totalSize != 0L) {
                builder.putFloat(DOWNLOAD_WORKER_PROGRESS, completed * 100f / totalSize)
            } else {
                builder.putFloat(DOWNLOAD_WORKER_PROGRESS, -2f)
            }
            builder.putLong(DOWNLOAD_WORKER_COMPLETED, completed)
            builder.putLong(DOWNLOAD_WORKER_TOTAL, totalSize)
        } else {
            builder.putFloat(DOWNLOAD_WORKER_PROGRESS, 0f)
            builder.putLong(DOWNLOAD_WORKER_COMPLETED, 0L)
            builder.putLong(DOWNLOAD_WORKER_TOTAL, totalSize)
        }
        return builder.build()
    }

    companion object {
        const val DOWNLOAD_WORKER_REQUEST_URL = "download_worker_request_url"
        const val DOWNLOAD_WORKER_FILE_PATH = "download_worker_file_path"

        const val DOWNLOAD_WORKER_PROGRESS = "download_worker_progress"
        const val DOWNLOAD_WORKER_COMPLETED = "download_worker_completed"
        const val DOWNLOAD_WORKER_TOTAL = "download_worker_total"
    }
}