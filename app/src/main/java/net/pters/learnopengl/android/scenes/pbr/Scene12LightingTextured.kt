package net.pters.learnopengl.android.scenes.pbr

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.*
import kotlin.math.cos
import kotlin.math.sin

class Scene12LightingTextured private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val albedoTexture: Texture,
    private val normalTexture: Texture,
    private val metallicTexture: Texture,
    private val roughnessTexture: Texture,
    private val aoTexture: Texture

) : Scene() {

    private val camera = Camera(eye = Float3(z = 15.0f))

    private val lights = mapOf(
        Float3(0.0f, 0.0f, 10.0f) to Float3(150.0f, 150.0f, 150.0f)
    )

    private var sphereIndexCount = -1

    private lateinit var program: Program

    private lateinit var vertexData: VertexData

    override fun draw() {
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        program.setFloat3("camPos", camera.getEye())
        program.setMat4("view", camera.getViewMatrix())

        val rows = 7
        val columns = 7
        val spacing = 2.5f

        // Render rows*column number of spheres with material properties defined by textures (they
        // all have the same material properties)
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val model = translation(
                    Float3(
                        x = (column - columns / 2.0f) * spacing,
                        y = (row - rows / 2.0f) * spacing,
                        z = 0.0f
                    )
                )
                program.setMat4("model", model)
                renderSphere()
            }
        }

        // Render light source (simply re-render sphere at light positions). This looks a bit off as
        // we use the same shader, but it'll make their positions obvious and keeps the codeprint small.
        lights.keys.forEachIndexed { index, position ->
            val newPos = position + Float3(x = sin(timer.sinceStartSecs() * 5.0f))
            program.setFloat3("lights[$index].Position", newPos)
            program.setFloat3("lights[$index].Color", lights.getValue(position))
            val model = translation(newPos) * scale(Float3(0.5f))
            program.setMat4("model", model)
            renderSphere()
        }
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        program = Program.create(
            vertexShaderCode = vertexShaderCode,
            fragmentShaderCode = fragmentShaderCode
        )
        program.use()
        program.setInt("albedoMap", 0)
        program.setInt("normalMap", 1)
        program.setInt("metallicMap", 2)
        program.setInt("roughnessMap", 3)
        program.setInt("aoMap", 4)
        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)
        program.setMat4("projection", projection)

        albedoTexture.load()
        normalTexture.load()
        metallicTexture.load()
        roughnessTexture.load()
        aoTexture.load()

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, albedoTexture.getId())
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, normalTexture.getId())
        glActiveTexture(GL_TEXTURE2)
        glBindTexture(GL_TEXTURE_2D, metallicTexture.getId())
        glActiveTexture(GL_TEXTURE3)
        glBindTexture(GL_TEXTURE_2D, roughnessTexture.getId())
        glActiveTexture(GL_TEXTURE4)
        glBindTexture(GL_TEXTURE_2D, aoTexture.getId())

        initSphere()
    }

    private fun initSphere() {
        val numXSegments = 64
        val numYSegments = 64

        val positions = mutableListOf<Float3>()
        val uv = mutableListOf<Float2>()
        val normals = mutableListOf<Float3>()
        val indices = mutableListOf<Int>()

        for (y in 0..numYSegments) {
            for (x in 0..numXSegments) {
                val xSegment = x / numXSegments.toFloat()
                val ySegment = y / numYSegments.toFloat()
                val xPos = cos(xSegment * 2.0f * PI) * sin(ySegment * PI)
                val yPos = cos(ySegment * PI)
                val zPos = sin(xSegment * 2.0f * PI) * sin(ySegment * PI)

                positions.add(Float3(xPos, yPos, zPos))
                uv.add(Float2(xSegment, ySegment))
                normals.add(Float3(xPos, yPos, zPos))
            }
        }

        var oddRow = false
        for (y in 0..numYSegments) {
            if (oddRow) {
                for (x in numXSegments downTo 0) {
                    indices.add((y + 1) * (numXSegments + 1) + x)
                    indices.add(y * (numXSegments + 1) + x)
                }
            } else {
                for (x in 0..numXSegments) {
                    indices.add(y * (numXSegments + 1) + x)
                    indices.add((y + 1) * (numXSegments + 1) + x)
                }
            }
            oddRow = oddRow.not()
        }
        sphereIndexCount = indices.size

        val data = mutableListOf<Float>()
        positions.forEachIndexed { index, position ->
            data.add(position.x)
            data.add(position.y)
            data.add(position.z)
            data.add(uv[index].x)
            data.add(uv[index].y)
            data.add(normals[index].x)
            data.add(normals[index].y)
            data.add(normals[index].z)
        }

        vertexData = VertexData(data.toFloatArray(), indices.toIntArray(), 8)
        vertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        vertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 3)
        vertexData.addAttribute(program.getAttributeLocation("aNormal"), 3, 5)
        vertexData.bind()
    }

    private fun renderSphere() {
        glBindVertexArray(vertexData.getVaoId())
        glDrawElements(GL_TRIANGLE_STRIP, sphereIndexCount, GL_UNSIGNED_INT, 0)
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene12LightingTextured(
                vertexShaderCode = resources.readRawTextFile(R.raw.pbr_scene11_lighting_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.pbr_scene12_lighting_textured_frag),
                albedoTexture = Texture(loadBitmap(context, R.raw.texture_rusted_iron_albedo)),
                normalTexture = Texture(loadBitmap(context, R.raw.texture_rusted_iron_normal)),
                metallicTexture = Texture(loadBitmap(context, R.raw.texture_rusted_iron_metallic)),
                roughnessTexture = Texture(
                    loadBitmap(
                        context,
                        R.raw.texture_rusted_iron_roughness
                    )
                ),
                aoTexture = Texture(loadBitmap(context, R.raw.texture_rusted_iron_ao))
            )
        }
    }
}
