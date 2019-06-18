package com.cry.opengldemo5.shape;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.cry.opengldemo5.common.Constant;
import com.cry.opengldemo5.render.GLESUtils;
import com.cry.opengldemo5.render.ViewGLRender;
import com.cry.opengldemo5.shape.base.Circle;
import com.cry.opengldemo5.shape.base.Point;
import com.cry.opengldemo5.shape.base.ShapeBuilder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 圆锥体
 */
public class Cone3DShapeRender extends ViewGLRender {
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

    public Cone3DShapeRender(Context context) {
        this.context = context;

        float[] dataPos = createConePosition();

        mVertexFloatBuffer = ByteBuffer.allocateDirect(dataPos.length * TOTAL_COMPONENT_COUNT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(dataPos);
        mVertexFloatBuffer.position(0);

        VERTEX_COUNT = dataPos.length / TOTAL_COMPONENT_COUNT;
    }

    private float step = 5f;

    private float[] createConePosition() {

        //圆锥的高度
        float coneHeight = 0.3f;
        //圆锥的底面大小
        float coneRadius = 0.15f;

        //纬度切分的大小
        int latitudePieces = 5;
        int longitudePieces = 10;

        ArrayList<Float> dataPos = new ArrayList<>();

        //将纬度按照90划分
        for (int latitude = 0; latitude < 90 + latitudePieces; latitude += latitudePieces) {
            //xy截面是一个等腰三角形

            float currentHeightCut = latitude * coneHeight / 90f;
            float currentHeightCut2 = (latitude + latitudePieces) * coneHeight / 90f;

            //计算出改纬度的每一个经度的坐标
            float rXZ = (float) currentHeightCut / coneHeight * coneRadius;
            float rYZ = (float) coneHeight - currentHeightCut;

            float rXZ2 = (float) currentHeightCut2 / coneHeight * coneRadius;
            float rYZ2 = (float) coneHeight - currentHeightCut2;

            //纬度还是圆
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
                float cB = (float) (1f * Math.cos(angleXZ2));
//                if (latitude == 90) {
//                    cR = 0.5f;
//                    cG = 0.5f;
//                    cB = 0.5f;
//                } else {
//
//                }

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
                float cG2 = (float) (1f * Math.cos(angleXZ2));
                float cB2 = (float) (1f);

//                if (latitude == 90) {
//                    cR2 = 0.5f;
//                    cG2 = 0.5f;
//                    cB2 = 0.5f;
//                } else {
//
//                }
                dataPos.add(cR2);
                dataPos.add(cG2);
                dataPos.add(cB2);
            }
        }

//        int needNumber = ShapeBuilder.getCircleVertexNum(360);
//
//        Circle circle = new Circle(new Point(0f, 0f, 0f), coneRadius);
//        //对每一组点进行赋值
//        for (int numberIndex = 0; numberIndex < needNumber; numberIndex++) {
//            int indexOffset = numberIndex * TOTAL_COMPONENT_COUNT;
//
//            float tempX0 = 0;
//            float tempY0 = 0;
//            float tempZ0 = 0;
//            if (numberIndex == 0) {   //第一个点。就是圆心
//                //位置
//                dataPos.add(circle.center.x);
//                dataPos.add(circle.center.y);
//                dataPos.add(circle.center.z);
//
//                dataPos.add(1.f);
//                dataPos.add(1.f);
//                dataPos.add(1.f);
//
//            } else if (numberIndex < needNumber - 1) {    //切分圆的点
//                //需要根据半径。中心点。来结算
//                int angleIndex = numberIndex - 1;
//                float angleRadius = (float) (((float) angleIndex / (float) 360) * Math.PI * 2f);
//                float centerX = circle.center.x;
//                float centerY = circle.center.y;
//                float centerZ = circle.center.z;
//                float radius = circle.radius;
//                float tempX = (float) (centerX + radius * Math.cos(angleRadius));
//                float tempY = (float) (centerY + radius * Math.sin(angleRadius));
//                float temp = centerZ + 0;
//                if (angleIndex == 0) {
//                    tempX0 = tempX;
//                    tempY0 = tempY;
//                    tempZ0 = temp;
//                }
//                //位置
//                dataPos.add(tempX);
//                dataPos.add(tempY);
//                dataPos.add(temp);
//
//                dataPos.add(1.f);
//                dataPos.add(1.f);
//                dataPos.add(1.f);
//
//            } else { //最后一个点了。重复数据中的二组的位置
//                //位置.index为1的点
//                int copyTargetIndex = 1;
//                //复制点
//                dataPos.add(tempX0);
//                dataPos.add(tempY0);
//                dataPos.add(tempZ0);
//
//                dataPos.add(1.f);
//                dataPos.add(1.f);
//                dataPos.add(1.f);
//
//            }
//
//        }

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
        Matrix.rotateM(mModelMatrix, 0, 180f, 1f, -1f, 1f);


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
                GLES20.GL_TRIANGLE_FAN,
                0,
                VERTEX_COUNT
        );

        GLES20.glDrawArrays(
                GLES20.GL_TRIANGLE_FAN,
                VERTEX_COUNT - 361,
                360
        );


    }
}
