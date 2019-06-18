package com.cry.opengldemo5.shape;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.cry.opengldemo5.common.Constant;
import com.cry.opengldemo5.render.GLESUtils;
import com.cry.opengldemo5.render.ViewGLRender;
import com.cry.opengldemo5.shape.base.Circle;
import com.cry.opengldemo5.shape.base.Cylinder;
import com.cry.opengldemo5.shape.base.Point;
import com.cry.opengldemo5.shape.base.ShapeBuilder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 从Triangle3DShapeRender复制
 * <p>
 * 想要实现 圆柱体
 * 圆柱体和圆很像。圆柱体是由两个面和一个侧面构成的
 *
 * 1. 绘制的顺序依赖于当前的观察点，是计算变得复杂
 * 2. 会为看不见的东西进行绘制，造成了浪费
 *
 * 深度缓冲区的技术为我们提供了一个更好的解决方案。
 * 他是一个特殊的缓冲区。用于记录屏幕上每个片段的深度。
 * 当缓存区打开时，OpenGL会为每个片段执行深度测试算法。如果这个片段比已经存在的片段更近，就会绘制他，否则，就会丢掉它
 *
 * 开启深度测试算法
 * GLES20.glEnable(GLES20.GL_DEPTH_TEST);
 *
 * 告诉OpenGL
 * 每一帧上也要清空深度缓存区
 * GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);
 *
 * 调整深度算法
 * glDepthFunc(GL_LEQUAL) GL_LESS
 * glDepthMask(false) 保持开启的情况下，手动控制开关
 *
 * 剔除？
 * glEnable(GL_CULL_FACE) 的方式
 *
 */
public class Cylinder3DShapeRender extends ViewGLRender {
    /**
     * 更新shader的位置
     */
    private static final String VERTEX_SHADER_FILE = "shape/triangle_matrix_color_vertex_shader.glsl";
    private static final String FRAGMENT_SHADER_FILE = "shape/triangle_matrix_color_fragment_shader.glsl";
    private static final String A_POSITION = "a_Position";
    private static final String A_COLOR = "a_Color";


    //在数组中，一个顶点需要3个来描述其位置，需要3个偏移量
    private static final int COORDS_PER_VERTEX = 3;
    //颜色信息的偏移量
    private static final int COORDS_PER_COLOR = 3;

    //在数组中，描述一个顶点，总共的顶点需要的偏移量。这里因为只有位置顶点，所以和上面的值一样
    private static final int TOTAL_COMPONENT_COUNT = COORDS_PER_VERTEX + COORDS_PER_COLOR;
    //一个点需要的byte偏移量。
    private static final int STRIDE = TOTAL_COMPONENT_COUNT * Constant.BYTES_PER_FLOAT;

    private final Context context;

    //pragram的指针
    private int mProgramObjectId;
    //顶点数据的内存映射
    private final FloatBuffer mVertexFloatBuffer;

    /*
    添加矩阵

     */
    private static final String U_MATRIX = "u_Matrix";
    //模型矩阵
    private float[] mModelMatrix = new float[16];

    private Matrix mViewMatrix;

    //投影矩阵
    private float[] mProjectionMatrix = new float[16];
    private int uMatrix;
    private int mCircleVertexNum = 0;
    private final int mCylinderVertexNum;

