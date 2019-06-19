package com.cry.opengldemo5.wallpaper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by subeiting on 2019/6/19.
 * 壁纸信息
 */
public class WallpaperInfo {

    public static class WallpaperType {
        public static final int WALLPAPER_TYPE_UNKNOW = 0;
        public static final int WALLPAPER_TYPE_IMAGE = 1;
        public static final int WALLPAPER_TYPE_VIDEO = 2;
    }

    public static class VideoSource {
        public static final int VIDEOSOURCE_UNKNOW = 0;
        public static final int VIDEOSOURCE_ASSETS = 1;
        public static final int VIDEOSOURCE_USER_ALBUM = 2;
    }

    public int mWallpaperType = WallpaperType.WALLPAPER_TYPE_UNKNOW;
    public Bitmap mImgBitmap = null;   //bitmap
    public String mVideoPath = "";  //视频对应的路径
    public int mVideoSource = WallpaperType.WALLPAPER_TYPE_UNKNOW; //视频对应的来源

    public static WallpaperInfo createImageWallpaperInfo(Bitmap bitmap) {
        return new WallpaperInfo(WallpaperType.WALLPAPER_TYPE_IMAGE, bitmap, "",
                WallpaperType.WALLPAPER_TYPE_UNKNOW);
    }

    public static WallpaperInfo createVideoWallpaperInfo(String videoPath, int videoSource) {
        return new WallpaperInfo(WallpaperType.WALLPAPER_TYPE_VIDEO, null, videoPath, videoSource);
    }

    private WallpaperInfo(int wallpaperType, Bitmap bitmap, String videoPath, int videoSource) {
        mWallpaperType = wallpaperType;
        mVideoPath = videoPath;
        mVideoSource = videoSource;
        mImgBitmap = bitmap;
    }
}
