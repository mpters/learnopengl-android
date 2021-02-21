package net.pters.learnopengl.android.scenes.lighting

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.perspective
import com.curiouscreature.kotlin.math.scale
import com.curiouscreature.kotlin.math.translation
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.*

class Scene1Colors private constructor(
    private val vertexShaderCode: String,
    private val cubeFragmentShaderCode: String,
    private val lightSourceFragmentShaderCode: String
) : Scene() {

    private val vertices = floatArrayOf(
        -0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f, 0.5f, -0.5f,
        0.5f, 0.5f, -0.5f,
        -0.5f, 0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,

        -0.5f, -0.5f, 0.5f,
        0.5f, -0.5f, 0.5f,
        0.5f, 0.5f, 0.5f,
        0.5f, 0.5f, 0.5f,
        -0.5f, 0.5f, 0.5f,
        -0.5f, -0.5f, 0.5f,

        -0.5f, 0.5f, 0.5f,
        -0.5f, 0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,
        -0.5f, -0.5f, 0.5f,
        -0.5f, 0.5f, 0.5f,

        0.5f, 0.5f, 0.5f,
        0.5f, 0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, 0.5f,
        0.5f, 0.5f, 0.5f,

        -0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, 0.5f,
        0.5f, -0.5f, 0.5f,
        -0.5f, -0.5f, 0.5f,
        -0.5f, -0.5f, -0.5f,

        -0.5f, 0.5f, -0.5f,
        0.5f, 0.5f, -0.5f,
        0.5f, 0.5f, 0.5f,
        0.5f, 0.5f, 0.5f,
        -0.5f, 0.5f, 0.5f,
        -0.5f, 0.5f, -0.5f
    )

    private val camera = Camera(
        eye = Float3(1.0f, 2.0f, 5.0f),
        pitch = -20.0f,
        yaw = -95.0f
    )

    private lateinit var cubeProgram: Program

    private lateinit var cubeVertexData: VertexData

    private lateinit var lightSourceProgram: Program

    private lateinit var lightSourceVertexData: VertexData

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        val view = camera.getViewMatrix().toFloatArray()

        cubeProgram.use()
        glUniformMatrix4fv(
            cubeProgram.getUniformLocation("view"),
            1,
            true,
            view,
            0
        )
        glBindVertexArray(cubeVertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 36)

        lightSourceProgram.use()
        glUniformMatrix4fv(
            lightSourceProgram.getUniformLocation("view"),
            1,
            true,
            view,
            0
        )
        glBindVertexArray(lightSourceVertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 36)
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)

        cubeProgram = Program.create(vertexShaderCode, cubeFragmentShaderCode)
        cubeVertexData = VertexData(vertices, null, 3)
        cubeVertexData.addAttribute(cubeProgram.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.bind()

        cubeProgram.use()
        glUniform3f(cubeProgram.getUniformLocation("objectColor"), 1.0f, 0.5f, 0.31f)
        glUniform3f(cubeProgram.getUniformLocation("lightColor"), 1.0f, 1.0f, 1.0f)
        glUniformMatrix4fv(
            cubeProgram.getUniformLocation("model"),
            1,
            true,
            translation(Float3(0.0f)).toFloatArray(),
            0
        )
        glUniformMatrix4fv(
            cubeProgram.getUniformLocation("projection"),
            1,
            true,
            projection.toFloatArray(),
            0
        )

        lightSourceProgram = Program.create(vertexShaderCode, lightSourceFragmentShaderCode)
        lightSourceVertexData = VertexData(vertices, null, 3)
        lightSourceVertexData.addAttribute(lightSourceProgram.getAttributeLocation("aPos"), 3, 0)
        lightSourceVertexData.bind()

        lightSourceProgram.use()
        val lightSourceModel = translation(Float3(1.2f, 1.0f, 2.0f)) * scale(Float3(0.2f))
        glUniformMatrix4fv(
            lightSourceProgram.getUniformLocation("model"),
            1,
            true,
            lightSourceModel.toFloatArray(),
            0
        )
        glUniformMatrix4fv(
            lightSourceProgram.getUniformLocation("projection"),
            1,
            true,
            projection.toFloatArray(),
            0
        )
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene1Colors(
                vertexShaderCode = resources.readRawTextFile(R.raw.lighting_scene1_colors_vert),
                cubeFragmentShaderCode = resources.readRawTextFile(R.raw.lighting_scene1_colors_cube_frag),
                lightSourceFragmentShaderCode = resources.readRawTextFile(R.raw.lighting_scene1_colors_light_source_frag)
            )
        }
    }
}
