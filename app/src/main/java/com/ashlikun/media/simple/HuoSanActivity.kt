package com.ashlikun.media.simple

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import com.ashlikun.adapter.ViewHolder
import com.ashlikun.adapter.recyclerview.CommonAdapter
import com.ashlikun.media.simple.data.HuoShanData
import com.ashlikun.media.simple.data.HuoShanData.FeedsData
import com.ashlikun.media.simple.divider.HorizontalDividerItemDecoration
import com.ashlikun.media.video.VideoListUtils
import com.ashlikun.media.video.VideoScreenUtils
import com.ashlikun.media.video.VideoUtils
import com.ashlikun.media.video.view.EasyMediaPlayer
import com.ashlikun.okhttputils.http.OkHttpManage
import com.ashlikun.okhttputils.http.callback.AbsCallback
import com.ashlikun.okhttputils.http.request.HttpRequest

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/13　14:36
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：火山小视频
 */
class HuoSanActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    var listDatas: MutableList<HuoShanData.FeedsData> = ArrayList<HuoShanData.FeedsData>()
    val adapter by lazy {
        object : CommonAdapter<FeedsData>(this, R.layout.item_list, listDatas) {
            override fun convert(holder: ViewHolder, s: FeedsData) {
                val videoPlayer: EasyMediaPlayer = holder.getView(R.id.videoPlay)
                //                GlideUtils.show(videoPlayer.getThumbImageView(), s.getImageUrl());
                videoPlayer.setVideoRatio(s.width / s.height)
                videoPlayer.setDataSource(s.url.orEmpty(), s.text.orEmpty())
            }
        }
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        recyclerView.setLayoutManager(GridLayoutManager(this, 2))
        recyclerView.addItemDecoration(
            HorizontalDividerItemDecoration.Builder(this)
                .size(50)
                .build()
        )
        recyclerView.adapter = adapter
        recyclerView.addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                val videoPlayer: EasyMediaPlayer = view.findViewById<EasyMediaPlayer>(R.id.videoPlay)
                VideoListUtils.onRecyclerAutoTiny(videoPlayer, false)
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                val videoPlayer: EasyMediaPlayer = view.findViewById<EasyMediaPlayer>(R.id.videoPlay)
                VideoListUtils.onRecyclerAutoTiny(videoPlayer, true)
            }
        })
        httpVideos
    }

    override fun onBackPressed() {
        if (VideoScreenUtils.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        VideoUtils.onPause()
    }

    override fun onResume() {
        super.onResume()
        VideoUtils.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        VideoUtils.onDestroy()
    }

    val httpVideos: Unit
        get() {
            val callback: AbsCallback<HuoShanData> = object : AbsCallback<HuoShanData>() {
                override fun onSuccess(responseBody: HuoShanData) {
                    if (responseBody.result == 1) {
                        listDatas.clear()
                        responseBody.feeds?.let { listDatas.addAll(it) }
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            val param = HttpRequest(VideoUrl.HUOSAHN)
            param.addParam("type", 7)
            param.addParam("page", 1)
            param.addParam("coldStart", false)
            param.addParam("count", "20")
            param.addParam("pv", false)
            param.addParam("id", 22)
            param.addParam("refreshTimes", "19")
            param.addParam("pcursor", "")
            param.addParam("source", "1")
            param.addParam("os", "android")
            param.addParam("__NStokensig", "dfdce17677d391c9cc1938be5cc71d831289523c3b669d9316abb3b49bfe33fc")
            param.addParam("token", "6f1730ea9c77425ab1ab622fea69ce37-1068800036")
            param.addParam("sig", "26737bcea65596c1f71d9243f45b690b")
            param.addParam("client_key", "3c2cd3f3")
            OkHttpManage.request(param).execute(callback)
        }
}