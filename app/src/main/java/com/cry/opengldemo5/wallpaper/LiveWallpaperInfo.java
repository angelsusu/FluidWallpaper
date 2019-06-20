package com.cry.opengldemo5.wallpaper;

import java.io.Serializable;

/**
 * Created by subeiting on 2019/6/19.
 * 壁纸信息
 */
public class LiveWallpaperInfo implements Serializable {

    public static class WallpaperType {
        public static final int WALLPAPER_TYPE_UNKNOW = 0;
        public static final int WALLPAPER_TYPE_IMAGE = 1;
        public static final int WALLPAPER_TYPE_VIDEO = 2;
    }

    public static class Source {
        public static final int SOURCE_UNKNOW = 0;
        public static final int SOURCE_ASSETS = 1;
        public static final int SOURCE_USER_ALBUM = 2;
    }

    public int mWallpaperType = WallpaperType.WALLPAPER_TYPE_UNKNOW;
    public int mSource = Source.SOURCE_UNKNOW; //视频对应的来源
    public int mResourcesId = 0;   //资源id
    public String mPath = "";  //视频对应的路径

    public static LiveWallpaperInfo createImageWallpaperInfo(int resourcesId, String path, int source) {
        return new LiveWallpaperInfo(WallpaperType.WALLPAPER_TYPE_IMAGE, resourcesId, path, source);
    }

    public static LiveWallpaperInfo createVideoWallpaperInfo(String videoPath, int videoSource) {
        return new LiveWallpaperInfo(WallpaperType.WALLPAPER_TYPE_VIDEO, 0, videoPath, videoSource);
    }

    private LiveWallpaperInfo(int wallpaperType, int resourcesId, String path, int source) {
        mWallpaperType = wallpaperType;
        mPath = path;
        mSource = source;
        mResourcesId = resourcesId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LiveWallpaperInfo) {
            LiveWallpaperInfo info = (LiveWallpaperInfo) obj;
            return info.mSource == mSource && info.mPath.equals(mPath)
                    && info.mWallpaperType == mWallpaperType && info.mResourcesId == mResourcesId;
        }
        return false;
    }
}
