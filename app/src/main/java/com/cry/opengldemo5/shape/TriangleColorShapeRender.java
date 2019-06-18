package com.cry.opengldemo5.shape;

import android.content.Context;
import android.opengl.GLES20;

import com.cry.opengldemo5.common.Constant;
import com.cry.opengldemo5.render.GLESUtils;
import com.cry.opengldemo5.render.ViewGLRender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 给TriangleShapeRender.添加颜色。
 * <p>
 * 0理解颜色的添加. 使用varying变量给他添加颜色的渐变。OpenGL是根据边进行颜色差值的。
 * <p>
 * 0.直线的差值方式
 * 1. 面内的差值方式：在三角形面内的任意一点。到三角形端点的距离作为差值，
 * <p>
 * 1. 修改着色器代码。
 * 0.在顶点着色器中添加 varying 变量接受混合的结果
 * 1.在片段着色器中将 顶点着色器中传递的 varying变量传递给 gl_FragColor
 * <p>
 * 2. 修改三角形的数组，添加颜色的信息
 * 3. 激活颜色信息的变量
 * <p>
 * Created by a2957 on 2018/5/3.
 */
public class TriangleColorShapeRender extends ViewGLRender {
    /**
     * 更新shader的位置
     */
    private static final String VERTEX_SHADER_FILE = "shape/triangle_color_vertex_shader.glsl";
    private static final String FRAGMENT_SHADER_FILE = "shape/triangle_color_fragment_shader.glsl";
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
            0.5f, 0.5f, 0.0f,     1.f,0f,0f, // top
            - 0.5f, -0.5f, 0.0f,  0.f,1f,0f,  // bottom left
            0.5f, -0.5f, 0.0f,    0.f,0f,1f // bottom right
    };
    //设置颜色，依次为红绿蓝和透明通道。
    //因为颜色是常量，所以用单独的数据表示？
//    private static float TRIANGLE_COLOR[] = {1.0f, 1.0f, 1.0f, 1.0f};
    private static final int VERTEX_COUNT = TRIANGLE_COLOR_COORDS.length / TOTAL_COMPONENT_COUNT;
    private final Context context;

    //pragram的指针
    private int mProgramObjectId;
    //顶点数据的内存映射
    private final FloatBuffer mVertexFloatBuffer;

    public TriangleColorShapeRender(Context context) {
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
        //0.先去得到着色器的代码
        String vertexShaderCode = GLESUtils.readAssetShaderCode(context, VERTEX_SHADER_FILE);
        String fragmentShaderCode = GLESUtils.readAssetShaderCode(context, FRAGMENT_SHADER_FILE);
        //1.得到之后，进行编译。得到id
        int vertexShaderObjectId = GLESUtils.compileShaderCode(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShaderObjectId = GLESUtils.compileShaderCode(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        //3.继续套路。取得到program
        mProgramObjectId = GLES20.glCreateProgram();
        //将shaderId绑定到program当中
        GLES20.glAttachShader(mProgramObjectId, vertexShaderObjectId);
        GLES20.glAttachShader(mProgramObjectId, fragmentShaderObjectId);
        //4.最后，启动GL link program
        GLES20.glLinkProgram(mProgramObjectId);

        //将这里的代码移动到onCreated当中
        //0.先使用这个program?这一步应该可以放到onCreate中进行
        GLES20.glUseProgram(mProgramObjectId);

        int aPosition = GLES20.glGetAttribLocation(mProgramObjectId, A_POSITION);
        mVertexFloatBuffer.position(0);
        GLES20.glVertexAttribPointer(
                aPosition,  //上面得到的id
                COORDS_PER_VERTEX, //告诉他用几个偏移量来描述一个顶点
                GLES20.GL_FLOAT, false,
                STRIDE, //一个顶点需要多少个字节的偏移量
                mVertexFloatBuffer);

        GLES20.glEnableVertexAttribArray(aPosition);
        /*******************
         * 新增的颜色信息的代码*
         *******************/

//        //4.去取得颜色的信息
        int aColor = GLES20.glGetAttribLocation(mProgramObjectId, A_COLOR);

        //3.将坐标数据放入
        //这里需要position到第一个颜色变量的位置
        mVertexFloatBuffer.position(COORDS_PER_VERTEX);
        GLES20.glVertexAttribPointer(
                aColor,  //上面得到的id
                COORDS_PER_COLOR, //告诉他用几个偏移量来描述一个顶点
                GLES20.GL_FLOAT, false,
                STRIDE, //一个顶点需要多少个字节的偏移量
                mVertexFloatBuffer);
        //2.开始启用我们的position
        GLES20.glEnableVertexAttribArray(aColor);

//        //取出颜色
//        int uColor = GLES20.glGetUniformLocation(mProgramObjectId, "uColor");
//
//        //开始绘制
//        //设置绘制三角形的颜色
//        GLES20.glUniform4fv(
//                uColor,
//                1,
//                TRIANGLE_COLOR,
//                0
//        );
        //这里就不需要再去取到vColor的数据的。varying的数据传递？
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
    }

    //在OnDrawFrame中进行绘制
    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);

        //绘制三角形.
        //draw arrays的几种方式 GL_TRIANGLES三角形 GL_TRIANGLE_STRIP三角形带的方式(开始的3个点描述一个三角形，后面每多一个点，多一个三角形) GL_TRIANGLE_FAN扇形(可以描述圆形)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTEX_COUNT);

    }
}
