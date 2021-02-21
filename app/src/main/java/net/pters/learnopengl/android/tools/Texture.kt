package net.pters.learnopengl.android.tools

import android.graphics.Bitmap
import android.opengl.GLES30.*
import android.opengl.GLUtils
import java.nio.IntBuffer

class Texture(private val bitmap: Bitmap) {

    private var id: Int? = null

    fun getId() = id ?: throw IllegalStateException("Call load() before using texture")

    fun load() {
        if (id != null) {
            return
        }

        val id = IntBuffer.allocate(1)
        glGenTextures(1, id)

        id[0].also {
            this.id = it
            glBindTexture(GL_TEXTURE_2D, it)
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MIN_FILTER,
            GL_LINEAR_MIPMAP_LINEAR
        )
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
        glGenerateMipmap(GL_TEXTURE_2D)
    }
}
