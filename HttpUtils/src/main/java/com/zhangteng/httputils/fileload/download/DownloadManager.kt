package com.zhangteng.httputils.fileload.download

import com.zhangteng.httputils.http.HttpUtils
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

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
        val contentLength = response.contentLength()
        var `is`: InputStream? = null
        val buf = ByteArray(2048)
        var len = 0
        var fos: FileOutputStream? = null
        return try {
            `is` = response.byteStream()
            var sum: Long = 0
            val dir = File(destFileDir)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, destFileName)
            fos = FileOutputStream(file)
            while (`is`.read(buf).also { len = it } != -1) {
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
            try {
                response.close()
                `is`?.close()
            } catch (e: IOException) {
            }
            try {
                fos?.close()
            } catch (e: IOException) {
            }
        }
    }
}