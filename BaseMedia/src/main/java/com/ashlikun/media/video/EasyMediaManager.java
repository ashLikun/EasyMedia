package com.ashlikun.media.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import com.ashlikun.media.music.HandleMusicPlayEvent;
import com.ashlikun.media.video.play.EasyVideoSystem;
import com.ashlikun.media.video.status.VideoDisplayType;
import com.ashlikun.media.video.view.EasyTextureView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author　　: 李坤
 * 创建时间: 2018/11/10 16:01
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：整个播放器的管理器
 * 保证只有一个播放器
 * 保证渲染器也是只有一个(这样实现无缝切换)
 */

public class EasyMediaManager implements TextureView.SurfaceTextureListener {

    /**
     * 单利模式,存放多个管理器
     */
    static volatile ConcurrentHashMap<String, EasyMediaManager> instance = new ConcurrentHashMap<>();

    public static final String TAG_VIDEO = "VIDEO";
    public static final String TAG_MUSIC = "MUSIC";
    public static final String TAG = "EasyMediaManager";
    /**
     * handler标识
     * 0:准备播放
     * 2：回收
     */
    public static final int HANDLER_PREPARE = 0;
    public static final int HANDLER_RELEASE = 2;
    /**
     * 播放器
     * 使用面向接口编程
     */
    private EasyMediaInterface mMediaPlay;
    private Context appContext;
    /**
     * 播放器控件
     */
    private EasyTextureView textureView;
    /**
     * 用来捕获视频流中的图像帧的
     */
    private SurfaceTexture savedSurfaceTexture;
    /**
     * 设置给MediaPlay的渲染器(就是内存中的一段绘图缓冲区),里面有savedSurfaceTexture
     * 这个是保证无缝切换的重点
     */
    private Surface surface;

    /**
     * 当前播放的视频的大小
     */
    public int currentVideoWidth = 0;
    public int currentVideoHeight = 0;
    /**
     * 播放器Handler,独立线程
     */
    private MediaHandler mediaHandler;
    /**
     * 主线程的handler
     */
    private Handler mainThreadHandler;
    /**
     * 处理播放器事件
     */
    private HandlePlayEvent handlePlayEvent;
    /**
     * 播放事件的回掉
     */
    public EasyVideoAction MEDIA_EVENT;

    //网络状态 准备的时候
    public String mNetSate = "NORMAL";

    public EasyMediaManager(String tag) {
        HandlerThread mMediaHandlerThread = new HandlerThread(TAG);
        mMediaHandlerThread.start();
        mediaHandler = new MediaHandler(mMediaHandlerThread.getLooper());
        mainThreadHandler = new Handler(Looper.getMainLooper());
        handlePlayEvent = tag.equals(TAG_VIDEO) ? new HandleVideoPlayEvent(tag) : new HandleMusicPlayEvent(tag);
    }

    /**
     * 获取对应TAG的实例
     *
     * @param tag
     * @return
     */
    public static EasyMediaManager getInstance(String tag) {
        if (instance.containsKey(tag)) {
            return instance.get(tag);
        } else {
            synchronized (EasyMediaManager.class) {
                if (instance.containsKey(tag)) {
                    return instance.get(tag);
                }
                EasyMediaManager mediaManager = new EasyMediaManager(tag);
                instance.put(tag, mediaManager);
                return mediaManager;
            }
        }
    }


    public static EasyMediaManager getInstance() {
        return getInstance(TAG_VIDEO);
    }

    public static EasyMediaManager getInstanceMusic() {
        return getInstance(TAG_MUSIC);
    }

    public static EasyMediaInterface getMusicMediaPlay() {
        return getInstance(TAG_MUSIC).getMediaPlay();
    }

    public static EasyTextureView getTextureView() {
        return getInstance().textureView;
    }

    public EasyMediaInterface getMediaPlay() {
        //双重校验DCL单例模式
        if (mMediaPlay == null) {
            //同步代码块
            synchronized (EasyMediaManager.class) {
                if (VideoUtils.mMediaPlayClass != null) {
                    try {
                        mMediaPlay = VideoUtils.mMediaPlayClass.newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                if (mMediaPlay == null) {
                    mMediaPlay = new EasyVideoSystem();
                }
            }
        }
        if (mMediaPlay != null) {
            mMediaPlay.setContext(appContext);
            mMediaPlay.setEasyMediaManager(this);
        }
        //返回一个实例
        return mMediaPlay;
    }

    /**
     * 初始化TextureView
     */
    public void initTextureView(Context context, @VideoDisplayType.Code int displayType, EasyTextureView old) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }
        textureView = new EasyTextureView(context);
        if (old != null) {
            textureView.setVideoSize(old.getCurrentVideoWidth(), old.getCurrentVideoHeight());
        }
        textureView.setDisplayType(displayType);
        textureView.setSurfaceTextureListener(EasyMediaManager.getInstance());
        //用之前已经存在的savedSurfaceTexture，实现无差别播放
        if (savedSurfaceTexture != null && savedSurfaceTexture != textureView.getSurfaceTexture()) {
            textureView.setSurfaceTexture(savedSurfaceTexture);
        }
    }


