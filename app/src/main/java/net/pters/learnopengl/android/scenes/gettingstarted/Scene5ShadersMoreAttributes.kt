package net.pters.learnopengl.android.scenes.gettingstarted

import android.content.Context
import android.opengl.GLES30.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.Program
import net.pters.learnopengl.android.tools.Scene
import net.pters.learnopengl.android.tools.VertexData
import net.pters.learnopengl.android.tools.readRawTextFile

class Scene5ShadersMoreAttributes private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String
) : Scene() {

    private val vertices = floatArrayOf(
        0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f,
        -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f
    )

    private lateinit var program: Program

    private lateinit var vertexData: VertexData

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        glBindVertexArray(vertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 3)
    }

    override fun init(width: Int, height: Int) {
        glViewport(0, 0, width, height)

        program = Program.create(vertexShaderCode, fragmentShaderCode)

        vertexData = VertexData(vertices, null, 6)
        vertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        vertexData.addAttribute(program.getAttributeLocation("aColor"), 3, 3)
        vertexData.bind()

        program.use()
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene5ShadersMoreAttributes(
                vertexShaderCode = resources.readRawTextFile(R.raw.gettingstarted_scene5_shaders_more_attributes_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.gettingstarted_scene5_shaders_more_attributes_frag)
            )
        }
    }
}
