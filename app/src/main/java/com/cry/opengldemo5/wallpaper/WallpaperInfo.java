package com.cry.opengldemo5.wallpaper;

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

    public int mWallpaperType = WallpaperType.WALLPAPER_TYPE_UNKNOW;
    public int mImgResId = -1;   //图片对应的资源id
    public String mVideoPath = "";  //视频对应的路径

    public static WallpaperInfo createImageWallpaperInfo(int imgResId) {
        return new WallpaperInfo(WallpaperType.WALLPAPER_TYPE_IMAGE, imgResId, "");
    }

    public static WallpaperInfo createVideoWallpaperInfo(String videoPath) {
        return new WallpaperInfo(WallpaperType.WALLPAPER_TYPE_VIDEO, 0, videoPath);
    }

    private WallpaperInfo(int wallpaperType, int imgResId, String videoPath) {
        mWallpaperType = wallpaperType;
        mImgResId = imgResId;
        mVideoPath = videoPath;
    }
}
