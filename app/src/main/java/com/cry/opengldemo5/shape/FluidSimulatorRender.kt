package com.cry.opengldemo5.shape

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import android.view.MotionEvent
import com.cry.opengldemo5.render.DealTouchEvent
import com.cry.opengldemo5.render.GLESUtils
import com.cry.opengldemo5.render.ViewGLRender
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfo
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfoManager
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.collections.ArrayList


typealias gl = GLES30
val X = 0
val Y = 0

class FBO(w: Int, h: Int, internalFormat: Int, format: Int, type: Int, param: Int) {
    var texture = 0
    var fbo = 0
    var width = 0
    var height = 0

    init {
        if (w >= 0) {
            width = w
            height = h
            val textures = IntArray(1)
            gl.glActiveTexture(gl.GL_TEXTURE0)
            gl.glGenTextures(1, textures, 0)
            texture = textures[0]
            gl.glBindTexture(gl.GL_TEXTURE_2D, texture)
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, param)
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, param)
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE)
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE)
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, internalFormat, w, h, 0, format, type, null)

            val buffers = IntArray(1)
            gl.glGenBuffers(1, buffers, 0)
            fbo = buffers[0]
            gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbo)
            gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT0, gl.GL_TEXTURE_2D, texture, 0)

            if (gl.glCheckFramebufferStatus(gl.GL_FRAMEBUFFER) != gl.GL_FRAMEBUFFER_COMPLETE) {
                Log.d("xxx", "ERROR::FRAMEBUFFER:: Framebuffer is not complete!")
            }
            gl.glViewport(0, 0, w, h)
            gl.glClear(gl.GL_COLOR_BUFFER_BIT)
        }
    }

    fun attach(id: Int): Int {
        gl.glActiveTexture(gl.GL_TEXTURE0 + id)
        gl.glBindTexture(gl.GL_TEXTURE_2D, texture)
        return id
    }

    companion object {
        fun buildDefault(): FBO {
            return FBO(-1, -1, -1, -1 ,-1, -1)
        }
    }
}

class DoubleFBO(w: Int, h: Int, internalFormat: Int, format: Int, type: Int, param: Int) {
    var read = FBO(w, h, internalFormat, format, type, param)
    var write = FBO(w, h, internalFormat, format, type, param)

    fun swap() {
        val temp = read
        read = write
        write = temp
    }

    companion object {
        fun buildDefault(): DoubleFBO {
            return DoubleFBO(-1, -1, -1, -1 ,-1, -1)
        }
    }
}

class GLProgram(vertexShader: Int, fragmentShader: Int) {
    var program: Int = 0
    var uniforms = mutableMapOf<String, Int>()

    init  {
        if (vertexShader != -1 && fragmentShader != -1) {
            program = gl.glCreateProgram()

            gl.glAttachShader(this.program, vertexShader)
            gl.glAttachShader(this.program, fragmentShader)
            gl.glLinkProgram(this.program)

            var params = IntBuffer.allocate(1)
            gl.glGetProgramiv(program, gl.GL_ACTIVE_UNIFORMS, params)
            val range = params[0] - 1

            for (i in 0..range) {
                var length = IntArray(1)
                var size = IntArray(1)
                var type = IntArray(1)
                var name = ByteArray(50)
                gl.glGetActiveUniform(program, i, 50, length, 0,
                        size, 0, type, 0,
                        name, 0)

                var strName = String(name, 0, length[0])
                uniforms[strName] = gl.glGetUniformLocation(program, strName)

                val location = gl.glGetAttribLocation(program, "aPosition")
                Log.d("xxx", strName)
            }
        }
    }

    fun bind() {
        gl.glUseProgram(this.program)
    }
}

class FluidSimulatorRender(context: Context): ViewGLRender(), DealTouchEvent {
    val mContext = context
    var clearProgram               = GLProgram(-1, -1)
    var colorProgram               = GLProgram(-1, -1)
    var backgroundProgram          = GLProgram(-1, -1)
    var displayProgram             = GLProgram(-1, -1)
    var displayBloomProgram        = GLProgram(-1, -1)
    var displayShadingProgram      = GLProgram(-1, -1)
    var displayBloomShadingProgram = GLProgram(-1, -1)
    var bloomPrefilterProgram      = GLProgram(-1, -1)
    var bloomBlurProgram           = GLProgram(-1, -1)
    var bloomFinalProgram          = GLProgram(-1, -1)
    var splatProgram               = GLProgram(-1, -1)
    var advectionProgram           = GLProgram(-1, -1)
    var divergenceProgram          = GLProgram(-1, -1)
    var curlProgram                = GLProgram(-1, -1)
    var vorticityProgram           = GLProgram(-1, -1)
    var pressureProgram            = GLProgram(-1, -1)
    var gradienSubtractProgram     = GLProgram(-1, -1)
    var bgProgram = GLProgram(-1, -1)
    var testProgram = GLProgram(-1, -1)

