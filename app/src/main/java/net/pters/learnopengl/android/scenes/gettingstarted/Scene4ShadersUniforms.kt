package net.pters.learnopengl.android.scenes.gettingstarted

import android.content.Context
import android.opengl.GLES30.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.Program
import net.pters.learnopengl.android.tools.Scene
import net.pters.learnopengl.android.tools.VertexData
import net.pters.learnopengl.android.tools.readRawTextFile
import kotlin.math.sin

class Scene4ShadersUniforms private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String
) : Scene() {

    private val vertices = floatArrayOf(
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,
        0.0f, 0.5f, 0.0f
    )

    private lateinit var program: Program

    private lateinit var vertexData: VertexData

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        val greenValue = sin(timer.sinceStartSecs()) / 2f + 0.5f
        glUniform4f(program.getUniformLocation("ourColor"), 0.0f, greenValue, 0.0f, 1.0f)

        glBindVertexArray(vertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 3)
    }

    override fun init(width: Int, height: Int) {
        glViewport(0, 0, width, height)

        program = Program.create(vertexShaderCode, fragmentShaderCode)

        vertexData = VertexData(vertices, null, 3)
        vertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        vertexData.bind()

        program.use()
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene4ShadersUniforms(
                vertexShaderCode = resources.readRawTextFile(R.raw.gettingstarted_scene4_shaders_uniforms_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.gettingstarted_scene4_shaders_uniforms_frag)
            )
        }
    }
}
