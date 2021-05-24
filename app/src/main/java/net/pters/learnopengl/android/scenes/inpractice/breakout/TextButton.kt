package net.pters.learnopengl.android.scenes.inpractice.breakout

import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.Float4
import net.pters.learnopengl.android.tools.InputTracker

class TextButton private constructor(
    private val renderer: TextRenderer,
    private val text: String,
    private val scale: Float,
    private val color: Float3,
    private val selectedColor: Float3,
    private val bounds: Float4,
) {

    var wasPressed: Boolean = false

    private var isSelected: Boolean = false

    fun processInput(inputTracker: InputTracker) {
        if (inputTracker.lastAction == InputTracker.Action.DOWN) {
            if (isInBounds(inputTracker.position)) {
                isSelected = true
            }
        } else if (inputTracker.lastAction == InputTracker.Action.UP && isSelected) {
            isSelected = false
            if (isInBounds(inputTracker.position)) {
                wasPressed = true
            }
        } else {
            isSelected = false
            wasPressed = false
        }
    }

    fun render() {
        val color = if (isSelected) selectedColor else color
        renderer.render(text, bounds.xy, scale, color)
    }

    private fun isInBounds(position: Float2?) = if (position == null) false else {
        position.x >= bounds.x && position.x <= bounds.z && position.y >= bounds.y && position.y <= bounds.w
    }

    companion object {

        fun centered(
            renderer: TextRenderer,
            text: String,
            y: Float,
            scale: Float,
            color: Float3,
            selectedColor: Float3,
            screenWidth: Int,
        ): TextButton {
            val dimens = renderer.measure(text, scale)
            return TextButton(
                renderer = renderer,
                text = text,
                scale = scale,
                color = color,
                selectedColor = selectedColor,
                bounds = Float4().apply {
                    this.x = screenWidth / 2.0f - dimens.x / 2.0f
                    this.y = y
                    this.z = this.x + dimens.x
                    this.w = this.y + dimens.y
                }
            )
        }
    }
}
