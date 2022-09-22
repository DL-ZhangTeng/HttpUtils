package com.zhangteng.httputils.utils

import android.os.Environment
import android.util.Log
import com.zhangteng.httputils.http.GlobalHttpUtils
import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.internal.cache.DiskLruCache
import okhttp3.internal.concurrent.TaskRunner
import okhttp3.internal.io.FileSystem
import okhttp3.internal.io.FileSystem.Companion.SYSTEM
import okio.ByteString.Companion.encodeUtf8
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException

object DiskLruCacheUtils {
    private var mDiskLruCache: DiskLruCache? = null
    private fun createDiskLruCache() {
        val cache: Cache = GlobalHttpUtils.instance.okHttpClient.cache!!
        val clazz: Class<*> = Cache::class.java
        try {
            val cacheField = clazz.getDeclaredField("cache")
            cacheField.isAccessible = true
            mDiskLruCache = cacheField[cache] as DiskLruCache
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
            val file = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/RxHttpUtilsCache"
            )
            if (!file.exists()) {
                file.mkdirs()
            }
            try {
                mDiskLruCache = DiskLruCache::class.java
                    .getDeclaredConstructor(
                        FileSystem::class.java,
                        File::class.java,
                        Int::class.java,
                        Int::class.java,
                        Long::class.java,
                        TaskRunner::class.java
                    )
                    .newInstance(SYSTEM, file, 201105, 2, 10485760, TaskRunner.INSTANCE)
            } catch (ex: IllegalAccessException) {
                ex.printStackTrace()
            } catch (ex: InstantiationException) {
                ex.printStackTrace()
            } catch (ex: InvocationTargetException) {
                ex.printStackTrace()
            } catch (ex: NoSuchMethodException) {
                ex.printStackTrace()
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            val file = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/RxHttpUtilsCache"
            )
            if (!file.exists()) {
                file.mkdirs()
            }
            try {
                mDiskLruCache = DiskLruCache::class.java
                    .getDeclaredConstructor(
                        FileSystem::class.java,
                        File::class.java,
                        Int::class.java,
                        Int::class.java,
                        Long::class.java,
                        TaskRunner::class.java
                    )
                    .newInstance(SYSTEM, file, 201105, 2, 10485760, TaskRunner.INSTANCE)
            } catch (ex: IllegalAccessException) {
                ex.printStackTrace()
            } catch (ex: InstantiationException) {
                ex.printStackTrace()
            } catch (ex: InvocationTargetException) {
                ex.printStackTrace()
            } catch (ex: NoSuchMethodException) {
                ex.printStackTrace()
            }
        }
    }

    private fun key(key: String): String {
        return key.encodeUtf8().md5().hex()
    }

    private fun key(key: HttpUrl?): String {
        return key.toString().encodeUtf8().md5().hex()
    }

    /**
     * 从本地缓存中移除一条缓存
     *
     * @param imageUrl
     * @return
     */
    fun remove(imageUrl: String): Boolean {
        if (mDiskLruCache == null) {
            createDiskLruCache()
        }
        try {
            val key = key(imageUrl)
            mDiskLruCache!!.remove(key)
            mDiskLruCache = null
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("DiskLruCacheUtils", "移除失败")
            mDiskLruCache = null
        }
        return false
    }

    /**
     * 从本地缓存中移除一条缓存
     *
     * @param imageUrl
     * @return
     */
    fun remove(imageUrl: HttpUrl?): Boolean {
        if (mDiskLruCache == null) {
            createDiskLruCache()
        }
        try {
            val key = key(imageUrl)
            mDiskLruCache!!.remove(key)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("DiskLruCacheUtils", "移除失败")
        }
        mDiskLruCache = null
        return false
    }

    /**
     * 获取当前本地缓存的大小
     *
     * @return
     */
    val cacheSize: Long
        get() {
            if (mDiskLruCache == null) {
                createDiskLruCache()
            }
            var size: Long = 0
            try {
                size = mDiskLruCache!!.size()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mDiskLruCache = null
            return size
        }

    /**
     * 将内存中的操作记录同步到日志文件（也就是journal文件）当中
     * 建议Activity的onPause()方法中去调用一次
     */
    fun flush() {
        if (mDiskLruCache == null) {
            createDiskLruCache()
        }
        try {
            mDiskLruCache!!.flush()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("DiskLruCacheUtils", "日志文件写入失败")
        }
        mDiskLruCache = null
    }

    /**
     * 将DiskLruCache关闭掉(open()方法对应)<br></br>
     * 关闭掉了之后就不能再调用DiskLruCache中任何操作缓存数据的方法<br></br>
     * 建议在Activity的onDestroy()方法中去调用
     */
    fun close() {
        if (mDiskLruCache == null) {
            createDiskLruCache()
        }
        try {
            mDiskLruCache!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("DiskLruCacheUtils", "DiskLruCache关闭失败")
        }
        mDiskLruCache = null
    }

    /**
     * 将所有的缓存数据全部删除<br></br>
     */
    fun evictAll(): Boolean {
        if (mDiskLruCache == null) {
            createDiskLruCache()
        }
        return try {
            mDiskLruCache!!.evictAll()
            mDiskLruCache = null
            true
        } catch (e: IOException) {
            e.printStackTrace()
            mDiskLruCache = null
            Log.d("DiskLruCacheUtils", "本地缓存删除失败")
            false
        }
    }

    /**
     * 将所有的缓存数据全部删除并关闭<br></br>
     */
    fun delete() {
        if (mDiskLruCache == null) {
            createDiskLruCache()
        }
        try {
            mDiskLruCache!!.delete()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("DiskLruCacheUtils", "本地缓存删除失败")
        }
        mDiskLruCache = null
    }
}