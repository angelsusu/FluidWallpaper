/*
 *
 * CameraFilter.java
 * 
 * Created by Wuwang on 2016/11/19
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.cry.opengldemo5.camera.gles.filter;

import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.util.Arrays;

/**
 * Description: 相机的基本滤镜。提供矩阵转换等功能
 */
public class OesFilter2 extends AFilter {

    private int mHCoordMatrix;
    private float[] mCoordMatrix = Arrays.copyOf(OM, 16);
    private int mUChangeColor;
    //黑白图片的公式：RGB 按照 0.2989 R，0.5870 G 和 0.1140 B 的比例构成像素灰度值。
    float[] grayFilterColorData = {0.299f, 0.587f, 0.114f};
//    float[] coolFilterColorData = {0.0f, 0.0f, 0.1f};

    public OesFilter2(Resources mRes) {
        super(mRes);
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/oes_base_vertex.glsl", "shader/oes_base_filter_fragment.glsl");
        mHCoordMatrix = GLES20.glGetUniformLocation(mProgram, "vCoordMatrix");
        mUChangeColor = GLES20.glGetUniformLocation(mProgram, "u_ChangeColor");


    }

    public void setCoordMatrix(float[] matrix) {
        this.mCoordMatrix = matrix;
    }

    @Override
    protected void onSetExpandData() {
        super.onSetExpandData();
        GLES20.glUniformMatrix4fv(mHCoordMatrix, 1, false, mCoordMatrix, 0);
        //设置自己的颜色矩阵
        GLES20.glUniform3fv(mUChangeColor, 1, grayFilterColorData, 0);
    }

    @Override
    protected void onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + getTextureType());
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, getTextureId());
        GLES20.glUniform1i(mHTexture, getTextureType());
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }

}
