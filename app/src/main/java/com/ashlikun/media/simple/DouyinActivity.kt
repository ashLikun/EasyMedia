package com.ashlikun.media.simple

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ashlikun.adapter.ViewHolder
import com.ashlikun.adapter.recyclerview.CommonAdapter
import com.ashlikun.glideutils.GlideUtils
import com.ashlikun.media.simple.data.DouyingData
import com.ashlikun.media.simple.data.DouyingData.AwemeListData
import com.ashlikun.media.video.VideoData
import com.ashlikun.media.video.VideoScreenUtils
import com.ashlikun.media.video.VideoUtils
import com.ashlikun.media.video.view.EasyMediaPlayer
import com.ashlikun.media.video.view.MiniMediaPlay
import com.ashlikun.okhttputils.http.OkHttpManage
import com.ashlikun.okhttputils.http.callback.AbsCallback
import com.ashlikun.okhttputils.http.request.HttpRequest
import com.ashlikun.xlayoutmanage.viewpager.OnViewPagerListener
import com.ashlikun.xlayoutmanage.viewpager.ViewPagerLayoutManager
import okhttp3.Response

/**
 * @author　　: 李坤
 * 创建时间: 2019/4/4 16:45
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：抖音
 */
class DouyinActivity : AppCompatActivity() {
    val recyclerView: RecyclerView by lazy {
        findViewById<View>(R.id.recyclerView) as RecyclerView
    }
    var listDatas: MutableList<VideoData> = ArrayList()
    val adapter by lazy {
        object : CommonAdapter<VideoData>(this, R.layout.item_douyin, listDatas) {
            override fun convert(holder: ViewHolder, s: VideoData) {
                val videoPlayer = holder.getView<MiniMediaPlay>(R.id.videoPlay)
                //                videoPlayer.setControllerVisiable(false);
//                GlideUtils.show(videoPlayer.getThumbImageView(), s.getImageUrl());
//                videoPlayer.setVideoRatio(1);
                videoPlayer.setDataSource(s.url, s.title)
            }
        }
    }
    var manager: ViewPagerLayoutManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        recyclerView.layoutManager = ViewPagerLayoutManager(this, ViewPagerLayoutManager.VERTICAL).also { manager = it }
        recyclerView.adapter = adapter
        manager!!.setOnViewPagerListener(object : OnViewPagerListener {
            override fun onInitComplete() {}
            override fun onPageRelease(isNext: Boolean, position: Int) {}
            override fun onPageSelected(position: Int, isBottom: Boolean) {
                val holder = recyclerView!!.getChildViewHolder(recyclerView!!.getChildAt(0)) as ViewHolder
                val videoPlayer = holder.getView<MiniMediaPlay>(R.id.videoPlay)
                if (!videoPlayer.isCurrentPlay) {
                    videoPlayer.startVideo()
                }
            }
        })
        adapter.setOffClickEffects()
        //        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
//            @Override
//            public void onChildViewAttachedToWindow(View view) {
//                EasyVideoPlayer videoPlayer = view.findViewById(R.id.videoPlay);
//                MediaListUtils.onRecyclerAutoTiny(videoPlayer, false);
//            }
//
//            @Override
//            public void onChildViewDetachedFromWindow(View view) {
//                EasyVideoPlayer videoPlayer = view.findViewById(R.id.videoPlay);
//                MediaListUtils.onRecyclerAutoTiny(videoPlayer, true);
//            }
//        });
        VideoUrl.douyinlist.forEach {
            listDatas.add(VideoData(it, "测试"))
        }
        adapter.notifyDataSetChanged()
        val callback: AbsCallback<DouyingData> = object : AbsCallback<DouyingData>() {
            override fun onSuccess(responseBody: DouyingData) {
                if (responseBody.status_code == 0) {
//                    listDatas.clear()
                    responseBody.aweme_list?.let {
                        //循环检查地址
                        it.forEach {
                            Log.e("播放地址", it.url)
//                            listDatas.add(VideoData(it.url, it.desc.orEmpty()))
                        }
                    }
//                    adapter.notifyDataSetChanged()
                }
            }
        }
        OkHttpManage.get(VideoUrl.DOYYIN).execute(callback)
    }


    override fun onBackPressed() {
        if (!VideoScreenUtils.backPress()) {
            super.onBackPressed()
        }
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
}