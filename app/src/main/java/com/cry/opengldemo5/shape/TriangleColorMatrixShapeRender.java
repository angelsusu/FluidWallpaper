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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 添加变化矩阵。让图形在手机上看起来正常一点
 *
 * <p>
 * - 归一化设备坐标系。将设备的坐标都归一
 * - 虚拟坐标空间。
 * 通过正交投影的方式。将虚拟坐标变换成归一化设备坐标时。实际上定义了三维世界的部分区域
 * <p>
 * <p>
 * 矩阵的复习。
 * 单位矩阵
 * 平移矩阵。可以让物体平移。
 * 表示位置的矩阵。[x,y,z,w] openGL通常会把w通常为1。正交矩阵之后，w就不为1了
 * <p>
 * 正交投影。
 * 通过正交投影的矩阵变化，让图形显示的像是正常的。不需要考虑设备屏幕适配的相关因素。
 * 透视出发。
 * 其实就是得到一个方向的视图。可以想象成是正视图。
 * <p>
 * <p>
 * orthoM()
 * float[] 目标数组。只要的有16个元素，才能存储正交投影矩阵
 * mOffset 结果矩阵起始的偏移量
 * left    x轴的最小范围
 * right   x轴的最大范围
 * bottom  y轴的最小范围
 * top     y轴的最大范围
 * near    z轴的最小范围
 * far     z轴的最大范围
 * <p>
 * 正交矩阵会把所有再左右之间，上下之间和远近之间的事物映射到归一化设备坐标中。从-1到1的范围。
 * 和平移矩阵的主要区别是z是一个负值。效果是反转z轴。以为只，物体离得越远，z的负值就越小
 * <p>
 * 原因是归一化的设备坐标系使用的是左手。而OpenGL使用的是右手。所以 =>归一化坐标，就需要反过来。
 * <p>
 * <p>
 * <p>
 * <p>
 * 0 .更新着色器的代码
 * 1. 在onChange方法内设置矩阵
 */
public class TriangleColorMatrixShapeRender extends ViewGLRender {
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

    //顶点的坐标系
    private static float TRIANGLE_COLOR_COORDS[] = {
            //Order of coordinates: X, Y, Z, R,G,B,
            0.5f, 0.5f, 0.0f, 1.f, 1f, 1f, // top
            -0.5f, -0.5f, 0.0f, 1.f, 1f, 1f,  // bottom left
            0.5f, -0.5f, 0.0f, 1.f, 1f, 1f // bottom right
    };

    private static final int VERTEX_COUNT = TRIANGLE_COLOR_COORDS.length / TOTAL_COMPONENT_COUNT;
    private final Context context;

    //pragram的指针
    private int mProgramObjectId;
    //顶点数据的内存映射
    private final FloatBuffer mVertexFloatBuffer;

    /*
    添加矩阵

     */
    private static final String U_MATRIX = "u_Matrix";
    private Matrix mModelMatrix;
    private Matrix mViewMatrix;

    //投影矩阵
    private float[] mProjectionMatrix = new float[16];
    private int uMatrix;

    public TriangleColorMatrixShapeRender(Context context) {
        this.context = context;
  /*
        0. 调用GLES20的包的方法时，其实就是调用JNI的方法。
        所以分配本地的内存块，将java数据复制到本地内存中，而本地内存可以不受垃圾回收的控制
        1. 使用nio中的ByteBuffer来创建内存区域。
        2. ByteOrder.nativeOrder()来保证，同一个平台使用相同的顺序
        3. 然后可以通过put方法，将内存复制过去。

        因为这里是Float，所以就使用floatBuffer
         */
        mVertexFloatBuffer = ByteBuffer
                .allocateDirect(TRIANGLE_COLOR_COORDS.length * Constant.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TRIANGLE_COLOR_COORDS);
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
        GLES20.glUniformMatrix4fv(uMatrix,1,false,mProjectionMatrix,0);

        //绘制三角形.
        //draw arrays的几种方式 GL_TRIANGLES三角形 GL_TRIANGLE_STRIP三角形带的方式(开始的3个点描述一个三角形，后面每多一个点，多一个三角形) GL_TRIANGLE_FAN扇形(可以描述圆形)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTEX_COUNT);

    }
}
