package net.pters.learnopengl.android.scenes.inpractice.breakout

import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3
import net.pters.learnopengl.android.tools.Texture

class PowerUp(
    val type: PowerUpType,
    var duration: Float,
    var activated: Boolean = false,
    position: Float2,
    color: Float3 = Float3(1.0f),
    texture: Texture
) : GameObject(
    position = position,
    size = Float2(120.0f, 40.0f),
    velocity = Float2(0.0f, 150.0f),
    color = color,
    rotation = 0.0f,
    solid = false,
    destroyed = false,
    texture = texture
)

enum class PowerUpType {
    CHAOS, CONFUSE, INCREASE, PASS_THROUGH, SPEED, STICKY
}
