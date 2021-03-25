package net.pters.learnopengl.android.scenes.advancedlighting

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.*

class Scene52SteepParallaxMapping private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val texture: Texture,
    private val heightTexture: Texture,
    private val normalTexture: Texture
) : Scene() {

    private val camera = Camera()

    private lateinit var program: Program

    private lateinit var vertexData: VertexData

    override fun draw() {
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        val model = rotation(normalize(Float3(1.0f, 0.0f, 1.0f)), timer.sinceStartSecs() * 10.0f)
        program.setMat4("model", model)
        program.setMat4("view", camera.getViewMatrix())
        program.setFloat3("viewPos", camera.getEye())

        glBindVertexArray(vertexData.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 6)
        glBindVertexArray(0)

    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        texture.load()
        heightTexture.load()
        normalTexture.load()

        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)

        program = Program.create(vertexShaderCode, fragmentShaderCode)
        program.use()
        program.setInt("diffuseMap", 0)
        program.setInt("normalMap", 1)
        program.setInt("depthMap", 2)
        program.setFloat("heightScale", 0.1f)
        program.setFloat3("lightPos", Float3(0.5f, 1.0f, 0.3f))
        program.setMat4("projection", projection)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, texture.getId())
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, normalTexture.getId())
        glActiveTexture(GL_TEXTURE2)
        glBindTexture(GL_TEXTURE_2D, heightTexture.getId())

        initQuadVertexData()
    }

    private fun initQuadVertexData() {
        // Positions
        val pos1 = Float3(-1.0f, 1.0f, 0.0f)
        val pos2 = Float3(-1.0f, -1.0f, 0.0f)
        val pos3 = Float3(1.0f, -1.0f, 0.0f)
        val pos4 = Float3(1.0f, 1.0f, 0.0f)

        // Texture coordinates
        val uv1 = Float2(0.0f, 1.0f)
        val uv2 = Float2(0.0f, 0.0f)
        val uv3 = Float2(1.0f, 0.0f)
        val uv4 = Float2(1.0f, 1.0f)

        // Normal vector
        val nm = Float3(0.0f, 0.0f, 1.0f)

        // Calculate tangent vectors of both triangles
        val tang1 = Float3()
        val bitang1 = Float3()
        val tang2 = Float3()
        val bitang2 = Float3()

        // triangle 1
        val edge1 = pos2 - pos1
        val edge2 = pos3 - pos1
        val deltaUV1 = uv2 - uv1
        val deltaUV2 = uv3 - uv1

        var f = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y)

        tang1.x = f * (deltaUV2.y * edge1.x - deltaUV1.y * edge2.x)
        tang1.y = f * (deltaUV2.y * edge1.y - deltaUV1.y * edge2.y)
        tang1.z = f * (deltaUV2.y * edge1.z - deltaUV1.y * edge2.z)
        tang1.xyz = normalize(tang1.xyz)

        bitang1.x = f * (-deltaUV2.x * edge1.x + deltaUV1.x * edge2.x)
        bitang1.y = f * (-deltaUV2.x * edge1.y + deltaUV1.x * edge2.y)
        bitang1.z = f * (-deltaUV2.x * edge1.z + deltaUV1.x * edge2.z)
        bitang1.xyz = normalize(bitang1.xyz)

        // Triangle 2
        edge1.xyz = pos3 - pos1
        edge2.xyz = pos4 - pos1
        deltaUV1.xy = uv3 - uv1
        deltaUV2.xy = uv4 - uv1

        f = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y)

        tang2.x = f * (deltaUV2.y * edge1.x - deltaUV1.y * edge2.x)
        tang2.y = f * (deltaUV2.y * edge1.y - deltaUV1.y * edge2.y)
        tang2.z = f * (deltaUV2.y * edge1.z - deltaUV1.y * edge2.z)
        tang2.xyz = normalize(tang2.xyz)

        bitang2.x = f * (-deltaUV2.x * edge1.x + deltaUV1.x * edge2.x)
        bitang2.y = f * (-deltaUV2.x * edge1.y + deltaUV1.x * edge2.y)
        bitang2.z = f * (-deltaUV2.x * edge1.z + deltaUV1.x * edge2.z)
        bitang2.xyz = normalize(bitang2)

        val quadVertices = floatArrayOf(
            pos1.x,
            pos1.y,
            pos1.z,
            nm.x,
            nm.y,
            nm.z,
            uv1.x,
            uv1.y,
            tang1.x,
            tang1.y,
            tang1.z,
            bitang1.x,
            bitang1.y,
            bitang1.z,
            pos2.x,
            pos2.y,
            pos2.z,
            nm.x,
            nm.y,
            nm.z,
            uv2.x,
            uv2.y,
            tang1.x,
            tang1.y,
            tang1.z,
            bitang1.x,
            bitang1.y,
            bitang1.z,
            pos3.x,
            pos3.y,
            pos3.z,
            nm.x,
            nm.y,
            nm.z,
            uv3.x,
            uv3.y,
            tang1.x,
            tang1.y,
            tang1.z,
            bitang1.x,
            bitang1.y,
            bitang1.z,

            pos1.x,
            pos1.y,
            pos1.z,
            nm.x,
            nm.y,
            nm.z,
            uv1.x,
            uv1.y,
            tang2.x,
            tang2.y,
            tang2.z,
            bitang2.x,
            bitang2.y,
            bitang2.z,
            pos3.x,
            pos3.y,
            pos3.z,
            nm.x,
            nm.y,
            nm.z,
            uv3.x,
            uv3.y,
            tang2.x,
            tang2.y,
            tang2.z,
            bitang2.x,
            bitang2.y,
            bitang2.z,
            pos4.x,
            pos4.y,
            pos4.z,
            nm.x,
            nm.y,
            nm.z,
            uv4.x,
            uv4.y,
            tang2.x,
            tang2.y,
            tang2.z,
            bitang2.x,
            bitang2.y,
            bitang2.z
        )

        vertexData = VertexData(quadVertices, null, 14)
        vertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        vertexData.addAttribute(program.getAttributeLocation("aNormal"), 3, 3)
        vertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 6)
        vertexData.addAttribute(program.getAttributeLocation("aTangent"), 3, 8)
        vertexData.addAttribute(program.getAttributeLocation("aBitangent"), 3, 11)
        vertexData.bind()
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene52SteepParallaxMapping(
                vertexShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene51_parallax_mapping_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene52_steep_parallax_mapping_frag),
                texture = Texture(loadBitmap(context, R.raw.texture_toy_box_diffuse)),
                heightTexture = Texture(loadBitmap(context, R.raw.texture_toy_box_disp)),
                normalTexture = Texture(loadBitmap(context, R.raw.texture_toy_box_normal))
            )
        }
    }
}
