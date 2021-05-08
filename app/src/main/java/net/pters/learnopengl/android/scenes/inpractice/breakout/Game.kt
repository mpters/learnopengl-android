package net.pters.learnopengl.android.scenes.inpractice.breakout

import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.clamp
import com.curiouscreature.kotlin.math.length
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

    private lateinit var ball: Ball

    fun init() {
        val spriteProgram = resourceManager.loadProgram(
            "sprite",
            R.raw.inpractice_breakout_sprite_vert,
            R.raw.inpractice_breakout_sprite_frag
        )
        spriteRenderer = SpriteRenderer(spriteProgram)

        resourceManager.loadTexture("background", R.raw.texture_background)
        resourceManager.loadTexture("face", R.raw.texture_awesomeface)
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

        val initialBallVelocity = Float2(100.0f, -350.0f)
        val ballRadius = 25.0f
        ball = Ball(
            position = player.position + Float2(
                playerSize.x / 2.0f - ballRadius,
                -ballRadius * 2.0f
            ),
            radius = ballRadius,
            velocity = initialBallVelocity,
            texture = resourceManager.getTexture("face")
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
                        if (ball.stuck) {
                            ball.position.x -= velocity
                        }
                    }
                }
                inputTracker.lastAction == InputTracker.Action.DOWN && inputTracker.isLowerRight(
                    width,
                    height
                ) -> {
                    if (player.position.x <= width - player.size.x) {
                        player.position.x += velocity
                        if (ball.stuck) {
                            ball.position.x += velocity
                        }
                    }
                }
                inputTracker.lastAction == InputTracker.Action.UP -> {
                    if (ball.stuck) {
                        ball.stuck = false
                    }
                }
            }
        }
    }

    fun update(deltaSecs: Float) {
        ball.move(deltaSecs, width)
        doCollisions()
    }

    fun render() {
        if (state == State.ACTIVE) {
            spriteRenderer.draw(
                resourceManager.getTexture("background"),
                Float2(0.0f),
                Float2(width.toFloat(), height.toFloat())
            )
            levels[currentLevel].draw(spriteRenderer)

            player.draw(spriteRenderer)
            ball.draw(spriteRenderer)
        }
    }

    private fun doCollisions() = levels[currentLevel].bricks.forEach { brick ->
        if (brick.destroyed.not()) {
            if (ball.intersectsWith(brick) && brick.solid.not()) {
                brick.destroyed = true
            }
        }
    }

    private fun Ball.intersectsWith(other: GameObject): Boolean {
        // Get center point circle first
        val center = position + radius
        // Calculate AABB info (center, half-extents)
        val aabbHalfExtents = other.size / 2.0f
        val aabbCenter = other.position + aabbHalfExtents
        // Get difference vector between both centers
        var difference = center - aabbCenter
        val clamped = clamp(difference, -aabbHalfExtents, aabbHalfExtents)
        // Add clamped value to aabbCenter and we get the value of box closest to circle
        val closest = aabbCenter + clamped
        // Retrieve vector between center circle and closest point AABB and check if length <= radius
        difference = closest - center
        return length(difference) < radius
    }

    enum class State { ACTIVE, MENU, WIN }
}
