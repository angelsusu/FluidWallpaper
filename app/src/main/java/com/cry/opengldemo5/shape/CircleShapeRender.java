package com.cry.opengldemo5.shape;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.cry.opengldemo5.common.Constant;
import com.cry.opengldemo5.render.GLESUtils;
import com.cry.opengldemo5.render.ViewGLRender;
import com.cry.opengldemo5.shape.base.Circle;
import com.cry.opengldemo5.shape.base.Point;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 *
 */
public class CircleShapeRender extends ViewGLRender {
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

    private static int VERTEX_COUNT;

    private final Context context;

    //pragram的指针
    private int mProgramObjectId;
    //顶点数据的内存映射
    private final FloatBuffer mVertexFloatBuffer;
    private final float[] mCircleColorCoords;


    /*
    添加矩阵

     */
    private static final String U_MATRIX = "u_Matrix";
    private Matrix mModelMatrix;
    private Matrix mViewMatrix;

    //投影矩阵
    private float[] mProjectionMatrix = new float[16];
    private int uMatrix;

    public CircleShapeRender(Context context) {
        this.context = context;

        //创建一个圆。
        //圆心
        Point center = new Point(0f, 0f, 0f);
        //圆的半径
        float radius = 0.5f;
        //多少个点来切分这个圆.越多的切分。越圆
        int numbersRoundCircle = 360;
        //
        Circle circle = new Circle(center, radius);
        mCircleColorCoords = createCircleCoords(circle, numbersRoundCircle);
        mVertexFloatBuffer = ByteBuffer
                .allocateDirect(mCircleColorCoords.length * Constant.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mCircleColorCoords);
        mVertexFloatBuffer.position(0);

        VERTEX_COUNT = getCircleVertexNum(numbersRoundCircle);
    }

    private float[] createCircleCoords(Circle circle, int numbersRoundCircle) {
        //先计算总共需要多少个点
        int needNumber = getCircleVertexNum(numbersRoundCircle);
        //创建数组
        float[] circleColorCoord = new float[needNumber * TOTAL_COMPONENT_COUNT];
        //接下来给每个点分配数据

        //对每一组点进行赋值
        for (int numberIndex = 0; numberIndex < needNumber; numberIndex++) {
            int indexOffset = numberIndex * TOTAL_COMPONENT_COUNT;

            if (numberIndex == 0) {   //第一个点。就是圆心
                //位置
                circleColorCoord[indexOffset] = circle.center.x;
                circleColorCoord[indexOffset + 1] = circle.center.y;
                circleColorCoord[indexOffset + 2] = circle.center.z;

                //下面是颜色。给一个白色
                circleColorCoord[indexOffset + 3] = 1.f;
                circleColorCoord[indexOffset + 4] = 1.f;
                circleColorCoord[indexOffset + 5] = 1.f;
            } else if (numberIndex < needNumber - 1) {    //切分圆的点
                //需要根据半径。中心点。来结算
                int angleIndex = numberIndex - 1;
                float angleRadius = (float) (((float) angleIndex / (float) numbersRoundCircle) * Math.PI * 2f);
                float centerX = circle.center.x;
                float centerY = circle.center.y;
                float centerZ = circle.center.z;
                float radius = circle.radius;
                float tempX = (float) (centerX + radius * Math.cos(angleRadius));
                float tempY = (float) (centerY + radius * Math.sin(angleRadius));
                float temp = centerZ + 0;

                //位置

                circleColorCoord[indexOffset] = tempX;
                circleColorCoord[indexOffset + 1] = tempY;
                circleColorCoord[indexOffset + 2] = temp;

                //下面是颜色。给一个白色
                circleColorCoord[indexOffset + 3] = (float) (1.f* Math.cos(angleRadius));
                circleColorCoord[indexOffset + 4] = (float) (1.f* Math.sin(angleRadius));
                circleColorCoord[indexOffset + 5] = 1.f;
            } else { //最后一个点了。重复数据中的二组的位置
                //位置.index为1的点
                int copyTargetIndex = 1;
                //复制点
                circleColorCoord[indexOffset] = circleColorCoord[copyTargetIndex * TOTAL_COMPONENT_COUNT];
                circleColorCoord[indexOffset + 1] = circleColorCoord[copyTargetIndex * TOTAL_COMPONENT_COUNT + 1];
                circleColorCoord[indexOffset + 2] = circleColorCoord[copyTargetIndex * TOTAL_COMPONENT_COUNT + 2];

                circleColorCoord[indexOffset + 3] = circleColorCoord[copyTargetIndex * TOTAL_COMPONENT_COUNT + 3];
                circleColorCoord[indexOffset + 4] = circleColorCoord[copyTargetIndex * TOTAL_COMPONENT_COUNT + 4];
                circleColorCoord[indexOffset + 5] = circleColorCoord[copyTargetIndex * TOTAL_COMPONENT_COUNT + 5];
            }

        }
        return circleColorCoord;

    }

    /*
    需要的点的个数等于 1(圆心)+切分圆的点数+1(为了闭合，切分圆的起点和终点，需要重复一次)
     */
    private int getCircleVertexNum(int numbersRoundCircle) {
        return +1 + numbersRoundCircle + 1;
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
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, VERTEX_COUNT);
    }

    public static class TestRender {
    }
}
