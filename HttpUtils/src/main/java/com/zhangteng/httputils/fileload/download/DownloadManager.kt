package com.zhangteng.httputils.fileload.download

import android.os.Environment
import android.util.Log
import androidx.work.*
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.utils.ThreadPoolUtils
import okhttp3.ResponseBody
import java.io.*
import java.net.HttpURLConnection
import java.util.*
import kotlin.math.max

/**
 * Created by swing on 2018/4/24.
 */
class DownloadManager private constructor(var builder: Builder) {

    /**
     * description 开启下载，实现了断点续传下载
     */
    fun start() {
        if (builder.downloadUrl.isNullOrEmpty()) {
            Log.i("DownloadManager", "下载失败，请稍后重试")
            return
        }
        ThreadPoolUtils.instance.addExecuteTask {
            //创建存储目录
            val dir = File(builder.downloadPath!!)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val fileName =
                builder.downloadUrl!!.substring(builder.downloadUrl!!.lastIndexOf("/") + 1)
            val file = File(builder.downloadPath, fileName)

            //如果文件不存在，说明是第一次下载，创建一个新的文件,并开始下载
            if (!file.exists()) {
                file.createNewFile()
                startDownload(file)
            } else {
                continuousDownload(file)
            }
        }
    }

    /**
     * description 使用WorkManager方式开启下载，实现了断点续传下载、断网重连下载
     */
    fun startByWorker() {
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
                            builder.onDownloadListener?.onError(Exception("下载失败，请稍后重试"))
                            Log.i("DownloadManager", "下载失败，请稍后重试")
                        } else {
                            builder.onDownloadListener?.onDownload(
                                completed,
                                totalSize,
                                progress,
                                completed == totalSize && progress == 100f,
                                filePath
                            )
                            builder.onDownloadListener?.onComplete(File(filePath))
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
                            builder.onDownloadListener?.onDownload(
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
                        builder.onDownloadListener?.start()
                        builder.onDownloadListener?.onDownload(
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
                            builder.onDownloadListener?.onDownload(
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
                        builder.onDownloadListener?.onError(Exception("取消下载"))
                        Log.i("DownloadManager", "取消下载")
                    }
                    WorkInfo.State.FAILED -> {
                        builder.onDownloadListener?.onError(Exception("下载失败，请稍后重试"))
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
                builder.onDownloadListener?.onDownload(
                    finalSum,
                    contentLength,
                    finalSum * 1.0f / contentLength * 100,
                    finalSum == contentLength,
                    file.absolutePath
                )
            }
            fos.flush()
            builder.onDownloadListener?.onComplete(file)
            file
        } catch (e: IOException) {
            builder.onDownloadListener?.onError(e)
            null
        } finally {
            response.close()
            inputStream?.close()
            fos?.close()
        }
    }

    /**
     * description 普通下载任务开始
     * @param file 本地下载的文件
     */
    private fun startDownload(file: File) {
        val response = DownloadRetrofit.instance
            .retrofit
            .create(DownloadRangeApi::class.java)
            .downloadFile(builder.downloadUrl)
            .execute()
        if (!response.isSuccessful) {
            Log.i("DownloadWorker", "startDownload: 下载失败,正在准备重试")
            //重试
            return start()
        }

        //获取文件总长度
        val totalSize = response.body()?.contentLength() ?: -1

        if (totalSize <= 0L) {
            //重试
            return start()
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
                val completed = file.length()
                builder.onDownloadListener?.onDownload(
                    completed,
                    totalSize,
                    completed * 100f / totalSize,
                    completed == totalSize,
                    builder.downloadPath!!
                )
            }
            index++
        }
        fos.flush()

        //成功
        val completed = file.length()
        builder.onDownloadListener?.onDownload(
            completed,
            totalSize,
            completed * 100f / totalSize,
            completed == totalSize,
            builder.downloadPath!!
        )
        builder.onDownloadListener?.onComplete(file)
    }

    /**
     * description 续传下载任务开始
     * @param file 本地下载的文件
     */
    private fun continuousDownload(file: File) {
        //如果文件已存在,获取文件的长度
        val startIndex = file.length() //获取文件长度

        //文件异常，删除已经存储的文件，重新创建一个文件
        if (startIndex < 0) {
            file.delete()
            file.createNewFile()
            Log.i("DownloadWorker", "文件异常,正在准备重试")
            return start()
        }

        val response = DownloadRetrofit.instance
            .retrofit
            .create(DownloadRangeApi::class.java)
            .downloadFileByRange(builder.downloadUrl, "bytes=$startIndex-")
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
            //成功
            val completed = file.length()
            builder.onDownloadListener?.onDownload(
                completed,
                totalSize,
                completed * 100f / totalSize,
                completed == totalSize,
                builder.downloadPath!!
            )
            builder.onDownloadListener?.onComplete(file)
            return
        }
        //文件异常，删除已经存储的文件，重新创建一个文件
        if (startIndex > totalSize) {
            file.delete()
            file.createNewFile()
            Log.i("DownloadWorker", "文件异常,正在准备重试")
            return start()
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
                        //成功
                        val completed = file.length()
                        builder.onDownloadListener?.onDownload(
                            completed,
                            totalSize,
                            completed * 100f / totalSize,
                            completed == totalSize,
                            builder.downloadPath!!
                        )
                    }
                    index++
                }
                //成功
                val completed = file.length()
                builder.onDownloadListener?.onDownload(
                    completed,
                    totalSize,
                    completed * 100f / totalSize,
                    completed == totalSize,
                    builder.downloadPath!!
                )
                builder.onDownloadListener?.onComplete(file)
                return
            } catch (e: Exception) {
                Log.i("DownloadWorker", "断点下载失败,正在准备重试")
                return start()
            } finally {
                randomAccessFile?.close()
            }
        } else {
            //文件大小异常，删除文件重新下载
            file.delete()
            file.createNewFile()
            return start()
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
         * description: 进度回调，不使用worker时在子线程回调
         */
        var onDownloadListener: OnDownloadListener? = null

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
}