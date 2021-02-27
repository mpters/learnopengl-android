package net.pters.learnopengl.android.scenes.advancedopengl

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.Mat4
import com.curiouscreature.kotlin.math.perspective
import com.curiouscreature.kotlin.math.translation
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.scenes.Vertices.cubeWithTexture
import net.pters.learnopengl.android.scenes.Vertices.ndcQuadWithTexture
import net.pters.learnopengl.android.scenes.Vertices.planeWithTexture
import net.pters.learnopengl.android.tools.*
import java.nio.IntBuffer

class Scene7Framebuffers private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val screenVertexShaderCode: String,
    private val screenFragmentShaderCode: String,
    private val containerTexture: Texture,
    private val metalTexture: Texture
) : Scene() {

    private val camera = Camera()

    private lateinit var program: Program

    private lateinit var screenProgram: Program

    private lateinit var cubeVertexData: VertexData

    private lateinit var planeVertexData: VertexData

    private lateinit var screenQuadVertexData: VertexData

    private var fboId = -1

    private var texColorBufferId = -1

    override fun draw() {
        // Bind to our custom framebuffer and draw scene as we normally would
        glBindFramebuffer(GL_FRAMEBUFFER, fboId)
        glEnable(GL_DEPTH_TEST)
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        program.use()
        program.setMat4("view", camera.getViewMatrix())

        // Cube 1
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, containerTexture.getId())
        glBindVertexArray(cubeVertexData.getVaoId())
        var model = translation(Float3(-1.0f, 0.0f, -1.0f))
        program.setMat4("model", model)
        glDrawArrays(GL_TRIANGLES, 0, 36)

        // Cube 2
        model = translation(Float3(2.0f, 0.0f, 0.0f))
        program.setMat4("model", model)
        glDrawArrays(GL_TRIANGLES, 0, 36)

        // Floor
        glBindTexture(GL_TEXTURE_2D, metalTexture.getId())
        glBindVertexArray(planeVertexData.getVaoId())
        program.setMat4("model", Mat4())
        glDrawArrays(GL_TRIANGLES, 0, 6)

        // Now bind back to default framebuffer and draw a screen quad with attached color texture
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)
        glDisable(GL_DEPTH_TEST) // Disable depth testing so screen quad isn't discarded

        screenProgram.use()
        glBindVertexArray(screenQuadVertexData.getVaoId())
        glBindTexture(GL_TEXTURE_2D, texColorBufferId)
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        containerTexture.load()
        metalTexture.load()

        program = Program.create(vertexShaderCode, fragmentShaderCode)
        screenProgram = Program.create(screenVertexShaderCode, screenFragmentShaderCode)

        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)
        program.use()
        program.setMat4("projection", projection)

        cubeVertexData = VertexData(cubeWithTexture, null, 5)
        cubeVertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 3)
        cubeVertexData.bind()

        planeVertexData = VertexData(planeWithTexture, null, 5)
        planeVertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        planeVertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 3)
        planeVertexData.bind()

        screenQuadVertexData = VertexData(ndcQuadWithTexture, null, 4)
        screenQuadVertexData.addAttribute(program.getAttributeLocation("aPos"), 2, 0)
        screenQuadVertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 2)
        screenQuadVertexData.bind()

        initFramebuffer(width, height)
    }

    private fun initFramebuffer(width: Int, height: Int) {
        // Create & bind framebuffer
        val fbo = IntBuffer.allocate(1)
        glGenFramebuffers(1, fbo)
        fboId = fbo[0]
        glBindFramebuffer(GL_FRAMEBUFFER, fboId)

        // Create texture as a color attachment for our framebuffer
        val texColorBuffer = IntBuffer.allocate(1)
        glGenTextures(1, texColorBuffer)
        texColorBufferId = texColorBuffer[0]
        glBindTexture(GL_TEXTURE_2D, texColorBufferId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, null)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glBindTexture(GL_TEXTURE_2D, 0)

        // Attach it to currently bound framebuffer
        glFramebufferTexture2D(
            GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0,
            GL_TEXTURE_2D,
            texColorBufferId,
            0
        )

        // Create a renderbuffer object for depth and stencil attachment (we won't be sampling these)
        val rbo = IntBuffer.allocate(1)
        glGenRenderbuffers(1, rbo)
        val rboId = rbo[0]
        glBindRenderbuffer(GL_RENDERBUFFER, rboId)
        // Use a single renderbuffer object for both a depth & stencil buffer.
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height)
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
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene7Framebuffers(
                vertexShaderCode = resources.readRawTextFile(R.raw.simple_texture_mvp_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.simple_texture_frag),
                screenVertexShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene7_framebuffers_screen_vert),
                screenFragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene7_framebuffers_screen_frag),
                containerTexture = Texture(
                    loadBitmap(
                        context,
                        R.raw.texture_container
                    ).flipVertically()
                ),
                metalTexture = Texture(loadBitmap(context, R.raw.texture_metal))
            )
        }
    }
}
