package com.ashlikun.media;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import com.ashlikun.media.status.MediaDisplayType;
import com.ashlikun.media.view.EasyTextureView;

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
     * 单利模式
     */
    public static EasyMediaManager easyMediaManager;

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
    public EasyMediaInterface mMediaPlay;
    /**
     * 播放器控件
     */
    public static EasyTextureView textureView;
    /**
     * 用来捕获视频流中的图像帧的
     */
    public static SurfaceTexture savedSurfaceTexture;
    /**
     * 设置给MediaPlay的渲染器(就是内存中的一段绘图缓冲区),里面有savedSurfaceTexture
     * 这个是保证无缝切换的重点
     */
    public static Surface surface;

    /**
     * 当前播放的视频的大小
     */
    public int currentVideoWidth = 0;
    public int currentVideoHeight = 0;
    /**
     * 播放器Handler,独立线程
     */
    public MediaHandler mediaHandler;
    /**
     * 主线程的handler
     */
    public Handler mainThreadHandler;
    /**
     * 播放事件的回掉
     */
    public static EasyMediaAction MEDIA_EVENT;
    /**
     * 是否允许过非wifi播放视频
     */
    public static boolean WIFI_ALLOW_PLAY = true;

    public EasyMediaManager() {
        HandlerThread mMediaHandlerThread = new HandlerThread(TAG);
        mMediaHandlerThread.start();
        mediaHandler = new MediaHandler(mMediaHandlerThread.getLooper());
        mainThreadHandler = new Handler();
    }

    public static EasyMediaManager instance() {
        if (easyMediaManager == null) {
            easyMediaManager = new EasyMediaManager();
        }
        return easyMediaManager;
    }

    /**
     * 初始化TextureView
     */
    public void initTextureView(Context context, @MediaDisplayType.Code int displayType) {
        EasyMediaManager.instance().removeTextureView();
        EasyMediaManager.textureView = new EasyTextureView(context);
        textureView.setDisplayType(displayType);
        EasyMediaManager.textureView.setSurfaceTextureListener(EasyMediaManager.instance());
    }

    /**
     * 正在播放的视频数据
     *
     * @return
     */
    public static MediaData getCurrentDataSource() {
        return instance().mMediaPlay.currentDataSource;
    }

    public static void setCurrentDataSource(MediaData currentDataSource) {
        instance().mMediaPlay.currentDataSource = currentDataSource;
    }

    public static int getCurrentPosition() {
        if (instance().mMediaPlay == null) {
            return 0;
        }
        return instance().mMediaPlay.getCurrentPosition();
    }

    public static int getDuration() {
        if (instance().mMediaPlay == null) {
            return 0;
        }
        return instance().mMediaPlay.getDuration();
    }

    public static void seekTo(int time) {
        if (instance().mMediaPlay == null) {
            return;
        }
        instance().mMediaPlay.seekTo(time);
    }

    public static void pause() {
        instance().mMediaPlay.pause();
    }

    public static void start() {
        instance().mMediaPlay.start();
    }

    public static void stop() {
        instance().mMediaPlay.stop();
    }

    public static boolean isPlaying() {
        try {
            return instance().mMediaPlay.isPlaying();
        } catch (IllegalStateException e) {
        }
        return false;
    }

    /**
     * 释放播放器
     */
    public void releaseMediaPlayer() {
        Message msg = new Message();
        msg.what = HANDLER_RELEASE;
        mediaHandler.sendMessage(msg);
    }

    /**
     * 准备播放
     */
    public void prepare() {
        releaseMediaPlayer();
        Message msg = new Message();
        msg.what = HANDLER_PREPARE;
        mediaHandler.sendMessage(msg);
    }

    /**
     * 释放渲染器和保存的SurfaceTexture，textureView
     */
    public void releaseAllSufaceView() {
        if (EasyMediaManager.surface != null) {
            EasyMediaManager.surface.release();
        }
        if (EasyMediaManager.savedSurfaceTexture != null) {
            EasyMediaManager.savedSurfaceTexture.release();
        }
        EasyMediaManager.textureView = null;
        EasyMediaManager.savedSurfaceTexture = null;
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
        if (EasyMediaManager.textureView != null && EasyMediaManager.textureView.getParent() != null) {
            EasyMediaManager.textureView.setSurfaceTextureListener(null);
            ((ViewGroup) EasyMediaManager.textureView.getParent()).removeView(EasyMediaManager.textureView);
        }
        EasyMediaManager.savedSurfaceTexture = null;
        EasyMediaManager.textureView = null;
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
                    mMediaPlay.prepare();
                    if (surface != null) {
                        surface.release();
                    }
                    surface = new Surface(savedSurfaceTexture);
                    mMediaPlay.setSurface(surface);
                    break;
                case HANDLER_RELEASE:
                    if (isPlaying()) {
                        stop();
                    }
                    mMediaPlay.release();
                    break;
            }
        }
    }
}
