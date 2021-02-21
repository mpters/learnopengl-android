package net.pters.learnopengl.android.scenes.modelloading

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.perspective
import com.curiouscreature.kotlin.math.translation
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.Camera
import net.pters.learnopengl.android.tools.Program
import net.pters.learnopengl.android.tools.Scene
import net.pters.learnopengl.android.tools.model.DefaultProgramLocations
import net.pters.learnopengl.android.tools.model.Model
import net.pters.learnopengl.android.tools.model.ObjLoader
import net.pters.learnopengl.android.tools.readRawTextFile

class SceneBackpack private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
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

        program.setMat4("view", camera.getViewMatrix())
        program.setFloat3("viewPos", camera.getEye())

        backpackModel.draw()
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        program = Program.create(vertexShaderCode, fragmentShaderCode)
        program.use()

        backpackModel.bind(program, DefaultProgramLocations.resolve(program))

        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)
        program.setMat4("model", translation(Float3(0.0f)))
        program.setMat4("projection", projection)

        val material = backpackModel.getPrimaryMaterial()
        program.setFloat3("pointLight.position", Float3(3.0f, 1.0f, 3.0f))
        program.setFloat3("pointLight.ambient", material.ambientColor)
        program.setFloat3("pointLight.diffuse", material.diffuseColor)
        program.setFloat3("pointLight.specular", material.specularColor)
        program.setFloat("pointLight.constant", 1.0f)
        program.setFloat("pointLight.linear", 0.09f)
        program.setFloat("pointLight.quadratic", 0.032f)
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return SceneBackpack(
                vertexShaderCode = resources.readRawTextFile(R.raw.modelloading_scene_backpack_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.modelloading_scene_backpack_frag),
                ObjLoader.fromAssets(
                    context,
                    directory = "backpack",
                    objFileName = "backpack.obj",
                    mtlFileName = "backpack.mtl",
                    specularTextureFileName = "specular.jpg"
                )
            )
        }
    }
}
