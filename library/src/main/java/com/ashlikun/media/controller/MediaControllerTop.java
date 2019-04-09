package com.ashlikun.media.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ashlikun.media.MediaData;
import com.ashlikun.media.MediaScreenUtils;
import com.ashlikun.media.R;
import com.ashlikun.media.status.MediaViewType;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/7　15:25
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：控制器顶部
 */

public class MediaControllerTop extends RelativeLayout implements View.OnClickListener {
    //当前系统时间
    public TextView videoCurrentTime;
    public TextView titleView;
    //电源管理
    public ImageView batteryLevel;
    public ImageView backButton;
    public ViewGroup batteryTimeLayout;

    public MediaControllerTop(Context context) {
        this(context, null);
    }

    public MediaControllerTop(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaControllerTop(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setBackgroundResource(R.drawable.easy_media_title_bg);
        LayoutInflater.from(getContext()).inflate(R.layout.easy_layout_controller_top, this);
        videoCurrentTime = findViewById(R.id.video_current_time);
        batteryLevel = findViewById(R.id.battery_level);
        titleView = findViewById(R.id.title);
        backButton = findViewById(R.id.back);
        batteryTimeLayout = findViewById(R.id.battery_time_layout);

        backButton.setOnClickListener(this);
    }

    public void setInitData(MediaData mediaData, int screen) {
        if (mediaData != null) {
            setTitle(mediaData.getTitle());
        }
        if (screen == MediaViewType.FULLSCREEN) {
            setSystemTimeAndBattery();
            setBackIsShow(true);
            setBatteryIsShow(true);
        } else if (screen == MediaViewType.NORMAL || screen == MediaViewType.LIST) {
            setBackIsShow(false);
            setBatteryIsShow(false);
        }
    }

    //设置系统的时间和电量
    public void setSystemTimeAndBattery() {
        SimpleDateFormat dateFormater = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        videoCurrentTime.setText(dateFormater.format(date));
        if (!battertReceiver.getDebugUnregister()) {
            battertReceiver.setDebugUnregister(true);
            getContext().registerReceiver(
                    battertReceiver,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            );
        }
    }

    public void setTitle(String title) {
        if (title != null) {
            titleView.setText(title);
        }
    }

    public void setBackIsShow(boolean isShow) {
        backButton.setVisibility(isShow ? VISIBLE : GONE);
    }

    public void setBatteryIsShow(boolean isShow) {
        batteryTimeLayout.setVisibility(isShow ? VISIBLE : GONE);
    }

    //电量
    private BroadcastReceiver battertReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                int percent = level * 100 / scale;
                if (percent < 15) {
                    batteryLevel.setBackgroundResource(R.mipmap.easy_media_battery_level_10);
                } else if (percent >= 15 && percent < 40) {
                    batteryLevel.setBackgroundResource(R.mipmap.easy_media_battery_level_30);
                } else if (percent >= 40 && percent < 60) {
                    batteryLevel.setBackgroundResource(R.mipmap.easy_media_battery_level_50);
                } else if (percent >= 60 && percent < 80) {
                    batteryLevel.setBackgroundResource(R.mipmap.easy_media_battery_level_70);
                } else if (percent >= 80 && percent < 95) {
                    batteryLevel.setBackgroundResource(R.mipmap.easy_media_battery_level_90);
                } else if (percent >= 95 && percent <= 100) {
                    batteryLevel.setBackgroundResource(R.mipmap.easy_media_battery_level_100);
                }
                getContext().unregisterReceiver(battertReceiver);
                battertReceiver.setDebugUnregister(false);
            }
        }
    };


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back) {
            MediaScreenUtils.backPress();
        }
    }
}
