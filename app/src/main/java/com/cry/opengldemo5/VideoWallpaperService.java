package com.cry.opengldemo5;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.cry.opengldemo5.wallpaper.LiveWallpaperInfo;
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfoManager;

import java.io.IOException;

/**
 * Created by xieguohua on 2019/6/19.
 */
public class VideoWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        Log.d("data", "onCreateEngine: ");
        return new MyEngine();
    }

    class MyEngine extends Engine {

        private MediaPlayer mediaPlayer = new MediaPlayer();

        @Override
        public SurfaceHolder getSurfaceHolder() {
            return super.getSurfaceHolder();
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
        }

        //手势移动时回调
        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
        }

        //Surface创建时回调
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            play(holder);
        }

        //Surface销毁时回调
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mediaPlayer.release();
            mediaPlayer = null;
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
        }

        private void play(SurfaceHolder holder) {
            LiveWallpaperInfo liveWallpaperInfo = LiveWallpaperInfoManager.getInstance().getCurrentWallpaperInfo();
            mediaPlayer.setSurface(holder.getSurface());
            try {
                if (liveWallpaperInfo.mSource == LiveWallpaperInfo.Source.SOURCE_ASSETS) {
                    AssetManager aManager = getApplicationContext().getAssets();
                    AssetFileDescriptor fileDescriptor = aManager.openFd(liveWallpaperInfo.mPath);
                    mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
                } else {
                    mediaPlayer.setDataSource(liveWallpaperInfo.mPath);
                }
                //循环播放我们的视频
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(1, 1);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void startWallpaper(Context context) {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(context, VideoWallpaperService.class));
        context.startActivity(intent);
    }
}