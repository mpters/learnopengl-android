package net.pters.learnopengl.android.scenes.advancedopengl

import android.content.Context
import android.opengl.GLES30.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.Program
import net.pters.learnopengl.android.tools.Scene
import net.pters.learnopengl.android.tools.VertexData
import net.pters.learnopengl.android.tools.readRawTextFile

class Scene141InstancingQuads private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
) : Scene() {

    private val quadVertices = floatArrayOf(
        -0.05f, 0.05f, 1.0f, 0.0f, 0.0f,
        0.05f, -0.05f, 0.0f, 1.0f, 0.0f,
        -0.05f, -0.05f, 0.0f, 0.0f, 1.0f,

        -0.05f, 0.05f, 1.0f, 0.0f, 0.0f,
        0.05f, -0.05f, 0.0f, 1.0f, 0.0f,
        0.05f, 0.05f, 0.0f, 1.0f, 1.0f
    )

    private val translations = mutableListOf<Float>().apply {
        val offset = 0.1f
        for (y in -10 until 10 step 2) {
            for (x in -10 until 10 step 2) {
                add(x / 10.0f + offset)
                add(y / 10.0f + offset)
            }
        }
    }.toFloatArray()

    private lateinit var program: Program

    private lateinit var vertexData: VertexData

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        glBindVertexArray(vertexData.getVaoId())
        glDrawArraysInstanced(GL_TRIANGLES, 0, 6, 100)
    }

    override fun init(width: Int, height: Int) {
        glViewport(0, 0, width, height)

        program = Program.create(vertexShaderCode, fragmentShaderCode)
        program.use()

        vertexData = VertexData(quadVertices + translations, null, 5)
        vertexData.addAttribute(program.getAttributeLocation("aPos"), 2, 0)
        vertexData.addAttribute(program.getAttributeLocation("aColor"), 3, 3)
        vertexData.addAttribute(
            VertexData.Attribute(
                location = program.getAttributeLocation("aOffset"),
                size = 2,
                offset = quadVertices.size,
                stride = 2,
                divisor = 1
            )
        )
        vertexData.bind()
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene141InstancingQuads(
                vertexShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene141_instancing_quads_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene141_instancing_quads_frag)
            )
        }
    }
}
