package com.ashlikun.media.simple;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ashlikun.adapter.ViewHolder;
import com.ashlikun.adapter.recyclerview.CommonAdapter;
import com.ashlikun.glideutils.GlideUtils;
import com.ashlikun.media.EasyVideoPlayer;
import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.simple.data.NeiHanData;
import com.ashlikun.media.simple.divider.HorizontalDividerItemDecoration;
import com.ashlikun.media.status.MediaScreenStatus;
import com.ashlikun.okhttputils.http.OkHttpUtils;
import com.ashlikun.okhttputils.http.SimpleCallback;
import com.ashlikun.okhttputils.http.request.RequestParam;
import com.ashlikun.okhttputils.http.response.HttpResponse;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/13　14:36
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */

public class ActivityRecyclerView extends AppCompatActivity {
    RecyclerView recyclerView;
    List<NeiHanData> listDatas = new ArrayList<>();
    CommonAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this)
                .size(50)
                .build());
        recyclerView.setAdapter(adapter = new CommonAdapter<NeiHanData>(this, R.layout.item_list, listDatas) {
            @Override
            public void convert(ViewHolder holder, NeiHanData s) {
                EasyVideoPlayer videoPlayer = holder.getView(R.id.videoPlay);
                videoPlayer.setVideoRatio(s.getWidth(), s.getHeigth());
                videoPlayer.setCurrentScreen(MediaScreenStatus.SCREEN_WINDOW_LIST);
                videoPlayer.setDataSource(s.getVideoUrl(), s.getText());
                GlideUtils.show(videoPlayer.getThumbImageView(), "http://p3.pstatp.com/" + s.getImageUrl());
            }
        });
        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                EasyVideoPlayer videoPlayer = view.findViewById(R.id.videoPlay);
                MediaUtils.onRecyclerAutoTiny(videoPlayer, false);
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                EasyVideoPlayer videoPlayer = view.findViewById(R.id.videoPlay);
                MediaUtils.onRecyclerAutoTiny(videoPlayer, true);
            }
        });
        getHttpVideos();
    }


    @Override
    public void onBackPressed() {
        if (MediaUtils.backPress()) {
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
        SimpleCallback<HttpResponse> callback = new SimpleCallback<HttpResponse>() {
            @Override
            public void onSuccess(HttpResponse responseBody) {
                if ("success".equals(responseBody.getStringValue("message"))) {
                    try {
                        List<NeiHanData> data = responseBody.getValue(new TypeToken<List<NeiHanData>>() {
                        }.getType(), "data", "data");
                        for (int i = 0; i < data.size(); i++) {
                            if (data.get(i).type != 1) {
                                data.remove(i);
                                i--;
                            }
                        }
                        listDatas.addAll(data);
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        RequestParam param = new RequestParam(VideoUrl.NEIHAN_SERVICE_API);
        OkHttpUtils.getInstance().execute(param, callback);
    }
}
