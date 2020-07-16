package com.ashlikun.media.video;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import com.ashlikun.media.R;
import com.ashlikun.media.video.play.EasyVideoIjkplayer;
import com.ashlikun.media.video.status.VideoDisplayType;
import com.ashlikun.media.video.view.BaseEasyVideoPlay;
import com.ashlikun.media.video.view.EasyVideoPlayer;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/13 16:03
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器工具
 */

public class VideoUtils {
    public static Context mContext;
    public static boolean isDebug = true;
    public static Class<? extends EasyMediaInterface> mMediaPlayClass;
    private static EasyVideoIjkplayer.OnCreateIjkplay onCreateIjkplay;


    public static final String EASY_MEDIA_PROGRESS = "EASY_MEDIA_PROGRESS";
    /**
     * 更新进度的定时器
     */
    private static ScheduledExecutorService POOL_SCHEDULE;
    private static Handler mHandler;

    /**
     * 在Applicable里面初始化
     *
     * @param context
     * @param mediaPlayClass 设置播放器,默认为系统播放器
     */
    public static void init(Application context, Class<? extends EasyMediaInterface> mediaPlayClass) {
        VideoUtils.mContext = context;
        VideoUtils.mMediaPlayClass = mediaPlayClass;
    }

    public static void setOnCreateIjkplay(EasyVideoIjkplayer.OnCreateIjkplay onCreateIjkplay) {
        VideoUtils.onCreateIjkplay = onCreateIjkplay;
    }

    public static EasyVideoIjkplayer.OnCreateIjkplay getOnCreateIjkplay() {
        return onCreateIjkplay;
    }

    public static void setIsDebug(boolean isDebug) {
        VideoUtils.isDebug = isDebug;
    }

    public static Handler getMainHander() {
        if (mHandler == null) {
            synchronized (VideoUtils.class) {
                if (mHandler == null) {
                    mHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return mHandler;
    }

    public static ScheduledExecutorService POOL_SCHEDULE() {
        if (POOL_SCHEDULE == null) {
            synchronized (VideoUtils.class) {
                if (POOL_SCHEDULE == null) {
                    POOL_SCHEDULE = new ScheduledThreadPoolExecutor(4);
                }
            }
        }
        return POOL_SCHEDULE;
    }

    public static String stringForTime(int timeMs) {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * 获取activity
     */
    public static Activity getActivity(Context context) {
        if (context == null) {
            return null;
        }

        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) context).getBaseContext());
        } else if (context instanceof ContextThemeWrapper) {
            return getActivity(((ContextThemeWrapper) context).getBaseContext());
        }

        return null;
    }

    /**
     * 获取Activity的跟布局DecorView
     *
     * @param context
     * @return
     */
    public static ViewGroup getDecorView(Context context) {
        return (ViewGroup) (VideoUtils.getActivity(context)).getWindow().getDecorView();
    }

