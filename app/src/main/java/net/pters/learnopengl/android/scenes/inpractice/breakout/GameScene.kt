package net.pters.learnopengl.android.scenes.inpractice.breakout

import android.content.Context
import android.opengl.GLES30.*
import net.pters.learnopengl.android.tools.Scene

class GameScene private constructor(
    private val contextProvider: ResourceManager.ContextProvider
) : Scene() {

    private lateinit var game: Game

    override fun draw() {
        val delta = timer.sinceLastFrameSecs()
        game.processInput(delta)
        game.update(delta)

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)
        game.render()
    }

    override fun init(width: Int, height: Int) {
        glViewport(0, 0, width, height)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        game = Game(contextProvider, width, height)
        game.init()
    }

    companion object {

        fun create(context: Context): Scene {
            return GameScene(object : ResourceManager.ContextProvider {
                override fun getContext() = context
            })
        }
    }
}
