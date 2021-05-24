package net.pters.learnopengl.android.scenes.inpractice.breakout

import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3
import net.pters.learnopengl.android.tools.Texture

open class GameObject(
    val position: Float2,
    val size: Float2,
    val velocity: Float2 = Float2(0.0f, 0.0f),
    val color: Float3 = Float3(1.0f),
    val rotation: Float = 0.0f,
    val solid: Boolean,
    var destroyed: Boolean = false,
    val texture: Texture
) {

    fun draw(renderer: SpriteRenderer) {
        renderer.draw(texture, position, size, rotation, color)
    }
}
