package com.ashlikun.media.simple;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ashlikun.adapter.ViewHolder;
import com.ashlikun.adapter.recyclerview.CommonAdapter;
import com.ashlikun.media.simple.data.DouyingData;
import com.ashlikun.media.video.VideoScreenUtils;
import com.ashlikun.media.video.VideoUtils;
import com.ashlikun.media.video.view.MiniVideoPlay;
import com.ashlikun.okhttputils.http.OkHttpUtils;
import com.ashlikun.okhttputils.http.callback.AbsCallback;
import com.ashlikun.okhttputils.http.request.HttpRequest;
import com.ashlikun.xlayoutmanage.viewpager.OnViewPagerListener;
import com.ashlikun.xlayoutmanage.viewpager.ViewPagerLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author　　: 李坤
 * 创建时间: 2019/4/4 16:45
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：抖音
 */

public class DouyinActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<DouyingData.AwemeListData> listDatas = new ArrayList<>();
    CommonAdapter adapter;
    ViewPagerLayoutManager manager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(manager = new ViewPagerLayoutManager(this, ViewPagerLayoutManager.VERTICAL));
        manager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            public void onInitComplete() {
            }

            @Override
            public void onPageRelease(boolean isNext, int position) {

            }

            @Override
            public void onPageSelected(int position, boolean isBottom) {
                ViewHolder holder = (ViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(0));
                MiniVideoPlay videoPlayer = holder.getView(R.id.videoPlay);
                if (!videoPlayer.isCurrentPlay()) {
                    videoPlayer.startVideo();
                }
            }
        });
        recyclerView.setAdapter(adapter = new CommonAdapter<DouyingData.AwemeListData>(this, R.layout.item_douyin, listDatas) {
            @Override
            public void convert(ViewHolder holder, DouyingData.AwemeListData s) {
                MiniVideoPlay videoPlayer = holder.getView(R.id.videoPlay);
//                videoPlayer.setControllerVisiable(false);
//                GlideUtils.show(videoPlayer.getThumbImageView(), s.getImageUrl());
//                videoPlayer.setVideoRatio(1);
                videoPlayer.setDataSource(s.getUrl(), s.getText());
            }
        });
        adapter.setOffClickEffects();
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
        getHttpVideos();
    }


    @Override
    public void onBackPressed() {
        if (VideoScreenUtils.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VideoUtils.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VideoUtils.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoUtils.onDestroy();
    }

    public void getHttpVideos() {
        AbsCallback<DouyingData> callback = new AbsCallback<DouyingData>() {
            @Override
            public void onSuccess(DouyingData responseBody) {
                if (responseBody.status_code == 0) {
                    listDatas.clear();
                    listDatas.addAll(responseBody.aweme_list);
                    adapter.notifyDataSetChanged();
                }
            }
        };
        HttpRequest param = HttpRequest.get(VideoUrl.DOYYIN);
        OkHttpUtils.request(param).execute(callback);
    }
}
