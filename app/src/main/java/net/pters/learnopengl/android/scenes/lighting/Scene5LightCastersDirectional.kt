package net.pters.learnopengl.android.scenes.lighting

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.scenes.Vertices.cubePositions
import net.pters.learnopengl.android.scenes.Vertices.cubeWithNormalsAndTexture
import net.pters.learnopengl.android.tools.*

class Scene5LightCastersDirectional private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val containerTexture: Texture,
    private val specularTexture: Texture
) : Scene() {

    private val camera = Camera()

    private lateinit var cubeProgram: Program

    private lateinit var cubeVertexData: VertexData

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        cubeProgram.use()
        cubeProgram.setFloat3("viewPos", camera.getEye())
        cubeProgram.setMat4("view", camera.getViewMatrix())
        glBindVertexArray(cubeVertexData.getVaoId())

        cubePositions.forEachIndexed { index, position ->
            val angle = 20.0f * index
            val model = translation(position) * rotation(normalize(Float3(1.0f, 0.3f, 0.5f)), angle)
            cubeProgram.setMat4("model", model)

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

        cubeProgram = Program.create(vertexShaderCode, fragmentShaderCode)
        cubeVertexData = VertexData(cubeWithNormalsAndTexture, null, 8)
        cubeVertexData.addAttribute(cubeProgram.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.addAttribute(cubeProgram.getAttributeLocation("aNormal"), 3, 3)
        cubeVertexData.addAttribute(cubeProgram.getAttributeLocation("aTexCoords"), 2, 6)
        cubeVertexData.bind()

        cubeProgram.use()
        cubeProgram.setInt("material.diffuse", 0)
        cubeProgram.setInt("material.specular", 1)
        cubeProgram.setFloat("material.shininess", 32.0f)
        cubeProgram.set3f("light.direction", -0.2f, -1.0f, -0.3f)
        cubeProgram.set3f("light.ambient", 0.2f, 0.2f, 0.2f)
        cubeProgram.set3f("light.diffuse", 0.5f, 0.5f, 0.5f)
        cubeProgram.set3f("light.specular", 1.0f, 1.0f, 1.0f)
        cubeProgram.setMat4("projection", projection)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, containerTexture.getId())
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, specularTexture.getId())
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene5LightCastersDirectional(
                vertexShaderCode = resources.readRawTextFile(R.raw.lighting_scene4_lighting_maps_cube_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.lighting_scene5_light_casters_directional_frag),
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
