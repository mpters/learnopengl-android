package net.pters.learnopengl.android.scenes.lighting

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.scenes.Vertices.cubePositions
import net.pters.learnopengl.android.scenes.Vertices.cubeWithNormalsAndTexture
import net.pters.learnopengl.android.tools.*
import kotlin.math.cos

class Scene8MultipleLights private constructor(
    private val cubeVertexShaderCode: String,
    private val lightSourceVertexShaderCode: String,
    private val cubeFragmentShaderCode: String,
    private val lightSourceFragmentShaderCode: String,
    private val containerTexture: Texture,
    private val specularTexture: Texture
) : Scene() {

    private val pointLightPositions = listOf(
        Float3(0.7f, 0.2f, 2.0f),
        Float3(2.3f, -3.3f, -4.0f),
        Float3(-4.0f, 2.0f, -12.0f),
        Float3(0.0f, 0.0f, -3.0f)
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
        cubeProgram.setFloat3("spotLight.position", camera.getEye())
        cubeProgram.setFloat3("spotLight.direction", camera.getFront())
        glBindVertexArray(cubeVertexData.getVaoId())

        cubePositions.forEachIndexed { index, position ->
            val angle = 20.0f * index
            val model = translation(position) * rotation(normalize(Float3(1.0f, 0.3f, 0.5f)), angle)
            cubeProgram.setMat4("model", model)

            glDrawArrays(GL_TRIANGLES, 0, 36)
        }

        lightSourceProgram.use()
        lightSourceProgram.setMat4("view", view)
        glBindVertexArray(lightSourceVertexData.getVaoId())

        pointLightPositions.forEach {
            val lightSourceModel = translation(it) * scale(Float3(0.2f))
            lightSourceProgram.setMat4("model", lightSourceModel)

            glDrawArrays(GL_TRIANGLES, 0, 36)
        }
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        containerTexture.load()
        specularTexture.load()

        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)

        cubeProgram = Program.create(cubeVertexShaderCode, cubeFragmentShaderCode)
        cubeVertexData = VertexData(cubeWithNormalsAndTexture, null, 8)
        cubeVertexData.addAttribute(cubeProgram.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.addAttribute(cubeProgram.getAttributeLocation("aNormal"), 3, 3)
        cubeVertexData.addAttribute(cubeProgram.getAttributeLocation("aTexCoords"), 2, 6)
        cubeVertexData.bind()

        cubeProgram.use()
        cubeProgram.setInt("material.diffuse", 0)
        cubeProgram.setInt("material.specular", 1)
        cubeProgram.setFloat("material.shininess", 32.0f)
        cubeProgram.setMat4("projection", projection)

        // Directional light
        cubeProgram.set3f("dirLight.direction", -0.2f, -1.0f, -0.3f)
        cubeProgram.set3f("dirLight.ambient", 0.05f, 0.05f, 0.05f)
        cubeProgram.set3f("dirLight.diffuse", 0.4f, 0.4f, 0.4f)
        cubeProgram.set3f("dirLight.specular", 0.5f, 0.5f, 0.5f)

        // Point lights
        pointLightPositions.forEachIndexed { i, pos ->
            cubeProgram.setFloat3("pointLights[$i].position", pos)
            cubeProgram.set3f("pointLights[$i].ambient", 0.05f, 0.05f, 0.05f)
            cubeProgram.set3f("pointLights[$i].diffuse", 0.8f, 0.8f, 0.8f)
            cubeProgram.set3f("pointLights[$i].specular", 1.0f, 1.0f, 1.0f)
            cubeProgram.setFloat("pointLights[$i].constant", 1.0f)
            cubeProgram.setFloat("pointLights[$i].linear", 0.09f)
            cubeProgram.setFloat("pointLights[$i].quadratic", 0.032f)
        }

        // Spotlight
        cubeProgram.set3f("spotLight.ambient", 0.0f, 0.0f, 0.0f)
        cubeProgram.set3f("spotLight.diffuse", 1.0f, 1.0f, 1.0f)
        cubeProgram.set3f("spotLight.specular", 1.0f, 1.0f, 1.0f)
        cubeProgram.setFloat("spotLight.constant", 1.0f)
        cubeProgram.setFloat("spotLight.linear", 0.09f)
        cubeProgram.setFloat("spotLight.quadratic", 0.032f)
        cubeProgram.setFloat("spotLight.cutOff", cos(radians(12.5f)))
        cubeProgram.setFloat("spotLight.outerCutOff", cos(radians(17.5f)))

        lightSourceProgram =
            Program.create(lightSourceVertexShaderCode, lightSourceFragmentShaderCode)
        lightSourceVertexData = VertexData(cubeWithNormalsAndTexture, null, 8)
        lightSourceVertexData.addAttribute(lightSourceProgram.getAttributeLocation("aPos"), 3, 0)
        lightSourceVertexData.bind()

        lightSourceProgram.use()
        lightSourceProgram.setMat4("projection", projection)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, containerTexture.getId())
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, specularTexture.getId())
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene8MultipleLights(
                cubeVertexShaderCode = resources.readRawTextFile(R.raw.lighting_scene4_lighting_maps_cube_vert),
                lightSourceVertexShaderCode = resources.readRawTextFile(R.raw.lighting_scene1_colors_vert),
                cubeFragmentShaderCode = resources.readRawTextFile(R.raw.lighting_scene8_multiple_lights_cube_frag),
                lightSourceFragmentShaderCode = resources.readRawTextFile(R.raw.lighting_scene1_colors_light_source_frag),
                containerTexture = Texture(
                    loadBitmap(
                        context,
                        R.raw.texture_container2
                    ).flipVertically()
                ),
                specularTexture = Texture(
                    loadBitmap(
                        context,
                        R.raw.texture_container2_specular
                    ).flipVertically()
                )
            )
        }
    }
}
