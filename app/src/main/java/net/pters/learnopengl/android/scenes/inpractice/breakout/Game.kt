package net.pters.learnopengl.android.scenes.inpractice.breakout

import com.curiouscreature.kotlin.math.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.InputTracker
import kotlin.math.absoluteValue


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

    private lateinit var particleGenerator: ParticleGenerator

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
        resourceManager.loadTexture("particle", R.raw.texture_particle)

        val particleProgram = resourceManager.loadProgram(
            "particle",
            R.raw.inpractice_breakout_particle_vert,
            R.raw.inpractice_breakout_particle_frag
        )
        particleGenerator = ParticleGenerator(
            particleProgram, resourceManager.getTexture("particle"),
            100
        )

        var level = Level(resourceManager, R.raw.breakout_one)
        level.load(width, height / 2)
        levels.add(level)
        level = Level(resourceManager, R.raw.breakout_two)
        level.load(width, height / 2)
        levels.add(level)
        level = Level(resourceManager, R.raw.breakout_three)
        level.load(width, height / 2)
        levels.add(level)
        level = Level(resourceManager, R.raw.breakout_four)
        level.load(width, height / 2)
        levels.add(level)

        player = GameObject(
            position = Float2(width / 2 - playerSize.x / 2.0f, height - playerSize.y),
            size = playerSize,
            solid = true,
            texture = resourceManager.getTexture("paddle")
        )

        val ballRadius = 25.0f
        ball = Ball(
            position = player.position + Float2(
                playerSize.x / 2.0f - ballRadius,
                -ballRadius * 2.0f
            ),
            radius = ballRadius,
            velocity = initialBallVelocity.copy(),
            texture = resourceManager.getTexture("face")
        )

        val projection = ortho(0.0f, width.toFloat(), height.toFloat(), 0.0f, -1.0f, 1.0f)
        spriteProgram.use()
        spriteProgram.setMat4("projection", projection)
        particleProgram.use()
        particleProgram.setMat4("projection", projection)
    }

    fun processInput(deltaSecs: Float) {
        if (state == State.ACTIVE) {
            val velocity = 600.0f * deltaSecs
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
        particleGenerator.update(deltaSecs, ball, 2, Float2(ball.radius / 2.0f))

        if (ball.position.y >= height) { // Did ball reach bottom edge?
            resetLevel()
            resetPlayer()
        }
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
            particleGenerator.draw()
            ball.draw(spriteRenderer)
        }
    }

    private fun doCollisions() = levels[currentLevel].bricks.forEach { brick ->
        if (brick.destroyed.not()) {
            val collision = ball.checkCollision(brick)
            if (collision?.first == true) {
                // Destroy block if not solid
                if (brick.solid.not()) {
                    brick.destroyed = true
                }

                // Collision resolution
                val dir = collision.second
                val diffVector = collision.third
                when (dir) {
                    Direction.LEFT, Direction.RIGHT -> { // Horizontal collision
                        ball.velocity.x = -ball.velocity.x // Reverse horizontal velocity
                        // Relocate
                        val penetration = ball.radius - diffVector.x.absoluteValue
                        if (dir == Direction.LEFT) {
                            ball.position.x += penetration // Move ball to right
                        } else {
                            ball.position.x -= penetration // Move ball to left
                        }
                    }
                    Direction.UP, Direction.DOWN -> { // Vertical collision
                        ball.velocity.y = -ball.velocity.y // Reverse vertical velocity
                        // Relocate
                        val penetration = ball.radius - diffVector.y.absoluteValue
                        if (dir == Direction.UP) {
                            ball.position.y -= penetration // Move ball back up
                        } else {
                            ball.position.y += penetration // Move ball back down
                        }
                    }
                }
            }
        }
    }.also {
        val collision = ball.checkCollision(player)
        if (ball.stuck.not() && collision?.first == true) {
            // Check where it hit the board, and change velocity based on where it hit the board
            val centerBoard = player.position.x + player.size.x / 2.0f
            val distance = (ball.position.x + ball.radius) - centerBoard
            val percentage = distance / (player.size.x / 2.0f)
            // Then move accordingly
            val strength = 3.0f
            val oldVelocity = ball.velocity
            ball.velocity.x = initialBallVelocity.x * percentage * strength
            ball.velocity.xy = normalize(ball.velocity) * length(oldVelocity)
            ball.velocity.y = -ball.velocity.y.absoluteValue
        }
    }

    private fun resetLevel() {
        levels[currentLevel].load(width, height / 2)
    }

    private fun resetPlayer() {
        player.size.xy = playerSize
        player.position.xy = Float2(width / 2.0f - playerSize.x / 2.0f, height - playerSize.y)
        ball.position.xy =
            player.position + Float2(playerSize.x / 2.0f - ball.radius, -(ball.radius * 2.0f))
        ball.velocity.xy = initialBallVelocity
        ball.stuck = true
        inputTracker.lastAction = null
    }

    private fun vectorDirection(target: Float2): Direction? {
        val compass = mapOf(
            Float2(0.0f, 1.0f) to Direction.UP,
            Float2(1.0f, 0.0f) to Direction.RIGHT,
            Float2(0.0f, -1.0f) to Direction.DOWN,
            Float2(-1.0f, 0.0f) to Direction.LEFT
        )

        var max = 0.0f
        var bestMatch: Direction? = null
        compass.forEach { (t, u) ->
            val dotProduct = dot(normalize(target), t)
            if (dotProduct >= max) {
                max = dotProduct
                bestMatch = u
            }
        }
        return bestMatch
    }

    private fun Ball.checkCollision(other: GameObject): Triple<Boolean, Direction, Float2>? {
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

        return if (length(difference) < radius) {
            vectorDirection(difference)?.let {
                Triple(true, it, difference)
            }
        } else {
            Triple(false, Direction.UP, Float2())
        }
    }

    enum class State { ACTIVE, MENU, WIN }

    private enum class Direction {
        UP, RIGHT, DOWN, LEFT
    }

    companion object {

        private val initialBallVelocity = Float2(100.0f, -600.0f)

        private val playerSize = Float2(200.0f, 40.0f)
    }
}
