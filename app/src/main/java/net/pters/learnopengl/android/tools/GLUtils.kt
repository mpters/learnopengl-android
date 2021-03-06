package net.pters.learnopengl.android.tools

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30.*
import androidx.annotation.RawRes
import java.nio.IntBuffer

internal fun compileShader(type: Int, code: String) = glCreateShader(type).also { id ->
    glShaderSource(id, code)
    glCompileShader(id)

    val compileStatus = IntBuffer.allocate(1)
    glGetShaderiv(id, GL_COMPILE_STATUS, compileStatus)
    if (compileStatus[0] == GL_FALSE) {
        throw RuntimeException("Compiling shader failed: ${glGetShaderInfoLog(id)}")
    }
}

internal fun genId(glGenCall: (IntBuffer) -> Unit) = IntBuffer.allocate(1).let {
    glGenCall(it)
    it[0]
}

internal fun loadBitmap(context: Context, @RawRes textureId: Int) = BitmapFactory.decodeResource(
    context.resources,
    textureId,
    BitmapFactory.Options().apply { inScaled = false })
