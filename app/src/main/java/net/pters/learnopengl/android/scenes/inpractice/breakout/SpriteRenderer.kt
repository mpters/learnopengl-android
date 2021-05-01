package net.pters.learnopengl.android.scenes.inpractice.breakout

import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.*
import net.pters.learnopengl.android.tools.Program
import net.pters.learnopengl.android.tools.Texture
import net.pters.learnopengl.android.tools.VertexData

class SpriteRenderer(private val program: Program) {

    private val vertices: VertexData = VertexData(
        floatArrayOf(
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 0.0f
        ), null, 4
    )

    init {
        vertices.addAttribute(program.getAttributeLocation("vertex"), 4, 0)
        vertices.bind()
    }

    fun draw(
        texture: Texture,
        position: Float2,
        size: Float2 = Float2(10.0f),
        rotate: Float = 0.0f,
        color: Float3 = Float3(1.0f)
    ) {
        val model = translation(Float3(position, 0.0f)) *
                translation(Float3(0.5f * size.x, 0.5f * size.y, 0.0f)) *
                rotation(Float3(0.0f, 0.0f, 1.0f), rotate) *
                translation(Float3(-0.5f * size.x, -0.5f * size.y, 0.0f)) *
                scale(Float3(size, 1.0f))

        program.use()
        program.setMat4("model", model)
        program.setFloat3("spriteColor", color)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, texture.getId())

        glBindVertexArray(vertices.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 6)
        glBindVertexArray(0)
    }
}
