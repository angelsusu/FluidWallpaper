package com.cry.opengldemo5.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.cry.opengldemo5.R;
import com.cry.opengldemo5.common.Constant;
import com.cry.opengldemo5.render.GLESUtils;
import com.cry.opengldemo5.render.ViewGLRender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 简单色彩处理
 * <p>
 * 在GLSL中，颜色是用包含四个浮点的向量vec4表示，四个浮点分别表示RGBA四个通道，取值范围为0.0-1.0。
 * 我们先读取图片每个像素的色彩值，再对读取到的色彩值进行调整，这样就可以完成对图片的色彩处理了。
 * 我们应该都知道，黑白图片上，每个像素点的RGB三个通道值应该是相等的。
 * 知道了这个，将彩色图片处理成黑白图片就非常简单了。我们直接出处像素点的RGB三个通道，
 * 相加然后除以3作为处理后每个通道的值就可以得到一个黑白图片了。
 * 这是均值的方式是常见黑白图片处理的一种方法。
 * 类似的还有权值方法（给予RGB三个通道不同的比例）、只取绿色通道等方式。
 * 与之类似的，冷色调的处理就是单一增加蓝色通道的值，暖色调的处理可以增加红绿通道的值。
 * 还有其他复古、浮雕等处理也都差不多。
 * <p>
 * 图片模糊处理
 * 图片模糊处理相对上面的色调处理稍微复杂一点，通常图片模糊处理是采集周边多个点，
 * 然后利用这些点的色彩和这个点自身的色彩进行计算，得到一个新的色彩值作为目标色彩。
 * 模糊处理有很多算法，类似高斯模糊、径向模糊等等。
 * <p>
 * 放大镜效果
 * 放大镜效果相对模糊处理来说，处理过程也会相对简单一些。
 * 我们只需要将制定区域的像素点，都以需要放大的区域中心点为中心，
 * 向外延伸其到这个中心的距离即可实现放大效果。
 * 具体实现，可参考着色器中vChangeType=4时的操作。
 */
public class TextureFilterShapeRender extends ViewGLRender {
    /**
     * 更新shader的位置
     */
    private static final String VERTEX_SHADER_FILE = "texture/texture_vertex_shader.glsl";
    private static final String FRAGMENT_SHADER_FILE = "texture/texture_fragment_fliter_shader.glsl";
    private static final String A_POSITION = "a_Position";
    private static final String A_COORDINATE = "a_TextureCoordinates";
    private static final String U_TEXTURE = "u_TextureUnit";
    private static final String U_MATRIX = "u_Matrix";
    private static final String U_CHANGE_COLOR = "u_ChangeColor";
    private static final String U_CHANGE_TYPE = "u_ChangeType";

    private static final int COORDS_PER_VERTEX = 2;
    private static final int COORDS_PER_ST = 2;
    private static final int TOTAL_COMPONENT_COUNT = COORDS_PER_VERTEX + COORDS_PER_ST;
    private static final int STRIDE = TOTAL_COMPONENT_COUNT * Constant.BYTES_PER_FLOAT;

    //顶点的坐标系
    private static float TEXTURE_COORDS[] = {
            //Order of coordinates: X, Y,S,T
            -1.0f, 1.0f, 0.0f, 0.0f,
            -1.0f, -1.0f, 0.0f, 1.0f, //bottom left
            1.0f, 1.0f, 1.0f, 0.0f, // top right
            1.0f, -1.0f, 1.0f, 1.0f, // bottom right
    };


    private static final int VERTEX_COUNT = TEXTURE_COORDS.length / TOTAL_COMPONENT_COUNT;
    private final Context context;

    //pragram的指针
    private int mProgramObjectId;
    //顶点数据的内存映射
    private final FloatBuffer mVertexFloatBuffer;

    //模型矩阵
    private float[] mModelMatrix = new float[16];

    private Matrix mViewMatrix;

    //投影矩阵
    private float[] mProjectionMatrix = new float[16];
    private int uMatrix;
    private int uTexture;
    private int mTextureId;
    private int uChangeColor;
    private int uChangeType;
    private int changeType = 3;

