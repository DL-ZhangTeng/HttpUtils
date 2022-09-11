package com.zhangteng.httputils.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.utils.e

/**
 * @description: Http请求取消生命周期观察者
 * @author: Swing
 * @date: 2021/9/22
 */
class HttpLifecycleEventObserver : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event != Lifecycle.Event.ON_DESTROY) {
            return
        }

        // 移除监听
        source.lifecycle.removeObserver(this)
        // 取消请求
        try {
            HttpUtils.instance.cancelSingleRequest(source)
        } catch (e: IllegalStateException) {
            e.message.e("cancelSingleRequest")
        }
    }

    companion object {
        /**
         * 绑定组件的生命周期
         *
         * @param lifecycleOwner 请传入 AppCompatActivity 或者 AndroidX.Fragment 子类
         * 如需传入其他对象可继承[androidx.lifecycle.LifecycleOwner]
         * 请参考以下两个类[androidx.lifecycle.LifecycleService][androidx.lifecycle.ProcessLifecycleOwner]
         */
        fun bind(lifecycleOwner: LifecycleOwner) {
            lifecycleOwner.lifecycle.addObserver(HttpLifecycleEventObserver())
        }

        /**
         * 判断宿主是否处于活动状态
         */
        fun isLifecycleActive(lifecycleOwner: LifecycleOwner?): Boolean {
            return lifecycleOwner != null && lifecycleOwner.lifecycle.currentState != Lifecycle.State.DESTROYED
        }

        /**
         * 判断宿主是否处于死亡状态
         */
        fun isLifecycleDestroy(lifecycleOwner: LifecycleOwner?): Boolean {
            return lifecycleOwner != null && lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED
        }
    }
}