package com.cry.opengldemo5.shape;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;

import com.cry.opengldemo5.common.Constant;
import com.cry.opengldemo5.render.GLESUtils;
import com.cry.opengldemo5.render.ViewGLRender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 剪裁空间。
 * OpenGL 期望分量都在-w和w之间
 * <p>
 * 透视除法-线性投影
 * <p>
 * 同质化坐标 (1,1,1,1)(2,2,2,2)(3,3,3,3)在透视之后，都会映射到(1,1,1)
 * <p>
 * 除以w的优势
 * 可以在正交投影和透视投影之间切换。保留z分量作为深度缓冲区(depth buffer)之间有好处
 * 其实就是用w来表现近大远小的特点
 * <p>
 * <p>
 * 定义透视投影。
 * 利用z分量，把它作为物体与焦点的距离，并且把这个距离映射到w。这个位置越大，w值就越大，所得的物体就越小。
 * <p>
 * 因为frustumM()有缺陷。而perspectiveM支持  Build.VERSION_CODES.ICE_CREAM_SANDWICH api 14上面可以使用。
 * 所以其实我们使用perspectiveM就可以了
 * <p>
 * <p>
 * 利用模型矩阵来平移位置
 * <p>
 * * 解耦之后有三种矩阵
 * <p>
 * 模型矩阵 控制物体的移动和旋转
 * 视图矩阵 相当于一个相机。控制相机观看物体的视角
 * 投影矩阵 帮助创造三维的幻想
 * <p>
 * 最后的矩阵为 = 投影矩阵*视图矩阵*模型矩阵
 */
public class Triangle3DShapeRender extends ViewGLRender {
    /**
     * 更新shader的位置
     */
    private static final String VERTEX_SHADER_FILE = "shape/triangle_matrix_color_vertex_shader.glsl";
    private static final String FRAGMENT_SHADER_FILE = "shape/triangle_matrix_color_fragment_shader.glsl";
    private static final String A_POSITION = "a_Position";
    private static final String A_COLOR = "a_Color";


    //在数组中，一个顶点需要3个来描述其位置，需要3个偏移量
    private static final int COORDS_PER_VERTEX = 4;
    //颜色信息的偏移量
    private static final int COORDS_PER_COLOR = 3;

    //在数组中，描述一个顶点，总共的顶点需要的偏移量。这里因为只有位置顶点，所以和上面的值一样
    private static final int TOTAL_COMPONENT_COUNT = COORDS_PER_VERTEX + COORDS_PER_COLOR;
    //一个点需要的byte偏移量。
    private static final int STRIDE = TOTAL_COMPONENT_COUNT * Constant.BYTES_PER_FLOAT;

    //顶点的坐标系
    private static float TRIANGLE_COLOR_COORDS[] = {
            //Order of coordinates: X, Y, Z,w, R,G,B,
            0.5f, 0.5f, 0.0f,2f, 1.f, 0f, 0f, // top
            -0.5f, -0.5f, 0.0f,1f, 0.f, 1f, 0f,  // bottom left
            0.5f, -0.5f, 0.0f,1f, 0.f, 0f, 1f // bottom right
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
    //模型矩阵
    private float[] mModelMatrix = new float[16];

    private Matrix mViewMatrix;

    //投影矩阵
    private float[] mProjectionMatrix = new float[16];
    private int uMatrix;

    public Triangle3DShapeRender(Context context) {
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
//        //主要还是长宽进行比例缩放
        float aspectRatio = width > height ?
                (float) width / (float) height :
                (float) height / (float) width;
        /**
         * fovy是表示视角 45du 的视角来创建一个投影
         */
        Matrix.perspectiveM(mProjectionMatrix, 0, 45, aspectRatio, 1f, 10f);

        //设置模型矩阵.沿着z轴平移-2
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0f, 0f, -2f);

        //添加旋转
        Matrix.rotateM(mModelMatrix,0,-60f,1f,0,0);


        //缓存相乘的结果
        float[] temp = new float[16];
        Matrix.multiplyMM(temp, 0, mProjectionMatrix, 0, mModelMatrix, 0);
        //最后复制到投影矩阵中
        System.arraycopy(temp, 0, mProjectionMatrix, 0, temp.length);

    }

    //在OnDrawFrame中进行绘制
    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);

        //传递给着色器
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, mProjectionMatrix, 0);

        //绘制三角形.
        //draw arrays的几种方式 GL_TRIANGLES三角形 GL_TRIANGLE_STRIP三角形带的方式(开始的3个点描述一个三角形，后面每多一个点，多一个三角形) GL_TRIANGLE_FAN扇形(可以描述圆形)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTEX_COUNT);

    }
}
