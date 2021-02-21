package net.pters.learnopengl.android.scenes.advancedopengl

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.scenes.Vertices.cubeWithTexture
import net.pters.learnopengl.android.tools.*

class Scene3StencilTesting private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val singleColorfragmentShaderCode: String,
    private val marbleTexture: Texture,
    private val metalTexture: Texture
) : Scene() {

    private val planeVertices = floatArrayOf(
        5.0f, -0.5f, 5.0f, 2.0f, 0.0f,
        -5.0f, -0.5f, 5.0f, 0.0f, 0.0f,
        -5.0f, -0.5f, -5.0f, 0.0f, 2.0f,

        5.0f, -0.5f, 5.0f, 2.0f, 0.0f,
        -5.0f, -0.5f, -5.0f, 0.0f, 2.0f,
        5.0f, -0.5f, -5.0f, 2.0f, 2.0f
    )

    private val camera = Camera()

    private lateinit var program: Program

    private lateinit var singleColorProgram: Program

    private lateinit var cubeVertexData: VertexData

    private lateinit var planeVertexData: VertexData

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)

        val view = camera.getViewMatrix()
        program.use()
        program.setMat4("view", view)

        // Floor
        glStencilMask(0x00)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, metalTexture.getId())
        glBindVertexArray(planeVertexData.getVaoId())
        program.setMat4("model", Mat4())
        glDrawArrays(GL_TRIANGLES, 0, 6)

        // Cube 1
        glStencilFunc(GL_ALWAYS, 1, 0xFF)
        glStencilMask(0xFF)
        glBindTexture(GL_TEXTURE_2D, marbleTexture.getId())
        glBindVertexArray(cubeVertexData.getVaoId())
        var model = translation(Float3(-1.0f, 0.0f, -1.0f))
        program.setMat4("model", model)
        glDrawArrays(GL_TRIANGLES, 0, 36)

        // Cube 2
        model = translation(Float3(2.0f, 0.0f, 0.0f))
        program.setMat4("model", model)
        glDrawArrays(GL_TRIANGLES, 0, 36)

        singleColorProgram.use()
        singleColorProgram.setMat4("view", view)

        // Cube 1 outline
        glStencilFunc(GL_NOTEQUAL, 1, 0xFF)
        glStencilMask(0x00)
        glDisable(GL_DEPTH_TEST)
        val scale = scale(Float3(1.1f))
        model = translation(Float3(-1.0f, 0.0f, -1.0f)) * scale
        singleColorProgram.setMat4("model", model)
        glDrawArrays(GL_TRIANGLES, 0, 36)

        // Cube 2 outline
        model = translation(Float3(2.0f, 0.0f, 0.0f)) * scale
        singleColorProgram.setMat4("model", model)
        glDrawArrays(GL_TRIANGLES, 0, 36)

        glStencilMask(0xFF)
        glStencilFunc(GL_ALWAYS, 0, 0xFF)
        glEnable(GL_DEPTH_TEST)
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_STENCIL_TEST)
        glStencilFunc(GL_NOTEQUAL, 1, 0xff)
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)
        glViewport(0, 0, width, height)

        marbleTexture.load()
        metalTexture.load()

        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)

        program = Program.create(vertexShaderCode, fragmentShaderCode)
        program.use()
        program.setMat4("projection", projection)

        singleColorProgram = Program.create(vertexShaderCode, singleColorfragmentShaderCode)
        singleColorProgram.use()
        singleColorProgram.setMat4("projection", projection)

        cubeVertexData = VertexData(cubeWithTexture, null, 5)
        cubeVertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 3)
        cubeVertexData.bind()

        planeVertexData = VertexData(planeVertices, null, 5)
        planeVertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        planeVertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 3)
        planeVertexData.bind()
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene3StencilTesting(
                vertexShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene3_stencil_testing_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene3_stencil_testing_frag),
                singleColorfragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene3_stencil_testing_single_color_frag),
                marbleTexture = Texture(loadBitmap(context, R.raw.texture_marble)),
                metalTexture = Texture(loadBitmap(context, R.raw.texture_metal))
            )
        }
    }
}
