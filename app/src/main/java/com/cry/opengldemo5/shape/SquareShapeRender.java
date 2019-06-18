package com.cry.opengldemo5.shape;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.cry.opengldemo5.common.Constant;
import com.cry.opengldemo5.render.GLESUtils;
import com.cry.opengldemo5.render.ViewGLRender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 从TriangleColorMatrixShapeRender 而来。
 * <p>
 * 正方形
 * 分析一下:
 * 正方形就是两个三角形。4个点构成
 * <p>
 * 1. 修改数组
 * 2. 添加buffer
 * 3.修改绘画
 */
public class SquareShapeRender extends ViewGLRender {
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

//    //正方形的点0
//    private static float SQUARE_COLOR_COORDS[] = {
//            //Order of coordinates: X, Y, Z, R,G,B,
//            -0.5f, 0.5f, 0.0f, 1.f, 0f, 0f,  //  0.top left RED
//            -0.5f, -0.5f, 0.0f, 0.f, 0f, 1f, //  1.bottom right Blue
//            0.5f, -0.5f, 0.0f, 0.f, 1f, 0f,  //  2.bottom left GREEN
//            0.5f, 0.5f, 0.0f, 0.f, 0f, 0f,   //  3.top right WHITE
//    };

 //正方形的点1
    private static float SQUARE_COLOR_COORDS[] = {
            //Order of coordinates: X, Y, Z, R,G,B,
            -0.5f, 0.5f, 0.0f, 1.f, 0f, 0f,  //  0.top left RED
            -0.5f, -0.5f, 0.0f, 0.f, 0f, 1f, //  1.bottom right Blue
            0.5f, 0.5f, 0.0f, 1f, 1f, 1f,   //  3.top right WHITE
            0.5f, -0.5f, 0.0f, 0.f, 1f, 0f,  //  2.bottom left GREEN
    };

//    /*
//    创建一个遍历的点的顺序.
//    0,1,2,0 一个三角形
//    0,3,2 另一个三角
//     */
//    private static short SQUARE_INDEX[] = {
//            0, 1, 2, 0, 3, 2
//    };

    private static final int VERTEX_COUNT = SQUARE_COLOR_COORDS.length / TOTAL_COMPONENT_COUNT;
    private final Context context;

    //pragram的指针
    private int mProgramObjectId;
    //顶点数据的内存映射
    private final FloatBuffer mVertexFloatBuffer;
//    private final ShortBuffer mIndexBuffer;

    /*
    添加矩阵

     */
    private static final String U_MATRIX = "u_Matrix";
    private Matrix mModelMatrix;
    private Matrix mViewMatrix;

    //投影矩阵
    private float[] mProjectionMatrix = new float[16];
    private int uMatrix;

    public SquareShapeRender(Context context) {
        this.context = context;

        mVertexFloatBuffer = ByteBuffer
                .allocateDirect(SQUARE_COLOR_COORDS.length * Constant.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(SQUARE_COLOR_COORDS);
        mVertexFloatBuffer.position(0);

//        /*
//        新增-为位置添加内存空间
//         */
//        mIndexBuffer = ByteBuffer
//                .allocateDirect(SQUARE_INDEX.length * Constant.BYTES_PER_SHORT)
//                .order(ByteOrder.nativeOrder())
//                .asShortBuffer()
//                .put(SQUARE_INDEX);
//        mIndexBuffer.position(0);
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


        /***************
         **新增代码******
         *************/
        uMatrix = GLES20.glGetUniformLocation(mProgramObjectId, U_MATRIX);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
        //主要还是长宽进行比例缩放
        float aspectRatio = width > height ?
                (float) width / (float) height :
                (float) height / (float) width;

        if (width > height) {
            //横屏。需要设置的就是左右。
            Matrix.orthoM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1, 1f, -1.f, 1f);
        } else {
            //竖屏。需要设置的就是上下
            Matrix.orthoM(mProjectionMatrix, 0, -1, 1f, -aspectRatio, aspectRatio, -1.f, 1f);
        }
    }

    //在OnDrawFrame中进行绘制
    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);

        //传递给着色器
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, mProjectionMatrix, 0);

        //绘制三角形.
        //draw arrays的几种方式 GL_TRIANGLES三角形 GL_TRIANGLE_STRIP三角形带的方式(开始的3个点描述一个三角形，后面每多一个点，多一个三角形) GL_TRIANGLE_FAN扇形(可以描述圆形)
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTEX_COUNT);
        //使用indexBuffer的方式
//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, SQUARE_INDEX.length, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer);
        //1.使用三角形带的方式
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0, VERTEX_COUNT);
    }
}
