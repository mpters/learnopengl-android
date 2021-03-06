package net.pters.learnopengl.android.scenes.advancedopengl

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.Mat4
import com.curiouscreature.kotlin.math.perspective
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.scenes.Vertices
import net.pters.learnopengl.android.scenes.Vertices.cubeWithNormals
import net.pters.learnopengl.android.tools.*

class Scene9CubemapsEnvironmentMapping private constructor(
    private val cubeVertexShaderCode: String,
    private val cubeFragmentShaderCode: String,
    private val skyboxVertexShaderCode: String,
    private val skyboxFragmentShaderCode: String,
    private val skybox: Cubemap
) : Scene() {

    private val camera = Camera()

    private lateinit var cubeProgram: Program

    private lateinit var cubeVertexData: VertexData

    private lateinit var skyboxProgram: Program

    private lateinit var skyboxVertexData: VertexData

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        val view = camera.getViewMatrix()

        cubeProgram.use()
        cubeProgram.setMat4("view", view)
        cubeProgram.setFloat3("cameraPos", camera.getEye())
        glBindVertexArray(cubeVertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 36)

        skyboxProgram.use()
        skyboxProgram.setMat4("view", view.withoutTranslation())
        glBindVertexArray(skyboxVertexData.getVaoId())
        glDepthFunc(GL_LEQUAL)
        glDrawArrays(GL_TRIANGLES, 0, 36)
        glDepthFunc(GL_LESS)
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        skybox.load()

        cubeProgram = Program.create(cubeVertexShaderCode, cubeFragmentShaderCode)
        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)
        cubeProgram.use()
        cubeProgram.setMat4("model", Mat4())
        cubeProgram.setMat4("projection", projection)

        cubeVertexData = VertexData(cubeWithNormals, null, 6)
        cubeVertexData.addAttribute(cubeProgram.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.addAttribute(cubeProgram.getAttributeLocation("aNormal"), 3, 3)
        cubeVertexData.bind()

        skyboxProgram = Program.create(skyboxVertexShaderCode, skyboxFragmentShaderCode)
        skyboxProgram.use()
        skyboxProgram.setMat4("projection", projection)

        skyboxVertexData = VertexData(Vertices.skybox, null, 3)
        skyboxVertexData.addAttribute(cubeProgram.getAttributeLocation("aPos"), 3, 0)
        skyboxVertexData.bind()
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene9CubemapsEnvironmentMapping(
                cubeVertexShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene9_environment_mapping_cube_vert),
                cubeFragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene9_environment_mapping_cube_frag),
                skyboxVertexShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene8_cubemaps_skybox_vert),
                skyboxFragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene8_cubemaps_skybox_frag),
                Cubemap(
                    right = loadBitmap(context, R.raw.texture_skybox_right),
                    left = loadBitmap(context, R.raw.texture_skybox_left),
                    top = loadBitmap(context, R.raw.texture_skybox_top),
                    bottom = loadBitmap(context, R.raw.texture_skybox_bottom),
                    front = loadBitmap(context, R.raw.texture_skybox_front),
                    back = loadBitmap(context, R.raw.texture_skybox_back)
                )
            )
        }
    }
}
