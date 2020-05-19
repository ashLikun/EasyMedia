package com.ashlikun.media.video.controller;

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

import com.ashlikun.media.R;
import com.ashlikun.media.video.VideoData;
import com.ashlikun.media.video.VideoScreenUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/7　15:25
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：控制器顶部
 */

public class VideoControllerTop extends RelativeLayout implements View.OnClickListener {
    //当前系统时间
    public TextView videoCurrentTime;
    public TextView titleView;
    //电源管理
    public ImageView batteryLevel;
    public ImageView backButton;
    public ViewGroup batteryTimeLayout;

    /**
     * 非全屏时候返回键隐藏的时候预留左边空间
     */
    private int backGoneLeftSize = 0;
    private int defaultBackGoneLeftSize = 0;

    public VideoControllerTop(Context context) {
        this(context, null);
    }

    public VideoControllerTop(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoControllerTop(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setBackgroundResource(R.drawable.easy_video_title_bg);
        LayoutInflater.from(getContext()).inflate(R.layout.easy_video_layout_controller_top, this);
        videoCurrentTime = findViewById(R.id.video_current_time);
        batteryLevel = findViewById(R.id.battery_level);
        titleView = findViewById(R.id.title);
        backButton = findViewById(R.id.back);
        batteryTimeLayout = findViewById(R.id.battery_time_layout);
        defaultBackGoneLeftSize = ((MarginLayoutParams) titleView.getLayoutParams()).leftMargin;
        backGoneLeftSize = defaultBackGoneLeftSize;
        backButton.setOnClickListener(this);
    }

    public void setInitData(VideoData mediaData) {
        if (mediaData != null) {
            setTitle(mediaData.getTitle());
        }

    }

    public void setFull(boolean isFull) {
        if (isFull) {
            setSystemTimeAndBattery();
            setBackIsShow(true);
            setBatteryIsShow(true);
        } else {
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
        if (isShow) {
            backButton.setVisibility(VISIBLE);
            ((LayoutParams) titleView.getLayoutParams()).leftMargin = defaultBackGoneLeftSize;
        } else {
            ((LayoutParams) titleView.getLayoutParams()).leftMargin = backGoneLeftSize;
            backButton.setVisibility(GONE);
        }
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
                    batteryLevel.setBackgroundResource(R.mipmap.easy_video_battery_level_10);
                } else if (percent >= 15 && percent < 40) {
                    batteryLevel.setBackgroundResource(R.mipmap.easy_video_battery_level_30);
                } else if (percent >= 40 && percent < 60) {
                    batteryLevel.setBackgroundResource(R.mipmap.easy_video_battery_level_50);
                } else if (percent >= 60 && percent < 80) {
                    batteryLevel.setBackgroundResource(R.mipmap.easy_video_battery_level_70);
                } else if (percent >= 80 && percent < 95) {
                    batteryLevel.setBackgroundResource(R.mipmap.easy_video_battery_level_90);
                } else if (percent >= 95 && percent <= 100) {
                    batteryLevel.setBackgroundResource(R.mipmap.easy_video_battery_level_100);
                }
                getContext().unregisterReceiver(battertReceiver);
                battertReceiver.setDebugUnregister(false);
            }
        }
    };

    /**
     * 非全屏时候返回键隐藏的时候预留左边空间
     *
     * @param backGoneLeftSize
     */
    public void setBackGoneLeftSize(int backGoneLeftSize) {
        this.backGoneLeftSize = backGoneLeftSize;
        setBackIsShow(backButton.getVisibility() == VISIBLE);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back) {
            VideoScreenUtils.backPress();
        }
    }
}