    /**
     * 获取activity
     *
     * @param context
     * @return
     */
    public static AppCompatActivity getAppCompActivity(Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof AppCompatActivity) {
            return (AppCompatActivity) context;
        } else if (context instanceof ContextThemeWrapper) {
            return getAppCompActivity(((ContextThemeWrapper) context).getBaseContext());
        }
        return null;
    }

    public static void setRequestedOrientation(Context context, int orientation) {
        getActivity(context).setRequestedOrientation(
                orientation);
    }

    public static Window getWindow(Context context) {
        if (getAppCompActivity(context) != null) {
            return getAppCompActivity(context).getWindow();
        } else {
            return getActivity(context).getWindow();
        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 保存播放进度
     *
     * @param context
     * @param url      对应的Url
     * @param progress 进度
     */
    public static void saveProgress(Context context, VideoData url, int progress) {
        if (!EasyVideoPlayer.SAVE_PROGRESS) {
            return;
        }
        if (progress < 5000) {
            progress = 0;
        }
        SharedPreferences spn = context.getSharedPreferences(EASY_MEDIA_PROGRESS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spn.edit();
        editor.putInt(url.toString(), progress);
        editor.apply();
    }

    /**
     * 获取保存的进度
     *
     * @param context
     * @param url
     * @return
     */
    public static int getSavedProgress(Context context, Object url) {
        if (!EasyVideoPlayer.SAVE_PROGRESS) {
            return 0;
        }
        SharedPreferences spn;
        spn = context.getSharedPreferences("",
                Context.MODE_PRIVATE);
        return spn.getInt(url.toString(), 0);
    }

    /**
     * 清空进度
     */
    public static void clearProgress(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            SharedPreferences spn = context.getSharedPreferences(EASY_MEDIA_PROGRESS,
                    Context.MODE_PRIVATE);
            spn.edit().clear().apply();
        } else {
            SharedPreferences spn = context.getSharedPreferences(EASY_MEDIA_PROGRESS,
                    Context.MODE_PRIVATE);
            spn.edit().putInt(url, 0).apply();
        }
    }

    /**
     * 从播放数组里面获取当前播放的
     */
    public static VideoData getCurrentMediaData(List<VideoData> dataSource, int index) {
        if (dataSource == null || index < 0) {
            return null;
        }
        if (dataSource.size() > index) {
            return dataSource.get(index);
        }
        return null;
    }

    /**
     * 当前的播放数组是否包含正在播放的url
     */
    public static boolean isContainsUri(List<VideoData> dataSource, VideoData object) {
        if (dataSource == null || object == null) {
            return false;
        }
        for (VideoData o : dataSource) {
            return object == o || object.equals(o);
        }
        return false;
    }

    /**
     * 设置播放的事件回掉
     *
     * @param easyMediaAction 会一直持有这个对象，在application里面调用
     */
    public static void setEasyMediaAction(EasyVideoAction easyMediaAction) {
        if (easyMediaAction instanceof Application) {
            EasyMediaManager.getInstance().MEDIA_EVENT = easyMediaAction;
        }
    }

    /**
     * 释放video
     */
    public static void releaseAllVideos() {
        //这里判断，防止播放器点击全屏（这种清空不能释放）
        if (VideoScreenUtils.isBackOk()) {
            //把之前的设置到完成状态
            EasyVideoPlayerManager.completeAll();
            //释放播放器
            EasyMediaManager.getInstance().releaseMediaPlayer();
        }
    }

    /**
     * 对应activity得生命周期
     */
    public static void onPause() {
        BaseEasyVideoPlay videoPlayer = EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny();
        if (videoPlayer != null) {
            videoPlayer.onPause();
        }
    }

    /**
     * 对应activity得生命周期
     */
    public static void onResume() {
        BaseEasyVideoPlay videoPlayer = EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny();
        if (videoPlayer != null) {
            videoPlayer.onResume();
        }
    }

    /**
     * 对应activity得生命周期
     */
    public static void onDestroy() {
        //把之前的设置到完成状态
        EasyVideoPlayerManager.completeAll();
        //释放播放器
        EasyMediaManager.getInstance().releaseMediaPlayer();
        //释放渲染器
        EasyMediaManager.getInstance().releaseAllSufaceView();
    }

    public static void setAudioFocus(Context context, boolean isFocus) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (isFocus) {
            mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        } else {
            mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        }

    }

    /**
     * 显示非wifi播放提示
     *
     * @param context
     * @param play
     * @return 是否弹窗
     */
    public static boolean showWifiDialog(Context context, VideoData data, final BaseEasyVideoPlay play) {
        if (!data.isLocal() &&
                !NetworkUtils.isWifiConnected(context) && !EasyMediaManager.getInstance().WIFI_ALLOW_PLAY) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(context.getResources().getString(R.string.easy_video_tips_not_wifi));
            builder.setPositiveButton(context.getResources().getString(R.string.easy_video_tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EasyMediaManager.getInstance().WIFI_ALLOW_PLAY = true;
                    dialog.dismiss();
                    play.onEvent(EasyVideoAction.ON_CLICK_START_NO_WIFI_GOON);
                    play.startVideo();
                }
            });
            builder.setNegativeButton(context.getResources().getString(R.string.easy_video_tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (play.isScreenFull()) {
                        VideoScreenUtils.clearFullscreenLayout(play.getContext());
                    }
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (play.isScreenFull()) {
                        dialog.dismiss();
                        VideoScreenUtils.clearFullscreenLayout(play.getContext());
                    }
                }
            });
            builder.create().show();
            return true;
        }
        return false;
    }

    public static AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {//是否新建个class，代码更规矩，并且变量的位置也很尴尬
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    VideoUtils.releaseAllVideos();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (EasyMediaManager.isPlaying()) {
                        EasyMediaManager.pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };

    /**
     * 设置TextureView显示角度
     *
     * @param type
     */
    public static void setVideoImageDisplayType(@VideoDisplayType.Code int type) {
        if (EasyMediaManager.getTextureView() != null) {
            EasyMediaManager.getTextureView().setDisplayType(type);
            EasyMediaManager.getTextureView().requestLayout();
        }
    }

    /**
     * 设置TextureView旋转
     *
     * @param rotation
     */
    public static void setTextureViewRotation(int rotation) {
        if (EasyMediaManager.getTextureView() != null) {
            EasyMediaManager.getTextureView().setRotation(rotation);
        }
    }


    public static void d(String content) {
        if (!isDebug) {
            return;
        }
        String tag = generateTag();
        Log.d(tag, content);
    }

    /**
     * 得到标签,log标签+类名+方法名+第几行
     *
     * @return
     */
    private static String generateTag() {
        StackTraceElement caller = new Throwable().getStackTrace()[2];
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        return tag;
    }
}
