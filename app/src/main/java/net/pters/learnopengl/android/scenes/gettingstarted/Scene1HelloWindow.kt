package net.pters.learnopengl.android.scenes.gettingstarted

import android.opengl.GLES30.*
import net.pters.learnopengl.android.tools.Scene

class Scene1HelloWindow private constructor() : Scene() {

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)
    }

    override fun init(width: Int, height: Int) {
        glViewport(0, 0, width, height)
    }

    companion object {

        fun create() = Scene1HelloWindow()
    }
}
