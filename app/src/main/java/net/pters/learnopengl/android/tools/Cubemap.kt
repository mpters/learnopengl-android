package net.pters.learnopengl.android.tools

import android.graphics.Bitmap
import android.opengl.GLES30.*
import android.opengl.GLUtils
import java.nio.IntBuffer

class Cubemap(
    private val right: Bitmap,
    private val left: Bitmap,
    private val top: Bitmap,
    private val bottom: Bitmap,
    private val front: Bitmap,
    private val back: Bitmap
) {

    private var id: Int? = null

    fun getId() = id ?: throw IllegalStateException("Call load() before using cubemap")

    fun load() {
        if (id != null) return

        val ib = IntBuffer.allocate(1)
        glGenTextures(1, ib)
        id = ib[0]
        glBindTexture(GL_TEXTURE_CUBE_MAP, getId())

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE)

        GLUtils.texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, right, 0)
        GLUtils.texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, left, 0)
        GLUtils.texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, top, 0)
        GLUtils.texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, bottom, 0)
        GLUtils.texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, front, 0)
        GLUtils.texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, back, 0)
    }
}
