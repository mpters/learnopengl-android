package net.pters.learnopengl.android.scenes.advancedlighting

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.scenes.Vertices
import net.pters.learnopengl.android.tools.*
import net.pters.learnopengl.android.tools.model.Model
import net.pters.learnopengl.android.tools.model.ObjLoader
import net.pters.learnopengl.android.tools.model.ProgramLocations
import kotlin.random.Random

class Scene9SSAO private constructor(
    private val gBufferVertexShaderCode: String,
    private val gBufferFragmentShaderCode: String,
    private val lightingVertexShaderCode: String,
    private val lightingFragmentShaderCode: String,
    private val ssaoVertexShaderCode: String,
    private val ssaoFragmentShaderCode: String,
    private val blurVertexShaderCode: String,
    private val blurFragmentShaderCode: String,
    private val backpackModel: Model
) : Scene() {

    private val camera = Camera()

    private var gBufferFboId = -1

    private var gAlbedoBufferId = -1

    private var gNormalBufferId = -1

    private var gPositionBufferId = -1

    private var ssaoFboId = -1

    private var ssaoColorBufferId = -1

    private var blurFboId = -1

    private var blurColorBufferId = -1

    private var noiseTextureId = -1

    private var width: Int = -1

    private var height: Int = -1

    private lateinit var gBufferProgram: Program

    private lateinit var lightingProgram: Program

    private lateinit var ssaoProgram: Program

    private lateinit var blurProgram: Program

    private lateinit var cubeVertexData: VertexData

    private lateinit var screenQuadVertexData: VertexData

    override fun draw() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // 1. Geometry pass: Render scene's geometry/color data into G-Buffer
        glBindFramebuffer(GL_FRAMEBUFFER, gBufferFboId)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        gBufferProgram.use()
        gBufferProgram.setMat4("view", camera.getViewMatrix())

        // Room cube
        var model = translation(Float3(0.0f, 7.0f, 0.0f)) * scale(Float3(7.5f))
        gBufferProgram.setMat4("model", model)
        // Invert normals as we're inside the cube
        gBufferProgram.setBoolean("invertedNormals", true)
        glBindVertexArray(cubeVertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 36)
        gBufferProgram.setBoolean("invertedNormals", false)

        // Backpack model on the floor
        model = translation(Float3(0.0f, 0.5f, 0.0f)) * rotation(Float3(x = 1.0f), -90.0f)
        gBufferProgram.setMat4("model", model)
        backpackModel.draw()
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // 2. Generate SSAO texture
        glBindFramebuffer(GL_FRAMEBUFFER, ssaoFboId)
        glClear(GL_COLOR_BUFFER_BIT)
        ssaoProgram.use()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, gPositionBufferId)
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, gNormalBufferId)
        glActiveTexture(GL_TEXTURE2)
        glBindTexture(GL_TEXTURE_2D, noiseTextureId)
        glBindVertexArray(screenQuadVertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 6)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // 3. Blur SSAO texture to remove noise
        glBindFramebuffer(GL_FRAMEBUFFER, blurFboId)
        glClear(GL_COLOR_BUFFER_BIT)
        blurProgram.use()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, ssaoColorBufferId)
        glBindVertexArray(screenQuadVertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 6)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // 4. Lighting pass: Traditional deferred Blinn-Phong lighting with added screen-space ambient occlusion
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        lightingProgram.use()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, gPositionBufferId)
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, gNormalBufferId)
        glActiveTexture(GL_TEXTURE2)
        glBindTexture(GL_TEXTURE_2D, gAlbedoBufferId)
        glActiveTexture(GL_TEXTURE3) // Add extra SSAO texture to lighting pass
        glBindTexture(GL_TEXTURE_2D, ssaoColorBufferId)
        glBindVertexArray(screenQuadVertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        this.width = width
        this.height = height

        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        gBufferProgram = Program.create(gBufferVertexShaderCode, gBufferFragmentShaderCode)
        gBufferProgram.use()
        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)
        gBufferProgram.setMat4("projection", projection)

        lightingProgram = Program.create(lightingVertexShaderCode, lightingFragmentShaderCode)
        lightingProgram.use()
        lightingProgram.setInt("gPosition", 0)
        lightingProgram.setInt("gNormal", 1)
        lightingProgram.setInt("gAlbedo", 2)
        lightingProgram.setInt("ssao", 3)
        val lightPosView = camera.getViewMatrix() * Float4(2.0f, 4.0f, -2.0f, 1.0f)
        lightingProgram.setFloat3("light.Position", lightPosView.xyz)
        lightingProgram.setFloat3("light.Color", Float3(0.2f, 0.2f, 0.7f))
        lightingProgram.setFloat("light.Linear", 0.09f)
        lightingProgram.setFloat("light.Quadratic", 0.032f)

        ssaoProgram = Program.create(ssaoVertexShaderCode, ssaoFragmentShaderCode)
        ssaoProgram.use()
        ssaoProgram.setMat4("projection", projection)
        ssaoProgram.setInt("gPosition", 0)
        ssaoProgram.setInt("gNormal", 1)
        ssaoProgram.setInt("texNoise", 2)

        blurProgram = Program.create(blurVertexShaderCode, blurFragmentShaderCode)
        blurProgram.use()
        blurProgram.setInt("ssaoInput", 0)

        cubeVertexData = VertexData(Vertices.ndcCubeWithNormalsAndTexture, null, 8)
        cubeVertexData.addAttribute(gBufferProgram.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.addAttribute(gBufferProgram.getAttributeLocation("aNormal"), 3, 3)
        cubeVertexData.bind()

        screenQuadVertexData = VertexData(Vertices.ndcQuadWithTexture, null, 4)
        screenQuadVertexData.addAttribute(lightingProgram.getAttributeLocation("aPos"), 2, 0)
        screenQuadVertexData.addAttribute(lightingProgram.getAttributeLocation("aTexCoords"), 2, 2)
        screenQuadVertexData.bind()

        backpackModel.bind(
            gBufferProgram, ProgramLocations(
                attribPosition = gBufferProgram.getAttributeLocation("aPos"),
                attribNormal = gBufferProgram.getAttributeLocation("aNormal"),
                attribTexCoords = null,
                uniformDiffuseTexture = null
            )
        )

        generateNoiseTexture()
        generateSampleKernel()

        initGBuffer()
        initSSAOFramebuffer()
    }

    private fun generateNoiseTexture() {
        val randomFloat = { Random.nextDouble(0.0, 1.0).toFloat() }
        var ssaoNoise = floatArrayOf()
        for (i in 0 until 16) {
            // Rotate around z-axis (in tangent space)
            val noise = floatArrayOf(randomFloat() * 2.0f - 1.0f, randomFloat() * 2.0f - 1.0f, 0.0f)
            ssaoNoise += noise
        }

        noiseTextureId = genId { glGenTextures(1, it) }
        glBindTexture(GL_TEXTURE_2D, noiseTextureId)
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA32F,
            4,
            4,
            0,
            GL_RGB,
            GL_FLOAT,
            ssaoNoise.toFloatBuffer()
        )
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
    }

    private fun generateSampleKernel() {
        val randomFloat = { Random.nextDouble(0.0, 1.0).toFloat() }
        var kernel = floatArrayOf()
        for (i in 0 until 64) {
            val sample = normalize(
                Float3(
                    randomFloat() * 2.0f - 1.0f,
                    randomFloat() * 2.0f - 1.0f,
                    randomFloat()
                )
            ) * randomFloat()
            var scale = i / 64.0f
            // Scale samples s.t. they're more aligned to center of kernel
            scale = 0.1f + (scale * scale) * (1.0f - 0.1f)
            kernel += (sample * scale).toArray()
        }
        ssaoProgram.use()
        glUniform3fv(ssaoProgram.getUniformLocation("samples[0]"), 64, kernel, 0)
    }

    private fun initGBuffer() {
        gBufferFboId = genId { glGenFramebuffers(1, it) }
        glBindFramebuffer(GL_FRAMEBUFFER, gBufferFboId)

        // Position color buffer
        gPositionBufferId = genId { glGenTextures(1, it) }
        glBindTexture(GL_TEXTURE_2D, gPositionBufferId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, null)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glFramebufferTexture2D(
            GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0,
            GL_TEXTURE_2D,
            gPositionBufferId,
            0
        )

        // Normal color buffer
        gNormalBufferId = genId { glGenTextures(1, it) }
        glBindTexture(GL_TEXTURE_2D, gNormalBufferId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, null)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glFramebufferTexture2D(
            GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT1,
            GL_TEXTURE_2D,
            gNormalBufferId,
            0
        )

        // Color + specular color buffer
        gAlbedoBufferId = genId { glGenTextures(1, it) }
        glBindTexture(GL_TEXTURE_2D, gAlbedoBufferId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glFramebufferTexture2D(
            GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT2,
            GL_TEXTURE_2D,
            gAlbedoBufferId,
            0
        )

        // Tell OpenGL which color attachments we'll use (of this framebuffer) for rendering
        glDrawBuffers(
            3,
            intArrayOf(GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2),
            0
        )

        // Create and attach depth buffer (renderbuffer)
        val depthRboId = genId { glGenRenderbuffers(1, it) }
        glBindRenderbuffer(GL_RENDERBUFFER, depthRboId)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height)
        glFramebufferRenderbuffer(
            GL_FRAMEBUFFER,
            GL_DEPTH_STENCIL_ATTACHMENT,
            GL_RENDERBUFFER,
            depthRboId
        )

        // Check that framebuffer is complete
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Framebuffer is not complete")
        }

        // Switch back to default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    private fun initSSAOFramebuffer() {
        // SSAO color buffer
        ssaoFboId = genId { glGenFramebuffers(1, it) }
        glBindFramebuffer(GL_FRAMEBUFFER, ssaoFboId)
        ssaoColorBufferId = genId { glGenTextures(1, it) }
        glBindTexture(GL_TEXTURE_2D, ssaoColorBufferId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, width, height, 0, GL_RED, GL_UNSIGNED_BYTE, null)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glFramebufferTexture2D(
            GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0,
            GL_TEXTURE_2D,
            ssaoColorBufferId,
            0
        )

        // Check that framebuffer is complete
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Framebuffer is not complete")
        }

        // Blur stage
        blurFboId = genId { glGenFramebuffers(1, it) }
        glBindFramebuffer(GL_FRAMEBUFFER, blurFboId)
        blurColorBufferId = genId { glGenTextures(1, it) }
        glBindTexture(GL_TEXTURE_2D, blurColorBufferId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, width, height, 0, GL_RED, GL_UNSIGNED_BYTE, null)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glFramebufferTexture2D(
            GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0,
            GL_TEXTURE_2D,
            blurColorBufferId,
            0
        )

        // Check that framebuffer is complete
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Framebuffer is not complete")
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene9SSAO(
                gBufferVertexShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene9_ssao_gbuffer_vert),
                gBufferFragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene9_ssao_gbuffer_frag),
                lightingVertexShaderCode = resources.readRawTextFile(R.raw.simple_texture_2d_vert),
                lightingFragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene9_ssao_lighting_frag),
                ssaoVertexShaderCode = resources.readRawTextFile(R.raw.simple_texture_2d_vert),
                ssaoFragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene9_ssao_frag),
                blurVertexShaderCode = resources.readRawTextFile(R.raw.simple_texture_2d_vert),
                blurFragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene9_ssao_blur_frag),
                ObjLoader.fromAssets(
                    context,
                    directory = "backpack",
                    objFileName = "backpack.obj",
                    mtlFileName = "backpack.mtl",
                    specularTextureFileName = "specular.jpg"
                )
            )
        }
    }
}
