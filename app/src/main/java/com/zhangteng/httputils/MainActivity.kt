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
import com.zhangteng.httputils.result.flow.launchGoFlowIResponse
import com.zhangteng.utils.StateViewHelper
import com.zhangteng.utils.e
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope

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

        GlobalScope.launchGoFlowIResponse({
            HttpUtils.instance.ConfigGlobalHttpUtils()
                .createService(Api::class.java)
                .getHomeListByFlow(0)
        }, {
            Gson().toJson(it).e()
        }, {
            Gson().toJson(it).e()
        }, {

        },
            mProgressDialog,
            this
        )
    }
}