    /**
     * 释放播放器
     */
    public void releaseMediaPlayer() {
        Message msg = new Message();
        msg.what = HANDLER_RELEASE;
        mediaHandler.sendMessage(msg);
    }

    public void setContext(Context context) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }
    }

    public boolean isPlayingNei() {
        try {
            return getMediaPlay().isPlaying();
        } catch (IllegalStateException e) {
        }
        return false;
    }

    /**
     * 准备播放
     */
    public void prepare() {
        releaseMediaPlayer();
        mNetSate = NetworkUtils.getNetWorkTypeName(appContext);
        Message msg = new Message();
        msg.what = HANDLER_PREPARE;
        mediaHandler.sendMessage(msg);
    }

    /**
     * 释放渲染器和保存的SurfaceTexture，textureView
     */
    public void releaseAllSufaceView() {
        if (EasyMediaManager.getInstance().surface != null) {
            EasyMediaManager.getInstance().surface.release();
        }
        if (EasyMediaManager.getInstance().savedSurfaceTexture != null) {
            EasyMediaManager.getInstance().savedSurfaceTexture.release();
        }
        textureView = null;
        EasyMediaManager.getInstance().savedSurfaceTexture = null;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (savedSurfaceTexture == null) {
            savedSurfaceTexture = surfaceTexture;
            prepare();
        } else {
            //说明之前正在某个SurfaceTexture上渲染播放，这里直接拿来播放
            textureView.setSurfaceTexture(savedSurfaceTexture);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return savedSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    /**
     * 移除TextureView
     */
    public void removeTextureView() {
        TextureView textureView = EasyMediaManager.getInstance().textureView;
        if (textureView != null && textureView.getParent() != null) {
            textureView.setSurfaceTextureListener(null);
            ((ViewGroup) textureView.getParent()).removeView(textureView);
        }
        EasyMediaManager.getInstance().savedSurfaceTexture = null;
        EasyMediaManager.getInstance().textureView = null;
    }

    public class MediaHandler extends Handler {
        public MediaHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_PREPARE:
                    currentVideoWidth = 0;
                    currentVideoHeight = 0;
                    getMediaPlay().prepare();
                    if (surface != null) {
                        surface.release();
                    }
                    if (savedSurfaceTexture != null) {
                        surface = new Surface(savedSurfaceTexture);
                        getMediaPlay().setSurface(surface);
                    }
                    break;
                case HANDLER_RELEASE:
                    if (isPlayingNei()) {
                        stop();
                    }
                    getMediaPlay().release();
                    break;
            }
        }
    }

    public Handler getMediaHandler() {
        return mainThreadHandler;
    }

    public HandlePlayEvent getHandlePlayEvent() {
        return handlePlayEvent;
    }


    /********************************************************************************************
     *                                           这几个静态方法只适用视频播放
     ********************************************************************************************/

    /**
     * 正在播放的视频数据
     *
     * @return
     */
    public static VideoData getCurrentDataSource() {
        return getInstance().getMediaPlay().getCurrentDataSource();
    }

    public static void setCurrentDataSource(VideoData currentDataSource) {
        getInstance().getMediaPlay().setCurrentDataSource(currentDataSource);
    }

    public static long getCurrentPosition() {
        if (getInstance().getMediaPlay() == null) {
            return 0;
        }
        return getInstance().getMediaPlay().getCurrentPosition();
    }

    public static int getBufferedPercentage() {
        if (getInstance().getMediaPlay() == null) {
            return 0;
        }
        return getInstance().getMediaPlay().getBufferedPercentage();
    }

    public static long getDuration() {
        if (getInstance().getMediaPlay() == null) {
            return 0;
        }
        return getInstance().getMediaPlay().getDuration();
    }

    public static void seekTo(long time) {
        if (getInstance().getMediaPlay() == null) {
            return;
        }
        getInstance().getMediaPlay().seekTo(time);
    }

    public static void pause() {
        getInstance().getMediaPlay().pause();
    }

    public static void start() {
        getInstance().getMediaPlay().start();
    }


    public static void stop() {
        getInstance().getMediaPlay().stop();
    }

    public static boolean isPlaying() {
        try {
            return getInstance().getMediaPlay().isPlaying();
        } catch (IllegalStateException e) {
        }
        return false;
    }

    public static void pauseOther(EasyMediaManager easyMediaManager) {
        //暂停其他的
        for (Map.Entry<String, EasyMediaManager> entry : instance.entrySet()) {
            if (entry.getValue() != easyMediaManager) {
                entry.getValue().getHandlePlayEvent().onPause();
            }
        }
    }
}
