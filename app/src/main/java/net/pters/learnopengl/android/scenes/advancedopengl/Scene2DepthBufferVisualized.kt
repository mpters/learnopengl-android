package net.pters.learnopengl.android.scenes.advancedopengl

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.Mat4
import com.curiouscreature.kotlin.math.perspective
import com.curiouscreature.kotlin.math.translation
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.scenes.Vertices.cubeWithTexture
import net.pters.learnopengl.android.tools.*

class Scene2DepthBufferVisualized private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String
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

    private lateinit var cubeVertexData: VertexData

    private lateinit var planeVertexData: VertexData

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        program.setMat4("view", camera.getViewMatrix())

        // Cube 1
        glBindVertexArray(cubeVertexData.getVaoId())
        var model = translation(Float3(-1.0f, 0.0f, -1.0f))
        program.setMat4("model", model)
        glDrawArrays(GL_TRIANGLES, 0, 36)

        // Cube 2
        model = translation(Float3(2.0f, 0.0f, 0.0f))
        program.setMat4("model", model)
        glDrawArrays(GL_TRIANGLES, 0, 36)

        // Floor
        glBindVertexArray(planeVertexData.getVaoId())
        program.setMat4("model", Mat4())
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        program = Program.create(vertexShaderCode, fragmentShaderCode)
        program.use()

        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)
        program.setMat4("projection", projection)

        cubeVertexData = VertexData(cubeWithTexture, null, 5)
        cubeVertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.bind()

        planeVertexData = VertexData(planeVertices, null, 5)
        planeVertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        planeVertexData.bind()
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene2DepthBufferVisualized(
                vertexShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene2_depth_buffer_visualized_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene2_depth_buffer_visualized_frag)
            )
        }
    }
}
