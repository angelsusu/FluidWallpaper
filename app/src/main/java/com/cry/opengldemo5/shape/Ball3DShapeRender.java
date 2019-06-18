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
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 想要实现 立方体
 * <p>
 * GL_POINTS
 * GL_LINES
 * GL_LINE_LOOP
 * GL_LINE_STRIP
 * GL_TRIANGLES               每三个顶之间绘制三角形，之间不连接
 * GL_TRIANGLE_STRIP          顺序在每三个顶点之间均绘制三角形。这个方法可以保证从相同的方向上所有三角形均被绘制。以V0V1V2,V1V2V3,V2V3V4……的形式绘制三角形
 * GL_TRIANGLE_FAN            以V0V1V2,V0V2V3,V0V3V4，……的形式绘制三角形
 * <p>
 * int GL_POINTS       //将传入的顶点坐标作为单独的点绘制
 * int GL_LINES        //将传入的坐标作为单独线条绘制，ABCDEFG六个顶点，绘制AB、CD、EF三条线
 * int GL_LINE_STRIP   //将传入的顶点作为折线绘制，ABCD四个顶点，绘制AB、BC、CD三条线
 * int GL_LINE_LOOP    //将传入的顶点作为闭合折线绘制，ABCD四个顶点，绘制AB、BC、CD、DA四条线。
 * int GL_TRIANGLES    //将传入的顶点作为单独的三角形绘制，ABCDEF绘制ABC,DEF两个三角形
 * int GL_TRIANGLE_FAN    //将传入的顶点作为扇面绘制，ABCDEF绘制ABC、ACD、ADE、AEF四个三角形
 * int GL_TRIANGLE_STRIP   //将传入的顶点作为三角条带绘制，ABCDEF绘制ABC,BCD,CDE,DEF四个三角形
 */
public class Ball3DShapeRender extends ViewGLRender {
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
    private FloatBuffer mVertexFloatBuffer;

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

    private int VERTEX_COUNT;

    public Ball3DShapeRender(Context context) {
        this.context = context;

        float[] dataPos = createBallPosition();

        mVertexFloatBuffer = ByteBuffer.allocateDirect(dataPos.length * TOTAL_COMPONENT_COUNT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(dataPos);
        mVertexFloatBuffer.position(0);

        VERTEX_COUNT = dataPos.length / TOTAL_COMPONENT_COUNT;
    }

    private float step = 5f;

    private float[] createBallPosition() {
        //创建球的坐标系
        //球上每一点的坐标 为 x=R*cos(a)*sin(b),y = R*sin(b),z = R*cos(a)*cos(b)
        //a是 圆心于该点的连线 和 xy屏幕的夹角 ，b是连线于xz屏幕的与z的夹角

        //球的半径
        float ballRadius = 0.35f;

        //纬度切分的大小
        int latitudePieces = 5;
        int longitudePieces = 10;

        ArrayList<Float> dataPos = new ArrayList<>();

        //需要使用上下两个环构成一个带。来画面
        for (int latitude = -90; latitude < 90 + latitudePieces; latitude += latitudePieces) {
            //xy截面是圆
            double angleXY = latitude * (Math.PI / 180f);
            double angleXY2 = (latitude + latitudePieces) * (Math.PI / 180f);
            //计算出改纬度的每一个经度的坐标
            float rXZ = (float) Math.cos(angleXY) * ballRadius;
            float rYZ = (float) Math.sin(angleXY) * ballRadius;

            float rXZ2 = (float) Math.cos(angleXY2) * ballRadius;
            float rYZ2 = (float) Math.sin(angleXY2) * ballRadius;

            //xz截面也是圆
            for (int longitude = 0; longitude < 360 + longitudePieces; longitude += longitudePieces) {
                double angleXZ = longitude * (Math.PI / 180);
                double angleXZ2 = (longitude + longitudePieces) * (Math.PI / 180);

                float xXZ = (float) (rXZ * Math.cos(angleXZ));
                float yXZ = rYZ;
                float zXZ = (float) (rXZ * Math.sin(angleXZ));
                dataPos.add(xXZ);
                dataPos.add(yXZ);
                dataPos.add(zXZ);

                float cR = (float) (1f);
                float cG = (float) (1f);
                float cB = (float) (1f);
                dataPos.add(cR);
                dataPos.add(cG);
                dataPos.add(cB);

                float xXZ2 = (float) (rXZ2 * Math.cos(angleXZ2));
                float yXZ2 = rYZ2;
                float zXZ2 = (float) (rXZ2 * Math.sin(angleXZ2));
                dataPos.add(xXZ2);
                dataPos.add(yXZ2);
                dataPos.add(zXZ2);

                float cR2 = (float) (1f);
                float cG2 = (float) (1f);
                float cB2 = (float) (1f);
                dataPos.add(cR2);
                dataPos.add(cG2);
                dataPos.add(cB2);
            }

        }
        int size = dataPos.size();
        float[] temp = new float[size];
        for (int i = 0; i < size; i++) {
            temp[i] = dataPos.get(i);
        }
        return temp;
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
//        //主要还是长宽进行比例缩放
        float aspectRatio =
                (float) width / (float) height;

        /**
         * fovy是表示视角 45du 的视角来创建一个投影
         */
        Matrix.perspectiveM(mProjectionMatrix, 0, 45, aspectRatio, 1f, 10f);

        //设置模型矩阵.沿着z轴平移-2
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0f, 0f, -2f);

        //添加旋转
        Matrix.rotateM(mModelMatrix, 0, 30f, 1f, -1f, -1f);


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
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        //传递给着色器
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, mProjectionMatrix, 0);

        GLES20.glDrawArrays(
                GLES20.GL_LINE_STRIP,
                0,
                VERTEX_COUNT
        );

    }
}
