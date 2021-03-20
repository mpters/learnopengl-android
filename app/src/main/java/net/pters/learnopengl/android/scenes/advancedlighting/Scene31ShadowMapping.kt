package net.pters.learnopengl.android.scenes.advancedlighting

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.scenes.Vertices.ndcCubeWithNormalsAndTexture
import net.pters.learnopengl.android.tools.*
import kotlin.math.cos
import kotlin.math.sin

class Scene31ShadowMapping private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val depthVertexShaderCode: String,
    private val depthFragmentShaderCode: String,
    private val floorTexture: Texture
) : Scene() {

    private val planeVertices = floatArrayOf(
        // Positions, normals, texture coordinates
        25.0f, -0.5f, 25.0f, 0.0f, 1.0f, 0.0f, 25.0f, 0.0f,
        -25.0f, -0.5f, 25.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
        -25.0f, -0.5f, -25.0f, 0.0f, 1.0f, 0.0f, 0.0f, 25.0f,

        25.0f, -0.5f, 25.0f, 0.0f, 1.0f, 0.0f, 25.0f, 0.0f,
        -25.0f, -0.5f, -25.0f, 0.0f, 1.0f, 0.0f, 0.0f, 25.0f,
        25.0f, -0.5f, -25.0f, 0.0f, 1.0f, 0.0f, 25.0f, 25.0f
    )

    private val camera = Camera()

    private val lightPosition = Float3(0.0f, 4.0f, 0.0f)

    private var width: Int = -1

    private var height: Int = -1

    private lateinit var depthMapBuffer: DepthMapBuffer

    private lateinit var depthProgram: Program

    private lateinit var program: Program

    private lateinit var cubeVertexData: VertexData

    private lateinit var floorVertexData: VertexData

    override fun draw() {
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        lightPosition.x = sin(timer.sinceStartSecs()) * 3.0f
        lightPosition.z = cos(timer.sinceStartSecs()) * 2.0f
        val lightProjection = ortho(-10.0f, 10.0f, -10.0f, 10.0f, 1f, 7.5f)
        val lightView = lookAtM(lightPosition)
        val lightSpaceMatrix = lightProjection * lightView

        // 1. Render depth of scene to texture (from light's perspective)
        depthProgram.use()
        depthProgram.setMat4("lightSpaceMatrix", lightSpaceMatrix)
        depthMapBuffer.bind()
        renderScene(depthProgram)

        // Switch back to default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // Reset viewport
        glViewport(0, 0, width, height)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // 2. Render scene as normal using the generated depth map
        program.use()
        program.setFloat3("lightPos", lightPosition)
        program.setMat4("lightSpaceMatrix", lightSpaceMatrix)
        program.setMat4("view", camera.getViewMatrix())
        program.setFloat3("viewPos", camera.getEye())

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, floorTexture.getId())
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, depthMapBuffer.getTextureId())
        renderScene(program)
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        this.width = width
        this.height = height

        glEnable(GL_DEPTH_TEST)

        floorTexture.load()

        depthProgram = Program.create(depthVertexShaderCode, depthFragmentShaderCode)
        program = Program.create(vertexShaderCode, fragmentShaderCode)

        cubeVertexData = VertexData(ndcCubeWithNormalsAndTexture, null, 8)
        cubeVertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.addAttribute(program.getAttributeLocation("aNormal"), 3, 3)
        cubeVertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 6)
        cubeVertexData.bind()

        floorVertexData = VertexData(planeVertices, null, 8)
        floorVertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        floorVertexData.addAttribute(program.getAttributeLocation("aNormal"), 3, 3)
        floorVertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 6)
        floorVertexData.bind()

        depthMapBuffer = DepthMapBuffer.create(2048)

        program.use()
        program.setInt("diffuseTexture", 0)
        program.setInt("shadowMap", 1)
        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)
        program.setMat4("projection", projection)
    }

    private fun renderScene(program: Program) {
        glBindVertexArray(floorVertexData.getVaoId())
        var model = Mat4()
        program.setMat4("model", model)
        glDrawArrays(GL_TRIANGLES, 0, 6)

        glBindVertexArray(cubeVertexData.getVaoId())
        model = scale(Float3(0.5f)) * translation(Float3(0.0f, 1.5f, 0.0f))
        program.setMat4("model", model)
        glDrawArrays(GL_TRIANGLES, 0, 36)

        model = translation(Float3(2.0f, 0.0f, 1.0f))
        program.setMat4("model", model)
        glDrawArrays(GL_TRIANGLES, 0, 36)

        model = rotation(normalize(Float3(1.0f, 0.0f, 1.0f)), 60.0f) *
                scale(Float3(0.25f)) * translation(Float3(-1.0f, 0.0f, 2.0f))
        program.setMat4("model", model)
        glDrawArrays(GL_TRIANGLES, 0, 36)
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene31ShadowMapping(
                vertexShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene31_shadow_mapping_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene31_shadow_mapping_frag),
                depthVertexShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene31_shadow_mapping_depth_vert),
                depthFragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene31_shadow_mapping_depth_frag),
                floorTexture = Texture(loadBitmap(context, R.raw.texture_wood))
            )
        }
    }
}
