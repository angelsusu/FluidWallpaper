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
 * OpenGL中的我哪里可以用来表示图像。照片甚至由一个数学算法生成的分型数据。
 * <p>
 * 1. 创建纹理的第一步
 * 通过这样的方式可以生成一个纹理。生成的纹理的id,保存在texttureObjectId中。
 * 如果返回的不是0，则表示成功
 * int[] textureObjectIds = new int[1];
 * GLES20.glGenTextures(1,textureObjectIds,0);
 * <p>
 * 2.加载位图数据并于纹理绑定
 * <p>
 * 2-1. OpenGL需要非压缩方式的原始数据。得到bitmap
 * 2-2. 纹理绑定
 * //绑定纹理，告诉它绑定的是一个2D的纹理
 * GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureObjectIds[0]);
 * <p>
 * 2-3. 理解纹理过滤。
 * 设置放大和缩小的时候，使用的插值算法
 * //缩小使用三线性过滤
 * GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
 * //放大使用双线性过滤
 * GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
 * <p>
 * 2-4 加载纹理
 * 将我们得到的位图数据，复制到当前的绑定的纹理对象中。
 * 因为这些数据已经被复制到OpenGL中了，所以Android中不需要继续持有这个位图数据了。调用recycle()方法立即释放
 * GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);
 * //接着生成2D的mip贴图
 * GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
 * 2-5 解除绑定防止其他的修改
 * GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
 * <p>
 * 3.更新着色器
 * 更新着色器，让其能够接受我们的纹理
 * <p>
 * 4.定义纹理坐标
 * ST
 * 裁剪纹理。
 * <p>
 * 5.使用着色器的套路。得到对应的属性值，
 * <p>
 * 我们使用纹理单元保存了纹理。因为一个GPU只能同时绘制数量有限的纹理，它使用这些纹理党员表示当前正在被绘制的活动的我呢里。
 * <p>
 * * 设置uniform并返回属性位置
 * //设置激活的纹理单元到0。因为我们刚刚开始的时候，将纹理绑定到这个纹理单元上了。
 * glActiveTexture(GL_TEXTURE0)
 * <p>
 * //绑定纹理单元
 * glBindTexture(GL_TEXTURE_2D,textureId)
 * //设置纹理矩阵
 * glUniformli(uTextureUnitLocation,0)
 */
public class Texture2DShapeRender extends ViewGLRender {
    /**
     * 更新shader的位置
     */
    private static final String VERTEX_SHADER_FILE = "texture/texture_vertex_shader.glsl";
    private static final String FRAGMENT_SHADER_FILE = "texture/texture_fragment_shader.glsl";
    private static final String A_POSITION = "a_Position";
    private static final String A_COORDINATE = "a_TextureCoordinates";
    private static final String U_TEXTURE = "u_TextureUnit";
    private static final String U_MATRIX = "u_Matrix";

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

    public Texture2DShapeRender(Context context) {
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
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher, options);
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

    //在OnDrawFrame中进行绘制
    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);

        //传递给着色器
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, mProjectionMatrix, 0);

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
