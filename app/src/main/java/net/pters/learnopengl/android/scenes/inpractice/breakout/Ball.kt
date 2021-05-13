package net.pters.learnopengl.android.scenes.inpractice.breakout

import com.curiouscreature.kotlin.math.Float2
import net.pters.learnopengl.android.tools.Texture

class Ball(
    position: Float2,
    val radius: Float,
    var stuck: Boolean = true,
    var sticky: Boolean = false,
    var passThrough: Boolean = false,
    velocity: Float2,
    texture: Texture
) : GameObject(
    position = position,
    size = Float2(radius * 2.0f),
    velocity = velocity,
    solid = true,
    texture = texture
) {

    fun move(deltaSecs: Float, windowWidth: Int): Float2 {
        if (!stuck) {
            position.xy += velocity * deltaSecs
            if (position.x <= 0.0f) {
                velocity.x = -velocity.x
                position.x = 0.0f
            } else if (position.x + size.x >= windowWidth) {
                velocity.x = -velocity.x
                position.x = windowWidth - size.x
            }

            if (position.y <= 0.0f) {
                velocity.y = -velocity.y
                position.y = 0.0f
            }
        }

        return position
    }

    fun reset(position: Float2, velocity: Float2) {

    }
}
