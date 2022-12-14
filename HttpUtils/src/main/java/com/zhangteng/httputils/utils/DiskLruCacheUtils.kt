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
     * ????????????????????????????????????
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
            Log.d("DiskLruCacheUtils", "????????????")
            mDiskLruCache = null
        }
        return false
    }

    /**
     * ????????????????????????????????????
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
            Log.d("DiskLruCacheUtils", "????????????")
        }
        mDiskLruCache = null
        return false
    }

    /**
     * ?????????????????????????????????
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
     * ????????????????????????????????????????????????????????????journal???????????????
     * ??????Activity???onPause()????????????????????????
     */
    fun flush() {
        if (mDiskLruCache == null) {
            createDiskLruCache()
        }
        try {
            mDiskLruCache!!.flush()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("DiskLruCacheUtils", "????????????????????????")
        }
        mDiskLruCache = null
    }

    /**
     * ???DiskLruCache?????????(open()????????????)<br></br>
     * ????????????????????????????????????DiskLruCache????????????????????????????????????<br></br>
     * ?????????Activity???onDestroy()??????????????????
     */
    fun close() {
        if (mDiskLruCache == null) {
            createDiskLruCache()
        }
        try {
            mDiskLruCache!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("DiskLruCacheUtils", "DiskLruCache????????????")
        }
        mDiskLruCache = null
    }

    /**
     * ????????????????????????????????????<br></br>
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
            Log.d("DiskLruCacheUtils", "????????????????????????")
            false
        }
    }

    /**
     * ?????????????????????????????????????????????<br></br>
     */
    fun delete() {
        if (mDiskLruCache == null) {
            createDiskLruCache()
        }
        try {
            mDiskLruCache!!.delete()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("DiskLruCacheUtils", "????????????????????????")
        }
        mDiskLruCache = null
    }
}