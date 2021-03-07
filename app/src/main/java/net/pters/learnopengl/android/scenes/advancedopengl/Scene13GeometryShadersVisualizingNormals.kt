package net.pters.learnopengl.android.scenes.advancedopengl

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.perspective
import com.curiouscreature.kotlin.math.translation
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.Camera
import net.pters.learnopengl.android.tools.Program
import net.pters.learnopengl.android.tools.Scene
import net.pters.learnopengl.android.tools.model.Model
import net.pters.learnopengl.android.tools.model.ObjLoader
import net.pters.learnopengl.android.tools.model.ProgramLocations
import net.pters.learnopengl.android.tools.readRawTextFile

class Scene13GeometryShadersVisualizingNormals private constructor(
    private val modelVertexShaderCode: String,
    private val modelFragmentShaderCode: String,
    private val normalVertexShaderCode: String,
    private val normalFragmentShaderCode: String,
    private val normalGeoShaderCode: String,
    private val backpackModel: Model
) : Scene() {

    private val camera = Camera(
        eye = Float3(-2.0f, -3.0f, 8.0f),
        pitch = 20.0f,
        yaw = -78.0f
    )

    private lateinit var modelProgram: Program

    private lateinit var normalProgram: Program

    override fun draw() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        val view = camera.getViewMatrix()
        modelProgram.use()
        modelProgram.setMat4("view", view)
        backpackModel.draw()

        normalProgram.use()
        normalProgram.setMat4("view", view)
        backpackModel.draw()
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        val model = translation(Float3(0.0f))
        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)

        modelProgram = Program.create(modelVertexShaderCode, modelFragmentShaderCode)
        modelProgram.use()
        modelProgram.setMat4("model", model)
        modelProgram.setMat4("projection", projection)

        normalProgram =
            Program.create(normalVertexShaderCode, normalFragmentShaderCode, normalGeoShaderCode)
        normalProgram.use()
        normalProgram.setMat4("model", model)
        normalProgram.setMat4("projection", projection)

        backpackModel.bind(
            modelProgram, ProgramLocations(
                attribPosition = modelProgram.getAttributeLocation("aPos"),
                attribNormal = modelProgram.getAttributeLocation("aNormal"),
                attribTexCoords = modelProgram.getAttributeLocation("aTexCoords"),
                uniformDiffuseTexture = modelProgram.getUniformLocation("texture_diffuse1")
            )
        )
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene13GeometryShadersVisualizingNormals(
                modelVertexShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene13_geometry_shaders_visualizing_normals_model_vert),
                modelFragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene13_geometry_shaders_visualizing_normals_model_frag),
                normalVertexShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene13_geometry_shaders_visualizing_normals_display_vert),
                normalFragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene13_geometry_shaders_visualizing_normals_display_frag),
                normalGeoShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene13_geometry_shaders_visualizing_normals_display_geo),
                ObjLoader.fromAssets(
                    context,
                    directory = "backpack",
                    objFileName = "backpack.obj",
                    mtlFileName = "backpack.mtl"
                )
            )
        }
    }
}
