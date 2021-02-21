package net.pters.learnopengl.android.scenes.lighting

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.perspective
import com.curiouscreature.kotlin.math.scale
import com.curiouscreature.kotlin.math.translation
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.*

class Scene2BasicLighting private constructor(
    private val cubeVertexShaderCode: String,
    private val lightSourceVertexShaderCode: String,
    private val cubeFragmentShaderCode: String,
    private val lightSourceFragmentShaderCode: String
) : Scene() {

    private val vertices = floatArrayOf(
        -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
        0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
        0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
        0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
        -0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,

        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
        0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
        0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
        0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
        -0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,

        -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f,
        -0.5f, 0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
        -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
        -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
        -0.5f, -0.5f, 0.5f, -1.0f, 0.0f, 0.0f,
        -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f,

        0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
        0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
        0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
        0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f,

        -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
        0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
        0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f,
        0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f,
        -0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,

        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
        0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
        0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
        0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
        -0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f
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

        val view = camera.getViewMatrix()

        cubeProgram.use()
        cubeProgram.setFloat3("viewPos", camera.getEye())
        cubeProgram.setMat4("view", view)
        glBindVertexArray(cubeVertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 36)

        lightSourceProgram.use()
        lightSourceProgram.setMat4("view", view)
        glBindVertexArray(lightSourceVertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 36)
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        val lightPos = Float3(1.2f, 1.0f, 2.0f)
        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)

        cubeProgram = Program.create(cubeVertexShaderCode, cubeFragmentShaderCode)
        cubeVertexData = VertexData(vertices, null, 6)
        cubeVertexData.addAttribute(cubeProgram.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.addAttribute(cubeProgram.getAttributeLocation("aNormal"), 3, 3)
        cubeVertexData.bind()

        cubeProgram.use()
        cubeProgram.setFloat3("lightPos", lightPos)
        cubeProgram.set3f("lightColor", 1.0f, 1.0f, 1.0f)
        cubeProgram.set3f("objectColor", 1.0f, 0.5f, 0.31f)
        cubeProgram.setMat4("model", translation(Float3(0.0f)))
        cubeProgram.setMat4("projection", projection)

        lightSourceProgram =
            Program.create(lightSourceVertexShaderCode, lightSourceFragmentShaderCode)
        lightSourceVertexData = VertexData(vertices, null, 6)
        lightSourceVertexData.addAttribute(lightSourceProgram.getAttributeLocation("aPos"), 3, 0)
        lightSourceVertexData.bind()

        lightSourceProgram.use()
        val lightSourceModel = translation(lightPos) * scale(Float3(0.2f))
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
            return Scene2BasicLighting(
                cubeVertexShaderCode = resources.readRawTextFile(R.raw.lighting_scene2_basic_lighting_cube_vert),
                lightSourceVertexShaderCode = resources.readRawTextFile(R.raw.lighting_scene1_colors_vert),
                cubeFragmentShaderCode = resources.readRawTextFile(R.raw.lighting_scene2_basic_lighting_cube_frag),
                lightSourceFragmentShaderCode = resources.readRawTextFile(R.raw.lighting_scene1_colors_light_source_frag)
            )
        }
    }
}
