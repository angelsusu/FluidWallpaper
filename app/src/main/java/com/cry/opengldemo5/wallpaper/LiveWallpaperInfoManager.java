package com.cry.opengldemo5.wallpaper;

/**
 * Created by subeiting on 2019/6/19.
 */
public class LiveWallpaperInfoManager {
    private LiveWallpaperInfo mLiveWallpaperInfo = null;
    private boolean mIsChanged = false;

    private LiveWallpaperInfoManager() {

    }

    private static class SingletonHolder {
        private static final LiveWallpaperInfoManager sInstance = new LiveWallpaperInfoManager();
    }

    public static LiveWallpaperInfoManager getInstance() {
        return SingletonHolder.sInstance;
    }

    public void setCurrentWallpaperInfo(LiveWallpaperInfo liveWallpaperInfo) {
        mIsChanged = (mLiveWallpaperInfo == null || !liveWallpaperInfo.equals(mLiveWallpaperInfo));
        mLiveWallpaperInfo = liveWallpaperInfo;
    }

    public LiveWallpaperInfo getCurrentWallpaperInfo() {
        return mLiveWallpaperInfo;
    }

    public boolean isChanged() {
        return mIsChanged;
    }

    public void resetChanged() {
        mIsChanged = false;
    }
}
