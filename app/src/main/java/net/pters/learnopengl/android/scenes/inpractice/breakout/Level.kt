package net.pters.learnopengl.android.scenes.inpractice.breakout

import androidx.annotation.RawRes
import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3

class Level(private val resourceManager: ResourceManager, @RawRes private val levelId: Int) {

    val bricks = mutableListOf<GameObject>()

    fun draw(renderer: SpriteRenderer) {
        bricks.forEach { brick ->
            if (brick.destroyed.not()) {
                brick.draw(renderer)
            }
        }
    }

    fun isCompleted(): Boolean {
        bricks.forEach { brick ->
            if (brick.solid.not() && brick.destroyed.not()) {
                return false
            }
        }
        return true
    }

    fun load(levelWidth: Int, levelHeight: Int) {
        bricks.clear()

        val tileData = mutableListOf<List<Int>>()

        resourceManager.readTextFile(levelId).lines().forEach { line ->
            val columns = mutableListOf<Int>()
            line.forEach {
                if (it.isDigit()) {
                    columns.add(it.toString().toInt())
                }
            }
            tileData.add(columns)
        }

        init(tileData, levelWidth, levelHeight)
    }

    private fun init(tileData: List<List<Int>>, levelWidth: Int, levelHeight: Int) {
        val height = tileData.size
        val width = tileData.first().size
        val unitWidth = levelWidth / width.toFloat()
        val unitHeight = levelHeight / height.toFloat()

        tileData.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { columnIndex, tile ->
                if (tile == 1) {
                    bricks.add(
                        GameObject(
                            position = Float2(unitWidth * columnIndex, unitHeight * rowIndex),
                            size = Float2(unitWidth, unitHeight),
                            color = Float3(0.8f, 0.8f, 0.7f),
                            solid = true,
                            texture = resourceManager.getTexture("block_solid")
                        )
                    )
                } else if (tile > 1) {
                    val color = when (tile) {
                        2 -> Float3(0.2f, 0.6f, 1.0f)
                        3 -> Float3(0.0f, 0.7f, 0.0f)
                        4 -> Float3(0.8f, 0.8f, 0.4f)
                        5 -> Float3(1.0f, 0.5f, 0.0f)
                        else -> Float3(1.0f)
                    }
                    bricks.add(
                        GameObject(
                            position = Float2(unitWidth * columnIndex, unitHeight * rowIndex),
                            size = Float2(unitWidth, unitHeight),
                            color = color,
                            solid = false,
                            texture = resourceManager.getTexture("block")
                        )
                    )
                }
            }
        }
    }
}
