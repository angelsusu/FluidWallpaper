package com.cry.opengldemo5.wallpaper;

import com.cry.opengldemo5.sp.PreferencesUtils;

/**
 * Created by subeiting on 2019/6/19.
 */
public class LiveWallpaperInfoManager {
    private LiveWallpaperInfo mLiveWallpaperInfo = null;
    private boolean mIsChanged = false;

    public static final String WALLPAPER_TYPE = "wallpaper_type";
    public static final String WALLPAPER_SOURCE = "wallpaper_type";
    public static final String WALLPAPER_RESID = "wallpaper_resid";
    public static final String WALLPAPER_PATH = "wallpaper_path";

    private LiveWallpaperInfoManager() {
        initData();
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
        saveWallpaperInfo();
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

    private void initData() {
        int type = PreferencesUtils.getInt(WALLPAPER_TYPE, LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_UNKNOW);
        int source = PreferencesUtils.getInt(WALLPAPER_SOURCE, LiveWallpaperInfo.Source.SOURCE_UNKNOW);
        int resId = PreferencesUtils.getInt(WALLPAPER_RESID, 0);
        String path = PreferencesUtils.getString(WALLPAPER_PATH, "");
        if (type == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE) {
            mLiveWallpaperInfo = LiveWallpaperInfo.createImageWallpaperInfo(resId, path, source);
        } else if (type == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_VIDEO) {
            mLiveWallpaperInfo = LiveWallpaperInfo.createVideoWallpaperInfo(path, source);
        }
    }

    private void saveWallpaperInfo() {
        if (mLiveWallpaperInfo == null) {
            return;
        }
        PreferencesUtils.putInt(WALLPAPER_TYPE, mLiveWallpaperInfo.mWallpaperType);
        PreferencesUtils.putInt(WALLPAPER_SOURCE, mLiveWallpaperInfo.mSource);
        PreferencesUtils.putInt(WALLPAPER_RESID, mLiveWallpaperInfo.mResourcesId);
        PreferencesUtils.putString(WALLPAPER_PATH, mLiveWallpaperInfo.mPath);
    }
}
