package com.ashlikun.media.simple.music;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ashlikun.media.music.BaseEasyMusicPlay;

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

}
