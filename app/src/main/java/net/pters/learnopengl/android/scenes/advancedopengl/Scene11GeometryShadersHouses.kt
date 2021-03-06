package net.pters.learnopengl.android.scenes.advancedopengl

import android.content.Context
import android.opengl.GLES30.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.Program
import net.pters.learnopengl.android.tools.Scene
import net.pters.learnopengl.android.tools.VertexData
import net.pters.learnopengl.android.tools.readRawTextFile

class Scene11GeometryShadersHouses private constructor(
    private val vertexShaderCode: String,
    private val geometryShaderCode: String,
    private val fragmentShaderCode: String,
) : Scene() {

    private val pointVertices = floatArrayOf(
        -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, // top-left
        0.5f, 0.5f, 0.0f, 1.0f, 0.0f, // top-right
        0.5f, -0.5f, 0.0f, 0.0f, 1.0f, // bottom-right
        -0.5f, -0.5f, 1.0f, 1.0f, 0.0f  // bottom-left
    )

    private lateinit var program: Program

    private lateinit var vertexData: VertexData

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        glBindVertexArray(vertexData.getVaoId())
        glDrawArrays(GL_POINTS, 0, 4)
    }

    override fun init(width: Int, height: Int) {
        glViewport(0, 0, width, height)

        program = Program.create(vertexShaderCode, fragmentShaderCode, geometryShaderCode)
        program.use()

        vertexData = VertexData(pointVertices, null, 5)
        vertexData.addAttribute(program.getAttributeLocation("aPos"), 2, 0)
        vertexData.addAttribute(program.getAttributeLocation("aColor"), 3, 2)
        vertexData.bind()
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene11GeometryShadersHouses(
                vertexShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene11_geometry_shaders_houses_vert),
                geometryShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene11_geometry_shaders_houses_geo),
                fragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene11_geometry_shaders_houses_frag)
            )
        }
    }
}