    fun initPrograms() {
        val baseVertexShader = GLESUtils.compileShaderCode(gl.GL_VERTEX_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/baseVertexShader.glsl"))
        val clearShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/clearShader.glsl"))
        val colorShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/colorShader.glsl"))
        val backgroundShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/backgroundShader.glsl"))
        val displayShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/displayShader.glsl"))
        val displayBloomShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/displayBloomShader.glsl"))
        val displayShadingShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/displayShadingShader.glsl"))
        val displayBloomShadingShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/displayBloomShadingShader.glsl"))
        val bloomPrefilterShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/bloomPrefilterShader.glsl"))
        val bloomBlurShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/bloomBlurShader.glsl"))
        val bloomFinalShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/bloomFinalShader.glsl"))
        val splatShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/splatShader.glsl"))
        val advectionShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/advectionShader.glsl"))
        val divergenceShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/divergenceShader.glsl"))
        val curlShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/curlShader.glsl"))
        val vorticityShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/vorticityShader.glsl"))
        val pressureShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/pressureShader.glsl"))
        val gradientSubtractShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/gradientSubtractShader.glsl"))
        var bgVertexShader = GLESUtils.compileShaderCode(gl.GL_VERTEX_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/bgVertextShader.glsl"))
        val bgFragmentShader = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/bgFragmentShader.glsl"))

        val testShader = GLESUtils.compileShaderCode(gl.GL_VERTEX_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/shader_test.glsl"))
        val testFragment = GLESUtils.compileShaderCode(gl.GL_FRAGMENT_SHADER,
                GLESUtils.readAssetShaderCode(mContext, "shape/fragment_test.glsl"))

        clearProgram = GLProgram(baseVertexShader, clearShader)
        colorProgram = GLProgram(baseVertexShader, colorShader)
        backgroundProgram = GLProgram(baseVertexShader, backgroundShader)
        displayProgram = GLProgram(baseVertexShader, displayShader)
        displayBloomProgram = GLProgram(baseVertexShader, displayBloomShader)
        displayShadingProgram = GLProgram(baseVertexShader, displayShadingShader)
        displayBloomShadingProgram = GLProgram(baseVertexShader, displayBloomShadingShader)
        bloomPrefilterProgram = GLProgram(baseVertexShader, bloomPrefilterShader)
        bloomBlurProgram = GLProgram(baseVertexShader, bloomBlurShader)
        bloomFinalProgram = GLProgram(baseVertexShader, bloomFinalShader)
        splatProgram = GLProgram(baseVertexShader, splatShader)
        advectionProgram = GLProgram(baseVertexShader, advectionShader)
        divergenceProgram = GLProgram(baseVertexShader, divergenceShader)
        curlProgram = GLProgram(baseVertexShader, curlShader)
        vorticityProgram = GLProgram(baseVertexShader, vorticityShader)
        pressureProgram = GLProgram(baseVertexShader, pressureShader)
        gradienSubtractProgram = GLProgram(baseVertexShader, gradientSubtractShader)
        testProgram = GLProgram(testShader, testFragment)
        bgProgram = GLProgram(bgVertexShader, bgFragmentShader)
    }

    override fun onSurfaceCreated(gl10: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl10, config)
    }

    override fun onSurfaceChanged(gl10: GL10?, width: Int, height: Int) {
        mWidth = width
        mHeight = height
        initPrograms()
        init()
        initFramebuffers()
        multipleSplats((Math.random() * 20).toInt() + 5)
    }

    override fun onDrawFrame(gl10: GL10?) {

        update()

        var errorCode: Int = gl.glGetError();
        while (errorCode != GL10.GL_NO_ERROR) {
            Log.i("xxx", "errorCode =" + errorCode);
            errorCode = gl.glGetError();
        }
    }

    var simWidth: Int = 0
    var simHeight: Int = 0
    var dyeWidth: Int = 0
    var dyeHeight: Int = 0
    var density: DoubleFBO = DoubleFBO.buildDefault()
    var velocity: DoubleFBO = DoubleFBO.buildDefault()
    var pressure: DoubleFBO = DoubleFBO.buildDefault()
    var divergence: FBO? = null
    var curl: FBO? = null
    var bloom: FBO = FBO.buildDefault()
    var mWidth = 1080
    var mHeight = 2000
    var bloomFramebuffers = ArrayList<FBO>()

    data class Size(var width: Int, var height: Int)

    data class Format(var internalFormat: Int, var format: Int)

    data class Extend(var formatRGBA: Format, var formatRG: Format, var formatR: Format, var halfFloatTexType: Int = gl.GL_HALF_FLOAT, var supportLinearFiltering: Boolean = true)

    data class RGB(var r: Float = 0.toFloat(), var g: Float = 0.toFloat(), var b: Float = 0.toFloat())

    val ext = Extend(Format(gl.GL_RGBA16F, gl.GL_RGBA),
            Format(gl.GL_RG16F, gl.GL_RG),
            Format(gl.GL_R16F, gl.GL_RED))

    data class Config(var SIM_RESOLUTION: Int = 128,
                      var DYE_RESOLUTION: Int = 512,
                      var DENSITY_DISSIPATION: Float = 0.97f,
                      var VELOCITY_DISSIPATION: Float = 0.98f,
                      var PRESSURE_DISSIPATION: Float = 0.8f,
                      var PRESSURE_ITERATIONS: Int = 20,
                      var CURL: Float = 30f,
                      var SPLAT_RADIUS: Float = 0.5f,
                      var SHADING: Boolean = true,
                      var COLORFUL: Boolean = true,
                      var PAUSED: Boolean = false,
                      var BACK_COLOR: RGB = RGB(),
                      var TRANSPARENT: Boolean = false,
                      var BLOOM: Boolean = false,
                      var BLOOM_ITERATIONS: Int = 8,
                      var BLOOM_RESOLUTION: Int = 256,
                      var BLOOM_INTENSITY: Float = 0.8f,
                      var BLOOM_THRESHOLD: Float = 0.6f,
                      var BLOOM_SOFT_KNEE: Float = 0.7f)

    val config = Config()

    data class PointerPrototype(var id: Int = -1,
                                var x: Float = 0f,
                                var y: Float = 0f,
                                var dx: Float = 0f,
                                var dy: Float = 0f,
                                var down: Boolean = false,
                                var moved: Boolean = false,
                                var color: RGB = RGB(30f, 0f, 300f))
    val pointers = ArrayList<PointerPrototype>()
    var lastColorChangeTime: Long = 0;
    var splatStack = Stack<Int>()

    fun initFramebuffers() {
        val simRes = getResolution(config.SIM_RESOLUTION)
        val dyeRes = getResolution(config.DYE_RESOLUTION)

        simWidth = simRes.width
        simHeight = simRes.height
        dyeWidth = dyeRes.width
        dyeHeight = dyeRes.height

        val texType = ext.halfFloatTexType;
        val rgba = ext.formatRGBA;
        val rg = ext.formatRG;
        val r = ext.formatR;
        val filtering = if (ext.supportLinearFiltering) gl.GL_LINEAR else gl.GL_NEAREST

        density = createDoubleFBO(dyeWidth, dyeHeight, rgba.internalFormat, rgba.format, texType, filtering);
//        if (density == null)
//            density = createDoubleFBO(dyeWidth, dyeHeight, rgba.internalFormat, rgba.format, texType, filtering);
//        else
//            density?.let {
//                resizeDoubleFBO(it, dyeWidth, dyeHeight, rgba.internalFormat, rgba.format, texType, filtering)
//            }

        velocity = createDoubleFBO(simWidth, simHeight, rg.internalFormat, rg.format, texType, filtering);
//        if (velocity == null)
//            velocity = createDoubleFBO(simWidth, simHeight, rg.internalFormat, rg.format, texType, filtering);
//        else
//            velocity?.let {
//                resizeDoubleFBO(it, simWidth, simHeight, rg.internalFormat, rg.format, texType, filtering);
//            }

        divergence = createFBO(simWidth, simHeight, r.internalFormat, r.format, texType, gl.GL_NEAREST);
        curl = createFBO(simWidth, simHeight, r.internalFormat, r.format, texType, gl.GL_NEAREST);
        pressure = createDoubleFBO(simWidth, simHeight, r.internalFormat, r.format, texType, gl.GL_NEAREST);

        initBloomFramebuffers();
    }

    fun initBloomFramebuffers() {
        val res = getResolution(config.BLOOM_RESOLUTION);

        val texType = ext.halfFloatTexType;
        val rgba = ext.formatRGBA;
        val filtering = if (ext.supportLinearFiltering) gl.GL_LINEAR else gl.GL_NEAREST

        bloom = createFBO(res.width, res.height, rgba.internalFormat, rgba.format, texType, filtering);

        bloomFramebuffers.clear()
        for (i in 0..config.BLOOM_ITERATIONS)
        {
            val width = res.width.shr(i + 1);
            val height = res.height.shr(i + 1);

            if (width < 2 || height < 2) break;

            val fbo = createFBO(width, height, rgba.internalFormat, rgba.format, texType, filtering);
            bloomFramebuffers.add(fbo);
        }
    }

    fun update() {
        //resizeCanvas()
        input()
        if (!config.PAUSED)
           step(0.016f)
        render(null)
        //requestAnimationFrame(update)
    }

    fun createFBO(w: Int, h: Int, internalFormat: Int, format: Int, type: Int, param: Int): FBO {
        return FBO(w, h, internalFormat, format, type, param)
    }

    fun createDoubleFBO(w: Int, h: Int, internalFormat: Int, format: Int, type: Int, param: Int): DoubleFBO {
        return DoubleFBO(w, h, internalFormat, format, type, param)
    }

    fun resizeDoubleFBO(target: DoubleFBO, w: Int, h: Int, internalFormat: Int, format: Int, type: Int, param: Int): DoubleFBO {
        target.read = resizeFBO(target.read, w, h, internalFormat, format, type, param)
        target.write = createFBO(w, h, internalFormat, format, type, param)
        return target
    }

    fun resizeFBO (target: FBO, w: Int, h: Int, internalFormat: Int, format: Int, type: Int, param: Int): FBO {
        val newFBO = createFBO(w, h, internalFormat, format, type, param)
        clearProgram.bind()
        gl.glUniform1i(clearProgram.uniforms["uTexture"]?:-1, target.attach(0))
        gl.glUniform1f(clearProgram.uniforms["value"]?:-1, 1f)
        blit(newFBO.fbo)
        return newFBO
    }

    fun blit(destination: Int) {
//        val buffers = IntArray(1)
//        if (destination == -1) {
//            gl.glGenBuffers(1, buffers, 0)
//        }
        gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, destination);
        gl.glDrawElements(gl.GL_TRIANGLES, 6, gl.GL_UNSIGNED_INT, 0);
    }

    fun blittest() {
        gl.glViewport(0, 0, 500, 500)
        gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0)
        gl.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        gl.glClear(gl.GL_COLOR_BUFFER_BIT);
        testProgram.bind()
//        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, buf1)
//        gl.glBindBuffer(gl.GL_ELEMENT_ARRAY_BUFFER, buf2)
        gl.glDrawElements(gl.GL_TRIANGLES, 6, gl.GL_UNSIGNED_INT, 0);
    }

