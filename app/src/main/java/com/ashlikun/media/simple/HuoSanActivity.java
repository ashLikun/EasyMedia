package com.ashlikun.media.simple;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashlikun.adapter.ViewHolder;
import com.ashlikun.adapter.recyclerview.CommonAdapter;
import com.ashlikun.media.video.VideoListUtils;
import com.ashlikun.media.video.VideoScreenUtils;
import com.ashlikun.media.video.VideoUtils;
import com.ashlikun.media.simple.data.HuoShanData;
import com.ashlikun.media.simple.divider.HorizontalDividerItemDecoration;
import com.ashlikun.media.video.view.EasyVideoPlayer;
import com.ashlikun.okhttputils.http.OkHttpUtils;
import com.ashlikun.okhttputils.http.callback.AbsCallback;
import com.ashlikun.okhttputils.http.request.HttpRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/13　14:36
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：火山小视频
 */

public class HuoSanActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<HuoShanData.FeedsData> listDatas = new ArrayList<>();
    CommonAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this)
                .size(50)
                .build());
        recyclerView.setAdapter(adapter = new CommonAdapter<HuoShanData.FeedsData>(this, R.layout.item_list, listDatas) {
            @Override
            public void convert(ViewHolder holder, HuoShanData.FeedsData s) {
                EasyVideoPlayer videoPlayer = holder.getView(R.id.videoPlay);
//                GlideUtils.show(videoPlayer.getThumbImageView(), s.getImageUrl());
                videoPlayer.setVideoRatio(s.getWidth() / s.getHeight());
                videoPlayer.setDataSource(s.getUrl(), s.getText());
            }
        });
        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                EasyVideoPlayer videoPlayer = view.findViewById(R.id.videoPlay);
                VideoListUtils.onRecyclerAutoTiny(videoPlayer, false);
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                EasyVideoPlayer videoPlayer = view.findViewById(R.id.videoPlay);
                VideoListUtils.onRecyclerAutoTiny(videoPlayer, true);
            }
        });
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
