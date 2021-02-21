package net.pters.learnopengl.android.tools

import com.curiouscreature.kotlin.math.*
import kotlin.math.cos
import kotlin.math.sin

class Camera(
    private var eye: Float3 = Float3(0.0f, 0.0f, 5.0f),
    private var front: Float3 = Float3(0.0f, 0.0f, -1.0f),
    private var up: Float3 = Float3(0.0f, 1.0f, 0.0f),
    private val worldUp: Float3 = Float3(0.0f, 1.0f, 0.0f),
    private var pitch: Float = 0.0f,
    private var yaw: Float = -90.0f,
    private val sensitivity: Float = 0.1f,
    private val speedFactor: Float = 10.0f,
) {

    private var isLooking = false

    private var isMoving = false

    private var speed: Float = 0.0f

    private val downPosition: Float2 = Float2()

    private val offset: Float2 = Float2()

    private var right: Float3 = Float3()

    init {
        calcMatrices()
    }

    fun advance(elapsedSecs: Float) {
        if (isLooking) {
            yaw += offset.x * sensitivity * elapsedSecs
            pitch += offset.y * sensitivity * elapsedSecs
            pitch = clamp(pitch, -89.0f, 89.0f)
        }
        if (isMoving) {
            eye += front * speed * speedFactor * elapsedSecs
        }

        calcMatrices()
    }

    fun getEye() = eye

    fun getFront() = front

    fun getViewMatrix() = inverse(lookAt(eye, eye + front, up))

    fun halt() {
        isLooking = false
        isMoving = false
        offset.x = 0.0f
        offset.y = 0.0f
        speed = 0.0f
    }

    fun lookAround(x: Float, y: Float) {
        offset.x = x - downPosition.x
        offset.y = downPosition.y - y
    }

    fun move(speed: Float) {
        isMoving = true
        this.speed -= speed
    }

    fun startLooking(x: Float, y: Float) {
        isLooking = true
        downPosition.x = x
        downPosition.y = y
    }

    private fun calcMatrices() {
        front.xyz = normalize(
            Float3(
                x = cos(radians(yaw)) * cos(radians(pitch)),
                y = sin(radians(pitch)),
                z = sin(radians(yaw)) * cos(radians(pitch))
            )
        )
        right = normalize(cross(front, worldUp))
        up = normalize(cross(right, front))
    }
}
