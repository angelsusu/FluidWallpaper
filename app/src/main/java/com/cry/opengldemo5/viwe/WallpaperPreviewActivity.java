package com.cry.opengldemo5.viwe;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.cry.opengldemo5.ImageWallpaperService;
import com.cry.opengldemo5.R;
import com.cry.opengldemo5.VideoWallpaperService;
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfo;

import java.io.IOException;

/**
 * Created by xieguohua on 2019/6/19.
 */
public class WallpaperPreviewActivity extends AppCompatActivity {

    public static final String LIVE_WALLPAPER_INFO = "liveWallpaperInfo";
    private LiveWallpaperInfo mLiveWallpaperInfo;
    private MediaPlayer mMediaPlayer = new MediaPlayer();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_preview);
        init();
        initView();
    }

    private void init() {
        if (mLiveWallpaperInfo.mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE) {
            initImageView();
        } else {
            initSurfaceView();
        }
    }

    private void initView() {
        Button button = findViewById(R.id.bt_set);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLiveWallpaperInfo.mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE) {
                    ImageWallpaperService.startWallpaper(WallpaperPreviewActivity.this);
                } else if (mLiveWallpaperInfo.mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_VIDEO) {
                    VideoWallpaperService.startWallpaper(WallpaperPreviewActivity.this);
                }
            }
        });
    }

    private void initImageView() {
        ImageView imageView = findViewById(R.id.iv_preview);
        imageView.setVisibility(View.VISIBLE);
        //imageView.setImageBitmap(mLiveWallpaperInfo.mResourcesID);
    }

    private void initSurfaceView() {
        SurfaceView surfaceView = findViewById(R.id.sv_preview);
        surfaceView.setVisibility(View.VISIBLE);
        mMediaPlayer.setSurface(surfaceView.getHolder().getSurface());

        try {
            if (mLiveWallpaperInfo.mSource == LiveWallpaperInfo.Source.SOURCE_ASSETS) {
                AssetManager aManager = getApplicationContext().getAssets();
                AssetFileDescriptor fileDescriptor = aManager.openFd(mLiveWallpaperInfo.mPath);
                mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            } else {
                mMediaPlayer.setDataSource(mLiveWallpaperInfo.mPath);
            }
            //循环播放我们的视频
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setVolume(1, 1);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    public static void startWallpaperPreviewActivity(Context context, LiveWallpaperInfo liveWallpaperInfo) {
        Intent intent = new Intent(context, WallpaperPreviewActivity.class);
        intent.putExtra(LIVE_WALLPAPER_INFO, liveWallpaperInfo);
        context.startActivity(intent);
    }
}
