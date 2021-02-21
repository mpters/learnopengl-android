package net.pters.learnopengl.android.scenes.gettingstarted

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.*

class Scene8CoordinateSystems private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val texture: Texture
) : Scene() {

    private val vertices = floatArrayOf(
        -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
        0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,

        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
        0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
        -0.5f, 0.5f, 0.5f, 0.0f, 1.0f,
        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,

        -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
        -0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
        -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

        0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
        0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
        0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
        0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
        0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
        0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
        0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
        -0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f
    )

    private val cubePositions = listOf(
        Float3(0.0f, 0.0f, 0.0f),
        Float3(2.0f, 5.0f, 15.0f),
        Float3(-1.5f, -2.2f, 2.5f),
        Float3(-3.8f, -2.0f, 2.3f),
        Float3(2.4f, -0.4f, 3.5f),
        Float3(-1.7f, 3.0f, 7.5f),
        Float3(1.3f, -2.0f, 2.5f),
        Float3(1.5f, 2.0f, 2.5f),
        Float3(1.5f, 0.2f, 1.5f),
        Float3(-1.3f, 1.0f, 1.5f)
    )

    private lateinit var program: Program

    private lateinit var vertexData: VertexData

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        glBindVertexArray(vertexData.getVaoId())

        cubePositions.forEachIndexed { index, position ->
            val angle = 20.0f * index + timer.sinceStartSecs() * 5.0f
            val model = translation(position) * rotation(normalize(Float3(1.0f, 0.3f, 0.5f)), angle)
            glUniformMatrix4fv(
                program.getUniformLocation("model"),
                1,
                true,
                model.toFloatArray(),
                0
            )

            glDrawArrays(GL_TRIANGLES, 0, 36)
        }
    }

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        texture.load()

        program = Program.create(vertexShaderCode, fragmentShaderCode)

        vertexData = VertexData(vertices, null, 5)
        vertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        vertexData.addAttribute(program.getAttributeLocation("aTexCoord"), 2, 3)
        vertexData.bind()

        program.use()

        val view = translation(Float3(0.0f, 0.0f, 5.0f))
        glUniformMatrix4fv(
            program.getUniformLocation("view"),
            1,
            true,
            view.toFloatArray(),
            0
        )

        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)
        glUniformMatrix4fv(
            program.getUniformLocation("projection"),
            1,
            true,
            projection.toFloatArray(),
            0
        )
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene8CoordinateSystems(
                vertexShaderCode = resources.readRawTextFile(R.raw.gettingstarted_scene8_coordinate_systems_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.gettingstarted_scene8_coordinate_systems_frag),
                Texture(loadBitmap(context, R.raw.texture_container).flipVertically())
            )
        }
    }
}
