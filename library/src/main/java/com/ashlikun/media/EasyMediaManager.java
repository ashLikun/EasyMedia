package com.ashlikun.media;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.TextureView;

/**
 * 整个播放器的管理器，
 */
public class EasyMediaManager implements TextureView.SurfaceTextureListener {

    //单利模式
    public static EasyMediaManager easyMediaManager;

    public static final String TAG = "EasyMediaManager";
    //handler标识
    public static final int HANDLER_PREPARE = 0;
    public static final int HANDLER_RELEASE = 2;

    public static EasyTextureView textureView;
    public static SurfaceTexture savedSurfaceTexture;
    public static Surface surface;

    public int positionInList = -1;
    //播放器
    public EasyMediaInterface mMediaPlay;
    public int currentVideoWidth = 0;
    public int currentVideoHeight = 0;
    //播放器Handler,独立线程
    public MediaHandler mMediaHandler;
    //主线程的handler
    public Handler mainThreadHandler;

    public EasyMediaManager() {
        HandlerThread mMediaHandlerThread = new HandlerThread(TAG);
        mMediaHandlerThread.start();
        mMediaHandler = new MediaHandler(mMediaHandlerThread.getLooper());
        mainThreadHandler = new Handler();
    }

    public static EasyMediaManager instance() {
        if (easyMediaManager == null) {
            easyMediaManager = new EasyMediaManager();
        }
        return easyMediaManager;
    }

    public static Object[] getDataSource() {
        return instance().mMediaPlay.dataSourceObjects;
    }

    //这几个方法是不是多余了，为了不让其他地方动MediaInterface的方法
    public static void setDataSource(Object[] dataSourceObjects) {
        instance().mMediaPlay.dataSourceObjects = dataSourceObjects;
    }

    //正在播放的url或者uri
    public static Object getCurrentDataSource() {
        return instance().mMediaPlay.currentDataSource;
    }

    public static void setCurrentDataSource(Object currentDataSource) {
        instance().mMediaPlay.currentDataSource = currentDataSource;
    }

    public static int getCurrentPosition() {
        return instance().mMediaPlay.getCurrentPosition();
    }

    public static int getDuration() {
        return instance().mMediaPlay.getDuration();
    }

    public static void seekTo(int time) {
        instance().mMediaPlay.seekTo(time);
    }

    public static void pause() {
        instance().mMediaPlay.pause();
    }

    public static void start() {
        instance().mMediaPlay.start();
    }

    public static boolean isPlaying() {
        return instance().mMediaPlay.isPlaying();
    }

    public void releaseMediaPlayer() {
        Message msg = new Message();
        msg.what = HANDLER_RELEASE;
        mMediaHandler.sendMessage(msg);
    }

    public void prepare() {
        releaseMediaPlayer();
        Message msg = new Message();
        msg.what = HANDLER_PREPARE;
        mMediaHandler.sendMessage(msg);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (savedSurfaceTexture == null) {
            savedSurfaceTexture = surfaceTexture;
            prepare();
        } else {
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
                    mMediaPlay.release();
                    break;
            }
        }
    }
}
