package com.cry.opengldemo5.wallpaper;

/**
 * Created by subeiting on 2019/6/19.
 * 壁纸信息
 */
public class WallpaperInfo {

    private static class WallpaperType {
        public static final int WALLPAPER_TYPE_UNKONW = 0;
        public static final int WALLPAPER_TYPE_IMAGE = 1;
        public static final int WALLPAPER_TYPE_VIDEO = 2;
    }

    public int mWallpaperType = WallpaperType.WALLPAPER_TYPE_UNKONW;
    public int mImgResId = -1;   //图片对应的资源id
    public String mVideoPath = "";  //视频对应的路径
}
