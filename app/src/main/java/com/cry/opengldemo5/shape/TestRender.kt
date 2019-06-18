package com.cry.opengldemo5.shape

import android.content.Context
import android.opengl.GLES30
import com.cry.opengldemo5.render.GLESUtils
import com.cry.opengldemo5.render.ViewGLRender
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TestRender(context: Context): ViewGLRender() {
    var mContext = context
    var buf1 = 0
    var buf2 = 0
    var program = 0
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)

        val testShader = GLESUtils.compileShaderCode(GLES30.GL_VERTEX_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/shader_test.glsl"))
        val testFragment = GLESUtils.compileShaderCode(GLES30.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/fragment_test.glsl"))

        program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, testShader)
        GLES30.glAttachShader(program, testFragment)
        GLES30.glLinkProgram(program)

        val triangleCoords = floatArrayOf(-1f, -1f, -1f, 1f, 1f, 1f, 1f, -1f)
        var vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(triangleCoords)
                position(0)
            }
        }
        val elements = intArrayOf(0, 1, 2, 0, 2, 3)
        val elementBuffer: IntBuffer = ByteBuffer.allocateDirect(elements.size * 4).run {
            order(ByteOrder.nativeOrder())
            asIntBuffer().apply {
                put(elements)
                position(0)
            }
        }
        val buffers = IntArray(1)
        GLES30.glGenBuffers(1, buffers, 0)
        buf1 = buffers[0]
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, triangleCoords.size * 4, vertexBuffer, GLES30.GL_STATIC_DRAW);
        val buffers2 = IntArray(1)
        GLES30.glGenBuffers(1, buffers2, 0)
        buf2 = buffers2[0]
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, buffers2[0]);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, elements.size * 4, elementBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 2 * 4, 0);
        GLES30.glEnableVertexAttribArray(0);
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)

        GLES30.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glUseProgram(program)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buf1)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, buf2)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_INT, 0);
    }

}