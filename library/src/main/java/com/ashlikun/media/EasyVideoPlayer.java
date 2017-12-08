package com.ashlikun.media;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ashlikun.media.controller.EasyMediaController;
import com.ashlikun.media.controller.MediaControllerInterface;
import com.ashlikun.media.view.EasyOnControllEvent;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;

import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_AUTO_COMPLETE;
import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_ERROR;
import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_NORMAL;
import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_PAUSE;
import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_PLAYING;
import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_PREPARING;
import static com.ashlikun.media.status.EasyMediaStatus.CURRENT_STATE_PREPARING_CHANGING_URL;
import static com.ashlikun.media.status.EasyScreenStatus.SCREEN_WINDOW_FULLSCREEN;
import static com.ashlikun.media.status.EasyScreenStatus.SCREEN_WINDOW_TINY;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/11/24 17:28
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：播放器view，包括视频和控制器
 */
public class EasyVideoPlayer extends FrameLayout
        implements EasyOnControllEvent {

    public static final String TAG = "EasyVideoPlayer";

    public static final int FULL_SCREEN_NORMAL_DELAY = 300;


    public static final String URL_KEY_DEFAULT = "URL_KEY_DEFAULT";//当播放的地址只有一个的时候的key
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER = 0;//default
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT = 1;
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP = 2;
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL = 3;
    public static boolean ACTION_BAR_EXIST = true;
    public static boolean TOOL_BAR_EXIST = true;
    public static int FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
    public static int NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    public static boolean SAVE_PROGRESS = true;
    public static boolean WIFI_TIP_DIALOG_SHOWED = false;
    public static int VIDEO_IMAGE_DISPLAY_TYPE = 0;
    public static long CLICK_QUIT_FULLSCREEN_TIME = 0;
    public static long lastAutoFullscreenTime = 0;
    public static AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {//是否新建个class，代码更规矩，并且变量的位置也很尴尬
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    releaseAllVideos();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    try {
                        if (EasyMediaManager.isPlaying()) {
                            EasyMediaManager.pause();
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT [" + this.hashCode() + "]");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };
    protected static EasyMediaAction MEDIA_EVENT;

    public int currentScreen = -1;//当前屏幕方向
    public int currentState = -1;//当前状态
    public Object[] objects = null;
    public int seekToInAdvance = 0;
    public int widthRatio = 0;
    public int heightRatio = 0;
    public Object[] dataSourceObjects;//这个参数原封不动直接通过JZMeidaManager传给JZMediaInterface。
    public int currentUrlMapIndex = 0;
    public int positionInList = -1;
    public int videoRotation = 0;
    public ViewGroup textureViewContainer;

    //播放器控制器
    MediaControllerInterface mediaController;

    boolean tmp_test_back = false;

    public EasyVideoPlayer(Context context) {
        super(context);
        init(context);
    }

    public EasyVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    public static void startFullscreen(Context context, Class _class, String url, Object... objects) {
        LinkedHashMap map = new LinkedHashMap();
        map.put(URL_KEY_DEFAULT, url);
        Object[] dataSourceObjects = new Object[1];
        dataSourceObjects[0] = map;
        startFullscreen(context, _class, dataSourceObjects, 0, objects);
    }

    public static void startFullscreen(Context context, Class _class, Object[] dataSourceObjects, int defaultUrlMapIndex, Object... objects) {
        hideSupportActionBar(context);
        MediaUtils.setRequestedOrientation(context, FULLSCREEN_ORIENTATION);
        ViewGroup vp = (MediaUtils.scanForActivity(context))//.getWindow().getDecorView();
                .findViewById(Window.ID_ANDROID_CONTENT);
        View old = vp.findViewById(R.id.easy_media_fullscreen_id);
        if (old != null) {
            vp.removeView(old);
        }
        try {
            Constructor<EasyVideoPlayer> constructor = _class.getConstructor(Context.class);
            final EasyVideoPlayer easyVideoPlayer = constructor.newInstance(context);
            easyVideoPlayer.setId(R.id.easy_media_tiny_id);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            vp.addView(easyVideoPlayer, lp);
//            final Animation ra = AnimationUtils.loadAnimation(context, R.anim.start_fullscreen);
//            EasyVideoPlayer.setAnimation(ra);
            easyVideoPlayer.setDataSource(dataSourceObjects, defaultUrlMapIndex, SCREEN_WINDOW_FULLSCREEN, objects);
            CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
            //  easyVideoPlayer.startButton.performClick();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean backPress() {
        if ((System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) < FULL_SCREEN_NORMAL_DELAY) {
            return false;
        }
        if (EasyVideoPlayerManager.getSecondFloor() != null) {
            CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
            if (MediaUtils.dataSourceObjectsContainsUri(EasyVideoPlayerManager.getFirstFloor().dataSourceObjects, EasyMediaManager.getCurrentDataSource())) {
                EasyVideoPlayer EasyVideoPlayer = EasyVideoPlayerManager.getSecondFloor();
                EasyVideoPlayer.onEvent(EasyVideoPlayer.currentScreen == SCREEN_WINDOW_FULLSCREEN ?
                        EasyMediaAction.ON_QUIT_FULLSCREEN :
                        EasyMediaAction.ON_QUIT_TINYSCREEN);
                EasyVideoPlayerManager.getFirstFloor().playOnThisJzvd();
            } else {
                quitFullscreenOrTinyWindow();
            }
            return true;
        } else if (EasyVideoPlayerManager.getFirstFloor() != null &&
                (EasyVideoPlayerManager.getFirstFloor().currentScreen == SCREEN_WINDOW_FULLSCREEN ||
                        EasyVideoPlayerManager.getFirstFloor().currentScreen == SCREEN_WINDOW_TINY)) {//以前我总想把这两个判断写到一起，这分明是两个独立是逻辑
            CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
            quitFullscreenOrTinyWindow();
            return true;
        }
        return false;
    }

    public static void quitFullscreenOrTinyWindow() {
        //直接退出全屏和小窗
        EasyVideoPlayerManager.getFirstFloor().clearFloatScreen();
        EasyMediaManager.instance().releaseMediaPlayer();
        EasyVideoPlayerManager.completeAll();
    }

    @SuppressLint("RestrictedApi")
    public static void showSupportActionBar(Context context) {
        if (ACTION_BAR_EXIST && MediaUtils.getAppCompActivity(context) != null) {
            ActionBar ab = MediaUtils.getAppCompActivity(context).getSupportActionBar();
            if (ab != null) {
                ab.setShowHideAnimationEnabled(false);
                ab.show();
            }
        }
        if (TOOL_BAR_EXIST) {
            MediaUtils.getWindow(context).clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @SuppressLint("RestrictedApi")
    public static void hideSupportActionBar(Context context) {
        if (ACTION_BAR_EXIST && MediaUtils.getAppCompActivity(context) != null) {
            ActionBar ab = MediaUtils.getAppCompActivity(context).getSupportActionBar();
            if (ab != null) {
                ab.setShowHideAnimationEnabled(false);
                ab.hide();
            }
        }
        if (TOOL_BAR_EXIST) {
            MediaUtils.getWindow(context).setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public static void clearSavedProgress(Context context, String url) {
        MediaUtils.clearSavedProgress(context, url);
    }


    //对应activity得生命周期
    public static void onResume() {
        if (EasyVideoPlayerManager.getCurrentVideoPlayer() != null) {
            EasyVideoPlayer jzvd = EasyVideoPlayerManager.getCurrentVideoPlayer();
            if (jzvd.currentState == CURRENT_STATE_PAUSE) {
                jzvd.onStatePlaying();
                EasyMediaManager.start();
            }
        }
    }

    //对应activity得生命周期
    public static void onPause() {
        if (EasyVideoPlayerManager.getCurrentVideoPlayer() != null) {
            EasyVideoPlayer jzvd = EasyVideoPlayerManager.getCurrentVideoPlayer();
            if (jzvd.currentState == CURRENT_STATE_AUTO_COMPLETE ||
                    jzvd.currentState == CURRENT_STATE_NORMAL) {
//                EasyVideoPlayer.releaseAllVideos();
            } else {
                jzvd.onStatePause();
                EasyMediaManager.pause();
            }
        }
    }

    //列表滑动时候自动小窗口
    public static void onScrollAutoTiny(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int lastVisibleItem = firstVisibleItem + visibleItemCount;
        int currentPlayPosition = EasyMediaManager.instance().positionInList;
        if (currentPlayPosition >= 0) {
            if ((currentPlayPosition < firstVisibleItem || currentPlayPosition > (lastVisibleItem - 1))) {
                if (EasyVideoPlayerManager.getCurrentVideoPlayer() != null &&
                        EasyVideoPlayerManager.getCurrentVideoPlayer().currentScreen != SCREEN_WINDOW_TINY) {
                    if (EasyVideoPlayerManager.getCurrentVideoPlayer().currentState == CURRENT_STATE_PAUSE) {
                        EasyVideoPlayer.releaseAllVideos();
                    } else {
                        Log.e(TAG, "onScroll: out screen");
                        EasyVideoPlayerManager.getCurrentVideoPlayer().startWindowTiny();
                    }
                }
            } else {
                if (EasyVideoPlayerManager.getCurrentVideoPlayer() != null &&
                        EasyVideoPlayerManager.getCurrentVideoPlayer().currentScreen == SCREEN_WINDOW_TINY) {
                    Log.e(TAG, "onScroll: into screen");
                    EasyVideoPlayer.backPress();
                }
            }
        }
    }

    //列表滑动时候清空全部播放
    public static void onScrollReleaseAllVideos(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int lastVisibleItem = firstVisibleItem + visibleItemCount;
        int currentPlayPosition = EasyMediaManager.instance().positionInList;
        if (currentPlayPosition >= 0) {
            if ((currentPlayPosition < firstVisibleItem || currentPlayPosition > (lastVisibleItem - 1))) {
                EasyVideoPlayer.releaseAllVideos();
            }
        }
    }

    //当子view附属到窗口时候
    public static void onChildViewAttachedToWindow(View view, int jzvdId) {
        if (EasyVideoPlayerManager.getCurrentVideoPlayer() != null && EasyVideoPlayerManager.getCurrentVideoPlayer().currentScreen == SCREEN_WINDOW_TINY) {
            EasyVideoPlayer videoPlayer = view.findViewById(jzvdId);
            if (videoPlayer != null && MediaUtils.getCurrentFromDataSource(videoPlayer.dataSourceObjects, videoPlayer.currentUrlMapIndex).equals(EasyMediaManager.getCurrentDataSource())) {
                EasyVideoPlayer.backPress();
            }
        }
    }

    //当子view从窗口分离
    public static void onChildViewDetachedFromWindow(View view) {
        if (EasyVideoPlayerManager.getCurrentVideoPlayer() != null && EasyVideoPlayerManager.getCurrentVideoPlayer().currentScreen != SCREEN_WINDOW_TINY) {
            EasyVideoPlayer videoPlayer = EasyVideoPlayerManager.getCurrentVideoPlayer();
            if (((ViewGroup) view).indexOfChild(videoPlayer) != -1) {
                if (videoPlayer.currentState == CURRENT_STATE_PAUSE) {
                    EasyVideoPlayer.releaseAllVideos();
                } else {
                    videoPlayer.startWindowTiny();
                }
            }
        }
    }


    public void init(Context context) {
        setBackgroundColor(0xff000000);
        textureViewContainer = new FrameLayout(getContext());
        addView(textureViewContainer, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mediaController = new EasyMediaController(getContext());
        addView((View) mediaController);
        mediaController.setOnControllEvent(this);
        try {
            if (isCurrentPlay()) {
                NORMAL_ORIENTATION = ((AppCompatActivity) context).getRequestedOrientation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDataSource(String url, int screen, Object... objects) {
        LinkedHashMap map = new LinkedHashMap();
        map.put(URL_KEY_DEFAULT, url);
        Object[] dataSourceObjects = new Object[1];
        dataSourceObjects[0] = map;
        setDataSource(dataSourceObjects, 0, screen, objects);
    }

    public void setDataSource(Object[] dataSourceObjects, int defaultUrlMapIndex, int screen, Object... objects) {
        if (EasyMediaManager.instance().mMediaPlay == null) {
            EasyMediaManager.instance().mMediaPlay = new EasyMediaSystem();
        }
        if (this.dataSourceObjects != null && MediaUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex) != null &&
                MediaUtils.getCurrentFromDataSource(this.dataSourceObjects, currentUrlMapIndex).equals(MediaUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex))) {
            return;
        }
        if (isCurrentVideoPlay() && MediaUtils.dataSourceObjectsContainsUri(dataSourceObjects, EasyMediaManager.getCurrentDataSource())) {
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
        } else if (isCurrentVideoPlay() && !MediaUtils.dataSourceObjectsContainsUri(dataSourceObjects, EasyMediaManager.getCurrentDataSource())) {
            startWindowTiny();
        } else if (!isCurrentVideoPlay() && MediaUtils.dataSourceObjectsContainsUri(dataSourceObjects, EasyMediaManager.getCurrentDataSource())) {
            if (EasyVideoPlayerManager.getCurrentVideoPlayer() != null &&
                    EasyVideoPlayerManager.getCurrentVideoPlayer().currentScreen == SCREEN_WINDOW_TINY) {
                //需要退出小窗退到我这里，我这里是第一层级
                tmp_test_back = true;
            }
        } else if (!isCurrentVideoPlay() && !MediaUtils.dataSourceObjectsContainsUri(dataSourceObjects, EasyMediaManager.getCurrentDataSource())) {
        }
        this.dataSourceObjects = dataSourceObjects;
        this.currentUrlMapIndex = defaultUrlMapIndex;
        currentScreen = screen;
        mediaController.setCurrentScreen(currentScreen);
        this.objects = objects;
        mediaController.setDataSource(dataSourceObjects, defaultUrlMapIndex, screen, objects);
        onStateNormal();
    }

    public void startVideo() {
        EasyVideoPlayerManager.completeAll();
        initTextureView();
        addTextureView();
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        MediaUtils.scanForActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        EasyMediaManager.setDataSource(dataSourceObjects);
        EasyMediaManager.setCurrentDataSource(MediaUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex));
        EasyMediaManager.instance().positionInList = positionInList;
        onStatePreparing();
        EasyVideoPlayerManager.setFirstFloor(this);
    }

    @Override
    public void onPlayStartClick() {
        if (dataSourceObjects == null || MediaUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex) == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentState == CURRENT_STATE_NORMAL) {
            if (!MediaUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex).toString().startsWith("file") && !
                    MediaUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex).toString().startsWith("/") &&
                    !MediaUtils.isWifiConnected(getContext()) && !WIFI_TIP_DIALOG_SHOWED) {
                mediaController.showWifiDialog(EasyMediaAction.ON_CLICK_START_ICON);
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

    @Override
    public void onFullscreenClick() {
        if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
            return;
        }
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            //退出全屏
            backPress();
        } else {
            Log.d(TAG, "toFullscreenActivity [" + this.hashCode() + "] ");
            onEvent(EasyMediaAction.ON_ENTER_FULLSCREEN);
            startWindowFullscreen();
        }
    }

    @Override
    public void onControllerClick() {
        if (currentState == CURRENT_STATE_ERROR) {
            startVideo();
        } else {
            mediaController.startDismissControlViewSchedule();
        }
    }

    public void onPrepared() {
        onStatePrepared();
        onStatePlaying();
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
        this.currentUrlMapIndex = urlMapIndex;
        this.seekToInAdvance = seekToInAdvance;
        EasyMediaManager.setDataSource(dataSourceObjects);
        EasyMediaManager.setCurrentDataSource(MediaUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex));
        EasyMediaManager.instance().prepare();
    }

    public void onStatePrepared() {//因为这个紧接着就会进入播放状态，所以不设置state
        if (seekToInAdvance != 0) {
            EasyMediaManager.seekTo(seekToInAdvance);
            seekToInAdvance = 0;
        } else {
            int position = MediaUtils.getSavedProgress(getContext(), MediaUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex));
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

    public void onStatePause() {
        currentState = CURRENT_STATE_PAUSE;
        mediaController.setCurrentState(currentState);
    }

    public void onStateError() {
        currentState = CURRENT_STATE_ERROR;
        mediaController.setCurrentState(currentState);
    }

    public void onStateAutoComplete() {
        currentState = CURRENT_STATE_AUTO_COMPLETE;
        mediaController.setCurrentState(currentState);
        mediaController.setMaxProgressAndTime();
    }

    public void onInfo(int what, int extra) {

    }

    //快进完成
    public void onSeekComplete() {

    }

    /**
     * 播放错误
     */
    public void onError(int what, int extra) {
        if (what != 38 && what != -38 && extra != -38) {
            onStateError();
            if (isCurrentPlay()) {
                EasyMediaManager.instance().releaseMediaPlayer();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN || currentScreen == SCREEN_WINDOW_TINY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (widthRatio != 0 && heightRatio != 0) {
            int specWidth = MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = (int) ((specWidth * (float) heightRatio) / widthRatio);
            setMeasuredDimension(specWidth, specHeight);

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(specHeight, MeasureSpec.EXACTLY);
            getChildAt(0).measure(childWidthMeasureSpec, childHeightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }

    /**
     * 播放自动完成
     */
    public void onAutoCompletion() {
        Runtime.getRuntime().gc();
        onEvent(EasyMediaAction.ON_AUTO_COMPLETE);
        mediaController.dismissVolumeDialog();
        mediaController.dismissProgressDialog();
        mediaController.dismissBrightnessDialog();
        onStateAutoComplete();

        if (currentScreen == SCREEN_WINDOW_FULLSCREEN || currentScreen == SCREEN_WINDOW_TINY) {
            backPress();
        }
        EasyMediaManager.instance().releaseMediaPlayer();
        MediaUtils.saveProgress(getContext(), MediaUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex), 0);
    }

    /**
     * 播放完成
     */
    public void onCompletion() {
        if (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE) {
            int position = mediaController.getCurrentPositionWhenPlaying();
            MediaUtils.saveProgress(getContext(), MediaUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex), position);
        }
        onStateNormal();
        textureViewContainer.removeView(EasyMediaManager.textureView);
        EasyMediaManager.instance().currentVideoWidth = 0;
        EasyMediaManager.instance().currentVideoHeight = 0;

        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        MediaUtils.scanForActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        clearFullscreenLayout();
        MediaUtils.setRequestedOrientation(getContext(), NORMAL_ORIENTATION);

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
        if (MediaUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex).equals(EasyMediaManager.getCurrentDataSource()) &&
                (System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) > FULL_SCREEN_NORMAL_DELAY) {
            //在非全屏的情况下只能backPress()
            if (EasyVideoPlayerManager.getSecondFloor() != null &&
                    EasyVideoPlayerManager.getSecondFloor().currentScreen == SCREEN_WINDOW_FULLSCREEN) {//点击全屏
            } else if (EasyVideoPlayerManager.getSecondFloor() == null && EasyVideoPlayerManager.getFirstFloor() != null &&
                    EasyVideoPlayerManager.getFirstFloor().currentScreen == SCREEN_WINDOW_FULLSCREEN) {//直接全屏
            } else {
                releaseAllVideos();
            }
        }
    }

    //释放video
    public static void releaseAllVideos() {
        if ((System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) > FULL_SCREEN_NORMAL_DELAY) {
            EasyVideoPlayerManager.completeAll();
            EasyMediaManager.instance().positionInList = -1;
            EasyMediaManager.instance().releaseMediaPlayer();
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
        EasyMediaManager.savedSurfaceTexture = null;
        if (EasyMediaManager.textureView != null && EasyMediaManager.textureView.getParent() != null) {
            ((ViewGroup) EasyMediaManager.textureView.getParent()).removeView(EasyMediaManager.textureView);
        }
    }

    /**
     * 清楚全屏和小窗口布局的布局
     */
    public void clearFullscreenLayout() {
        //.getWindow().getDecorView();
        ViewGroup vp = (MediaUtils.scanForActivity(getContext()))
                .findViewById(Window.ID_ANDROID_CONTENT);
        View oldF = vp.findViewById(R.id.easy_media_fullscreen_id);
        View oldT = vp.findViewById(R.id.easy_media_tiny_id);
        if (oldF != null) {
            vp.removeView(oldF);
        }
        if (oldT != null) {
            vp.removeView(oldT);
        }
        showSupportActionBar(getContext());
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
        hideSupportActionBar(getContext());
        MediaUtils.setRequestedOrientation(getContext(), FULLSCREEN_ORIENTATION);
        //.getWindow().getDecorView();
        ViewGroup vp = (MediaUtils.scanForActivity(getContext()))
                .findViewById(Window.ID_ANDROID_CONTENT);
        View old = vp.findViewById(R.id.easy_media_fullscreen_id);
        if (old != null) {
            vp.removeView(old);
        }
        textureViewContainer.removeView(EasyMediaManager.textureView);
        try {
            Constructor<EasyVideoPlayer> constructor = (Constructor<EasyVideoPlayer>) EasyVideoPlayer.this.getClass().getConstructor(Context.class);
            EasyVideoPlayer easyVideoPlayer = constructor.newInstance(getContext());
            easyVideoPlayer.setId(R.id.easy_media_fullscreen_id);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            vp.addView(easyVideoPlayer, lp);
            easyVideoPlayer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
            easyVideoPlayer.setDataSource(dataSourceObjects, currentUrlMapIndex, SCREEN_WINDOW_FULLSCREEN, objects);
            easyVideoPlayer.setState(currentState);
            easyVideoPlayer.addTextureView();
            EasyVideoPlayerManager.setSecondFloor(easyVideoPlayer);
//          final Animation ra = AnimationUtils.loadAnimation(getContext(), R.anim.start_fullscreen);
//          EasyVideoPlayer.setAnimation(ra);
            onStateNormal();
            easyVideoPlayer.setBufferProgress(mediaController.getBufferProgress());
            CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
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
        ViewGroup vp = (MediaUtils.scanForActivity(getContext()))//.getWindow().getDecorView();
                .findViewById(Window.ID_ANDROID_CONTENT);
        View old = vp.findViewById(R.id.easy_media_tiny_id);
        if (old != null) {
            vp.removeView(old);
        }
        textureViewContainer.removeView(EasyMediaManager.textureView);

        try {
            Constructor<EasyVideoPlayer> constructor = (Constructor<EasyVideoPlayer>) EasyVideoPlayer.this.getClass().getConstructor(Context.class);
            EasyVideoPlayer EasyVideoPlayer = constructor.newInstance(getContext());
            EasyVideoPlayer.setId(R.id.easy_media_tiny_id);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(400, 400);
            lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            vp.addView(EasyVideoPlayer, lp);
            EasyVideoPlayer.setDataSource(dataSourceObjects, currentUrlMapIndex, SCREEN_WINDOW_TINY, objects);
            EasyVideoPlayer.setState(currentState);
            EasyVideoPlayer.addTextureView();
            EasyVideoPlayerManager.setSecondFloor(EasyVideoPlayer);
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
                && MediaUtils.dataSourceObjectsContainsUri(dataSourceObjects, EasyMediaManager.getCurrentDataSource());
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
        return MediaUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex);
    }

    //设置TextureView旋转
    public static void setTextureViewRotation(int rotation) {
        if (EasyMediaManager.textureView != null) {
            EasyMediaManager.textureView.setRotation(rotation);
        }
    }

    //设置textureView显示角度
    public static void setVideoImageDisplayType(int type) {
        EasyVideoPlayer.VIDEO_IMAGE_DISPLAY_TYPE = type;
        if (EasyMediaManager.textureView != null) {
            EasyMediaManager.textureView.requestLayout();
        }
    }

    /**
     * 退出全屏和小窗的方法
     */
    public void playOnThisJzvd() {
        //1.清空全屏和小窗的播放器
        currentState = EasyVideoPlayerManager.getSecondFloor().currentState;
        mediaController.setCurrentState(currentState);
        currentUrlMapIndex = EasyVideoPlayerManager.getSecondFloor().currentUrlMapIndex;
        clearFloatScreen();
        //2.在本jzvd上播放
        setState(currentState);
        addTextureView();
    }

    /**
     * 清空全屏
     */
    public void clearFloatScreen() {
        MediaUtils.setRequestedOrientation(getContext(), NORMAL_ORIENTATION);
        showSupportActionBar(getContext());
        EasyVideoPlayer currentPlay = EasyVideoPlayerManager.getCurrentVideoPlayer();
        currentPlay.textureViewContainer.removeView(EasyMediaManager.textureView);
        ViewGroup vp = (MediaUtils.scanForActivity(getContext()))
                .findViewById(Window.ID_ANDROID_CONTENT);
        vp.removeView(currentPlay);
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
     * 自动退出全屏
     */
    public void autoQuitFullscreen() {
        if ((System.currentTimeMillis() - lastAutoFullscreenTime) > 2000
                && isCurrentPlay()
                && currentState == CURRENT_STATE_PLAYING
                && currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            lastAutoFullscreenTime = System.currentTimeMillis();
            backPress();
        }
    }

    /**
     * 实现播放事件的回掉
     *
     * @param type 事件类型
     */
    @Override
    public void onEvent(int type) {
        if (MEDIA_EVENT != null && isCurrentPlay() && dataSourceObjects != null) {
            MEDIA_EVENT.onEvent(type, MediaUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex), currentScreen, objects);
        }
    }

    /**
     * 设置播放的事件回掉
     *
     * @param easyMediaAction 会一直持有这个对象，在application里面调用
     */
    public static void setEasyMediaAction(EasyMediaAction easyMediaAction) {
        if (easyMediaAction instanceof Application) {
            MEDIA_EVENT = easyMediaAction;
        }
    }

    /**
     * 设置播放器,默认为系统播放器
     *
     * @param mediaInterface
     */
    public static void setMediaInterface(EasyMediaInterface mediaInterface) {
        EasyMediaManager.instance().mMediaPlay = mediaInterface;
    }
}