    var bgVertex = 0
    var bgElement = 0
    var bgVertexBuffer: FloatBuffer? = null
    val bgTriangleCoords = floatArrayOf(
            -1f, -1f, 0f, 1f,
            -1f, 1f, 0f, 0f,
            1f, 1f, 1f, 0f,
            1f, -1f, 1f, 1f)
    var bgTexture = 0
    fun initBg() {
        if (bgTexture == 0 || LiveWallpaperInfoManager.getInstance().isChanged) {
            val textureArr = IntArray(1)
            gl.glGenTextures(1, textureArr, 0)
            bgTexture = textureArr[0]
            gl.glActiveTexture(gl.GL_TEXTURE10)
            gl.glBindTexture(gl.GL_TEXTURE_2D, bgTexture)
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_REPEAT);    // Set texture wrapping to GL_REPEAT (usually basic wrapping method)
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_REPEAT);
            // Set texture filtering parameters
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_LINEAR);
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_LINEAR);

//            val options = BitmapFactory.Options()
//            options.inScaled = false   // No pre-scaling
//            val bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.test_wallpaper_six)
            val liveWallpaperInfo = LiveWallpaperInfoManager.getInstance().currentWallpaperInfo
            if (liveWallpaperInfo == null) {
                return
            }
            var bitmap: Bitmap? = null
            if (liveWallpaperInfo.mSource == LiveWallpaperInfo.Source.SOURCE_ASSETS) {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), liveWallpaperInfo.mResourcesId)
            } else {
                bitmap = BitmapFactory.decodeFile(liveWallpaperInfo.mPath);
            }

            bitmap?.let {
                GLUtils.texImage2D(gl.GL_TEXTURE_2D, 0, bitmap, 0)
            }

            //gl.glGenerateMipmap(gl.GL_TEXTURE_2D)
        }

        LiveWallpaperInfoManager.getInstance().resetChanged()

        if (bgVertex == 0 && bgVertexBuffer == null) {

            bgVertexBuffer = ByteBuffer.allocateDirect(bgTriangleCoords.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(bgTriangleCoords)
                    position(0)
                }
            }

            val buffers = IntArray(1)
            gl.glGenBuffers(1, buffers, 0)
            bgVertex = buffers[0]

            val buffers2 = IntArray(1)
            gl.glGenBuffers(1, buffers2, 0)
            bgElement = buffers2[0]
        }

        bgProgram.bind()
        gl.glViewport(0, 0, mWidth, mHeight)
        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, bgVertex);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, bgTriangleCoords.size * 4, bgVertexBuffer, gl.GL_STATIC_DRAW);

        gl.glVertexAttribPointer(0, 2, gl.GL_FLOAT, false, 4 * 4, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(1, 2, gl.GL_FLOAT, false, 4 * 4, 8);
        gl.glEnableVertexAttribArray(1);

        gl.glBindBuffer(gl.GL_ELEMENT_ARRAY_BUFFER, bgElement);
        gl.glBufferData(gl.GL_ELEMENT_ARRAY_BUFFER, elements.size * 4, elementBuffer, gl.GL_STATIC_DRAW);

        gl.glActiveTexture(gl.GL_TEXTURE10)
        gl.glBindTexture(gl.GL_TEXTURE_2D, bgTexture)
        gl.glUniform1i(bgProgram.uniforms["ourTexture"]?:0, 10);
    }

    var buf1 = 0
    var buf2 = 0
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
    fun init() {
        if (buf1 == 0 && buf2 == 0) {
            lastColorChangeTime = System.currentTimeMillis()
            pointers.add(PointerPrototype())

            val buffers = IntArray(1)
            gl.glGenBuffers(1, buffers, 0)
            buf1 = buffers[0]

            val buffers2 = IntArray(1)
            gl.glGenBuffers(1, buffers2, 0)
            buf2 = buffers2[0]
        }

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, buf1);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, triangleCoords.size * 4, vertexBuffer, gl.GL_STATIC_DRAW);

        gl.glBindBuffer(gl.GL_ELEMENT_ARRAY_BUFFER, buf2);
        gl.glBufferData(gl.GL_ELEMENT_ARRAY_BUFFER, elements.size * 4, elementBuffer, gl.GL_STATIC_DRAW);
        gl.glVertexAttribPointer(0, 2, gl.GL_FLOAT, false, 2 * 4, 0);
        gl.glEnableVertexAttribArray(0);
    }

    fun getResolution(resolution: Int): Size {
        var aspectRatio = mWidth.toFloat() / mHeight.toFloat()
        if (aspectRatio < 1) {
            aspectRatio = 1f / aspectRatio
        }

        val max = Math.round(resolution * aspectRatio)
        val min = Math.round(resolution.toFloat())

        val size = if (mWidth > mHeight) Size(max, min) else Size(min, max)
        return size
    }

    fun multipleSplats(amount: Int) {
        for (i in 0..amount) {
            val color = generateColor();
            color.r *= 10.0f;
            color.g *= 10.0f;
            color.b *= 10.0f;
            val x = mWidth * Math.random();
            val y = mHeight * Math.random();
            val dx = 1000 * (Math.random() - 0.5);
            val dy = 1000 * (Math.random() - 0.5);
            splat(x.toFloat(), y.toFloat(), dx.toFloat(), dy.toFloat(), color);
        }
    }

    fun generateColor(): RGB {
        val c = HSVtoRGB(Math.random(), 1.0, 1.0)
        c.r *= 0.15f
        c.g *= 0.15f
        c.b *= 0.15f
        return c
    }

    fun HSVtoRGB(h: Double, s: Double, v: Double): RGB {
        var r = 0.0
        var g = 0.0
        var b = 0.0
        val i = Math.floor(h * 6);
        val f = h * 6 - i;
        val p = v * (1 - s);
        val q = v * (1 - f * s);
        val t = v * (1 - (1 - f) * s);

        when (i.toInt() % 6) {
            0 -> {
                r = v
                g = t
                b = p
            }
            1 -> {
                r = q
                g = v
                b = p
            }
            2 -> {
                r = p
                g = v
                b = t
            }
            3 -> {
                r = p
                g = q
                b = v
            }
            4 -> {
                r = t
                g = p
                b = v
            }
            5 -> {
                r = v
                g = p
                b = q
            }
        }

        return RGB(r.toFloat(), g.toFloat(), b.toFloat())
    }

    fun splat (x: Float, y: Float, dx: Float, dy: Float, color: RGB) {
        gl.glViewport(0, 0, simWidth, simHeight);
        splatProgram.bind();
        gl.glUniform1i(splatProgram.uniforms["uTarget"]?:0, velocity.read.attach(0));
        gl.glUniform1f(splatProgram.uniforms["aspectRatio"]?:0, mWidth.toFloat() / mHeight);
        gl.glUniform2f(splatProgram.uniforms["point"]?:0, x / mWidth, 1.0f - y / mHeight.toFloat());
        gl.glUniform3f(splatProgram.uniforms["color"]?:0, dx, -dy, 1.0f);
        gl.glUniform1f(splatProgram.uniforms["radius"]?:0, config.SPLAT_RADIUS / 100.0f);
        blit(velocity.write.fbo);
        velocity.swap();

        gl.glViewport(0, 0, dyeWidth, dyeHeight);
        gl.glUniform1i(splatProgram.uniforms["uTarget"]?:0, density.read.attach(0));
        gl.glUniform3f(splatProgram.uniforms["color"]?:0, color.r, color.g, color.b);
        blit(density.write.fbo);
        density?.swap();
    }

    fun render (target: Int?) {

        if (config.BLOOM)
            applyBloom(density.read, bloom);

        if (target == null || !config.TRANSPARENT) {
            gl.glBlendFunc(gl.GL_ONE, gl.GL_ONE_MINUS_SRC_ALPHA);
            gl.glEnable(gl.GL_BLEND);
        }
        else {
            gl.glDisable(gl.GL_BLEND);
        }

        val width  = if (target == null) mWidth else dyeWidth;
        val height = if (target == null) mHeight else dyeHeight;

        gl.glViewport(X, Y, width, height);

        if (!config.TRANSPARENT) {
//            colorProgram.bind();
//            val bc = RGB(0f, 33f, 100f)//config.BACK_COLOR//config.BACK_COLOR;
//            gl.glUniform4f(colorProgram.uniforms["color"]?:0, bc.r / 255, bc.g / 255, bc.b / 255, 1f);
//            blit(0);
            initBg()
            blit(0)
            init()
        }

        if (target == null && config.TRANSPARENT) {
            backgroundProgram.bind();
            gl.glUniform1f(backgroundProgram.uniforms["aspectRatio"]?:0, mWidth / mHeight.toFloat());
            blit(0);
        }

        if (config.SHADING) {
            val program = if (config.BLOOM) displayBloomShadingProgram else displayShadingProgram;
            program.bind();
            gl.glUniform2f(program.uniforms["texelSize"]?:0, 1.0f / width, 1.0f / height);
            gl.glUniform1i(program.uniforms["uTexture"]?:0, density.read.attach(0));
            if (config.BLOOM) {
                gl.glUniform1i(program.uniforms["uBloom"]?:0, bloom.attach(1));
                gl.glUniform1i(program.uniforms["uDithering"]?:0, 2);
                //val scale = getTextureScale(ditheringTexture, width, height);
                gl.glUniform2f(program.uniforms["ditherScale"]?:0, 64f / mWidth, 64f / mHeight);
            }
        }
        else {
            val program = if (config.BLOOM) displayBloomProgram else displayProgram;
            program.bind();
            gl.glUniform1i(program.uniforms["uTexture"]?:0, density.read.attach(0));
            if (config.BLOOM) {
                gl.glUniform1i(program.uniforms["uBloom"]?:0, bloom.attach(1));
                gl.glUniform1i(program.uniforms["uDithering"]?:0, 2);
                //val scale = getTextureScale(ditheringTexture, width, height);
                gl.glUniform2f(program.uniforms["ditherScale"]?:0, 64f / mWidth, 64f / mHeight);
            }
        }

        blit(0);
    }

    fun applyBloom (source: FBO, destination: FBO) {
        if (bloomFramebuffers.size < 2)
            return;

        var last = destination;

        gl.glDisable(gl.GL_BLEND);
        bloomPrefilterProgram.bind();
        val knee = config.BLOOM_THRESHOLD * config.BLOOM_SOFT_KNEE + 0.0001f;
        val curve0 = config.BLOOM_THRESHOLD - knee;
        val curve1 = knee * 2f;
        val curve2 = 0.25f / knee;
        gl.glUniform3f(bloomPrefilterProgram.uniforms["curve"]?:0, curve0, curve1, curve2);
        gl.glUniform1f(bloomPrefilterProgram.uniforms["threshold"]?:0, config.BLOOM_THRESHOLD);
        gl.glUniform1i(bloomPrefilterProgram.uniforms["uTexture"]?:0, source.attach(0));
        gl.glViewport(0, 0, last.width, last.height);
        blit(last.fbo);

        bloomBlurProgram.bind();
        for (i in 0..bloomFramebuffers.size - 1) {
            val dest = bloomFramebuffers[i];
            gl.glUniform2f(bloomBlurProgram.uniforms["texelSize"]?:0, 1.0f / last.width, 1.0f / last.height);
            gl.glUniform1i(bloomBlurProgram.uniforms["uTexture"]?:0, last.attach(0));
            gl.glViewport(0, 0, dest.width, dest.height);
            blit(dest.fbo);
            last = dest;
        }

        gl.glBlendFunc(gl.GL_ONE, gl.GL_ONE);
        gl.glEnable(gl.GL_BLEND);

        for (i in bloomFramebuffers.size - 2 downTo  0) {
            val baseTex = bloomFramebuffers[i];
            gl.glUniform2f(bloomBlurProgram.uniforms["texelSize"]?:0, 1.0f / last.width, 1.0f / last.height);
            gl.glUniform1i(bloomBlurProgram.uniforms["uTexture"]?:0, last.attach(0));
            gl.glViewport(0, 0, baseTex.width, baseTex.height);
            blit(baseTex.fbo);
            last = baseTex;
        }

        gl.glDisable(gl.GL_BLEND);
        bloomFinalProgram.bind();
        gl.glUniform2f(bloomFinalProgram.uniforms["texelSize"]?:0, 1.0f / last.width, 1.0f / last.height);
        gl.glUniform1i(bloomFinalProgram.uniforms["uTexture"]?:0, last.attach(0));
        gl.glUniform1f(bloomFinalProgram.uniforms["intensity"]?:0, config.BLOOM_INTENSITY);
        gl.glViewport(0, 0, destination.width, destination.height);
        blit(destination.fbo);
    }

    fun resizeCanvas() {
        //initFramebuffers()
    }

    fun step (dt: Float) {
        gl.glDisable(gl.GL_BLEND);
        gl.glViewport(0, 0, simWidth, simHeight);

        curlProgram.bind();
        gl.glUniform2f(curlProgram.uniforms["texelSize"]?:0, 1.0f / simWidth, 1.0f / simHeight);
        gl.glUniform1i(curlProgram.uniforms["uVelocity"]?:0, velocity.read.attach(0));
        blit(curl!!.fbo);

        vorticityProgram.bind();
        gl.glUniform2f(vorticityProgram.uniforms["texelSize"]?:0, 1.0f / simWidth, 1.0f / simHeight);
        gl.glUniform1i(vorticityProgram.uniforms["uVelocity"]?:0, velocity.read.attach(0));
        gl.glUniform1i(vorticityProgram.uniforms["uCurl"]?:0, curl!!.attach(1));
        gl.glUniform1f(vorticityProgram.uniforms["curl"]?:0, config.CURL);
        gl.glUniform1f(vorticityProgram.uniforms["dt"]?:0, dt);
        blit(velocity.write.fbo);
        velocity.swap();

        divergenceProgram.bind();
        gl.glUniform2f(divergenceProgram.uniforms["texelSize"]?:0, 1.0f / simWidth, 1.0f / simHeight);
        gl.glUniform1i(divergenceProgram.uniforms["uVelocity"]?:0, velocity.read.attach(0));
        blit(divergence!!.fbo);

        clearProgram.bind();
        gl.glUniform1i(clearProgram.uniforms["uTexture"]?:0, pressure.read.attach(0));
        gl.glUniform1f(clearProgram.uniforms["value"]?:0, config.PRESSURE_DISSIPATION);
        blit(pressure.write.fbo);
        pressure.swap();

        pressureProgram.bind();
        gl.glUniform2f(pressureProgram.uniforms["texelSize"]?:0, 1.0f / simWidth, 1.0f / simHeight);
        gl.glUniform1i(pressureProgram.uniforms["uDivergence"]?:0, divergence!!.attach(0));
        for (i in 0..config.PRESSURE_ITERATIONS) {
            gl.glUniform1i(pressureProgram.uniforms["uPressure"]?:0, pressure.read.attach(1));
            blit(pressure.write.fbo);
            pressure.swap();
        }

        gradienSubtractProgram.bind();
        gl.glUniform2f(gradienSubtractProgram.uniforms["texelSize"]?:0, 1.0f / simWidth, 1.0f / simHeight);
        gl.glUniform1i(gradienSubtractProgram.uniforms["uPressure"]?:0, pressure.read.attach(0));
        gl.glUniform1i(gradienSubtractProgram.uniforms["uVelocity"]?:0, velocity.read.attach(1));
        blit(velocity.write.fbo);
        velocity.swap();

        advectionProgram.bind();
        gl.glUniform2f(advectionProgram.uniforms["texelSize"]?:0, 1.0f / simWidth, 1.0f / simHeight);
        if (!ext.supportLinearFiltering)
            gl.glUniform2f(advectionProgram.uniforms["dyeTexelSize"]?:0, 1.0f / simWidth, 1.0f / simHeight);
        val velocityId = velocity.read.attach(0);
        gl.glUniform1i(advectionProgram.uniforms["uVelocity"]?:0, velocityId);
        gl.glUniform1i(advectionProgram.uniforms["uSource"]?:0, velocityId);
        gl.glUniform1f(advectionProgram.uniforms["dt"]?:0, dt);
        gl.glUniform1f(advectionProgram.uniforms["dissipation"]?:0, config.VELOCITY_DISSIPATION);
        blit(velocity.write.fbo);
        velocity.swap();

        gl.glViewport(0, 0, dyeWidth, dyeHeight);

        if (!ext.supportLinearFiltering)
            gl.glUniform2f(advectionProgram.uniforms["dyeTexelSize"]?:0, 1.0f / dyeWidth, 1.0f / dyeHeight);
        gl.glUniform1i(advectionProgram.uniforms["uVelocity"]?:0, velocity.read.attach(0));
        gl.glUniform1i(advectionProgram.uniforms["uSource"]?:0, density.read.attach(1));
        gl.glUniform1f(advectionProgram.uniforms["dissipation"]?:0, config.DENSITY_DISSIPATION);
        blit(density.write.fbo);
        density.swap();
    }

    fun input() {
        if (splatStack.size > 0)
            multipleSplats(splatStack.pop());

        for (i in 0 until pointers.size) {
            val p = pointers[i];
            if (p.moved) {
                splat(p.x, p.y, p.dx, p.dy, p.color);
                p.moved = false;
            }
        }

        if (!config.COLORFUL)
            return;

        if (lastColorChangeTime + 100 < System.currentTimeMillis()) {
            lastColorChangeTime = System.currentTimeMillis()
            for (i in 0 until pointers.size) {
                val p = pointers[i];
                p.color = generateColor();
            }
        }
    }

    override fun onTouchEvent(e: MotionEvent) {
        var action = e.action
        val pointerCount = e.pointerCount
        if (pointerCount > 1) {
            action = e.actionMasked
        }
        when (action) {
            MotionEvent.ACTION_MOVE -> {
                for (index in 0 .. (pointerCount - 1)) {
                    pointers[index].moved = pointers[index].down;
                    pointers[index].dx = (e.getX(index) - pointers[index].x) * 5.0f;
                    pointers[index].dy = (e.getY(index) - pointers[index].y) * 5.0f;
                    pointers[index].x = e.getX(index);
                    pointers[index].y = e.getY(index);
                    pointers[index].color = generateColor()
                }
            }
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_DOWN -> {
                for (index in 0 .. (pointerCount - 1)) {
                    if (index >= pointers.size) {
                        pointers.add(PointerPrototype())
                    }
                    pointers[index].down = true
                    pointers[index].color = generateColor()
                    pointers[index].x = e.getX(index)
                    pointers[index].y = e.getY(index)
                }
            }
            MotionEvent.ACTION_UP -> {
                for (pointer in pointers) {
                    pointer.down = false
                }
            }
        }
    }
}