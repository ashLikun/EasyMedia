package com.ashlikun.media;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.ashlikun.media.status.MediaStatus;
import com.ashlikun.media.view.EasyBaseVideoPlay;
import com.ashlikun.media.view.EasyVideoPlayTiny;
import com.ashlikun.media.view.EasyVideoPlayer;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_FULLSCREEN;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_AUTO_COMPLETE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_ERROR;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_NORMAL;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PAUSE;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/13 16:03
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */

public class MediaUtils {
    public static Context mContext;
    //是否存在 FLAG_FULLSCREEN
    public static boolean FLAG_FULLSCREEN_EXIST = true;
    //当onResume的时候是否去播放
    private static boolean ONRESUME_TO_PLAY = true;
    public static long CLICK_QUIT_FULLSCREEN_TIME = 0;
    public static final int FULL_SCREEN_NORMAL_DELAY = 300;
    public static final String EASY_MEDIA_PROGRESS = "EASY_MEDIA_PROGRESS";
    //更新进度的定时器
    private static ScheduledExecutorService POOL_SCHEDULE;
    private static Handler mHandler;

    //在Applicable里面初始化
    public static void init(Context context) {
        MediaUtils.mContext = context.getApplicationContext();
    }

    public static Handler getMainHander() {
        if (mHandler == null) {
            synchronized (MediaUtils.class) {
                if (mHandler == null) {
                    mHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return mHandler;
    }

    public static ScheduledExecutorService POOL_SCHEDULE() {
        if (POOL_SCHEDULE == null) {
            synchronized (MediaUtils.class) {
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
     * 是否有wifi
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }


    /**
     * 获取activity
     *
     * @param context
     * @return
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
//        return (MediaUtils.getActivity(context)).getWindow().getDecorView()
//                .findViewById(Window.ID_ANDROID_CONTENT);
        return (ViewGroup) (MediaUtils.getActivity(context)).getWindow().getDecorView();
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
    public static void saveProgress(Context context, MediaData url, int progress) {
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
    public static MediaData getCurrentMediaData(List<MediaData> dataSource, int index) {
        if (dataSource.size() > index) {
            return dataSource.get(index);
        }
        return null;
    }

    /**
     * 当前的播放数组是否包含正在播放的url
     */
    public static boolean isContainsUri(List<MediaData> dataSource, MediaData object) {
        if (dataSource == null || object == null) {
            return false;
        }
        for (Object o : dataSource) {
            return object == o || object.equals(o);
        }
        return false;
    }

    /**
     * 直接开始全屏播放
     *
     * @param easyVideoPlayer 请实例化一个播放器
     * @param url             地址  或者 AssetFileDescriptor
     * @param title           标题
     */
    public static void startFullscreen(EasyVideoPlayer easyVideoPlayer, String url, String title) {
        List<MediaData> mediaData = new ArrayList<>();
        mediaData.add(new MediaData.Builder()
                .url(url)
                .title(title)
                .builder());
        startFullscreen(easyVideoPlayer, mediaData, 0);
    }

    /**
     * 设置数据源
     *
     * @param data 视频ur
     */
    public static void startFullscreen(EasyVideoPlayer easyVideoPlayer, MediaData data) {
        List<MediaData> mediaData = new ArrayList<>();
        mediaData.add(data);
        startFullscreen(easyVideoPlayer, mediaData, 0);
    }

    public static void startFullscreen(EasyVideoPlayer easyVideoPlayer, List<MediaData> mediaData, int defaultIndex) {
        setActivityFullscreen(easyVideoPlayer.getContext(), true);
        MediaUtils.setRequestedOrientation(easyVideoPlayer.getContext(),
                easyVideoPlayer.isFullscreenPortrait() ? EasyVideoPlayer.ORIENTATION_FULLSCREEN_SENSOR : EasyVideoPlayer.ORIENTATION_FULLSCREEN_LANDSCAPE);
        ViewGroup vp = MediaUtils.getDecorView(easyVideoPlayer.getContext());
        View old = vp.findViewById(R.id.easy_media_fullscreen_id);
        if (old != null) {
            vp.removeView(old);
        }
        easyVideoPlayer.setId(R.id.easy_media_fullscreen_id);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        vp.addView(easyVideoPlayer, lp);
        Animation ra = AnimationUtils.loadAnimation(easyVideoPlayer.getContext(), R.anim.start_fullscreen);
        easyVideoPlayer.setAnimation(ra);
        easyVideoPlayer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
        easyVideoPlayer.setCurrentScreen(SCREEN_WINDOW_FULLSCREEN);
        int status = easyVideoPlayer.getCurrentState();
        easyVideoPlayer.setDataSource(mediaData, defaultIndex);
        easyVideoPlayer.setState(status);
        if (easyVideoPlayer.getCurrentState() == CURRENT_STATE_NORMAL) {
            easyVideoPlayer.onPlayStartClick();
        }
        CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
        EasyVideoPlayerManager.setVideoFullscreen(easyVideoPlayer);
    }

    /**
     * 开始小窗口播放
     */
    public static boolean startWindowTiny(EasyVideoPlayTiny tiny, List<MediaData> mediaData, int defaultIndex) {
        EasyVideoPlayer videoPlayerDefault = EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny();
        if (videoPlayerDefault != null && (videoPlayerDefault.getCurrentState() == CURRENT_STATE_NORMAL ||
                videoPlayerDefault.getCurrentState() == CURRENT_STATE_ERROR ||
                videoPlayerDefault.getCurrentState() == CURRENT_STATE_AUTO_COMPLETE)) {
            return false;
        }
        if (videoPlayerDefault != null) {
            videoPlayerDefault.removeTextureView();
        }
        tiny.setId(R.id.easy_media_tiny_id);

        tiny.setDataSource(mediaData, defaultIndex);
        tiny.addTextureView();
        EasyVideoPlayerManager.setVideoTiny(tiny);
        tiny.showWindow();
        if (videoPlayerDefault == null && !isContainsUri(mediaData, EasyMediaManager.getCurrentDataSource())) {
            tiny.play();
        } else {
            tiny.currentState = videoPlayerDefault.getCurrentState();
        }
        return true;

    }

    /**
     * 设置播放器,默认为系统播放器
     *
     * @param mediaInterface
     */
    public static void setMediaInterface(EasyMediaInterface mediaInterface) {
        EasyMediaManager.instance().mMediaPlay = mediaInterface;
    }

    /**
     * 设置播放的事件回掉
     *
     * @param easyMediaAction 会一直持有这个对象，在application里面调用
     */
    public static void setEasyMediaAction(EasyMediaAction easyMediaAction) {
        if (easyMediaAction instanceof Application) {
            EasyMediaManager.MEDIA_EVENT = easyMediaAction;
        }
    }

    //直接退出全屏和小窗
    public static void quitFullscreenOrTinyWindow() {
        if (EasyVideoPlayerManager.getVideoFullscreen() != null) {
            EasyVideoPlayerManager.getVideoFullscreen().clearFloatScreen();
        }
        if (EasyVideoPlayerManager.getVideoTiny() != null) {
            EasyVideoPlayerManager.getVideoTiny().cleanTiny();
        }
        EasyMediaManager.instance().releaseMediaPlayer();
        EasyVideoPlayerManager.completeAll();
    }

    //设置activity全屏
    public static void setActivityFullscreen(Context context, boolean isFullscreen) {
        if (isFullscreen) {
            int flag = MediaUtils.getWindow(context).getAttributes().flags;
            if ((flag & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
                FLAG_FULLSCREEN_EXIST = true;
            } else {
                FLAG_FULLSCREEN_EXIST = false;
            }
            if (!FLAG_FULLSCREEN_EXIST) {
                MediaUtils.getWindow(context).setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        } else {
            if (!FLAG_FULLSCREEN_EXIST) {
                MediaUtils.getWindow(context).clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }


    //返回键点击
    public static boolean backPress() {
        if ((System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) < FULL_SCREEN_NORMAL_DELAY) {
            return false;
        }
        CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
        if (EasyVideoPlayerManager.getVideoDefault() != null) {
            if (MediaUtils.isContainsUri(EasyVideoPlayerManager.getVideoDefault().getMediaData(), EasyMediaManager.getCurrentDataSource())) {
                //如果默认的Video播放过视频,就直接在这个默认的上面播放
                EasyVideoPlayerManager.getVideoDefault().onEvent(EasyMediaAction.ON_QUIT_TINYSCREEN);
                EasyVideoPlayerManager.getVideoDefault().playOnThisVideo();
            } else {
                //直接退出全屏或者小窗口
                MediaUtils.quitFullscreenOrTinyWindow();
            }
            return true;
        } else if (EasyVideoPlayerManager.getVideoFullscreen() != null || EasyVideoPlayerManager.getVideoTiny() != null) {
            MediaUtils.quitFullscreenOrTinyWindow();
            return true;
        }
        return false;
    }

    //释放video
    public static void releaseAllVideos() {
        if ((System.currentTimeMillis() - MediaUtils.CLICK_QUIT_FULLSCREEN_TIME) > MediaUtils.FULL_SCREEN_NORMAL_DELAY) {
            //把之前的设置到完成状态
            EasyVideoPlayerManager.completeAll();
            //释放播放器
            EasyMediaManager.instance().releaseMediaPlayer();
        }
    }

    /**
     * 当子view附属到窗口时候
     * 这2个方法是给列表使用的
     *
     * @param vidoPlayId 播放器控件的id
     */
    public static void onChildViewAttachedToWindow(View view, int vidoPlayId) {
        if (EasyVideoPlayerManager.getVideoTiny() != null) {
            EasyVideoPlayer videoPlayer = view.findViewById(vidoPlayId);
            if (videoPlayer != null && MediaUtils.getCurrentMediaData(videoPlayer.mediaData, videoPlayer.currentUrlIndex).equals(EasyMediaManager.getCurrentDataSource())) {
                MediaUtils.backPress();
            }
        }
    }

    //当子view从窗口分离
    public static void onChildViewDetachedFromWindow(View view) {
        if (EasyVideoPlayerManager.getVideoTiny() == null) {
            EasyVideoPlayer videoPlayer = EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny();
            if (((ViewGroup) view).indexOfChild(videoPlayer) != -1) {
                if (videoPlayer.getCurrentState() == CURRENT_STATE_PAUSE) {
                    releaseAllVideos();
                } else {
                    if (startWindowTiny(new EasyVideoPlayTiny(videoPlayer.getContext()), videoPlayer.mediaData, videoPlayer.currentUrlIndex)) {
                        videoPlayer.onStateNormal();
                    }
                }
            }
        }
    }


    /**
     * ListView列表滑动时候自动小窗口
     * RecyclerView请用recyclerView.addOnChildAttachStateChangeListener
     *
     * @param firstVisibleItem 第一个有效的Item
     * @param visibleItemCount 一共有效的Item
     */
    public static void onScrollAutoTiny(int currentPlayPosition, int firstVisibleItem, int visibleItemCount) {
        int lastVisibleItem = firstVisibleItem + visibleItemCount;
        if (currentPlayPosition >= 0) {
            if ((currentPlayPosition < firstVisibleItem || currentPlayPosition > (lastVisibleItem - 1))) {
                if (EasyVideoPlayerManager.getVideoTiny() == null) {
                    if (EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny().getCurrentState() == CURRENT_STATE_PAUSE) {
                        MediaUtils.releaseAllVideos();
                    } else {
                        EasyVideoPlayer videoPlayer = (EasyVideoPlayer) EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny();
                        startWindowTiny(new EasyVideoPlayTiny(videoPlayer.getContext()), videoPlayer.mediaData, videoPlayer.currentUrlIndex);
                        videoPlayer.onStateNormal();
                    }
                }
            } else {
                if (EasyVideoPlayerManager.getVideoTiny() != null) {
                    MediaUtils.backPress();
                }
            }
        }
    }

    /**
     * 列表滑动时候清空全部播放  ListView用的
     * RecyclerView请用recyclerView.addOnChildAttachStateChangeListener
     *
     * @param currentPlayPosition 当前正在播放的item
     * @param firstVisibleItem    第一个有效的Item
     * @param visibleItemCount    一共有效的Item
     */
    public static void onScrollReleaseAllVideos(int currentPlayPosition, int firstVisibleItem, int visibleItemCount) {
        int lastVisibleItem = firstVisibleItem + visibleItemCount;
        if (currentPlayPosition >= 0) {
            if ((currentPlayPosition < firstVisibleItem || currentPlayPosition > (lastVisibleItem - 1))) {
                MediaUtils.releaseAllVideos();
            }
        }
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2017/12/14 10:27
     * 邮箱　　：496546144@qq.com
     * <p>
     * 方法功能：RecyclerView列表滑动时候自动小窗口
     *
     * @param isChileViewDetached 子view是否分离 true:分离，false:添加
     */
    public static void onRecyclerAutoTiny(EasyVideoPlayer videoPlayer, boolean isChileViewDetached) {
        //如果当前的view播放的视频地址不是正在播放的视频地址就过滤掉这次
        if (videoPlayer == null || !MediaUtils.isContainsUri(videoPlayer.mediaData,
                EasyMediaManager.getCurrentDataSource())) {
            return;
        }
        if (isChileViewDetached) {
            if (EasyVideoPlayerManager.getVideoTiny() == null) {
                if (EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny().getCurrentState() == CURRENT_STATE_PAUSE) {
                    MediaUtils.releaseAllVideos();
                } else {
                    if (startWindowTiny(new EasyVideoPlayTiny(videoPlayer.getContext()), videoPlayer.mediaData, videoPlayer.currentUrlIndex)) {
                        videoPlayer.onStateNormal();
                    }

                }
            }
        } else if (EasyVideoPlayerManager.getVideoTiny() != null) {
            MediaUtils.backPress();
        }
    }

    /**
     * RecyclerView滑动时候清空全部播放
     * RecyclerView请用recyclerView.addOnChildAttachStateChangeListener
     *
     * @param videoPlayer 当前子view播放器
     */
    public static void onRecyclerRelease(EasyVideoPlayer videoPlayer) {
        //如果当前的view播放的视频地址不是正在播放的视频地址就过滤掉这次
        if (videoPlayer == null || !MediaUtils.isContainsUri(videoPlayer.mediaData,
                EasyMediaManager.getCurrentDataSource())) {
            return;
        }
        MediaUtils.releaseAllVideos();
    }


    //对应activity得生命周期
    public static void onPause() {
        EasyVideoPlayer videoPlayer = EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny();
        if (videoPlayer != null) {
            if (videoPlayer.getCurrentState() == CURRENT_STATE_AUTO_COMPLETE ||
                    videoPlayer.getCurrentState() == CURRENT_STATE_NORMAL) {
                MediaUtils.releaseAllVideos();
            } else {
                if (videoPlayer.getCurrentState() == MediaStatus.CURRENT_STATE_PLAYING) {
                    ONRESUME_TO_PLAY = true;
                } else {
                    ONRESUME_TO_PLAY = false;
                }
                videoPlayer.onStatePause();
                EasyMediaManager.pause();
            }
        }
    }

    //对应activity得生命周期
    public static void onResume() {
        EasyVideoPlayer videoPlayer = EasyVideoPlayerManager.getCurrentVideoPlayerNoTiny();
        if (videoPlayer != null) {
            if (videoPlayer.getCurrentState() == CURRENT_STATE_PAUSE && ONRESUME_TO_PLAY) {
                videoPlayer.onStatePlaying();
                EasyMediaManager.start();
            }
        }
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
    public static boolean showWifiDialog(Context context, MediaData data, final EasyBaseVideoPlay play) {
        if (!data.isLocal() &&
                !MediaUtils.isWifiConnected(context) && !EasyMediaManager.WIFI_ALLOW_PLAY) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(context.getResources().getString(R.string.tips_not_wifi));
            builder.setPositiveButton(context.getResources().getString(R.string.tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (play instanceof EasyVideoPlayer) {
                        ((EasyVideoPlayer) play).onEvent(EasyMediaAction.ON_CLICK_START_NO_WIFI_GOON);
                        ((EasyVideoPlayer) play).startVideo();
                    } else if (play instanceof EasyVideoPlayTiny) {
                        ((EasyVideoPlayTiny) play).onEvent(EasyMediaAction.ON_CLICK_START_NO_WIFI_GOON);
                        ((EasyVideoPlayTiny) play).startVideo();
                    }
                    EasyMediaManager.WIFI_ALLOW_PLAY = true;
                }
            });
            builder.setNegativeButton(context.getResources().getString(R.string.tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (play instanceof EasyVideoPlayer && ((EasyVideoPlayer) play).currentScreen == SCREEN_WINDOW_FULLSCREEN) {
                        ((EasyVideoPlayer) play).clearFullscreenLayout();
                    }
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (play instanceof EasyVideoPlayer && ((EasyVideoPlayer) play).currentScreen == SCREEN_WINDOW_FULLSCREEN) {
                        dialog.dismiss();
                        ((EasyVideoPlayer) play).clearFullscreenLayout();
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
                    MediaUtils.releaseAllVideos();
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
}
