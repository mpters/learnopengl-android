package net.pters.learnopengl.android.scenes.advancedlighting

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.perspective
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.*

class Scene2GammaCorrection private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val floorTexture: Texture,
    private val floorTextureGammaCorrected: Texture
) : Scene() {

    private val gammaCorrectionEnabled = true

    private val lightColors = floatArrayOf(
        0.25f, 0.25f, 0.25f,
        0.5f, 0.5f, 0.5f,
        0.75f, 0.75f, 0.75f,
        1.0f, 1.0f, 1.0f
    )

    private val lightPositions = floatArrayOf(
        -3.0f, 0.0f, 0.0f,
        -1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        3.0f, 0.0f, 0.0f
    )

    private val planeVertices = floatArrayOf(
        // Positions, normals, texture coordinates
        10.0f, -0.5f, 10.0f, 0.0f, 1.0f, 0.0f, 10.0f, 0.0f,
        -10.0f, -0.5f, 10.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
        -10.0f, -0.5f, -10.0f, 0.0f, 1.0f, 0.0f, 0.0f, 10.0f,

        10.0f, -0.5f, 10.0f, 0.0f, 1.0f, 0.0f, 10.0f, 0.0f,
        -10.0f, -0.5f, -10.0f, 0.0f, 1.0f, 0.0f, 0.0f, 10.0f,
        10.0f, -0.5f, -10.0f, 0.0f, 1.0f, 0.0f, 10.0f, 10.0f
    )

    private val camera = Camera(
        eye = Float3(0.0f, 5.0f, 5.0f),
        pitch = -45.0f
    )

    private lateinit var program: Program

    private lateinit var vertexData: VertexData

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        program.setMat4("view", camera.getViewMatrix())
        program.setFloat3("viewPos", camera.getEye())

        if (gammaCorrectionEnabled) {
            glBindTexture(GL_TEXTURE_2D, floorTextureGammaCorrected.getId())
        } else {
            glBindTexture(GL_TEXTURE_2D, floorTexture.getId())
        }

        glBindVertexArray(vertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        floorTexture.load()
        floorTextureGammaCorrected.load()

        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)

        program = Program.create(vertexShaderCode, fragmentShaderCode)
        program.use()
        program.setBoolean("gamma", gammaCorrectionEnabled)
        program.setMat4("projection", projection)
        glUniform3fv(program.getUniformLocation("lightColors[0]"), 4, lightColors, 0)
        glUniform3fv(program.getUniformLocation("lightPositions[0]"), 4, lightPositions, 0)

        vertexData = VertexData(planeVertices, null, 8)
        vertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        vertexData.addAttribute(program.getAttributeLocation("aNormal"), 3, 3)
        vertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 6)
        vertexData.bind()
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene2GammaCorrection(
                vertexShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene1_blinn_phong_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene2_gamme_correction_frag),
                floorTexture = Texture(loadBitmap(context, R.raw.texture_wood)),
                floorTextureGammaCorrected = Texture(
                    loadBitmap(context, R.raw.texture_wood),
                    GL_SRGB8_ALPHA8
                )
            )
        }
    }
}
