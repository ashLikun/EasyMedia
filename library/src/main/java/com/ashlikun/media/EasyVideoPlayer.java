package com.ashlikun.media;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.ashlikun.media.controller.EasyMediaController;
import com.ashlikun.media.controller.MediaControllerInterface;
import com.ashlikun.media.play.EasyMediaSystem;
import com.ashlikun.media.status.MediaDisplayType;
import com.ashlikun.media.status.MediaScreenStatus;
import com.ashlikun.media.status.MediaStatus;
import com.ashlikun.media.view.EasyOnControllEvent;

import java.lang.reflect.Constructor;

import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_FULLSCREEN;
import static com.ashlikun.media.status.MediaScreenStatus.SCREEN_WINDOW_TINY;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_AUTO_COMPLETE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_ERROR;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_NORMAL;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PAUSE;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PLAYING;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PREPARING;
import static com.ashlikun.media.status.MediaStatus.CURRENT_STATE_PREPARING_CHANGING_URL;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/24 17:28
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器view，负责视频的播放
 */
public class EasyVideoPlayer extends FrameLayout
        implements EasyOnControllEvent {
    public static int ORIENTATION_FULLSCREEN_SENSOR = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
    public static int ORIENTATION_NORMAL = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    public static int ORIENTATION_FULLSCREEN_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    //是否保存进度
    public static boolean SAVE_PROGRESS = true;
    //是否允许过非wifi播放视频
    public static boolean WIFI_ALLOW_PLAY = true;
    @MediaDisplayType.Code
    public static int VIDEO_IMAGE_DISPLAY_TYPE = 0;
    public static long lastAutoFullscreenTime = 0;

    protected static EasyMediaAction MEDIA_EVENT;
    @MediaScreenStatus.Code
    public int currentScreen = MediaScreenStatus.SCREEN_WINDOW_NORMAL;//当前屏幕方向
    @MediaStatus.Code
    public int currentState = MediaStatus.CURRENT_STATE_NORMAL;//当前状态
    public Object[] objects = null;
    public int seekToInAdvance = 0;
    public int widthRatio = 0;
    public int heightRatio = 0;
    //数据源，列表
    public Object[] dataSource;
    public int currentUrlIndex = 0;
    public int videoRotation = 0;
    public ViewGroup textureViewContainer;
    //全屏后是否可以竖屏
    protected boolean mFullscreenPortrait = true;

    //播放器控制器
    MediaControllerInterface mediaController;

    public EasyVideoPlayer(Context context) {
        this(context, null);
    }

    public EasyVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.background});
        if (!a.hasValue(0)) {
            setBackgroundColor(0xff000000);
        }
        a.recycle();
        textureViewContainer = new FrameLayout(getContext());
        addView(textureViewContainer, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mediaController = new EasyMediaController(getContext());
        addView((View) mediaController);
        mediaController.setOnControllEvent(this);
        try {
            if (isCurrentPlay()) {
                ORIENTATION_NORMAL = MediaUtils.getActivity(context).getRequestedOrientation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置当前屏幕，默认的和列表或者小窗口
     * 请在setDataSource之前设置
     *
     * @param currentScreen
     */
    public void setCurrentScreen(int currentScreen) {
        this.currentScreen = currentScreen;
    }

    /**
     * 设置数据源
     *
     * @param url     视频ur
     * @param objects 标题
     */
    public void setDataSource(String url, Object... objects) {
        Object[] dataSource = new Object[1];
        dataSource[0] = url;
        setDataSource(dataSource, 0, objects);
    }

    /**
     * 设置数据源
     *
     * @param dataSource   视频url，数组
     * @param defaultIndex 播放的url 位置 0 开始
     * @param objects      标题
     */
    public void setDataSource(Object[] dataSource, int defaultIndex, Object... objects) {
        //是否有播放器，没用就用系统的
        if (EasyMediaManager.instance().mMediaPlay == null) {
            EasyMediaManager.instance().mMediaPlay = new EasyMediaSystem();
        }
        if (this.dataSource != null && dataSource.length > defaultIndex &&
                MediaUtils.getCurrentFromDataSource(this.dataSource, currentUrlIndex).equals(MediaUtils.getCurrentFromDataSource(dataSource, currentUrlIndex))) {
            return;
        }
        if (isCurrentVideoPlay() && MediaUtils.dataSourceContainsUri(dataSource, EasyMediaManager.getCurrentDataSource())) {
            int position = 0;
            try {
                position = EasyMediaManager.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            if (position != 0) {
                MediaUtils.saveProgress(getContext(), EasyMediaManager.getCurrentDataSource(), position);
            }
            EasyMediaManager.instance().releaseMediaPlayer();
        } else if (isCurrentVideoPlay() && !MediaUtils.dataSourceContainsUri(dataSource, EasyMediaManager.getCurrentDataSource())) {
            startWindowTiny();
        } else if (!isCurrentVideoPlay() && MediaUtils.dataSourceContainsUri(dataSource, EasyMediaManager.getCurrentDataSource())) {
            if (EasyVideoPlayerManager.getCurrentVideoPlayer() != null &&
                    EasyVideoPlayerManager.getCurrentVideoPlayer().currentScreen == SCREEN_WINDOW_TINY) {
                //需要退出小窗退到我这里，我这里是第一层级
            }
        }
        this.dataSource = dataSource;
        this.currentUrlIndex = defaultIndex;
        this.objects = objects;
        mediaController.setCurrentScreen(currentScreen);
        mediaController.setDataSource(dataSource, defaultIndex, objects);
        onStateNormal();
    }

    /**
     * 开始播放
     */
    public void startVideo() {
        MediaUtils.releaseAllVideos();
        initTextureView();
        addTextureView();
        MediaUtils.setAudioFocus(getContext(), true);
        MediaUtils.getActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        EasyMediaManager.setCurrentDataSource(MediaUtils.getCurrentFromDataSource(dataSource, currentUrlIndex));
        onStatePreparing();
        EasyVideoPlayerManager.setFirstFloor(this);
    }

    /**
     * 当控制器播放按钮点击后
     */
    @Override
    public void onPlayStartClick() {
        if (dataSource == null || MediaUtils.getCurrentFromDataSource(dataSource, currentUrlIndex) == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentState == CURRENT_STATE_NORMAL) {
            if (!MediaUtils.getCurrentFromDataSource(dataSource, currentUrlIndex).toString().startsWith("file") && !
                    MediaUtils.getCurrentFromDataSource(dataSource, currentUrlIndex).toString().startsWith("/") &&
                    !MediaUtils.isWifiConnected(getContext()) && !WIFI_ALLOW_PLAY) {
                showWifiDialog();
                return;
            }
            startVideo();
            onEvent(EasyMediaAction.ON_CLICK_START_ICON);
        } else if (currentState == CURRENT_STATE_PLAYING) {
            onEvent(EasyMediaAction.ON_CLICK_PAUSE);
            EasyMediaManager.pause();
            onStatePause();
        } else if (currentState == CURRENT_STATE_PAUSE) {
            onEvent(EasyMediaAction.ON_CLICK_RESUME);
            EasyMediaManager.start();
            onStatePlaying();
        } else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
            onEvent(EasyMediaAction.ON_CLICK_START_AUTO_COMPLETE);
            startVideo();
        }
    }

    /**
     * 当控制器从新播放点击
     */
    @Override
    public void onRetryClick() {
        if (!MediaUtils.getCurrentFromDataSource(dataSource, currentUrlIndex).toString().startsWith("file") && !
                MediaUtils.getCurrentFromDataSource(dataSource, currentUrlIndex).toString().startsWith("/") &&
                !MediaUtils.isWifiConnected(getContext()) && !WIFI_ALLOW_PLAY) {
            showWifiDialog();
            onEvent(EasyMediaAction.ON_CLICK_START_ICON);
            return;
        }
        startVideo();
        onEvent(EasyMediaAction.ON_CLICK_START_ERROR);
    }

    /**
     * 控制器全屏点击
     */
    @Override
    public void onFullscreenClick() {
        if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
            return;
        }
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            //退出全屏
            MediaUtils.backPress();
        } else {
            onEvent(EasyMediaAction.ON_ENTER_FULLSCREEN);
            startWindowFullscreen();
        }
    }

    /**
     * 当控制器点击的时候
     */
    @Override
    public void onControllerClick() {
        if (currentState == CURRENT_STATE_ERROR) {
            startVideo();
        } else {
            mediaController.startDismissControlViewSchedule();
        }
    }


    public void setState(int state) {
        setState(state, 0, 0);
    }

    public void setState(int state, int urlMapIndex, int seekToInAdvance) {
        switch (state) {
            case CURRENT_STATE_NORMAL:
                onStateNormal();
                break;
            case CURRENT_STATE_PREPARING:
                onStatePreparing();
                break;
            case CURRENT_STATE_PREPARING_CHANGING_URL:
                onStatePreparingChangingUrl(urlMapIndex, seekToInAdvance);
                break;
            case CURRENT_STATE_PLAYING:
                onStatePlaying();
                break;
            case CURRENT_STATE_PAUSE:
                onStatePause();
                break;
            case CURRENT_STATE_ERROR:
                onStateError();
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                onStateAutoComplete();
                break;
        }
    }

    public void onStateNormal() {
        currentState = CURRENT_STATE_NORMAL;
        mediaController.setCurrentState(currentState);
    }


    public void onStatePreparing() {
        currentState = CURRENT_STATE_PREPARING;
        mediaController.setCurrentState(currentState);
    }

    public void onStatePreparingChangingUrl(int urlMapIndex, int seekToInAdvance) {
        currentState = CURRENT_STATE_PREPARING_CHANGING_URL;
        mediaController.setCurrentState(currentState);
        this.currentUrlIndex = urlMapIndex;
        this.seekToInAdvance = seekToInAdvance;
        EasyMediaManager.setCurrentDataSource(MediaUtils.getCurrentFromDataSource(dataSource, currentUrlIndex));
        EasyMediaManager.instance().prepare();
    }

    public void onStatePrepared() {//因为这个紧接着就会进入播放状态，所以不设置state
        if (seekToInAdvance != 0) {
            EasyMediaManager.seekTo(seekToInAdvance);
            seekToInAdvance = 0;
        } else {
            int position = MediaUtils.getSavedProgress(getContext(), MediaUtils.getCurrentFromDataSource(dataSource, currentUrlIndex));
            if (position != 0) {
                EasyMediaManager.seekTo(position);
            }
        }
    }

    //开始播放回掉
    public void onStatePlaying() {
        currentState = CURRENT_STATE_PLAYING;
        mediaController.setCurrentState(currentState);
    }

    //暂停
    public void onStatePause() {
        currentState = CURRENT_STATE_PAUSE;
        mediaController.setCurrentState(currentState);
    }

    //错误
    public void onStateError() {
        currentState = CURRENT_STATE_ERROR;
        mediaController.setCurrentState(currentState);
    }

    //自动完成
    public void onStateAutoComplete() {
        currentState = CURRENT_STATE_AUTO_COMPLETE;
        mediaController.setCurrentState(currentState);
        mediaController.setMaxProgressAndTime();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (widthRatio != 0 && heightRatio != 0) {
            int specWidth = MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = (int) ((specWidth * (float) heightRatio) / widthRatio);
            if (specHeight > specWidth / 5 * 4) {
                //高度最大值限制
                specHeight = specWidth / 5 * 4;
            }
            setMeasuredDimension(specWidth, specHeight);

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(specHeight, MeasureSpec.EXACTLY);
            textureViewContainer.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            ((View) mediaController).measure(childWidthMeasureSpec, childHeightMeasureSpec);
            if (getThumbImageView() != null) {
                getThumbImageView().measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }

    //播放器生命周期
    public void onPrepared() {
        onStatePrepared();
        onStatePlaying();
    }

    //播放器生命周期
    public void onInfo(int what, int extra) {

    }

    //播放器生命周期
    public void onSeekComplete() {

    }

    //播放器生命周期
    public void onError(int what, int extra) {
        if (what != 38 && what != -38 && extra != -38) {
            onStateError();
            if (isCurrentPlay()) {
                EasyMediaManager.instance().releaseMediaPlayer();
            }
        }
    }

    //播放器生命周期
    public void onAutoCompletion() {
        Runtime.getRuntime().gc();
        onEvent(EasyMediaAction.ON_AUTO_COMPLETE);
        mediaController.onAutoCompletion();
        onStateAutoComplete();
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN || currentScreen == SCREEN_WINDOW_TINY) {
            MediaUtils.backPress();
        }
        EasyMediaManager.instance().releaseMediaPlayer();
        MediaUtils.saveProgress(getContext(), MediaUtils.getCurrentFromDataSource(dataSource, currentUrlIndex), 0);
    }

    //播放器生命周期
    public void onCompletion() {
        if (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE) {
            int position = mediaController.getCurrentPositionWhenPlaying();
            MediaUtils.saveProgress(getContext(), MediaUtils.getCurrentFromDataSource(dataSource, currentUrlIndex), position);
        }
        onStateNormal();
        textureViewContainer.removeView(EasyMediaManager.textureView);
        EasyMediaManager.instance().currentVideoWidth = 0;
        EasyMediaManager.instance().currentVideoHeight = 0;

        MediaUtils.setAudioFocus(getContext(), false);
//        MediaUtils.getActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        clearFullscreenLayout();
//        MediaUtils.setRequestedOrientation(getContext(), ORIENTATION_NORMAL);

        if (EasyMediaManager.surface != null) {
            EasyMediaManager.surface.release();
        }
        if (EasyMediaManager.savedSurfaceTexture != null) {
            EasyMediaManager.savedSurfaceTexture.release();
        }
        EasyMediaManager.textureView = null;
        EasyMediaManager.savedSurfaceTexture = null;
    }

    /**
     * 释放播放器,全屏下不能释放,先退出全屏再释放
     */
    public void release() {
        if (MediaUtils.getCurrentFromDataSource(dataSource, currentUrlIndex).equals(EasyMediaManager.getCurrentDataSource()) &&
                (System.currentTimeMillis() - MediaUtils.CLICK_QUIT_FULLSCREEN_TIME) > MediaUtils.FULL_SCREEN_NORMAL_DELAY) {
            //在非全屏的情况下只能backPress()
            if (EasyVideoPlayerManager.getSecondFloor() != null &&
                    EasyVideoPlayerManager.getSecondFloor().currentScreen == SCREEN_WINDOW_FULLSCREEN) {//点击全屏
            } else if (EasyVideoPlayerManager.getSecondFloor() == null && EasyVideoPlayerManager.getFirstFloor() != null &&
                    EasyVideoPlayerManager.getFirstFloor().currentScreen == SCREEN_WINDOW_FULLSCREEN) {//直接全屏
            } else {
                MediaUtils.releaseAllVideos();
            }
        }
    }

    /**
     * 初始化TextureView
     */
    public void initTextureView() {
        removeTextureView();
        EasyMediaManager.textureView = new EasyTextureView(getContext());
        EasyMediaManager.textureView.setSurfaceTextureListener(EasyMediaManager.instance());
    }

    /**
     * 添加TextureView
     */
    public void addTextureView() {
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
        textureViewContainer.addView(EasyMediaManager.textureView, layoutParams);
    }

    /**
     * 移除TextureView
     */
    public void removeTextureView() {
        if (EasyMediaManager.textureView != null && EasyMediaManager.textureView.getParent() != null) {
            EasyMediaManager.textureView.setSurfaceTexture(null);
            EasyMediaManager.textureView.setSurfaceTextureListener(null);
            ((ViewGroup) EasyMediaManager.textureView.getParent()).removeView(EasyMediaManager.textureView);
        }
        EasyMediaManager.savedSurfaceTexture = null;
        EasyMediaManager.textureView = null;
    }

    /**
     * 清楚全屏和小窗口布局的布局
     */
    public void clearFullscreenLayout() {
        ViewGroup vp = MediaUtils.getDecorView(getContext());
        View oldF = vp.findViewById(R.id.easy_media_fullscreen_id);
        View oldT = vp.findViewById(R.id.easy_media_tiny_id);
        if (oldF != null) {
            vp.removeView(oldF);
        }
        if (oldT != null) {
            vp.removeView(oldT);
        }
        MediaUtils.setActivityFullscreen(getContext(), false);
    }

    public void onVideoSizeChanged() {
        if (EasyMediaManager.textureView != null) {
            if (videoRotation != 0) {
                EasyMediaManager.textureView.setRotation(videoRotation);
            }
            EasyMediaManager.textureView.setVideoSize(EasyMediaManager.instance().currentVideoWidth, EasyMediaManager.instance().currentVideoHeight);
        }
    }


    /**
     * 开始全屏播放
     * 在当前activity的跟布局加一个新的最大化的EasyVideoPlayer
     * 再把activity设置成全屏，
     */
    public void startWindowFullscreen() {
        MediaUtils.setActivityFullscreen(getContext(), true);
        MediaUtils.setRequestedOrientation(getContext(), mFullscreenPortrait ? ORIENTATION_FULLSCREEN_SENSOR : ORIENTATION_FULLSCREEN_LANDSCAPE);
        ViewGroup vp = MediaUtils.getDecorView(getContext());
        View old = vp.findViewById(R.id.easy_media_fullscreen_id);
        if (old != null) {
            vp.removeView(old);
        }
        textureViewContainer.removeView(EasyMediaManager.textureView);
        try {
            Constructor<EasyVideoPlayer> constructor = (Constructor<EasyVideoPlayer>) EasyVideoPlayer.this.getClass().getConstructor(Context.class);
            EasyVideoPlayer easyVideoPlayer = constructor.newInstance(getContext());
            easyVideoPlayer.setId(R.id.easy_media_fullscreen_id);
            easyVideoPlayer.setFullscreenPortrait(mFullscreenPortrait);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            vp.addView(easyVideoPlayer, lp);
            //View.SYSTEM_UI_FLAG_HIDE_NAVIGATION：隐藏虚拟按键(导航栏)。有些手机会用虚拟按键来代替物理按键。
            //View.SYSTEM_UI_FLAG_FULLSCREEN：Activity全屏显示，且状态栏被隐藏覆盖掉。
            easyVideoPlayer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
            easyVideoPlayer.setCurrentScreen(MediaScreenStatus.SCREEN_WINDOW_FULLSCREEN);
            easyVideoPlayer.setDataSource(dataSource, currentUrlIndex, objects);
            easyVideoPlayer.addTextureView();
            EasyVideoPlayerManager.setSecondFloor(easyVideoPlayer);
            easyVideoPlayer.setState(currentState);
            Animation ra = AnimationUtils.loadAnimation(getContext(), R.anim.start_fullscreen);
            easyVideoPlayer.setAnimation(ra);
            easyVideoPlayer.setBufferProgress(mediaController.getBufferProgress());
            MediaUtils.CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
            //还原默认的view
            onStateNormal();
            //取消定时器
            mediaController.cancelDismissControlViewSchedule();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始小窗口播放
     */
    public void startWindowTiny() {
        onEvent(EasyMediaAction.ON_ENTER_TINYSCREEN);
        if (currentState == CURRENT_STATE_NORMAL || currentState == CURRENT_STATE_ERROR || currentState == CURRENT_STATE_AUTO_COMPLETE) {
            return;
        }
        ViewGroup vp = MediaUtils.getDecorView(getContext());
        View old = vp.findViewById(R.id.easy_media_tiny_id);
        if (old != null) {
            vp.removeView(old);
        }
        textureViewContainer.removeView(EasyMediaManager.textureView);
        try {
            Constructor<EasyVideoPlayer> constructor = (Constructor<EasyVideoPlayer>) EasyVideoPlayer.this.getClass().getConstructor(Context.class);
            EasyVideoPlayer videoPlayer = constructor.newInstance(getContext());
            videoPlayer.setId(R.id.easy_media_tiny_id);
            int width = getResources().getDisplayMetrics().widthPixels / 5 * 2;
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, width);
            lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            videoPlayer.setTranslationY(-10);
            vp.addView(videoPlayer, lp);
            videoPlayer.setCurrentScreen(SCREEN_WINDOW_TINY);
            videoPlayer.setDataSource(dataSource, currentUrlIndex, objects);
            videoPlayer.setState(currentState);
            videoPlayer.setVideoRatio(widthRatio, heightRatio);
            videoPlayer.addTextureView();
            EasyVideoPlayerManager.setSecondFloor(videoPlayer);
            onStateNormal();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 当前EasyVideoPlay  是否正在播放
     */
    public boolean isCurrentPlay() {
        //不仅正在播放的url不能一样，并且各个清晰度也不能一样
        return isCurrentVideoPlay()
                && MediaUtils.dataSourceContainsUri(dataSource, EasyMediaManager.getCurrentDataSource());
    }

    /**
     * 是否是当前EasyVideoPlay在播放视频
     */
    public boolean isCurrentVideoPlay() {
        return EasyVideoPlayerManager.getCurrentVideoPlayer() != null
                && EasyVideoPlayerManager.getCurrentVideoPlayer() == this;
    }

    /**
     * 设置当前播放缓存进度
     */
    public void setBufferProgress(int bufferProgress) {
        mediaController.setBufferProgress(bufferProgress);
    }

    //获取当前播放uil
    public Object getCurrentUrl() {
        return MediaUtils.getCurrentFromDataSource(dataSource, currentUrlIndex);
    }

    //设置TextureView旋转
    public static void setTextureViewRotation(int rotation) {
        if (EasyMediaManager.textureView != null) {
            EasyMediaManager.textureView.setRotation(rotation);
        }
    }

    //设置TextureView显示角度
    public static void setVideoImageDisplayType(@MediaDisplayType.Code int type) {
        EasyVideoPlayer.VIDEO_IMAGE_DISPLAY_TYPE = type;
        if (EasyMediaManager.textureView != null) {
            EasyMediaManager.textureView.requestLayout();
        }
    }

    /**
     * 退出全屏和小窗口后调用这个方法 继续播放
     */
    public void playOnThisVideo() {
        //1.清空全屏和小窗的播放器
        currentState = EasyVideoPlayerManager.getSecondFloor().currentState;
        mediaController.setCurrentState(currentState);
        currentUrlIndex = EasyVideoPlayerManager.getSecondFloor().currentUrlIndex;
        clearFloatScreen();
        //2.在本Video上播放
        setState(currentState);
        addTextureView();
    }

    /**
     * 清空全屏
     */
    public void clearFloatScreen() {
        MediaUtils.setRequestedOrientation(getContext(), ORIENTATION_NORMAL);
        MediaUtils.setActivityFullscreen(getContext(), false);
        EasyVideoPlayer currentPlay = EasyVideoPlayerManager.getCurrentVideoPlayer();
        currentPlay.textureViewContainer.removeView(EasyMediaManager.textureView);

        MediaUtils.getDecorView(getContext()).removeView(currentPlay);
        EasyVideoPlayerManager.setSecondFloor(null);
    }

    /**
     * 重力感应的时候调用的函数
     *
     * @param x
     */
    public void autoFullscreen(float x) {
        if (isCurrentPlay()
                && currentState == CURRENT_STATE_PLAYING
                && currentScreen != SCREEN_WINDOW_FULLSCREEN
                && currentScreen != SCREEN_WINDOW_TINY) {
            if (x > 0) {
                MediaUtils.setRequestedOrientation(getContext(), ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                MediaUtils.setRequestedOrientation(getContext(), ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }
            onEvent(EasyMediaAction.ON_ENTER_FULLSCREEN);
            startWindowFullscreen();
        }
    }

    /**
     * 实现播放事件的回掉
     *
     * @param type 事件类型
     */
    @Override
    public void onEvent(int type) {
        if (MEDIA_EVENT != null && isCurrentPlay() && dataSource != null) {
            MEDIA_EVENT.onEvent(type, MediaUtils.getCurrentFromDataSource(dataSource, currentUrlIndex), currentScreen, objects);
        }
    }

    //显示非wifi播放提示
    public void showWifiDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onEvent(EasyMediaAction.ON_CLICK_START_NO_WIFI_GOON);
                startVideo();
                WIFI_ALLOW_PLAY = true;
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
                    dialog.dismiss();
                    clearFullscreenLayout();
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
                    dialog.dismiss();
                    clearFullscreenLayout();
                }
            }
        });
        builder.create().show();
    }

    /**
     * 自动退出全屏
     */
    public void autoQuitFullscreen() {
        if ((System.currentTimeMillis() - lastAutoFullscreenTime) > 2000
                && isCurrentPlay()
                && currentState == CURRENT_STATE_PLAYING
                && currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            lastAutoFullscreenTime = System.currentTimeMillis();
            MediaUtils.backPress();
        }
    }

    /**
     * 设置是列表播放的
     * 请在setData之前设置
     */
    public void setCurrentPlayList() {
        this.currentScreen = MediaScreenStatus.SCREEN_WINDOW_LIST;
    }

    /**
     * 全屏后是否可以竖屏
     */
    public void setFullscreenPortrait(boolean mFullscreenPortrait) {
        this.mFullscreenPortrait = mFullscreenPortrait;
    }

    public boolean isFullscreenPortrait() {
        return mFullscreenPortrait;
    }

    /**
     * 设置宽高比例
     *
     * @param widthRatio
     * @param heightRatio
     */
    public void setVideoRatio(int widthRatio, int heightRatio) {
        this.heightRatio = heightRatio;
        this.widthRatio = widthRatio;
    }

    public ImageView getThumbImageView() {
        return mediaController.getThumbImageView();
    }


}
