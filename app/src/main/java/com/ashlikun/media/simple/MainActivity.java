package com.ashlikun.media.simple;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.ashlikun.glideutils.GlideUtils;
import com.ashlikun.media.MediaData;
import com.ashlikun.media.MediaScreenUtils;
import com.ashlikun.media.MediaUtils;
import com.ashlikun.media.play.EasyMediaIjkplayer;
import com.ashlikun.media.view.EasyVideoPlayer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EasyVideoPlayer mediaPlay;
        String videoUrl2 = "http://ic.snssdk.com/neihan/video/playback/1513563522.2/?video_id=90ffcaa3a2a642bb8e6f02b73a5b27de&quality=origin&line=1&is_gif=0&device_platform=android";
//    String videoUrl2 = "http://p2.suibianyuming.com.cn/1524714504video_341679.mp4";
//    String videoUrl = "http://fs.mv.web.kugou.com/201712191633/784d23335957e44b18e748187f7726a9/G107/M02/16/13/S5QEAFl5rxCAaVBHAXjrv4kCk4A283.mp4";
    String[] permissions = new String[]{
            Manifest.permission.SYSTEM_ALERT_WINDOW,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quanxian();
        MediaUtils.init(getApplicationContext());
        MediaUtils.setMediaInterface(new EasyMediaIjkplayer());
        GlideUtils.init(new GlideUtils.OnNeedListener() {
            @Override
            public Application getApplication() {
                return MainActivity.this.getApplication();
            }

            @Override
            public boolean isDebug() {
                return BuildConfig.DEBUG;
            }

            @Override
            public String getBaseUrl() {
                return "www.baidu.com";
            }
        });
        setContentView(R.layout.activity_main);
        mediaPlay = (EasyVideoPlayer) findViewById(R.id.mediaPlay);
        mediaPlay.setDataSource(new MediaData.Builder()
                .title("标题")
                .url(videoUrl2)
                .builder());
        //  MediaUtils.startFullscreen(new EasyVideoPlayer(this), VideoUrl.videoUrls[0][0], "李坤李坤李坤");
        findViewById(R.id.listButton).setOnClickListener(this);
        findViewById(R.id.fullScreenButton).setOnClickListener(this);
        findViewById(R.id.fullScreenButton2).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.listButton) {
            Intent intent = new Intent(this, HuoSanActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.fullScreenButton) {
            MediaScreenUtils.startFullscreen(new EasyVideoPlayer(this), videoUrl2, "标题");
        } else if (v.getId() == R.id.fullScreenButton2) {
            EasyVideoPlayer easyVideoPlayer = new EasyVideoPlayer(this);
            easyVideoPlayer.setFullscreenPortrait(false);
            MediaScreenUtils.startFullscreen(easyVideoPlayer, videoUrl2, "标题");
        }
    }

    public void quanxian() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 10);
            } else {
                Toast.makeText(this, "有权限", Toast.LENGTH_LONG).show();
            }
//            if (!hasSelfPermissions(this, permissions)) {
//                ActivityCompat.requestPermissions(this, permissions, 111);
//            }
        }
    }

    public boolean hasSelfPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 111) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "没有权限", Toast.LENGTH_LONG).show();
            }
        }
    }
}
