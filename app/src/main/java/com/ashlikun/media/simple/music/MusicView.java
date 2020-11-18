package com.ashlikun.media.simple.music;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ashlikun.media.music.BaseEasyMusicPlay;
import com.ashlikun.media.video.VideoData;
import com.ashlikun.media.video.status.VideoStatus;

import java.util.List;

/**
 * 作者　　: 李坤
 * 创建时间: 2020/11/18　16:57
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public class MusicView extends BaseEasyMusicPlay {

    public MusicView(@NonNull Context context) {
        super(context);
    }

    public MusicView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MusicView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    public boolean setDataSource(List<VideoData> mediaData, int defaultIndex) {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentState() == VideoStatus.PLAYING) {
                    onPause();
                } else {
                    play();
                }
            }
        });
        return super.setDataSource(mediaData, defaultIndex);
    }

}