    public Cylinder3DShapeRender(Context context) {
        this.context = context;

        //上圆和下圆的点，难道不能复用吗？
        Point centerTop = new Point(0f, 0.3f, 0f);
        Point centerBottom = new Point(0f, -0.3f, 0f);
        Point centerCylinder = new Point(0f, 0.0f, 0f);
        float height = centerTop.y - centerBottom.y;
//        float height = 1f;
        float radius = 0.3f;
        //多少个点来切分这个圆.越多的切分。越圆
        int numbersRoundCircle = 360;
        Circle circleTop = new Circle(centerTop, radius);
        Circle circleBottom = new Circle(centerBottom, radius);

        float[] circleTopCoords = ShapeBuilder.create3DCircleCoords(circleTop, numbersRoundCircle, TOTAL_COMPONENT_COUNT);
        float[] circleBottomCoords = ShapeBuilder.create3DCircleCoords(circleBottom, numbersRoundCircle, TOTAL_COMPONENT_COUNT);

        Cylinder cylinder = new Cylinder(centerCylinder, radius, height);
        float[] cylinderCoords = ShapeBuilder.create3DCylinderCoords(cylinder, numbersRoundCircle, TOTAL_COMPONENT_COUNT);

        float[] targetCoords = new float[
                circleTopCoords.length +
                        circleBottomCoords.length +
                        cylinderCoords.length];
        //将这些点合并
        System.arraycopy(circleTopCoords, 0, targetCoords, 0, circleTopCoords.length);
        System.arraycopy(circleBottomCoords, 0, targetCoords, circleTopCoords.length, circleBottomCoords.length);
        System.arraycopy(cylinderCoords, 0, targetCoords,
                +circleTopCoords.length
                        + circleBottomCoords.length
                , cylinderCoords.length);

        mCircleVertexNum = ShapeBuilder.getCircleVertexNum(numbersRoundCircle);
        mCylinderVertexNum = ShapeBuilder.getCylinderVertexNum(numbersRoundCircle);


        mVertexFloatBuffer = ByteBuffer
                .allocateDirect(targetCoords.length * Constant.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(targetCoords);
        mVertexFloatBuffer.position(0);


    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);

        String vertexShaderCode = GLESUtils.readAssetShaderCode(context, VERTEX_SHADER_FILE);
        String fragmentShaderCode = GLESUtils.readAssetShaderCode(context, FRAGMENT_SHADER_FILE);
        int vertexShaderObjectId = GLESUtils.compileShaderCode(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShaderObjectId = GLESUtils.compileShaderCode(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgramObjectId = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgramObjectId, vertexShaderObjectId);
        GLES20.glAttachShader(mProgramObjectId, fragmentShaderObjectId);
        GLES20.glLinkProgram(mProgramObjectId);

        GLES20.glUseProgram(mProgramObjectId);

        int aPosition = GLES20.glGetAttribLocation(mProgramObjectId, A_POSITION);
        mVertexFloatBuffer.position(0);
        GLES20.glVertexAttribPointer(
                aPosition,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                STRIDE,
                mVertexFloatBuffer);

        GLES20.glEnableVertexAttribArray(aPosition);

        int aColor = GLES20.glGetAttribLocation(mProgramObjectId, A_COLOR);

        mVertexFloatBuffer.position(COORDS_PER_VERTEX);
        GLES20.glVertexAttribPointer(
                aColor,
                COORDS_PER_COLOR,
                GLES20.GL_FLOAT, false,
                STRIDE,
                mVertexFloatBuffer);
        GLES20.glEnableVertexAttribArray(aColor);

        uMatrix = GLES20.glGetUniformLocation(mProgramObjectId, U_MATRIX);
        //开启深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
        float aspectRatio =
                (float) width / (float) height ;
        /**
         * fovy是表示视角 45du 的视角来创建一个投影
         */
        Matrix.perspectiveM(mProjectionMatrix, 0, 45, aspectRatio, 1f, 10f);

        //设置模型矩阵.沿着z轴平移-2
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0f, 0f, -2f);

        //添加旋转
        Matrix.rotateM(mModelMatrix, 0, 60f, 1f, 0.5f, 1f);


        //缓存相乘的结果
        float[] temp = new float[16];
        Matrix.multiplyMM(temp, 0, mProjectionMatrix, 0, mModelMatrix, 0);
//        最后复制到投影矩阵中
        System.arraycopy(temp, 0, mProjectionMatrix, 0, temp.length);
    }

    //在OnDrawFrame中进行绘制
    @Override
    public void onDrawFrame(GL10 gl) {
//        super.onDrawFrame(gl);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);

        //传递给着色器
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, mProjectionMatrix, 0);

        //绘制三角形.
        //draw arrays的几种方式 GL_TRIANGLES三角形 GL_TRIANGLE_STRIP三角形带的方式(开始的3个点描述一个三角形，后面每多一个点，多一个三角形) GL_TRIANGLE_FAN扇形(可以描述圆形)

        //画top面
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, mCircleVertexNum);
        //画bottom面
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, mCircleVertexNum, mCircleVertexNum);
        //画侧面
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, mCircleVertexNum * 2, mCylinderVertexNum);


    }
}
