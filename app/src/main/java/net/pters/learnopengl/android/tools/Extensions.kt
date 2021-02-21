package net.pters.learnopengl.android.tools

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.annotation.RawRes
import java.nio.*

fun Bitmap.flipVertically(): Bitmap {
    val matrix = Matrix().apply { postScale(1.0f, -1.0f, width / 2.0f, height / 2.0f) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun FloatArray.toFloatBuffer(): FloatBuffer = ByteBuffer
    .allocateDirect(this.size * Float.SIZE_BYTES)
    .order(ByteOrder.nativeOrder())
    .asFloatBuffer().also {
        it.put(this).position(0)
    }

fun IntArray.toIntBuffer(): IntBuffer = ByteBuffer
    .allocateDirect(this.size * Int.SIZE_BYTES)
    .order(ByteOrder.nativeOrder())
    .asIntBuffer().also {
        it.put(this).position(0)
    }

fun Resources.readRawTextFile(@RawRes id: Int) =
    openRawResource(id).bufferedReader().use { it.readText() }

fun ShortArray.toShortBuffer(): ShortBuffer = ByteBuffer
    .allocateDirect(this.size * Short.SIZE_BYTES)
    .order(ByteOrder.nativeOrder())
    .asShortBuffer().also {
        it.put(this).position(0)
    }
