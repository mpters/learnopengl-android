package net.pters.learnopengl.android.scenes.advancedopengl

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.scenes.Vertices.cubeWithTexture
import net.pters.learnopengl.android.tools.*

class Scene5SortedBlending private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val marbleTexture: Texture,
    private val metalTexture: Texture,
    private val windowTexture: Texture
) : Scene() {

    private val planeVertices = floatArrayOf(
        5.0f, -0.5f, 5.0f, 2.0f, 0.0f,
        -5.0f, -0.5f, 5.0f, 0.0f, 0.0f,
        -5.0f, -0.5f, -5.0f, 0.0f, 2.0f,

        5.0f, -0.5f, 5.0f, 2.0f, 0.0f,
        -5.0f, -0.5f, -5.0f, 0.0f, 2.0f,
        5.0f, -0.5f, -5.0f, 2.0f, 2.0f
    )

    private val windowVertices = floatArrayOf(
        0.0f, 0.5f, 0.0f, 0.0f, 0.0f,
        0.0f, -0.5f, 0.0f, 0.0f, 1.0f,
        1.0f, -0.5f, 0.0f, 1.0f, 1.0f,

        0.0f, 0.5f, 0.0f, 0.0f, 0.0f,
        1.0f, -0.5f, 0.0f, 1.0f, 1.0f,
        1.0f, 0.5f, 0.0f, 1.0f, 0.0f
    )

    private val windowPositions = listOf(
        Float3(-1.5f, 0.0f, -0.48f),
        Float3(1.5f, 0.0f, 0.51f),
        Float3(0.0f, 0.0f, 0.7f),
        Float3(-0.3f, 0.0f, -2.3f),
        Float3(0.5f, 0.0f, -0.6f)
    )

    private val camera = Camera()

    private lateinit var program: Program

    private lateinit var cubeVertexData: VertexData

    private lateinit var planeVertexData: VertexData

    private lateinit var windowVertexData: VertexData

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        program.setMat4("view", camera.getViewMatrix())

        // Cube 1
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, marbleTexture.getId())
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

        // Windows
        glBindTexture(GL_TEXTURE_2D, windowTexture.getId())
        glBindVertexArray(windowVertexData.getVaoId())
        windowPositions.sortedByDescending {
            length(camera.getEye() - it)
        }.forEach {
            model = translation(it)
            program.setMat4("model", model)
            glDrawArrays(GL_TRIANGLES, 0, 6)
        }
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glViewport(0, 0, width, height)

        windowTexture.load()
        marbleTexture.load()
        metalTexture.load()

        program = Program.create(vertexShaderCode, fragmentShaderCode)
        program.use()

        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)
        program.setMat4("projection", projection)

        cubeVertexData = VertexData(cubeWithTexture, null, 5)
        cubeVertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 3)
        cubeVertexData.bind()

        planeVertexData = VertexData(planeVertices, null, 5)
        planeVertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        planeVertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 3)
        planeVertexData.bind()

        windowVertexData = VertexData(windowVertices, null, 5)
        windowVertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        windowVertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 3)
        windowVertexData.bind()
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene5SortedBlending(
                vertexShaderCode = resources.readRawTextFile(R.raw.simple_texture_mvp_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.simple_texture_frag),
                marbleTexture = Texture(loadBitmap(context, R.raw.texture_marble)),
                metalTexture = Texture(loadBitmap(context, R.raw.texture_metal)),
                windowTexture = Texture(
                    loadBitmap(
                        context,
                        R.raw.texture_blending_transparent_window
                    )
                )
            )
        }
    }
}
