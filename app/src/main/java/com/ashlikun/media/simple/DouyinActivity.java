package com.ashlikun.media.simple;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.ashlikun.adapter.ViewHolder;
import com.ashlikun.adapter.recyclerview.CommonAdapter;
import com.ashlikun.media.MediaScreenUtils;
import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.simple.data.HuoShanData;
import com.ashlikun.media.view.MiniVideoPlay;
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
    List<HuoShanData.FeedsData> listDatas = new ArrayList<>();
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
        recyclerView.setAdapter(adapter = new CommonAdapter<HuoShanData.FeedsData>(this, R.layout.item_douyin, listDatas) {
            @Override
            public void convert(ViewHolder holder, HuoShanData.FeedsData s) {
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
        if (MediaScreenUtils.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaUtils.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaUtils.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaUtils.releaseAllVideos();
    }

    public void getHttpVideos() {
        AbsCallback<HuoShanData> callback = new AbsCallback<HuoShanData>() {
            @Override
            public void onSuccess(HuoShanData responseBody) {
                if (responseBody.result == 1) {
                    listDatas.clear();
                    listDatas.addAll(responseBody.feeds);
                    adapter.notifyDataSetChanged();
                }
            }
        };
        HttpRequest param = new HttpRequest(VideoUrl.HUOSAHN);
        param.addParam("type", 7);
        param.addParam("page", 1);
        param.addParam("coldStart", false);
        param.addParam("count", "20");
        param.addParam("pv", false);
        param.addParam("id", 22);
        param.addParam("refreshTimes", "19");
        param.addParam("pcursor", "");
        param.addParam("source", "1");
        param.addParam("os", "android");
        param.addParam("__NStokensig", "dfdce17677d391c9cc1938be5cc71d831289523c3b669d9316abb3b49bfe33fc");
        param.addParam("token", "6f1730ea9c77425ab1ab622fea69ce37-1068800036");
        param.addParam("sig", "26737bcea65596c1f71d9243f45b690b");
        param.addParam("client_key", "3c2cd3f3");
        OkHttpUtils.request(param).execute(callback);
    }
}
