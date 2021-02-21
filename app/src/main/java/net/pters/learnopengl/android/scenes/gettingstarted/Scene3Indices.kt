package net.pters.learnopengl.android.scenes.gettingstarted

import android.content.Context
import android.opengl.GLES30.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.*
import java.nio.IntBuffer

class Scene3Indices private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String
) : Scene() {

    private val vertices = floatArrayOf(
        0.5f, 0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        -0.5f, 0.5f, 0.0f
    )

    private val indices = shortArrayOf(
        0, 1, 3,
        1, 2, 3
    )

    private lateinit var program: Program

    private var vaoId: Int = -1

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        glBindVertexArray(vaoId)
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0)
    }

    override fun init(width: Int, height: Int) {
        glViewport(0, 0, width, height)

        program = Program.create(vertexShaderCode, fragmentShaderCode)

        val vbo = IntBuffer.allocate(1)
        glGenBuffers(1, vbo)
        val ebo = IntBuffer.allocate(1)
        glGenBuffers(1, ebo)

        val vao = IntBuffer.allocate(1)
        glGenVertexArrays(1, vao)
        vaoId = vao[0]
        glBindVertexArray(vaoId)

        val vertexBuffer = vertices.toFloatBuffer()
        val indexBuffer = indices.toShortBuffer()
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
        glBufferData(
            GL_ARRAY_BUFFER,
            Float.SIZE_BYTES * vertexBuffer.capacity(),
            vertexBuffer,
            GL_STATIC_DRAW
        )
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo[0])
        glBufferData(
            GL_ELEMENT_ARRAY_BUFFER,
            Short.SIZE_BYTES * indexBuffer.capacity(),
            indexBuffer,
            GL_STATIC_DRAW
        )

        val aPosLocation = program.getAttributeLocation("aPos")
        glVertexAttribPointer(aPosLocation, 3, GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0)
        glEnableVertexAttribArray(aPosLocation)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        program.use()
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene3Indices(
                vertexShaderCode = resources.readRawTextFile(R.raw.gettingstarted_scene2_hellotriangle_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.gettingstarted_scene2_hellotriangle_frag)
            )
        }
    }
}
