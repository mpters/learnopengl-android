package net.pters.learnopengl.android.scenes.advancedopengl

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.Mat4
import com.curiouscreature.kotlin.math.perspective
import com.curiouscreature.kotlin.math.translation
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.scenes.Vertices
import net.pters.learnopengl.android.tools.*

class Scene10UniformBufferObjects private constructor(
    private val vertexShaderCode: String,
    private val blueFragmentShaderCode: String,
    private val greenFragmentShaderCode: String,
    private val redFragmentShaderCode: String,
    private val yellowFragmentShaderCode: String
) : Scene() {

    private val camera = Camera()

    private lateinit var blueProgram: Program

    private lateinit var greenProgram: Program

    private lateinit var redProgram: Program

    private lateinit var yellowProgram: Program

    private lateinit var vertexData: VertexData

    private var uboMatricesId = -1

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        val view = camera.getViewMatrix()
        glBindBuffer(GL_UNIFORM_BUFFER, uboMatricesId)
        glBufferSubData(GL_UNIFORM_BUFFER, Mat4.sizeBytes(), Mat4.sizeBytes(), view.toFloatBuffer())
        glBindBuffer(GL_UNIFORM_BUFFER, 0)

        glBindVertexArray(vertexData.getVaoId())

        blueProgram.use()
        blueProgram.setMat4("model", translation(Float3(0.75f, -0.75f, 0.0f)))
        glDrawArrays(GL_TRIANGLES, 0, 36)

        greenProgram.use()
        greenProgram.setMat4("model", translation(Float3(0.75f, 0.75f, 0.0f)))
        glDrawArrays(GL_TRIANGLES, 0, 36)

        redProgram.use()
        redProgram.setMat4("model", translation(Float3(-0.75f, 0.75f, 0.0f)))
        glDrawArrays(GL_TRIANGLES, 0, 36)

        yellowProgram.use()
        yellowProgram.setMat4("model", translation(Float3(-0.75f, -0.75f, 0.0f)))
        glDrawArrays(GL_TRIANGLES, 0, 36)
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        blueProgram = Program.create(vertexShaderCode, blueFragmentShaderCode)
        greenProgram = Program.create(vertexShaderCode, greenFragmentShaderCode)
        redProgram = Program.create(vertexShaderCode, redFragmentShaderCode)
        yellowProgram = Program.create(vertexShaderCode, yellowFragmentShaderCode)

        vertexData = VertexData(Vertices.cube, null, 3)
        vertexData.addAttribute(blueProgram.getAttributeLocation("aPos"), 3, 0)
        vertexData.bind()

        // Get the relevant block indices
        val uniformBlockIndexBlue = glGetUniformBlockIndex(blueProgram.getId(), "Matrices")
        val uniformBlockIndexGreen = glGetUniformBlockIndex(greenProgram.getId(), "Matrices")
        val uniformBlockIndexRed = glGetUniformBlockIndex(redProgram.getId(), "Matrices")
        val uniformBlockIndexYellow = glGetUniformBlockIndex(yellowProgram.getId(), "Matrices")

        // Link each shaders uniform block to uniform binding point 0
        glUniformBlockBinding(blueProgram.getId(), uniformBlockIndexRed, 0)
        glUniformBlockBinding(greenProgram.getId(), uniformBlockIndexGreen, 0)
        glUniformBlockBinding(redProgram.getId(), uniformBlockIndexBlue, 0)
        glUniformBlockBinding(yellowProgram.getId(), uniformBlockIndexYellow, 0)

        // Now actually create the buffer
        uboMatricesId = genId { glGenBuffers(1, it) }
        glBindBuffer(GL_UNIFORM_BUFFER, uboMatricesId)
        glBufferData(GL_UNIFORM_BUFFER, 2 * Mat4.sizeBytes(), null, GL_STATIC_DRAW)
        glBindBuffer(GL_UNIFORM_BUFFER, 0)

        // Define the range of the buffer that links to the uniform binding point
        glBindBufferRange(GL_UNIFORM_BUFFER, 0, uboMatricesId, 0, 2 * Mat4.sizeBytes())

        // Store the projection matrix (we only do this once now)
        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)
        glBindBuffer(GL_UNIFORM_BUFFER, uboMatricesId)
        glBufferSubData(GL_UNIFORM_BUFFER, 0, Mat4.sizeBytes(), projection.toFloatBuffer())
        glBindBuffer(GL_UNIFORM_BUFFER, 0)
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene10UniformBufferObjects(
                vertexShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene10_uniform_buffer_objects_vert),
                blueFragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene10_uniform_buffer_objects_blue_frag),
                greenFragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene10_uniform_buffer_objects_green_frag),
                redFragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene10_uniform_buffer_objects_red_frag),
                yellowFragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene10_uniform_buffer_objects_yellow_frag)
            )
        }
    }
}
