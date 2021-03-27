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

class Scene6HDR private constructor(
    private val lightingVertexShaderCode: String,
    private val lightingFragmentShaderCode: String,
    private val toneMappingVertexShaderCode: String,
    private val toneMappingFragmentShaderCode: String,
    private val woodTexture: Texture
) : Scene() {

    private val camera = Camera(yaw = 90.0f)

    private val lights = mapOf(
        // Position to color
        Float3(0.0f, 0.0f, 49.5f) to Float3(200.0f, 200.0f, 200.0f), // Back light
        Float3(-1.4f, -1.9f, 9.0f) to Float3(0.1f, 0.0f, 0.0f),
        Float3(0.0f, -1.8f, 4.0f) to Float3(0.0f, 0.0f, 0.2f),
        Float3(0.8f, -1.7f, 6.0f) to Float3(0.0f, 0.1f, 0.0f)
    )

    private var hdrFboId = -1

    private var hdrTextureId = -1

    private lateinit var hdrProgram: Program

    private lateinit var lightingProgram: Program

    private lateinit var cubeVertexData: VertexData

    private lateinit var screenQuadVertexData: VertexData

    override fun draw() {
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        // 1. Render scene into floating point framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, hdrFboId)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        lightingProgram.use()
        val model = translation(Float3(0.0f, 0.0f, 25.0f)) * scale(Float3(2.5f, 2.5f, 27.5f))
        lightingProgram.setMat4("model", model)
        lightingProgram.setMat4("view", camera.getViewMatrix())

        glBindTexture(GL_TEXTURE_2D, woodTexture.getId())
        glBindVertexArray(cubeVertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 36)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // 2. Render floating point color buffer to 2D quad and tonemap HDR colors to default
        // framebuffer's (clamped) color range
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        hdrProgram.use()
        glBindTexture(GL_TEXTURE_2D, hdrTextureId)
        glBindVertexArray(screenQuadVertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        woodTexture.load()

        hdrProgram = Program.create(toneMappingVertexShaderCode, toneMappingFragmentShaderCode)
        hdrProgram.use()
        hdrProgram.setBoolean("hdr", true)
        // hdrProgram.setFloat("exposure", 5.0f)

        lightingProgram = Program.create(lightingVertexShaderCode, lightingFragmentShaderCode)
        lightingProgram.use()
        lightingProgram.setBoolean("inverse_normals", true)
        lights.keys.forEachIndexed { i, position ->
            lightingProgram.setFloat3("lights[$i].Position", position)
            lightingProgram.setFloat3("lights[$i].Color", lights.getValue(position))
        }
        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)
        lightingProgram.setMat4("projection", projection)

        cubeVertexData = VertexData(Vertices.ndcCubeWithNormalsAndTexture, null, 8)
        cubeVertexData.addAttribute(lightingProgram.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.addAttribute(lightingProgram.getAttributeLocation("aNormal"), 3, 3)
        cubeVertexData.addAttribute(lightingProgram.getAttributeLocation("aTexCoords"), 2, 6)
        cubeVertexData.bind()

        screenQuadVertexData = VertexData(Vertices.ndcQuadWithTexture, null, 4)
        screenQuadVertexData.addAttribute(hdrProgram.getAttributeLocation("aPos"), 2, 0)
        screenQuadVertexData.addAttribute(hdrProgram.getAttributeLocation("aTexCoords"), 2, 2)
        screenQuadVertexData.bind()

        initHDRFramebuffer(width, height)
    }

    private fun initHDRFramebuffer(width: Int, height: Int) {
        hdrFboId = genId { glGenFramebuffers(1, it) }

        // Create floating point color buffer
        hdrTextureId = genId { glGenTextures(1, it) }
        glBindTexture(GL_TEXTURE_2D, hdrTextureId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, null)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // Create depth buffer (renderbuffer)
        val depthRboId = genId { glGenRenderbuffers(1, it) }
        glBindRenderbuffer(GL_RENDERBUFFER, depthRboId)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width, height)

        // Attach buffers
        glBindFramebuffer(GL_FRAMEBUFFER, hdrFboId)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRboId)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, hdrTextureId, 0)

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
            return Scene6HDR(
                lightingVertexShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene6_hdr_lighting_vert),
                lightingFragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene6_hdr_lighting_frag),
                toneMappingVertexShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene6_hdr_tone_mapping_vert),
                toneMappingFragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene6_hdr_tone_mapping_frag),
                woodTexture = Texture(loadBitmap(context, R.raw.texture_wood))
            )
        }
    }
}
