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

class Scene12GeometryShadersExplodingObjects private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val geometryShaderCode: String,
    private val backpackModel: Model
) : Scene() {

    private val camera = Camera(
        eye = Float3(-2.0f, -3.0f, 8.0f),
        pitch = 20.0f,
        yaw = -78.0f
    )

    private lateinit var program: Program

    override fun draw() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        program.setFloat("time", timer.sinceStartSecs())
        program.setMat4("view", camera.getViewMatrix())

        backpackModel.draw()
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        program = Program.create(vertexShaderCode, fragmentShaderCode, geometryShaderCode)
        program.use()

        backpackModel.bind(
            program, ProgramLocations(
                attribPosition = program.getAttributeLocation("aPos"),
                attribNormal = program.getAttributeLocation("aNormal"),
                attribTexCoords = program.getAttributeLocation("aTexCoords"),
                uniformDiffuseTexture = program.getUniformLocation("texture_diffuse1")
            )
        )

        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)
        program.setMat4("model", translation(Float3(0.0f)))
        program.setMat4("projection", projection)
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene12GeometryShadersExplodingObjects(
                vertexShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene12_geometry_shaders_exploding_objects_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene12_geometry_shaders_exploding_objects_frag),
                geometryShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene12_geometry_shaders_exploding_objects_geo),
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
