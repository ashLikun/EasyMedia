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
                .url(VideoUrl.meinv)
                .builder());
        //  MediaUtils.startFullscreen(new EasyVideoPlayer(this), VideoUrl.videoUrls[0][0], "李坤李坤李坤");
        findViewById(R.id.listButton).setOnClickListener(this);
        findViewById(R.id.fullScreenButton).setOnClickListener(this);
        findViewById(R.id.fullScreenButton2).setOnClickListener(this);
        findViewById(R.id.miniButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.listButton) {
            Intent intent = new Intent(this, HuoSanActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.fullScreenButton) {
            MediaScreenUtils.startFullscreen(new EasyVideoPlayer(this), VideoUrl.meinv, "标题");
        } else if (v.getId() == R.id.fullScreenButton2) {
            EasyVideoPlayer easyVideoPlayer = new EasyVideoPlayer(this);
            easyVideoPlayer.setFullscreenPortrait(false);
            MediaScreenUtils.startFullscreen(easyVideoPlayer, VideoUrl.meinv, "标题");
        } else if (v.getId() == R.id.miniButton) {
            Intent intent = new Intent(this, MiniActivity.class);
            startActivity(intent);
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
