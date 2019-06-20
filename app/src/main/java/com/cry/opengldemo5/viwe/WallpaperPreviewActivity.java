package com.cry.opengldemo5.viwe;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.cry.opengldemo5.ImageWallpaperService;
import com.cry.opengldemo5.R;
import com.cry.opengldemo5.VideoWallpaperService;
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfo;
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfoManager;

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
        mLiveWallpaperInfo = (LiveWallpaperInfo) getIntent().getSerializableExtra(LIVE_WALLPAPER_INFO);
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
                LiveWallpaperInfoManager.getInstance().setCurrentWallpaperInfo(mLiveWallpaperInfo);
                if (mLiveWallpaperInfo.mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE) {
                    ImageWallpaperService.startWallpaper(WallpaperPreviewActivity.this);
                } else if (mLiveWallpaperInfo.mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_VIDEO) {
                    WallpaperInfo info = WallpaperManager.getInstance(WallpaperPreviewActivity.this).getWallpaperInfo();
                    if(info != null && VideoWallpaperService.VIDEO_SERVICE_NAME.equals(info.getServiceName())) {
                        Intent intent = new Intent();
                        intent.setAction(VideoWallpaperService.VIDEO_BROADCAST_ACTION);
                        sendBroadcast(intent);
                    } else {
                        VideoWallpaperService.startWallpaper(WallpaperPreviewActivity.this);
                    }
                }
                finish();
            }
        });
    }

    private void initImageView() {
        ImageView imageView = findViewById(R.id.iv_preview);
        imageView.setVisibility(View.VISIBLE);
        if (mLiveWallpaperInfo.mSource == LiveWallpaperInfo.Source.SOURCE_ASSETS) {
            imageView.setBackgroundResource(mLiveWallpaperInfo.mResourcesId);
        } else {
            imageView.setImageBitmap(BitmapFactory.decodeFile(mLiveWallpaperInfo.mPath));
        }
    }

    private void initSurfaceView() {
        SurfaceView surfaceView = findViewById(R.id.sv_preview);
        surfaceView.setVisibility(View.VISIBLE);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mMediaPlayer.setSurface(holder.getSurface());
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
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
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
