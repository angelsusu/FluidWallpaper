package com.cry.opengldemo5;

import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.opengl.GLSurfaceView;
import android.os.PowerManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.cry.opengldemo5.listener.ScreenListener;
import com.cry.opengldemo5.render.DealTouchEvent;
import com.cry.opengldemo5.render.FluidSimulatorRender;
import com.cry.opengldemo5.render.GLESUtils;
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfo;
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfoManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by subeiting on 2019/6/14.
 */
public class ImageWallpaperService extends WallpaperService {

    public static final String IMAGE_SERVICE_NAME = "com.cry.opengldemo5.ImageWallpaperService";

    private MyEngine engine;
    private ScreenListener mScreenListener;
    private int mIndex;
    private DevicePolicyManager policyManager;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate() {
        Log.d("ImageWallpaperService", "onCreate");
        mScreenListener = new ScreenListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScreenListener.unregister();
    }

    @Override
    public Engine onCreateEngine() {
        Log.d("ImageWallpaperService", "onCreateEngine");
        engine = new MyEngine();
        return engine;
    }

    class MyEngine extends Engine {
        //获取SurfaceHolder时调用

        private WallpaperGLSurfaceView mGLSurfaceView;
        private DealTouchEvent dealTouchEvent;
        private FluidSimulatorRender mRender;
        private boolean mIsUserPresent = true;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            mGLSurfaceView = new WallpaperGLSurfaceView(ImageWallpaperService.this);
        }

        @Override
        public SurfaceHolder getSurfaceHolder() {
            Log.d("ImageWallpaperService", "getSurfaceHolder");
            return super.getSurfaceHolder();
        }

        //手势移动时回调
        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            Log.d("ImageWallpaperService", "onOffsetsChanged");
        }

        //Surface创建时回调
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);

            boolean isSupportEs2 = GLESUtils.isSupportEs2(ImageWallpaperService.this);
            //表示支持
            if (isSupportEs2) {
                //创建一个GLSurfaceView
                mRender = new FluidSimulatorRender(ImageWallpaperService.this);
                dealTouchEvent = mRender;
                mGLSurfaceView.setDelegate(mRender);
                mGLSurfaceView.setEGLContextClientVersion(2);
                //设置自己的Render.Render 内进行图形的绘制
                mGLSurfaceView.setRenderer(mRender);
            } else {
                Log.d("ImageWallpaperService", "not SupportEs2");
            }
            Log.d("ImageWallpaperService", "onSurfaceCreated");
            registerScreenListener();
        }

        private void registerScreenListener() {
            mScreenListener.register(new ScreenListener.ScreenStateListener() {
                 @Override
                 public void onScreenOn() {
                     if (mRender != null) {
                         mRender.showDebutAnimation();
                     }
                 }

                 @Override
                 public void onScreenOff() {
                     mIsUserPresent = false;
                     updateWallpaper();
                 }

                 @Override
                 public void onUserPresent() {
                     mIsUserPresent = true;
                     if (isHome() && mRender != null) {
                         mRender.showDebutAnimation();
                     }
                 }
            });
        }

        private void updateWallpaper() {
            mIndex ++;
            if (mIndex > 4) {
                mIndex = 0;
            }

            LiveWallpaperInfo liveWallpaperInfo = createImageWallpaperInfo(R.drawable.test_wallpaper_one, "明天会更好");
            switch (mIndex) {
                case 0:
                    liveWallpaperInfo = createImageWallpaperInfo(R.drawable.wallpaper1, "相信明天会更好");
                    break;
                case 1:
                    liveWallpaperInfo = createImageWallpaperInfo(R.drawable.wallpaper2, "相信后天会更好");
                    break;
                case 2:
                    liveWallpaperInfo = createImageWallpaperInfo(R.drawable.wallpaper3, "相信大后天会更好");
                    break;
                case 3:
                    liveWallpaperInfo = createImageWallpaperInfo(R.drawable.wallpaper4, "相信以后会更好");
                    break;
                case 4:
                    liveWallpaperInfo = createImageWallpaperInfo(R.drawable.test_wallpaper_six, "相信未来会更好");
                    break;
                default:
                    break;
            }
            LiveWallpaperInfoManager.getInstance().setCurrentWallpaperInfo(liveWallpaperInfo);
            if (isHome() && mRender != null) {
                mRender.showDebutAnimation();
            }
        }

        private LiveWallpaperInfo createImageWallpaperInfo(int resourcesId, String text) {
            return LiveWallpaperInfo.createImageWallpaperInfo(resourcesId, "", LiveWallpaperInfo.Source.SOURCE_ASSETS,
                    text);
        }

        //Surface销毁时回调
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mGLSurfaceView.onWallpaperDestroy();
            Log.d("ImageWallpaperService", "onSurfaceDestroyed");
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            Log.d("data", "onVisibilityChanged: visible:" + visible);
            if (mRender!= null && visible && mIsUserPresent) {
                mRender.showDebutAnimation();
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            dealTouchEvent.onTouchEvent(event);
            Log.d("ImageWallpaperService", "onTouchEvent");
        }
    }

    class WallpaperGLSurfaceView extends GLSurfaceView {

        DealTouchEvent delegate = null;

        public void setDelegate(DealTouchEvent delegate) {
            this.delegate = delegate;
        }

        public WallpaperGLSurfaceView(Context context) {
            super(context);
        }

        @Override
        public SurfaceHolder getHolder() {
            return engine.getSurfaceHolder();
        }

        public void onWallpaperDestroy() {
            super.onDetachedFromWindow();
        }
    }

    public static void startWallpaper(Context context) {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(context, ImageWallpaperService.class));
        context.startActivity(intent);
    }

    /**
     * 获得属于桌面的应用的应用包名称
     *
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

    /**
     * 判断当前界面是否是桌面
     */
    private boolean isHome() {
        ActivityManager mActivityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        return getHomes().contains(rti.get(0).topActivity.getPackageName());
    }
}