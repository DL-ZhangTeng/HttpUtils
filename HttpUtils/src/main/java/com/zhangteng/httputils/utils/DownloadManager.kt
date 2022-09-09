package com.zhangteng.httputils.utils

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
class DownloadManager {
    /**
     * 保存文件
     *
     * @param response     ResponseBody
     * @param destFileName 文件名（包括文件后缀）
     * @return 返回
     * @throws IOException
     */
    @Throws(IOException::class)
    fun saveFile(
        response: ResponseBody,
        destFileName: String?,
        progressListener: ProgressListener
    ): File {
        val destFileDir: String =
            HttpUtils.instance.context!!.getExternalFilesDir(null)
                .toString() + File.separator
        var contentLength: Long? = null
        var inputStream: InputStream? = null
        val buf = ByteArray(2048)
        var len = 0
        var fos: FileOutputStream? = null
        return try {
            contentLength = response.contentLength()
            inputStream = response.byteStream()
            var sum: Long = 0
            val dir = File(destFileDir)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, destFileName ?: UUID.randomUUID().toString())
            fos = FileOutputStream(file)
            while (inputStream.read(buf).also { len = it } != -1) {
                sum += len.toLong()
                fos.write(buf, 0, len)
                val finalSum = sum
                progressListener.onResponseProgress(
                    finalSum,
                    contentLength,
                    (finalSum * 1.0f / contentLength * 100).toInt(),
                    finalSum == contentLength,
                    file.absolutePath
                )
            }
            fos.flush()
            file
        } finally {
            response.close()
            inputStream?.close()
            fos?.close()
        }
    }

    /**
     * Created by swing on 2018/4/24.
     */
    interface ProgressListener {
        /**
         * 下载进度监听
         *
         * @param bytesRead     已经下载文件的大小
         * @param contentLength 文件的大小
         * @param progress      当前进度
         * @param done          是否下载完成
         * @param filePath      文件路径
         */
        fun onResponseProgress(
            bytesRead: Long,
            contentLength: Long,
            progress: Int,
            done: Boolean,
            filePath: String?
        )
    }
}