package net.pters.learnopengl.android.scenes.advancedlighting

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.perspective
import com.curiouscreature.kotlin.math.scale
import com.curiouscreature.kotlin.math.translation
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.scenes.Vertices
import net.pters.learnopengl.android.tools.*
import net.pters.learnopengl.android.tools.model.Model
import net.pters.learnopengl.android.tools.model.ObjLoader
import net.pters.learnopengl.android.tools.model.ProgramLocations
import kotlin.random.Random

class Scene81DeferredShading private constructor(
    private val gBufferVertexShaderCode: String,
    private val gBufferFragmentShaderCode: String,
    private val lightBoxVertexShaderCode: String,
    private val lightBoxFragmentShaderCode: String,
    private val lightingVertexShaderCode: String,
    private val lightingFragmentShaderCode: String,
    private val backpackModel: Model
) : Scene() {

    private val camera = Camera(eye = Float3(z = 10.0f))

    private val backpackPositions = listOf(
        Float3(-3.0f, -0.5f, -3.0f),
        Float3(0.0f, -0.5f, -3.0f),
        Float3(3.0f, -0.5f, -3.0f),
        Float3(-3.0f, -0.5f, 0.0f),
        Float3(0.0f, -0.5f, 0.0f),
        Float3(3.0f, -0.5f, 0.0f),
        Float3(-3.0f, -0.5f, 3.0f),
        Float3(0.0f, -0.5f, 3.0f),
        Float3(3.0f, -0.5f, 3.0f)
    )

    private val lights = mutableMapOf<Float3, Float3>()

    private var gBufferFboId = -1

    private var gAlbedoBufferId = -1

    private var gNormalBufferId = -1

    private var gPositionBufferId = -1

    private var width: Int = -1

    private var height: Int = -1

    private lateinit var gBufferProgram: Program

    private lateinit var lightBoxProgram: Program

    private lateinit var lightingProgram: Program

    private lateinit var cubeVertexData: VertexData

    private lateinit var screenQuadVertexData: VertexData

    init {
        // Generate lights
        val nextRand = { Random.nextDouble(0.0, 1.0).toFloat() }
        for (i in 0 until 32) {
            val position = Float3(
                x = nextRand() * 6.0f - 3.0f,
                y = nextRand() * 6.0f - 4.0f,
                z = nextRand() * 6.0f - 3.0f
            )
            val color = Float3(
                x = nextRand() / 2.0f + 0.5f,
                y = nextRand() / 2.0f + 0.5f,
                z = nextRand() / 2.0f + 0.5f
            )
            lights[position] = color
        }
    }

    override fun draw() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // 1. Geometry pass: Render scene's geometry/color data into G-Buffer
        glBindFramebuffer(GL_FRAMEBUFFER, gBufferFboId)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        gBufferProgram.use()
        gBufferProgram.setMat4("view", camera.getViewMatrix())
        backpackPositions.forEach {
            val model = translation(it) * scale(Float3(0.5f))
            gBufferProgram.setMat4("model", model)
            backpackModel.draw()
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // 2. Lighting pass: Calculate lighting by iterating over a screen filled quad
        // pixel-by-pixel using the G-Buffer's content
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        lightingProgram.use()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, gPositionBufferId)
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, gNormalBufferId)
        glActiveTexture(GL_TEXTURE2)
        glBindTexture(GL_TEXTURE_2D, gAlbedoBufferId)
        glBindVertexArray(screenQuadVertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 6)

        // 2.5. Copy content of geometry's depth buffer to default framebuffer's depth buffer
        glBindFramebuffer(GL_READ_FRAMEBUFFER, gBufferFboId)
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0) // Write to default framebuffer
        // Blit to default framebuffer. Note that this may or may not work as the internal formats
        // of both the FBO and default framebuffer have to match. The internal formats are
        // implementation defined. This works on all of my systems, but if it doesn't on yours
        // you'll likely have to write to the depth buffer in another shader stage (or somehow see
        // to match the default framebuffer's internal format with the FBO's internal format).
        glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_DEPTH_BUFFER_BIT, GL_NEAREST)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // 3. Render lights on top of scene
        lightBoxProgram.use()
        lightBoxProgram.setMat4("view", camera.getViewMatrix())
        glBindVertexArray(cubeVertexData.getVaoId())
        lights.keys.forEach { position ->
            val model = translation(position) * scale(Float3(0.125f))
            lightBoxProgram.setMat4("model", model)
            lightBoxProgram.setFloat3("lightColor", lights.getValue(position))
            glDrawArrays(GL_TRIANGLES, 0, 36)
        }
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

        lightBoxProgram = Program.create(lightBoxVertexShaderCode, lightBoxFragmentShaderCode)
        lightBoxProgram.use()
        lightBoxProgram.setMat4("projection", projection)

        lightingProgram = Program.create(lightingVertexShaderCode, lightingFragmentShaderCode)
        lightingProgram.use()
        lightingProgram.setInt("gPosition", 0)
        lightingProgram.setInt("gNormal", 1)
        lightingProgram.setInt("gAlbedoSpec", 2)
        lights.keys.forEachIndexed { i, position ->
            lightingProgram.setFloat3("lights[$i].Position", position)
            lightingProgram.setFloat3("lights[$i].Color", lights.getValue(position))
            lightingProgram.setFloat("lights[$i].Linear", 0.7f)
            lightingProgram.setFloat("lights[$i].Quadratic", 1.8f)
        }

        cubeVertexData = VertexData(Vertices.ndcCubeWithNormalsAndTexture, null, 8)
        cubeVertexData.addAttribute(lightBoxProgram.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.bind()

        screenQuadVertexData = VertexData(Vertices.ndcQuadWithTexture, null, 4)
        screenQuadVertexData.addAttribute(lightingProgram.getAttributeLocation("aPos"), 2, 0)
        screenQuadVertexData.addAttribute(lightingProgram.getAttributeLocation("aTexCoords"), 2, 2)
        screenQuadVertexData.bind()

        backpackModel.bind(
            gBufferProgram, ProgramLocations(
                attribPosition = gBufferProgram.getAttributeLocation("aPos"),
                attribNormal = gBufferProgram.getAttributeLocation("aNormal"),
                attribTexCoords = gBufferProgram.getAttributeLocation("aTexCoords"),
                uniformDiffuseTexture = gBufferProgram.getUniformLocation("texture_diffuse1"),
                uniformSpecularTexture = gBufferProgram.getUniformLocation("texture_specular1")
            )
        )

        initGBuffer()
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

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene81DeferredShading(
                gBufferVertexShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene81_deferred_shading_gbuffer_vert),
                gBufferFragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene81_deferred_shading_gbuffer_frag),
                lightBoxVertexShaderCode = resources.readRawTextFile(R.raw.simple_mvp_vert),
                lightBoxFragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene81_deferred_shading_light_box_frag),
                lightingVertexShaderCode = resources.readRawTextFile(R.raw.simple_texture_2d_vert),
                lightingFragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene81_deferred_shading_lighting_frag),
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
