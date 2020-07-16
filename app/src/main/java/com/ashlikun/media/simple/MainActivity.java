package com.ashlikun.media.simple;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ashlikun.media.video.EasyVideoAction;
import com.ashlikun.media.video.EasyVideoPlayerManager;
import com.ashlikun.media.video.VideoData;
import com.ashlikun.media.video.VideoScreenUtils;
import com.ashlikun.media.video.VideoUtils;
import com.ashlikun.media.video.view.EasyVideoPlayer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EasyVideoPlayer mediaPlay;
    String[] permissions = new String[]{
            Manifest.permission.SYSTEM_ALERT_WINDOW,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quanxian();


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
        setContentView(R.layout.activity_main);
        mediaPlay = (EasyVideoPlayer) findViewById(R.id.mediaPlay);
        mediaPlay.setDataSource(new VideoData.Builder()
                .title("标题")
                .url(VideoUrl.meinv2)
                .builder());
        mediaPlay.addVideoAction(new EasyVideoAction() {
            @Override
            public void onEvent(int type) {
                if (type == EasyVideoAction.ON_STATUS_PREPARING) {
//                    VideoUtils.onPause();
                }
            }
        });
        //  MediaUtils.startFullscreen(new EasyVideoPlayer(this), VideoUrl.videoUrls[0][0], "李坤李坤李坤");
        findViewById(R.id.detailsButton).setOnClickListener(this);
        findViewById(R.id.listButton).setOnClickListener(this);
        findViewById(R.id.fullScreenButton).setOnClickListener(this);
        findViewById(R.id.fullScreenButton2).setOnClickListener(this);
        findViewById(R.id.miniButton).setOnClickListener(this);
        findViewById(R.id.douyinButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.detailsButton) {
            Intent intent = new Intent(this, MainDetailsActivity.class);
            intent.putExtra("data", mediaPlay.getCurrentData());
            startActivity(intent);
        } else if (v.getId() == R.id.listButton) {
            Intent intent = new Intent(this, HuoSanActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.douyinButton) {
            Intent intent = new Intent(this, DouyinActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.fullScreenButton) {
            VideoScreenUtils.startFullscreen(new EasyVideoPlayer(this), VideoUrl.meinv2, "标题");
        } else if (v.getId() == R.id.fullScreenButton2) {
            EasyVideoPlayer easyVideoPlayer = new EasyVideoPlayer(this);
            easyVideoPlayer.setFullscreenPortrait(2);
            VideoScreenUtils.startFullscreen(easyVideoPlayer, VideoUrl.meinv2, "标题");
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

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlay.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (EasyVideoPlayerManager.getCurrentVideoPlay() != null &&
                mediaPlay != EasyVideoPlayerManager.getCurrentVideoPlay() &&
                mediaPlay.getCurrentData().equalsUrl(EasyVideoPlayerManager.getCurrentVideoPlay().getCurrentData())) {
            //从其他播放的地方再次播放
            VideoScreenUtils.startCacheVideo(mediaPlay);
        } else {
            VideoUtils.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlay.onDestroy();
    }
}
