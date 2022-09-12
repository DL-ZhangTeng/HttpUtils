package com.zhangteng.httputils

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.zhangteng.httputils.http.Api
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.result.coroutine.deferredGoIResponse
import com.zhangteng.httputils.result.coroutine.launchGo
import com.zhangteng.httputils.result.coroutine.launchGoDeferredIResponse
import com.zhangteng.httputils.result.coroutine.launchGoIResponse
import com.zhangteng.httputils.result.flow.flowGoIResponse
import com.zhangteng.httputils.result.flow.launchGoFlowIResponse
import com.zhangteng.utils.StateViewHelper
import com.zhangteng.utils.e
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var mProgressDialog: Dialog? = null
    private var mLoadTextView: TextView? = null
    private var mAnimation: Animation? = null
    private var mLoadImageView: ImageView? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mProgressDialog = Dialog(this, R.style.progress_dialog)
        val view = View.inflate(this, StateViewHelper.loadingLayout, null)
        mLoadTextView = view.findViewById(R.id.loadView)
        mLoadImageView = view.findViewById(R.id.progress_bar)
        mLoadImageView?.setImageResource(StateViewHelper.loadingImage)
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.loadings)
            .apply { interpolator = LinearInterpolator() }
        mLoadImageView?.startAnimation(mAnimation)
        mLoadTextView?.text = StateViewHelper.loadingText
        mProgressDialog?.setContentView(view)
        mProgressDialog?.setCancelable(true)
        mProgressDialog?.setCanceledOnTouchOutside(false)

        GlobalScope.launch {
            launchGo({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeList(0)
            }, {
                Gson().toJson(it).e("launchGo")
            }, {
                Gson().toJson(it).e("launchGo")
            }, {

            },
                mProgressDialog,
                this@MainActivity
            )
        }

        GlobalScope.launch {
            launchGoIResponse({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeList(0)
            }, {
                Gson().toJson(it).e("launchGoIResponse")
            }, {
                Gson().toJson(it).e("launchGoIResponse")
            }, {

            },
                mProgressDialog,
                this@MainActivity
            )
        }

        GlobalScope.launch {
            launchGo({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeList(0)
            }, {
                Gson().toJson(it).e("launchGo")
            }, {
                Gson().toJson(it).e("launchGo")
            }, {

            },
                mProgressDialog,
                this@MainActivity
            )
        }

        GlobalScope.launch {
            launchGoIResponse({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeList(0)
            }, {
                Gson().toJson(it).e("launchGoIResponse")
            }, {
                Gson().toJson(it).e("launchGoIResponse")
            }, {

            },
                mProgressDialog,
                this@MainActivity
            )
        }

        GlobalScope.launch {
            launchGoFlowIResponse({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeListByFlow(0)
            }, {
                Gson().toJson(it).e("launchGoFlowIResponse")
            }, {
                Gson().toJson(it).e("launchGoFlowIResponse")
            }, {

            },
                mProgressDialog,
                this@MainActivity
            )
        }

        GlobalScope.launch {
            HttpUtils.instance.ConfigGlobalHttpUtils()
                .createService(Api::class.java)
                .getHomeListByFlow(0)
                .flowGoIResponse({
                    Gson().toJson(it).e("flowGoIResponse")
                }, {
                    Gson().toJson(it).e("flowGoIResponse")
                }, {

                },
                    mProgressDialog,
                    this@MainActivity
                )
        }

        GlobalScope.launch {
            launchGoDeferredIResponse({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeListByDeferred(0)
            }, {
                Gson().toJson(it).e("launchGoDeferredIResponse")
            }, {
                Gson().toJson(it).e("launchGoDeferredIResponse")
            }, {

            },
                mProgressDialog,
                this@MainActivity
            )
        }

        GlobalScope.launch {
            HttpUtils.instance.ConfigGlobalHttpUtils()
                .createService(Api::class.java)
                .getHomeListByDeferred(0)
                .deferredGoIResponse({
                    Gson().toJson(it).e("deferredGoIResponse")
                }, {
                    Gson().toJson(it).e("deferredGoIResponse")
                }, {

                },
                    mProgressDialog,
                    this@MainActivity
                )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        HttpUtils.instance.cancelAllRequest()
    }
}