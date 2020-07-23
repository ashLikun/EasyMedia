package com.ashlikun.media.simple;

import android.app.Application;

import com.ashlikun.media.exoplay2.play.EasyVideoExo2;
import com.ashlikun.media.video.VideoUtils;
import com.ashlikun.okhttputils.http.OkHttpUtils;
import com.ashlikun.orm.LiteOrmUtil;

/**
 * 作者　　: 李坤
 * 创建时间: 2020/7/16　14:07
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //数据库
        LiteOrmUtil.init(this);
        OkHttpUtils.init(null);
        VideoUtils.init(this, EasyVideoExo2.class);

        VideoUtils.setOnCreateIjkplay(ijkMediaPlayer -> {
//            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http_proxy", "http://192.168.1.6:8888");
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 100);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
            //是否开启预缓冲，一般直播项目会开启，达到秒开的效果，不过带来了播放丢帧卡顿的体验
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 20000);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1316);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "infbuf", 1);  // 无限读
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_timeout", -1);
        });
    }
}
