package net.pters.learnopengl.android.scenes.inpractice.breakout

import com.curiouscreature.kotlin.math.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.InputTracker
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.random.Random


class Game(
    contextProvider: ResourceManager.ContextProvider,
    private val inputTracker: InputTracker,
    private val width: Int,
    private val height: Int
) {

    private val resourceManager = ResourceManager(contextProvider)

    private val levels = mutableListOf<Level>()

    private val powerUps = mutableListOf<PowerUp>()

    private var currentLevel = 0

    private var lives = 3

    private var state = State.MENU

    private var shakeTime = 0.0f

    private lateinit var spriteRenderer: SpriteRenderer

    private lateinit var textRenderer: TextRenderer

    private lateinit var particleGenerator: ParticleGenerator

    private lateinit var postProcessor: PostProcessor

    private lateinit var player: GameObject

    private lateinit var ball: Ball

    private lateinit var startButton: TextButton

    private lateinit var nextLevelButton: TextButton

    private lateinit var retryButton: TextButton

    fun init() {
        val spriteProgram = resourceManager.loadProgram(
            "sprite",
            R.raw.inpractice_breakout_sprite_vert,
            R.raw.inpractice_breakout_sprite_frag
        )
        spriteRenderer = SpriteRenderer(spriteProgram)

        resourceManager.loadTexture("background", R.raw.texture_background)
        resourceManager.loadTexture("face", R.raw.texture_awesomeface)
        resourceManager.loadTexture("font", R.raw.texture_font_arial)
        resourceManager.loadTexture("paddle", R.raw.texture_paddle)
        resourceManager.loadTexture("block", R.raw.texture_block)
        resourceManager.loadTexture("block_solid", R.raw.texture_block_solid)
        resourceManager.loadTexture("particle", R.raw.texture_particle)
        resourceManager.loadTexture("powerup_chaos", R.raw.texture_powerup_chaos)
        resourceManager.loadTexture("powerup_confuse", R.raw.texture_powerup_confuse)
        resourceManager.loadTexture("powerup_increase", R.raw.texture_powerup_increase)
        resourceManager.loadTexture("powerup_passthrough", R.raw.texture_powerup_passthrough)
        resourceManager.loadTexture("powerup_speed", R.raw.texture_powerup_speed)
        resourceManager.loadTexture("powerup_sticky", R.raw.texture_powerup_sticky)
        resourceManager.loadSound("bleep", R.raw.audio_bleep)
        resourceManager.loadSound("bleep2", R.raw.audio_bleep2)
        resourceManager.loadSound("breakout", R.raw.audio_breakout)
        resourceManager.loadSound("powerup", R.raw.audio_powerup)
        resourceManager.loadSound("solid", R.raw.audio_solid)

        val textProgram = resourceManager.loadProgram(
            "text",
            R.raw.inpractice_scene1_text_rendering_vert,
            R.raw.inpractice_scene1_text_rendering_frag
        )
        textRenderer = TextRenderer(textProgram, resourceManager.getTexture("font"))

        val particleProgram = resourceManager.loadProgram(
            "particle",
            R.raw.inpractice_breakout_particle_vert,
            R.raw.inpractice_breakout_particle_frag
        )
        particleGenerator = ParticleGenerator(
            particleProgram, resourceManager.getTexture("particle"),
            100
        )

        val postProgram = resourceManager.loadProgram(
            "post",
            R.raw.inpractice_breakout_postprocessing_vert,
            R.raw.inpractice_breakout_postprocessing_frag
        )
        postProcessor = PostProcessor(postProgram, width, height)

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
            size = playerSize.copy(),
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

        startButton = TextButton.centered(
            textRenderer,
            "Click here to start",
            height / 2.0f,
            3.0f,
            Float3(1.0f),
            Float3(0.7f, 1.0f, 0.7f),
            width
        )
        nextLevelButton = TextButton.centered(
            textRenderer,
            "Select next level",
            height / 2.0f + max(100.0f, height / 12.0f),
            2.5f,
            Float3(1.0f),
            Float3(0.7f, 1.0f, 0.7f),
            width
        )
        retryButton = TextButton.centered(
            textRenderer,
            "Click here to retry",
            height / 2.0f,
            3.0f,
            Float3(1.0f),
            Float3(0.7f, 1.0f, 0.7f),
            width
        )

        val projection = ortho(0.0f, width.toFloat(), height.toFloat(), 0.0f, -1.0f, 1.0f)
        spriteProgram.use()
        spriteProgram.setMat4("projection", projection)
        particleProgram.use()
        particleProgram.setMat4("projection", projection)
        textProgram.use()
        textProgram.setMat4("projection", projection)

        val backgroundMusic = resourceManager.getSound("breakout")
        backgroundMusic.isLooping = true
        backgroundMusic.start()
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
                    inputTracker.lastAction = null
                    if (ball.stuck) {
                        ball.stuck = false
                    }
                }
            }
        } else if (state == State.MENU) {
            startButton.processInput(inputTracker)
            nextLevelButton.processInput(inputTracker)

            if (startButton.wasPressed) {
                state = State.ACTIVE
            } else if (nextLevelButton.wasPressed) {
                if (currentLevel == 3) {
                    currentLevel = 0
                } else {
                    currentLevel++
                }
            }
        } else if (state == State.WIN) {
            retryButton.processInput(inputTracker)
            if (retryButton.wasPressed) {
                state = State.MENU
                postProcessor.chaos = false
            }
        }
    }

    fun update(deltaSecs: Float) {
        ball.move(deltaSecs, width)
        doCollisions()
        particleGenerator.update(deltaSecs, ball, 2, Float2(ball.radius / 2.0f))
        updatePowerUps(deltaSecs)

        // reduce shake time
        if (shakeTime > 0.0f) {
            shakeTime -= deltaSecs
            if (shakeTime <= 0.0f) {
                postProcessor.shake = false
            }
        }

        if (ball.position.y >= height) { // Did ball reach bottom edge?
            lives--
            if (lives == 0) {
                resetLevel()
                state = State.MENU

            }
            resetPlayer()
        }

        // check win condition
        if (state == State.ACTIVE && levels[currentLevel].isCompleted()) {
            resetLevel()
            resetPlayer()
            postProcessor.chaos = true
            state = State.WIN
        }
    }

    fun stop() {
        resourceManager.stopSounds()
    }

    fun render(time: Float) {
        postProcessor.beginRender()

        spriteRenderer.draw(
            resourceManager.getTexture("background"),
            Float2(0.0f),
            Float2(width.toFloat(), height.toFloat())
        )
        levels[currentLevel].draw(spriteRenderer)

        player.draw(spriteRenderer)

        // Draw PowerUps
        powerUps.forEach { powerUp ->
            if (powerUp.destroyed.not()) {
                powerUp.draw(spriteRenderer)
            }
        }

        particleGenerator.draw()
        ball.draw(spriteRenderer)

        postProcessor.endRender()
        postProcessor.render(time)

        textRenderer.render("Lives: $lives", Float2(15.0f, 10.0f), 2.0f, Float3(1.0f))

        if (state == State.MENU) {
            startButton.render()
            nextLevelButton.render()
        } else if (state == State.WIN) {
            val youWonDimens = textRenderer.measure("You WON!!!", 4.0f)
            textRenderer.render(
                "You WON!!!",
                Float2(width / 2.0f - youWonDimens.x / 2.0f, height / 4.0f),
                4.0f,
                Float3(1.0f)
            )
            retryButton.render()
        }
    }

    private fun doCollisions() = levels[currentLevel].bricks.forEach { brick ->
        if (brick.destroyed.not()) {
            val collision = ball.checkCollision(brick)
            if (collision?.first == true) {
                // Destroy block if not solid
                if (brick.solid.not()) {
                    brick.destroyed = true
                    spawnPowerUps(brick)
                    resourceManager.getSound("bleep").start()
                } else {
                    shakeTime = 0.05f
                    postProcessor.shake = true
                    resourceManager.getSound("bleep").start()
                }

                // Collision resolution
                val dir = collision.second
                val diffVector = collision.third
                if (!(ball.passThrough && !brick.solid)) { // don't do collision resolution on non-solid bricks if pass-through is activated
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
        }
    }.also {
        // also check collisions on PowerUps and if so, activate them
        powerUps.forEach { powerUp ->
            if (powerUp.destroyed.not()) {
                // first check if powerup passed bottom edge, if so: keep as inactive and destroy
                if (powerUp.position.y >= height) {
                    powerUp.destroyed = true
                }

                if (player.checkCollision(powerUp)) {
                    // collided with player, now activate powerup
                    activatePowerUp(powerUp)
                    powerUp.destroyed = true
                    powerUp.activated = true
                    resourceManager.getSound("powerup").start()
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

            // if Sticky powerup is activated, also stick ball to paddle once new velocity vectors were calculated
            ball.stuck = ball.sticky

            resourceManager.getSound("bleep2").start()
        }
    }

    private fun resetLevel() {
        levels.forEach {
            it.load(width, height / 2)
        }
        lives = 3
    }

    private fun resetPlayer() {
        player.size.xy = playerSize
        player.position.xy = Float2(width / 2.0f - playerSize.x / 2.0f, height - playerSize.y)
        ball.position.xy =
            player.position + Float2(playerSize.x / 2.0f - ball.radius, -(ball.radius * 2.0f))
        ball.velocity.xy = initialBallVelocity
        ball.stuck = true
        inputTracker.lastAction = null

        // also disable all active powerups
        powerUps.clear()
        postProcessor.chaos = false
        postProcessor.confuse = false
        ball.passThrough = false
        ball.sticky = false
        ball.color.rgb = Float3(1.0f)
        player.color.rgb = Float3(1.0f)
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

    private fun GameObject.checkCollision(other: GameObject): Boolean {
        // collision x-axis?
        val collisionX =
            position.x + size.x >= other.position.x && other.position.x + other.size.x >= position.x
        // collision y-axis?
        val collisionY =
            position.y + size.y >= other.position.y && other.position.y + other.size.y >= position.y
        return collisionX && collisionY
    }

    private fun shouldSpawn(chance: Int) = Random.nextInt(0, chance) == 0

    private fun spawnPowerUps(brick: GameObject) {
        if (shouldSpawn(75)) { // 1 in 75 chance
            powerUps.add(
                PowerUp(
                    type = PowerUpType.SPEED,
                    duration = 0.0f,
                    position = brick.position.copy(),
                    color = Float3(0.5f, 0.5f, 1.0f),
                    texture = resourceManager.getTexture("powerup_speed")
                )
            )
        }

        if (shouldSpawn(75)) {
            powerUps.add(
                PowerUp(
                    type = PowerUpType.STICKY,
                    duration = 20.0f,
                    position = brick.position.copy(),
                    color = Float3(1.0f, 0.5f, 1.0f),
                    texture = resourceManager.getTexture("powerup_sticky")
                )
            )
        }

        if (shouldSpawn(75)) {
            powerUps.add(
                PowerUp(
                    type = PowerUpType.PASS_THROUGH,
                    duration = 10.0f,
                    position = brick.position.copy(),
                    color = Float3(0.5f, 1.0f, 0.5f),
                    texture = resourceManager.getTexture("powerup_passthrough")
                )
            )
        }

        if (shouldSpawn(75)) {
            powerUps.add(
                PowerUp(
                    type = PowerUpType.INCREASE,
                    duration = 0.0f,
                    position = brick.position.copy(),
                    color = Float3(1.0f, 0.6f, 0.4f),
                    texture = resourceManager.getTexture("powerup_increase")
                )
            )
        }

        if (shouldSpawn(15)) { // negative powerups should spawn more often
            powerUps.add(
                PowerUp(
                    type = PowerUpType.CONFUSE,
                    duration = 15.0f,
                    position = brick.position.copy(),
                    color = Float3(1.0f, 0.3f, 0.3f),
                    texture = resourceManager.getTexture("powerup_confuse")
                )
            )
        }

        if (shouldSpawn(15)) {
            powerUps.add(
                PowerUp(
                    type = PowerUpType.CHAOS,
                    duration = 15.0f,
                    position = brick.position.copy(),
                    color = Float3(0.9f, 0.25f, 0.25f),
                    texture = resourceManager.getTexture("powerup_chaos")
                )
            )
        }
    }

    private fun activatePowerUp(powerUp: PowerUp) {
        if (powerUp.type == PowerUpType.SPEED) {
            ball.velocity.xy *= 1.2f
        } else if (powerUp.type == PowerUpType.STICKY) {
            ball.sticky = true
            player.color.rgb = Float3(1.0f, 0.5f, 1.0f)
        } else if (powerUp.type == PowerUpType.PASS_THROUGH) {
            ball.passThrough = true
            ball.color.rgb = Float3(1.0f, 0.5f, 0.5f)
        } else if (powerUp.type == PowerUpType.INCREASE) {
            player.size.x += 100
        } else if (powerUp.type == PowerUpType.CONFUSE) {
            if (postProcessor.chaos.not()) {
                postProcessor.confuse = true // only activate if chaos wasn't already active
            }
        } else if (powerUp.type == PowerUpType.CHAOS) {
            if (postProcessor.confuse.not()) {
                postProcessor.chaos = true
            }
        }
    }

    private fun isOtherPowerUpActive(type: PowerUpType) =
        powerUps.any { it.activated && it.type == type }

    private fun updatePowerUps(deltaSecs: Float) = powerUps.forEach { powerUp ->
        powerUp.position.xy += powerUp.velocity * deltaSecs

        if (powerUp.activated) {
            powerUp.duration -= deltaSecs
            if (powerUp.duration <= 0.0f) {
                // remove powerup from list (will later be removed)
                powerUp.activated = false

                // deactivate effects
                if (powerUp.type == PowerUpType.STICKY) {
                    if (!isOtherPowerUpActive(PowerUpType.STICKY)) {
                        // only reset if no other PowerUp of type sticky is active
                        ball.sticky = false
                        player.color.rgb = Float3(1.0f)
                    } else if (powerUp.type == PowerUpType.PASS_THROUGH) {
                        if (!isOtherPowerUpActive(PowerUpType.PASS_THROUGH)) {
                            // only reset if no other PowerUp of type pass-through is active
                            ball.passThrough = false
                            ball.color.rgb = Float3(1.0f)
                        }
                    } else if (powerUp.type == PowerUpType.CONFUSE) {
                        if (!isOtherPowerUpActive(PowerUpType.CONFUSE)) {
                            // only reset if no other PowerUp of type confuse is active
                            postProcessor.confuse = false
                        }
                    } else if (powerUp.type == PowerUpType.CHAOS) {
                        if (!isOtherPowerUpActive(PowerUpType.CHAOS)) {
                            // only reset if no other PowerUp of type chaos is active
                            postProcessor.chaos = false
                        }
                    }
                }
            }
        }
    }.also {
        powerUps.removeIf { powerUp ->
            powerUp.destroyed && !powerUp.activated
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
