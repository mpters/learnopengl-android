package net.pters.learnopengl.android.scenes.advancedlighting

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.perspective
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.*

class Scene1BlinnPhong private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val woodTexture: Texture
) : Scene() {

    private val planeVertices = floatArrayOf(
        // Positions, normals, texture coordinates
        10.0f, -0.5f, 10.0f, 0.0f, 1.0f, 0.0f, 10.0f, 0.0f,
        -10.0f, -0.5f, 10.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
        -10.0f, -0.5f, -10.0f, 0.0f, 1.0f, 0.0f, 0.0f, 10.0f,

        10.0f, -0.5f, 10.0f, 0.0f, 1.0f, 0.0f, 10.0f, 0.0f,
        -10.0f, -0.5f, -10.0f, 0.0f, 1.0f, 0.0f, 0.0f, 10.0f,
        10.0f, -0.5f, -10.0f, 0.0f, 1.0f, 0.0f, 10.0f, 10.0f
    )

    private val camera = Camera()

    private lateinit var program: Program

    private lateinit var vertexData: VertexData

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        program.setInt("blinn", 1)
        program.setFloat3("lightPos", Float3())
        program.setMat4("view", camera.getViewMatrix())
        program.setFloat3("viewPos", camera.getEye())

        glBindVertexArray(vertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        woodTexture.load()

        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)

        program = Program.create(vertexShaderCode, fragmentShaderCode)
        program.use()
        program.setMat4("projection", projection)

        vertexData = VertexData(planeVertices, null, 8)
        vertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        vertexData.addAttribute(program.getAttributeLocation("aNormal"), 3, 3)
        vertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 6)
        vertexData.bind()
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene1BlinnPhong(
                vertexShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene1_blinn_phong_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene1_blinn_phong_frag),
                woodTexture = Texture(loadBitmap(context, R.raw.texture_wood))
            )
        }
    }
}
