package net.pters.learnopengl.android.tools

import android.os.SystemClock

class Timer {

    private val startTimeMillis: Long

    private var lastFrameMillis: Long

    init {
        SystemClock.elapsedRealtime().also {
            lastFrameMillis = it
            startTimeMillis = it
        }
    }

    fun sinceLastFrameSecs() = (SystemClock.elapsedRealtime() - lastFrameMillis) / 1000.0f

    fun sinceStartSecs() = (SystemClock.elapsedRealtime() - startTimeMillis) / 1000.0f

    fun tick() {
        lastFrameMillis = SystemClock.elapsedRealtime()
    }
}
