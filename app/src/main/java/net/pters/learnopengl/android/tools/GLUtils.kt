package net.pters.learnopengl.android.tools

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30.*
import android.opengl.Matrix
import androidx.annotation.RawRes
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.Mat4
import com.curiouscreature.kotlin.math.transpose
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

/**
 * There where issues with flipped axis when using [com.curiouscreature.kotlin.math.lookAt] for some
 * reason I don't understand yet.
 */
internal fun lookAtM(
    eye: Float3,
    center: Float3 = Float3(0.0f),
    up: Float3 = Float3(0.0f, 1.0f, 0.0f)
) = FloatArray(16).let {
    Matrix.setLookAtM(it, 0, eye.x, eye.y, eye.z, center.x, center.y, center.z, up.x, up.y, up.z)
    transpose(Mat4.of(*it))
}
