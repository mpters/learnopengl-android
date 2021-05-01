package net.pters.learnopengl.android.scenes.inpractice.breakout

import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.ortho
import net.pters.learnopengl.android.R

class Game(
    contextProvider: ResourceManager.ContextProvider,
    private val width: Int,
    private val height: Int
) {

    private val resourceManager = ResourceManager(contextProvider)

    private var state = State.WIN

    private lateinit var spriteRenderer: SpriteRenderer

    fun init() {
        val spriteProgram = resourceManager.loadProgram(
            "sprite",
            R.raw.inpractice_breakout_sprite_vert,
            R.raw.inpractice_breakout_sprite_frag
        )
        spriteRenderer = SpriteRenderer(spriteProgram)

        resourceManager.loadTexture("face", R.raw.texture_awesomeface)

        val projection = ortho(0.0f, width.toFloat(), height.toFloat(), 0.0f,-1.0f, 1.0f)
        spriteProgram.use()
        spriteProgram.setMat4("projection", projection)
    }

    fun processInput(deltaSecs: Float) {}

    fun update(deltaSecs: Float) {}

    fun render() {
        spriteRenderer.draw(
            resourceManager.getTexture("face"),
            Float2(200.0f),
            Float2(300.0f, 400.0f),
            45.0f,
            Float3(0.0f, 1.0f, 0.0f)
        )
    }

    enum class State { ACTIVE, MENU, WIN }
}
