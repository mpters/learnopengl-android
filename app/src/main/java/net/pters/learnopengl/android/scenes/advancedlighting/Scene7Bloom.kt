package net.pters.learnopengl.android.scenes.advancedlighting

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.scenes.Vertices
import net.pters.learnopengl.android.tools.*

class Scene7Bloom private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val lightBoxFragmentShaderCode: String,
    private val blurVertexShaderCode: String,
    private val blurFragmentShaderCode: String,
    private val finalVertexShaderCode: String,
    private val finalFragmentShaderCode: String,
    private val containerTexture: Texture,
    private val woodTexture: Texture
) : Scene() {

    private val camera = Camera()

    private val cubeModels = listOf(
        translation(Float3(0.0f, 1.5f, 0.0f)) * scale(Float3(0.5f)), // Cube 1
        translation(Float3(2.0f, 0.0f, 1.0f)) * scale(Float3(0.5f)), // Cube 2
        translation(Float3(-1.0f, -1.0f, 2.0f)) * rotation(
            normalize(Float3(1.0f, 0.0f, 1.0f)),
            60.0f
        ), // Cube 3
        translation(Float3(0.0f, 2.7f, 4.0f)) * rotation(
            normalize(Float3(1.0f, 0.0f, 1.0f)),
            23.0f
        ) * scale(Float3(1.25f)), // Cube 4
        translation(Float3(-2.0f, 1.0f, -3.0f)) * rotation(
            normalize(Float3(1.0f, 0.0f, 1.0f)),
            124.0f
        ), // Cube 5
        translation(Float3(-3.0f, 0.0f, 0.0f)) * scale(Float3(0.5f)) // Cube 6
    )

    private val lights = mapOf(
        // Position to color
        Float3(0.0f, 0.5f, 1.5f) to Float3(5.0f, 5.0f, 5.0f),
        Float3(-4.0f, 0.5f, -3.0f) to Float3(10.0f, 0.0f, 0.0f),
        Float3(3.0f, 0.5f, 1.0f) to Float3(0.0f, 0.0f, 15.0f),
        Float3(-0.8f, 2.4f, -1.0f) to Float3(0.0f, 5.0f, 0.0f)
    )

    private val colorBufferIds = IntArray(2)

    private val pingPongFboIds = IntArray(2)

    private val pingPongColorBufferIds = IntArray(2)

    private var hdrFboId = -1

    private lateinit var program: Program

    private lateinit var lightBoxProgram: Program

    private lateinit var blurProgram: Program

    private lateinit var finalProgram: Program

    private lateinit var cubeVertexData: VertexData

    private lateinit var screenQuadVertexData: VertexData

    override fun draw() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // 1. Render scene into floating point framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, hdrFboId)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        program.use()
        val view = camera.getViewMatrix()
        program.setMat4("view", view)

        // Create one large cube that acts as the floor
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, woodTexture.getId())
        var model = translation(Float3(0.0f, -1.0f, 0.0f)) * scale(Float3(12.5f, 0.5f, 12.5f))
        program.setMat4("model", model)
        glBindVertexArray(cubeVertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 36)

        // Then create multiple cubes as the scenery
        glBindTexture(GL_TEXTURE_2D, containerTexture.getId())
        cubeModels.forEach {
            program.setMat4("model", it)
            glDrawArrays(GL_TRIANGLES, 0, 36)
        }

        // Finally show all the light sources as bright cubes
        lightBoxProgram.use()
        lightBoxProgram.setMat4("view", view)
        lights.keys.forEach { position ->
            model = translation(position) * scale(Float3(0.25f))
            lightBoxProgram.setMat4("model", model)
            lightBoxProgram.setFloat3("lightColor", lights.getValue(position))
            glDrawArrays(GL_TRIANGLES, 0, 36)
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // 2. Blur bright fragments with two-pass Gaussian blur
        var horizontal = true
        var firstIteration = true
        val amount = 10
        blurProgram.use()

        for (i in 0 until amount) {
            glBindFramebuffer(GL_FRAMEBUFFER, pingPongFboIds[horizontal.toInt()])
            blurProgram.setBoolean("horizontal", horizontal)
            // Bind texture of other framebuffer (or scene if first iteration)
            if (firstIteration) {
                glBindTexture(GL_TEXTURE_2D, colorBufferIds[1])
            } else {
                glBindTexture(GL_TEXTURE_2D, pingPongColorBufferIds[horizontal.not().toInt()])
            }
            glBindVertexArray(screenQuadVertexData.getVaoId())
            glDrawArrays(GL_TRIANGLES, 0, 6)
            horizontal = !horizontal
            if (firstIteration) {
                firstIteration = false
            }
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // 3. Now render floating point color buffer to 2D quad and tone map HDR colors to default
        // framebuffer's (clamped) color range
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        finalProgram.use()
        glBindTexture(GL_TEXTURE_2D, colorBufferIds[0])
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, pingPongColorBufferIds[horizontal.not().toInt()])
        glBindVertexArray(screenQuadVertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        containerTexture.load()
        woodTexture.load()

        program = Program.create(vertexShaderCode, fragmentShaderCode)
        program.use()
        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)
        program.setMat4("projection", projection)
        lights.keys.forEachIndexed { i, position ->
            program.setFloat3("lights[$i].Position", position)
            program.setFloat3("lights[$i].Color", lights.getValue(position))
        }

        lightBoxProgram = Program.create(vertexShaderCode, lightBoxFragmentShaderCode)
        lightBoxProgram.use()
        lightBoxProgram.setMat4("projection", projection)

        blurProgram = Program.create(blurVertexShaderCode, blurFragmentShaderCode)

        finalProgram = Program.create(finalVertexShaderCode, finalFragmentShaderCode)
        finalProgram.use()
        finalProgram.setBoolean("bloom", true)
        finalProgram.setFloat("exposure", 1.0f)
        finalProgram.setInt("scene", 0)
        finalProgram.setInt("bloomBlur", 1)

        cubeVertexData = VertexData(Vertices.ndcCubeWithNormalsAndTexture, null, 8)
        cubeVertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.addAttribute(program.getAttributeLocation("aNormal"), 3, 3)
        cubeVertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 6)
        cubeVertexData.bind()

        screenQuadVertexData = VertexData(Vertices.ndcQuadWithTexture, null, 4)
        screenQuadVertexData.addAttribute(finalProgram.getAttributeLocation("aPos"), 2, 0)
        screenQuadVertexData.addAttribute(finalProgram.getAttributeLocation("aTexCoords"), 2, 2)
        screenQuadVertexData.bind()

        initFramebuffers(width, height)
    }

    private fun initFramebuffers(width: Int, height: Int) {
        hdrFboId = genId { glGenFramebuffers(1, it) }
        glBindFramebuffer(GL_FRAMEBUFFER, hdrFboId)

        // Create two floating point color buffers (one for normal rendering, other for brightness
        // threshold values)
        glGenTextures(2, colorBufferIds, 0)
        colorBufferIds.forEachIndexed { i, id ->
            glBindTexture(GL_TEXTURE_2D, id)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, null)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            // We clamp to the edge as the blur filter would otherwise sample repeated texture values!
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, id, 0)
        }

        // Create and attach depth buffer (renderbuffer)
        val depthRboId = genId { glGenRenderbuffers(1, it) }
        glBindRenderbuffer(GL_RENDERBUFFER, depthRboId)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width, height)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRboId)

        // Tell OpenGL which color attachments we'll use (of this framebuffer) for rendering
        glDrawBuffers(2, intArrayOf(GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1), 0)

        // Check that framebuffer is complete
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Framebuffer is not complete")
        }

        // Switch back to default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // Ping-pong-framebuffer for blurring
        glGenFramebuffers(2, pingPongFboIds, 0)
        glGenTextures(2, pingPongColorBufferIds, 0)

        pingPongFboIds.forEachIndexed { index, fboId ->
            glBindFramebuffer(GL_FRAMEBUFFER, fboId)
            glBindTexture(GL_TEXTURE_2D, pingPongColorBufferIds[index])
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, null)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            // We clamp to the edge as the blur filter would otherwise sample repeated texture values!
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            glFramebufferTexture2D(
                GL_FRAMEBUFFER,
                GL_COLOR_ATTACHMENT0,
                GL_TEXTURE_2D,
                pingPongColorBufferIds[index],
                0
            )

            // Check that framebuffer is complete
            if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
                throw RuntimeException("Framebuffer is not complete")
            }
        }
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene7Bloom(
                vertexShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene7_bloom_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene7_bloom_frag),
                lightBoxFragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene7_bloom_light_box_frag),
                blurVertexShaderCode = resources.readRawTextFile(R.raw.simple_texture_2d_vert),
                blurFragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene7_bloom_blur_frag),
                finalVertexShaderCode = resources.readRawTextFile(R.raw.simple_texture_2d_vert),
                finalFragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene7_bloom_final_frag),
                containerTexture = Texture(loadBitmap(context, R.raw.texture_container2)),
                woodTexture = Texture(loadBitmap(context, R.raw.texture_wood))
            )
        }
    }
}
