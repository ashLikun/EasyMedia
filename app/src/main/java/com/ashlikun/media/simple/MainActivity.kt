package com.ashlikun.media.simple

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ashlikun.media.exoplay2.IjkExo2MediaPlayer
import com.ashlikun.media.exoplay2.play.liveConfig
import com.ashlikun.media.exoplay3.IjkExo3MediaPlayer
import com.ashlikun.media.exoplay3.play.liveConfig
import com.ashlikun.media.simple.music.MusicView
import com.ashlikun.media.video.EasyMediaEvent
import com.ashlikun.media.video.VideoData
import com.ashlikun.media.video.VideoScreenUtils
import com.ashlikun.media.video.VideoUtils
import com.ashlikun.media.video.play.liveConfig
import com.ashlikun.media.video.view.EasyLiveMediaPlay
import com.ashlikun.media.video.view.EasyMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class MainActivity : AppCompatActivity(), View.OnClickListener {
    val mediaPlay by lazy {
        findViewById<View>(R.id.mediaPlay) as EasyMediaPlayer
    }
    val mediaPlay2 by lazy {
        findViewById<View>(R.id.mediaPlay2) as EasyLiveMediaPlay
    }
    val musicPlay by lazy {
        findViewById<View>(R.id.aaaMusic) as MusicView
    }
    var permissions = arrayOf(
        Manifest.permission.SYSTEM_ALERT_WINDOW
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quanxian()


//        GlideUtils.init(new GlideUtils.OnNeedListener() {
//            @Override
//            public Application getApplication() {
//                return MainActivity.this.getApplication();
//            }
//
//            @Override
//            public boolean isDebug() {
//                return BuildConfig.DEBUG;
//            }
//
//            @Override
//            public String getBaseUrl() {
//                return "www.baidu.com";
//            }
//        });
        setContentView(R.layout.activity_main)
        mediaPlay.onPlayerCreate = { media, data, player ->
            if (player is IjkExo3MediaPlayer) {
                player.liveConfig()
            }
        }
//        mediaPlay.setDataSource(listOf(VideoData("rtsp://192.168.69.85:8086", "标题")))
        mediaPlay.setDataSource(listOf(VideoData(VideoUrl.meinv5, "标题")))
        mediaPlay.addEvent { type ->
            if (type == EasyMediaEvent.ON_STATUS_PREPARING) {
//                    VideoUtils.onPause();
            }
        }

        mediaPlay2.onPlayerCreate = { media, data, player ->
            if (player is IjkExo3MediaPlayer) {
                player.liveConfig()
            } else if (player is IjkExo2MediaPlayer) {
                player.liveConfig()
            } else if (player is IjkMediaPlayer) {
                player.liveConfig()
            }
        }
        mediaPlay2.setDataSource("rtsp://192.168.69.100:8086")
        mediaPlay2.postDelayed({
            mediaPlay2.startVideo()
        }, 2000)
        mediaPlay2.onSizeChange = { w, h ->
            Log.e("aaaaaa", "width = $w , height = ${h}")
            mediaPlay2.layoutParams.also {
                val height = mediaPlay2.height
                val width = mediaPlay2.width
                val hei = width * (h / (w * 1f))
                it.height = hei.toInt()
                Log.e("aaaaaa22", "width = $width , height = ${hei}")
                mediaPlay2.layoutParams = it
                mediaPlay2.requestLayout()
            }
        }
        musicPlay.setDataSource(
            VideoData("https:\\/\\/sipapp.510gow.com\\/k2ysbn_1606113268.mp3", "1111111")
        )
        musicPlay.setOnClickListener {
            musicPlay.setDataSource(
                VideoData("https:\\/\\/sipapp.510gow.com\\/k2ysbn_1606113268.mp3", "aaaaaaaaaaa")
            )
            musicPlay.startMusic()
        }
        //        musicPlay.startMusic();
        //  MediaUtils.startFullscreen(new EasyVideoPlayer(this), VideoUrl.videoUrls[0][0], "李坤李坤李坤");
        findViewById<View>(R.id.detailsButton).setOnClickListener(this)
        findViewById<View>(R.id.refreshView).setOnClickListener(this)
        findViewById<View>(R.id.listButton).setOnClickListener(this)
        findViewById<View>(R.id.fullScreenButton).setOnClickListener(this)
        findViewById<View>(R.id.fullScreenButton2).setOnClickListener(this)
        findViewById<View>(R.id.miniButton).setOnClickListener(this)
        findViewById<View>(R.id.douyinButton).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.refreshView -> {

                mediaPlay2.refresh()
//                mediaPlay2.postDelayed({ mediaPlay2.refresh() }, 10000)
            }

            R.id.detailsButton -> {
                val intent = Intent(this, MainDetailsActivity::class.java)
                intent.putExtra("data", mediaPlay.currentData)
                startActivity(intent)
            }

            R.id.listButton -> {
                val intent = Intent(this, HuoSanActivity::class.java)
                startActivity(intent)
            }

            R.id.douyinButton -> {
                val intent = Intent(this, DouyinActivity::class.java)
                startActivity(intent)
            }

            R.id.fullScreenButton -> {
                VideoScreenUtils.startFullscreen(EasyMediaPlayer(this), VideoUrl.meinv2, "标题")
            }

            R.id.fullScreenButton2 -> {
                val easyVideoPlayer = EasyMediaPlayer(this)
                easyVideoPlayer.fullscreenPortrait = 2
                VideoScreenUtils.startFullscreen(easyVideoPlayer, VideoUrl.meinv2, "标题")
            }

            R.id.miniButton -> {
                val intent = Intent(this, MiniActivity::class.java)
                startActivity(intent)
            }
        }
    }

    fun quanxian() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivityForResult(intent, 10)
            } else {
                Toast.makeText(this, "有权限", Toast.LENGTH_LONG).show()
            }
            //            if (!hasSelfPermissions(this, permissions)) {
//                ActivityCompat.requestPermissions(this, permissions, 111);
//            }
        }
    }

    fun hasSelfPermissions(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !Settings.canDrawOverlays(this)
            ) {
                Toast.makeText(this, "没有权限", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlay.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (!VideoScreenUtils.startCacheVideo(mediaPlay)) {
            //从其他播放的地方再次播放
            VideoUtils.onResume()
        }
    }

    override fun onBackPressed() {
        if (!VideoScreenUtils.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlay.onDestroy()
    }
}