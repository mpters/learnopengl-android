package net.pters.learnopengl.android.scenes.inpractice.breakout

import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.ortho
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.InputTracker

class Game(
    contextProvider: ResourceManager.ContextProvider,
    private val inputTracker: InputTracker,
    private val width: Int,
    private val height: Int
) {

    private val resourceManager = ResourceManager(contextProvider)

    private val levels = mutableListOf<Level>()

    private var currentLevel = 0

    private var state = State.ACTIVE

    private lateinit var spriteRenderer: SpriteRenderer

    private lateinit var player: GameObject

    fun init() {
        val spriteProgram = resourceManager.loadProgram(
            "sprite",
            R.raw.inpractice_breakout_sprite_vert,
            R.raw.inpractice_breakout_sprite_frag
        )
        spriteRenderer = SpriteRenderer(spriteProgram)

        resourceManager.loadTexture("background", R.raw.texture_background)
        resourceManager.loadTexture("paddle", R.raw.texture_paddle)
        resourceManager.loadTexture("block", R.raw.texture_block)
        resourceManager.loadTexture("block_solid", R.raw.texture_block_solid)

        var level = Level(resourceManager)
        level.load(R.raw.breakout_one, width, height / 2)
        levels.add(level)
        level = Level(resourceManager)
        level.load(R.raw.breakout_two, width, height / 2)
        levels.add(level)
        level = Level(resourceManager)
        level.load(R.raw.breakout_three, width, height / 2)
        levels.add(level)
        level = Level(resourceManager)
        level.load(R.raw.breakout_four, width, height / 2)
        levels.add(level)

        val playerSize = Float2(200.0f, 40.0f)
        player = GameObject(
            position = Float2(width / 2 - playerSize.x / 2.0f, height - playerSize.y),
            size = playerSize,
            solid = true,
            texture = resourceManager.getTexture("paddle")
        )

        val projection = ortho(0.0f, width.toFloat(), height.toFloat(), 0.0f, -1.0f, 1.0f)
        spriteProgram.use()
        spriteProgram.setMat4("projection", projection)
    }

    fun processInput(deltaSecs: Float) {
        if (state == State.ACTIVE) {
            val velocity = 300.0f * deltaSecs
            when {
                inputTracker.lastAction == InputTracker.Action.DOWN && inputTracker.isLowerLeft(
                    width,
                    height
                ) -> {
                    if (player.position.x >= 0.0f) {
                        player.position.x -= velocity
                    }
                }
                inputTracker.lastAction == InputTracker.Action.DOWN && inputTracker.isLowerRight(
                    width,
                    height
                ) -> {
                    if (player.position.x <= width - player.size.x) {
                        player.position.x += velocity
                    }
                }
            }
        }
    }

    fun update(deltaSecs: Float) {}

    fun render() {
        if (state == State.ACTIVE) {
            spriteRenderer.draw(
                resourceManager.getTexture("background"),
                Float2(0.0f),
                Float2(width.toFloat(), height.toFloat())
            )
            levels[currentLevel].draw(spriteRenderer)

            player.draw(spriteRenderer)
        }
    }

    enum class State { ACTIVE, MENU, WIN }
}