    public TextureFilterShapeRender(Context context) {
        this.context = context;

        mVertexFloatBuffer = ByteBuffer
                .allocateDirect(TEXTURE_COORDS.length * Constant.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEXTURE_COORDS);
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


        int aCoordinate = GLES20.glGetAttribLocation(mProgramObjectId, A_COORDINATE);
        mVertexFloatBuffer.position(COORDS_PER_VERTEX);
        GLES20.glVertexAttribPointer(
                aCoordinate,
                COORDS_PER_ST,
                GLES20.GL_FLOAT, false,
                STRIDE,
                mVertexFloatBuffer);

        GLES20.glEnableVertexAttribArray(aCoordinate);


        uChangeColor = GLES20.glGetUniformLocation(mProgramObjectId, U_CHANGE_COLOR);
        uChangeType = GLES20.glGetUniformLocation(mProgramObjectId, U_CHANGE_TYPE);

        uMatrix = GLES20.glGetUniformLocation(mProgramObjectId, U_MATRIX);

        uTexture = GLES20.glGetUniformLocation(mProgramObjectId, U_TEXTURE);

        mTextureId = createTexture2();
    }

    //使用mip贴图来生成纹理，相当于将图片复制到openGL里面？
    private int createTexture() {
        final Bitmap mBitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        //加载Bitmap
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher, options);
        //保存到textureObjectId
        int[] textureObjectId = new int[1];
        if (mBitmap != null && !mBitmap.isRecycled()) {
            //生成一个纹理，保存到这个数组中
            GLES20.glGenTextures(1, textureObjectId, 0);
            //绑定GL_TEXTURE_2D
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectId[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

//            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//

            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);

            //因为使用贴图的方式来生成纹理,故需要生成纹理
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

            //回收释放
            mBitmap.recycle();
            //因为我们已经复制成功了。所以就进行解除绑定。防止修改
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

            return textureObjectId[0];
        }
        return 0;
    }

    private int createTexture2() {
        final Bitmap mBitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        //加载Bitmap
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.lenna, options);
        //保存到textureObjectId
        int[] textureObjectId = new int[1];
        if (mBitmap != null && !mBitmap.isRecycled()) {
            //生成一个纹理，保存到这个数组中
            GLES20.glGenTextures(1, textureObjectId, 0);
            //绑定GL_TEXTURE_2D
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectId[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

//            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//

            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);

            //因为使用贴图的方式来生成纹理,故需要生成纹理
//            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

            //回收释放
            mBitmap.recycle();
            //因为我们已经复制成功了。所以就进行解除绑定。防止修改
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

            return textureObjectId[0];
        }
        return 0;
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

    //黑白图片的公式：RGB 按照 0.2989 R，0.5870 G 和 0.1140 B 的比例构成像素灰度值。
    float[] grayFilterColorData = {0.299f, 0.587f, 0.114f};

    //简单的色彩处理
    float[] coolFilterColorData = {0.0f, 0.0f, 0.1f};
    float[] warmFilterColorData = {0.1f, 0.1f, 0.0f};
    float[] blurFilterColorData = {0.006f, 0.004f, 0.002f};
    //放大
    float[] magnFilterColorData = {0.0f,0.0f,0.4f};


    //在OnDrawFrame中进行绘制
    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);

        //传递给着色器
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, mProjectionMatrix, 0);//
        if (changeType == 1) {
            //黑白路径
            GLES20.glUniform1i(uChangeType, 1);
            GLES20.glUniform3fv(uChangeColor, 1, grayFilterColorData, 0);
        } else if (changeType == 2) {
            GLES20.glUniform1i(uChangeType, 2);
            GLES20.glUniform3fv(uChangeColor, 1, warmFilterColorData, 0);
        } else if (changeType == 3) {
            GLES20.glUniform1i(uChangeType, 2);
            GLES20.glUniform3fv(uChangeColor, 1, coolFilterColorData, 0);
        } else if (changeType == 4) {
            GLES20.glUniform1i(uChangeType, 3);
            GLES20.glUniform3fv(uChangeColor, 1, blurFilterColorData, 0);
        }

        //绑定和激活纹理
        //因为我们生成了MIP，放到了GL_TEXTURE0 中，所以重新激活纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //重新去半丁纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);


        //设置纹理的坐标
        GLES20.glUniform1i(uTexture, 0);

        //绘制三角形.
        //draw arrays的几种方式 GL_TRIANGLES三角形 GL_TRIANGLE_STRIP三角形带的方式(开始的3个点描述一个三角形，后面每多一个点，多一个三角形) GL_TRIANGLE_FAN扇形(可以描述圆形)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);

    }
}
