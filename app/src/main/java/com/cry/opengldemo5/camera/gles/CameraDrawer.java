package com.cry.opengldemo5.camera.gles;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.cry.opengldemo5.camera.gles.filter.AFilter;
import com.cry.opengldemo5.camera.gles.filter.OesFilter;
import com.cry.opengldemo5.camera.gles.filter.OesFilter2;
import com.cry.opengldemo5.utils.Gl2Utils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * DESCRIPTION:
 * 真实提供Render功能的Render.实现Render接口，
 * 只是为了方便重写生命周期方法
 * <p>
 * 在这里创建SurfaceTextureView 并应用纹理
 * <p>
 * Author: Cry
 * DATE: 2018/5/9 下午11:40
 */
public class CameraDrawer implements GLSurfaceView.Renderer {
    //相机的id
    private int mCameraId;

    //应用于相机的滤镜
    private final AFilter mOesFilter;
    private SurfaceTexture mSurfaceTexture;


    //绘制的纹理ID
    private int mTextureId;
    private int mSurfaceWidth;
    private int mSurfaceHeight;


    private int mPreviewWidth;
    private int mPreviewHeight;

    //视图矩阵。控制旋转和变化
    private float[] mModelMatrix = new float[16];

    public CameraDrawer(Resources res) {
        mOesFilter = new OesFilter2(res);
    }

    //需要提供给Camera的surfaceTextureView
    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //在这开始绘制纹理和创建surfaceView
        int[] textureObjectIds = new int[1];
        //生成纹理iD
        GLES20.glGenTextures(1, textureObjectIds, 0);
        int textureObjectId = textureObjectIds[0];
        //将纹理ID绑定到GL_TEXTURE_EXTERNAL_OES
        //这里需要注意的是GL_TEXTURE_EXTERNAL_OES ，对应android 相机必须要的
        int targetGLenum = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        GLES20.glBindTexture(targetGLenum, textureObjectId);
        //设置放大缩小。设置边缘测量
        GLES20.glTexParameterf(targetGLenum,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(targetGLenum,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(targetGLenum,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(targetGLenum,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        mTextureId = textureObjectId;

        //应用这个纹理，来创建SurfaceTexture
        mSurfaceTexture = new SurfaceTexture(mTextureId);

        //创建滤镜.同时绑定滤镜上
        mOesFilter.create();
        mOesFilter.setTextureId(mTextureId);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //在这里监听到尺寸的改变。做出对应的变化
        this.mSurfaceWidth = width;
        this.mSurfaceHeight = height;
        calculateMatrix();
    }

    //计算需要变化的矩阵
    private void calculateMatrix() {
        Gl2Utils.getShowMatrix(mModelMatrix, mPreviewWidth, mPreviewHeight, this.mSurfaceWidth, this.mSurfaceHeight);

        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {  //前置摄像头
            Gl2Utils.flip(mModelMatrix, true, false);
            Gl2Utils.rotate(mModelMatrix, 90);
        } else {  //后置摄像头
            int rotateAngle = 270;
            Gl2Utils.rotate(mModelMatrix, rotateAngle);
        }
        mOesFilter.setMatrix(mModelMatrix);
    }

    public void setPreviewSize(int previewWidth, int previewHeight) {
        this.mPreviewWidth = previewWidth;
        this.mPreviewHeight = previewHeight;
        calculateMatrix();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //绘画的时候，需要要求surfaceView 更新
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
        }
        //然后在开始我们的动画
        mOesFilter.draw();
    }

    public void setCameraId(int cameraId) {
        this.mCameraId = cameraId;
    }
}
