package net.pters.learnopengl.android.tools

import com.curiouscreature.kotlin.math.Float2

class InputTracker {

    var lastAction: Action? = null

    var position: Float2? = null

    fun isLowerLeft(width: Int, height: Int) = position?.let {
        it.x < width / 2 && it.y > height / 2
    } ?: false

    fun isLowerRight(width: Int, height: Int) = position?.let {
        it.x > width / 2 && it.y > height / 2
    } ?: false

    enum class Action { DOWN, UP }
}
