package com.cry.opengldemo5.wallpaper;

/**
 * Created by subeiting on 2019/6/19.
 */
public class WallpaperInfoManager {
    private WallpaperInfo mWallpaperInfo = null;

    private WallpaperInfoManager() {

    }

    private static class SingletonHolder {
        private static final WallpaperInfoManager sInstance = new WallpaperInfoManager();
    }

    public static WallpaperInfoManager getInstance() {
        return SingletonHolder.sInstance;
    }

    public void setCurrentWallpaperInfo(WallpaperInfo wallpaperInfo) {
        mWallpaperInfo = wallpaperInfo;
    }

    public WallpaperInfo getCurrentWallpaperInfo() {
        return mWallpaperInfo;
    }
}
