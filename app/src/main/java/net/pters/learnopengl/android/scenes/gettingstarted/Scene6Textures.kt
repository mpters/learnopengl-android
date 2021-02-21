package net.pters.learnopengl.android.scenes.gettingstarted

import android.content.Context
import android.opengl.GLES30.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.*

class Scene6Textures private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val texture1: Texture,
    private val texture2: Texture
) : Scene() {

    private val vertices = floatArrayOf(
        0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
        0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
        -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
        -0.5f, 0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f
    )

    private val indices = intArrayOf(
        0, 1, 3,
        1, 2, 3
    )

    private lateinit var program: Program

    private lateinit var vertexData: VertexData

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        glBindVertexArray(vertexData.getVaoId())
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0)
    }

    override fun init(width: Int, height: Int) {
        glViewport(0, 0, width, height)

        texture1.load()
        texture2.load()

        program = Program.create(vertexShaderCode, fragmentShaderCode)

        vertexData = VertexData(vertices, indices, 8)
        vertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        vertexData.addAttribute(program.getAttributeLocation("aColor"), 3, 3)
        vertexData.addAttribute(program.getAttributeLocation("aTexCoord"), 2, 6)
        vertexData.bind()

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, texture1.getId())
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, texture2.getId())

        program.use()

        glUniform1i(program.getUniformLocation("texture1"), 0)
        glUniform1i(program.getUniformLocation("texture2"), 1)
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene6Textures(
                vertexShaderCode = resources.readRawTextFile(R.raw.gettingstarted_scene6_textures_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.gettingstarted_scene6_textures_frag),
                Texture(loadBitmap(context, R.raw.texture_container).flipVertically()),
                Texture(loadBitmap(context, R.raw.texture_awesomeface).flipVertically())
            )
        }
    }
}
