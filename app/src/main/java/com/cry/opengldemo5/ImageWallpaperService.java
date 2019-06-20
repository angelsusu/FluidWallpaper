package com.cry.opengldemo5;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.cry.opengldemo5.render.DealTouchEvent;
import com.cry.opengldemo5.render.GLESUtils;
import com.cry.opengldemo5.shape.FluidSimulatorRender;

/**
 * Created by subeiting on 2019/6/14.
 */
public class ImageWallpaperService extends WallpaperService {

    private MyEngine engine;

    @Override
    public void onCreate() {
        Log.d("ImageWallpaperService", "onCreate");
    }

    @Override
    public Engine onCreateEngine() {
        Log.d("ImageWallpaperService", "onCreateEngine");
        engine = new MyEngine();
        return engine;
    }

    class MyEngine extends Engine {
        //获取SurfaceHolder时调用

        private WallpaperGLSurfaceView glSurfaceView;
        private DealTouchEvent dealTouchEvent;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            glSurfaceView = new WallpaperGLSurfaceView(ImageWallpaperService.this);
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
                FluidSimulatorRender render = new FluidSimulatorRender(ImageWallpaperService.this);
                dealTouchEvent = render;
                glSurfaceView.setDelegate(render);
                glSurfaceView.setEGLContextClientVersion(2);
                //设置自己的Render.Render 内进行图形的绘制
                glSurfaceView.setRenderer(render);
            } else {
                Log.d("ImageWallpaperService", "not SupportEs2");
            }
            Log.d("ImageWallpaperService", "onSurfaceCreated");
        }

        //Surface销毁时回调
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            glSurfaceView.onWallpaperDestroy();
            Log.d("ImageWallpaperService", "onSurfaceDestroyed");
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
        Intent intent = new Intent(
                WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(context, ImageWallpaperService.class));
        context.startActivity(intent);
    }
}