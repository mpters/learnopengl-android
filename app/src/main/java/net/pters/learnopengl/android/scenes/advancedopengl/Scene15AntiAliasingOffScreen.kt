package net.pters.learnopengl.android.scenes.advancedopengl

import android.content.Context
import android.opengl.GLES30.*
import android.opengl.GLES31.GL_TEXTURE_2D_MULTISAMPLE
import android.opengl.GLES31.glTexStorage2DMultisample
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.normalize
import com.curiouscreature.kotlin.math.perspective
import com.curiouscreature.kotlin.math.rotation
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.scenes.Vertices.cube
import net.pters.learnopengl.android.scenes.Vertices.ndcQuadWithTexture
import net.pters.learnopengl.android.tools.*

class Scene15AntiAliasingOffScreen private constructor(
    private val cubeVertexShaderCode: String,
    private val cubeFragmentShaderCode: String,
    private val postVertexShaderCode: String,
    private val postFragmentShaderCode: String
) : Scene() {

    private val camera = Camera()

    private lateinit var cubeProgram: Program

    private lateinit var postProgram: Program

    private lateinit var cubeVertexData: VertexData

    private lateinit var screenQuadVertexData: VertexData

    private var width: Int = -1

    private var height: Int = -1

    private var intermediateFboId = -1

    private var msaaFboId = -1

    private var screenTextureId = -1

    override fun draw() {
        // Draw scene as normal in multi-sampled buffer
        glBindFramebuffer(GL_FRAMEBUFFER, msaaFboId)
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glEnable(GL_DEPTH_TEST)

        cubeProgram.use()
        cubeProgram.setMat4("view", camera.getViewMatrix())
        glBindVertexArray(cubeVertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 36)

        // Blit multi-sampled buffer to normal color buffer of intermediate FBO
        glBindFramebuffer(GL_READ_FRAMEBUFFER, msaaFboId)
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, intermediateFboId)
        glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST)

        // Switch back to default buffer an render quad with scene as its texture image
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)
        glDisable(GL_DEPTH_TEST)

        postProgram.use()
        glBindVertexArray(screenQuadVertexData.getVaoId())
        glBindTexture(GL_TEXTURE_2D, screenTextureId)
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glViewport(0, 0, width, height)

        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)
        cubeProgram = Program.create(cubeVertexShaderCode, cubeFragmentShaderCode)
        cubeProgram.use()
        cubeProgram.setMat4("model", rotation(normalize(Float3(1f, 1f, 1.0f)), 45.0f))
        cubeProgram.setMat4("projection", projection)

        postProgram = Program.create(postVertexShaderCode, postFragmentShaderCode)

        cubeVertexData = VertexData(cube, null, 3)
        cubeVertexData.addAttribute(cubeProgram.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.bind()

        screenQuadVertexData = VertexData(ndcQuadWithTexture, null, 4)
        screenQuadVertexData.addAttribute(postProgram.getAttributeLocation("aPos"), 2, 0)
        screenQuadVertexData.addAttribute(postProgram.getAttributeLocation("aTexCoords"), 2, 2)
        screenQuadVertexData.bind()

        initMSAAFramebuffer(width, height)
    }

    private fun initMSAAFramebuffer(width: Int, height: Int) {
        this.width = width
        this.height = height

        // Create & bind MSAA framebuffer
        msaaFboId = genId { glGenFramebuffers(1, it) }
        glBindFramebuffer(GL_FRAMEBUFFER, msaaFboId)

        // Create a multi-sampled color attachment texture
        val multiSampledTextureId = genId { glGenTextures(1, it) }
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, multiSampledTextureId)
        glTexStorage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 4, GL_RGBA8, width, height, true)
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0)
        glFramebufferTexture2D(
            GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0,
            GL_TEXTURE_2D_MULTISAMPLE,
            multiSampledTextureId,
            0
        )

        // Create a multi-sampled renderbuffer for depth and stencil attachments
        val rboId = genId { glGenRenderbuffers(1, it) }
        glBindRenderbuffer(GL_RENDERBUFFER, rboId)
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, 4, GL_DEPTH24_STENCIL8, width, height)
        glBindRenderbuffer(GL_RENDERBUFFER, 0)
        glFramebufferRenderbuffer(
            GL_FRAMEBUFFER,
            GL_DEPTH_STENCIL_ATTACHMENT,
            GL_RENDERBUFFER,
            rboId
        )

        // Check that framebuffer is complete
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Framebuffer is not complete")
        }

        // Switch back to default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // Create & bind second post-processing framebuffer
        intermediateFboId = genId { glGenFramebuffers(1, it) }
        glBindFramebuffer(GL_FRAMEBUFFER, intermediateFboId)

        // Create a color attachment texture
        screenTextureId = genId { glGenTextures(1, it) }
        glBindTexture(GL_TEXTURE_2D, screenTextureId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glFramebufferTexture2D(
            GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0,
            GL_TEXTURE_2D,
            screenTextureId,
            0
        )

        // Check that framebuffer is complete
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Intermediate framebuffer is not complete")
        }

        // Switch back to default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene15AntiAliasingOffScreen(
                cubeVertexShaderCode = resources.readRawTextFile(R.raw.simple_mvp_vert),
                cubeFragmentShaderCode = resources.readRawTextFile(R.raw.simple_green_frag),
                postVertexShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene15_antialiasing_offscreen_post_vert),
                postFragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene15_antialiasing_offscreen_post_frag),
            )
        }
    }
}
