package com.cry.opengldemo5.camera.core;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DESCRIPTION: Camera1的Wrapper类。 API 14
 * Camera主要涉及的就是几个参数
 * <p>
 * 预览的图片大小
 * pic的图片大小
 * 对焦模式
 * 闪光灯模式
 *
 * 需要注意的是相机的旋转方向的问题。
 * 这里是由两个地方去控制预览图的方向的
 * 1. 通过设置相机的displayOritation 和 pramaras中的旋转方向。主要它可以保存在选摄像头的时候的参数中
 * 2. 因为预览图是 通过SurfaceTextureView 中显示。可以设置matrix来控制它的旋转。  如这里是手动去控制纹理的绘制的话，则可以自己控制viewMatrix来控制
 *
 * <p>
 * Author: Cry
 * DATE: 2018/5/9 下午10:05
 */
public class CameraAPI14 implements ICamera {
    //当前的id
    private int mCameraId;
    private Camera mCamera;
    public Camera.Parameters mCameraParameters;
    private AspectRatio mRatio;
    private Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    private final ISize.ISizeMap mPreviewSizes = new ISize.ISizeMap();
    private final ISize.ISizeMap mPictureSizes = new ISize.ISizeMap();

    private int mDesiredHeight = 1920;
    private int mDesiredWidth = 1080;
    private boolean mAutoFocus;
    public ISize mPreviewSize;
    public ISize mPicSize;

    private final AtomicBoolean isPictureCaptureInProgress = new AtomicBoolean(false);
    private TakePhotoCallback photoCallBack;


    public CameraAPI14() {
        mDesiredHeight = 1920;
        mDesiredWidth = 1080;
        //创建默认的比例.因为后置摄像头的比例，默认的情况下，都是旋转了270
        mRatio = AspectRatio.of(mDesiredWidth, mDesiredHeight).inverse();
    }

    @Override
    public boolean open(int cameraId) {
        if (mCamera != null) {
            releaseCamera();
        }
        mCameraId = cameraId;
        mCamera = Camera.open(cameraId);
        //开启Camera之后，必然要涉及到的操作就是设置参数
        if (mCamera != null) {
            mCameraParameters = mCamera.getParameters();
            mPreviewSizes.clear();
            //先收集参数
            for (Camera.Size size : mCameraParameters.getSupportedPreviewSizes()) {
                mPreviewSizes.add(new ISize(size.width, size.height));
            }

            mPictureSizes.clear();
            for (Camera.Size size : mCameraParameters.getSupportedPictureSizes()) {
                mPictureSizes.add(new ISize(size.width, size.height));
            }
            //挑选出最需要的参数
            adJustParametersByAspectRatio();
            return true;
        }
        return false;
    }

    private void adJustParametersByAspectRatio() {
        SortedSet<ISize> sizes = mPreviewSizes.sizes(mRatio);
        if (sizes == null) {  //表示不支持
            return;
        }
        //当前先不考虑Orientation
        ISize previewSize;
        mPreviewSize = new ISize(mDesiredWidth, mDesiredHeight);
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            previewSize = new ISize(mDesiredHeight, mDesiredWidth);
            ;
//            mCameraParameters.setRotation(90);
        } else {
            previewSize = mPreviewSize;
        }

        //默认去取最大的尺寸
        mPicSize = mPictureSizes.sizes(mRatio).last();

        mCameraParameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
        mCameraParameters.setPictureSize(mPicSize.getWidth(), mPicSize.getHeight());

        //设置对角和闪光灯
        setAutoFocusInternal(mAutoFocus);
        //先不设置闪光灯
//        mCameraParameters.setFlashMode("FLASH_MODE_OFF");

        //设置到camera中
        mCameraParameters.setRotation(90);
        mCamera.setParameters(mCameraParameters);
        mCamera.setDisplayOrientation(90);
    }

    private boolean setAutoFocusInternal(boolean autoFocus) {
        mAutoFocus = autoFocus;
//        if (isCameraOpened()) {
        final List<String> modes = mCameraParameters.getSupportedFocusModes();
        if (autoFocus && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        } else {
            mCameraParameters.setFocusMode(modes.get(0));
        }
        return true;
//        } else {
//            return false;
//        }
    }

    private boolean setFlashInternal(int flash) {
//        if (isCameraOpened()) {
//            List<String> modes = mCameraParameters.getSupportedFlashModes();
//            String mode = FLASH_MODES.get(flash);
//            if (modes != null && modes.contains(mode)) {
//                mCameraParameters.setFlashMode(mode);
//                mFlash = flash;
//                return true;
//            }
//            String currentMode = FLASH_MODES.get(mFlash);
//            if (modes == null || !modes.contains(currentMode)) {
//                mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//                mFlash = Constants.FLASH_OFF;
//                return true;
//            }
        return false;
//        } else {
//            mFlash = flash;
//            return false;
//        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void setAspectRatio(AspectRatio ratio) {
        this.mRatio = ratio;
    }

    @Override
    public boolean preview() {
        if (mCamera != null) {
            mCamera.startPreview();
            return true;
        }
        return false;
    }

    @Override
    public boolean switchTo(int cameraId) {
        close();
        open(cameraId);
        return false;
    }

    @Override
    public boolean close() {
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void setPreviewTexture(SurfaceTexture texture) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(texture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ISize getPreviewSize() {
        return mPreviewSize;
    }

    @Override
    public ISize getPictureSize() {
        return mPicSize;
    }

    @Override
    public void takePhoto(TakePhotoCallback callback) {
        this.photoCallBack = callback;

        if (getAutoFocus()) {
            mCamera.cancelAutoFocus();
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    takePictureInternal();
                }
            });
        } else {
            takePictureInternal();
        }


    }


    void takePictureInternal() {
        if (!isPictureCaptureInProgress.getAndSet(true)) {
            mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    isPictureCaptureInProgress.set(false);
                    if (photoCallBack != null) {
                        photoCallBack.onTakePhoto(data, mPreviewSize.getWidth(), mPreviewSize.getHeight());
                    }
                    camera.cancelAutoFocus();
                    camera.startPreview();
                }
            });
        }
    }


    boolean getAutoFocus() {
        String focusMode = mCameraParameters.getFocusMode();
        return focusMode != null && focusMode.contains("continuous");
    }


    @Override
    public void setOnPreviewFrameCallback(PreviewFrameCallback callback) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    callback.onPreviewFrame(data, mPreviewSize.getWidth(), mPreviewSize.getHeight());
                }
            });
        }
    }
}
