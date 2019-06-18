package com.cry.opengldemo5.camera.gles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;

import com.cry.opengldemo5.camera.core.CameraAPI14;
import com.cry.opengldemo5.camera.core.ICamera;
import com.cry.opengldemo5.camera.core.ISize;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * DESCRIPTION: CameraRender Wrapper
 * Author: Cry
 * DATE: 2018/5/9 下午10:02
 */
public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer {

    public ICamera mCameraApi;
    private int mCameraId = 0;
    public CameraDrawer mCameraDrawer;
    private Runnable mRunnable;
    private int width;
    private int height;

    public CameraView(Context context) {
        super(context);
        initEGL(context);
    }

    //初始化OpenGL ES2.0
    private void initEGL(Context context) {
        setEGLContextClientVersion(2);
        setRenderer(this);
        //只有刷新之后，才会去重绘
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        mCameraApi = new CameraAPI14();
        mCameraDrawer = new CameraDrawer(context.getResources());
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCameraDrawer.onSurfaceCreated(gl, config);
        if (mRunnable != null) {
            mRunnable.run();
            mRunnable = null;
        }
        //在onSurfaceCreated中打开SurfaceView
        mCameraApi.open(mCameraId);
        //设置CameraDrawer
        mCameraDrawer.setCameraId(mCameraId);
        ISize previewSize = mCameraApi.getPreviewSize();
        width = previewSize.getWidth();
        height = previewSize.getHeight();
        mCameraDrawer.setPreviewSize(width, height);
        mCameraApi.setPreviewTexture(mCameraDrawer.getSurfaceTexture());
        //默认使用的GLThread
        mCameraDrawer.getSurfaceTexture().setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                requestRender();
            }
        });
        mCameraApi.preview();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCameraDrawer.onSurfaceChanged(gl, width, height);
        //设置ViewPort是必须要做的
        GLES20.glViewport(0, 0, width, height);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mCameraDrawer.onDrawFrame(gl);
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraApi.close();
    }

    int takePhotoFromGL = 1;

    public void takePhoto(ICamera.TakePhotoCallback callback) {
        if (takePhotoFromGL != 1) {
            if (mCameraApi != null) {
                mCameraApi.takePhoto(callback);
            }
        }else {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    //发送到GLThread中进行
//                sendImage(width,height);
                    ByteBuffer rgbaBuf = ByteBuffer.allocateDirect(width * height * 4);
                    rgbaBuf.position(0);
                    long start = System.nanoTime();
                    GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                            rgbaBuf);
                    long end = System.nanoTime();
                    callback.onTakePhoto(rgbaBuf.array(), width, height);
                }
            });
        }
    }

}
