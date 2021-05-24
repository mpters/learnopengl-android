package net.pters.learnopengl.android.scenes.inpractice.breakout

import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float4
import net.pters.learnopengl.android.tools.Program
import net.pters.learnopengl.android.tools.Texture
import net.pters.learnopengl.android.tools.VertexData
import kotlin.random.Random

class ParticleGenerator(
    private val program: Program,
    private val texture: Texture,
    numParticles: Int
) {

    private val particles: List<Particle> = mutableListOf<Particle>().apply {
        for (i in 0 until numParticles) {
            add(Particle())
        }
    }.toList()

    private val vertices: VertexData = VertexData(
        floatArrayOf(
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 0.0f
        ), null, 4
    ).apply {
        addAttribute(program.getAttributeLocation("vertex"), 4, 0)
        bind()
    }

    fun draw() {
        // Use additive blending to give it a 'glow' effect
        glBlendFunc(GL_SRC_ALPHA, GL_ONE)
        program.use()

        particles.filter { it.life > 0.0f }.forEach { particle ->
            program.setFloat2("offset", particle.position)
            program.setFloat4("color", particle.color)
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, texture.getId())
            glBindVertexArray(vertices.getVaoId())
            glDrawArrays(GL_TRIANGLES, 0, 6)
            glBindVertexArray(0)
        }

        // Don't forget to reset to default blending mode
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    fun update(deltaSecs: Float, gameObject: GameObject, newParticles: Int, offset: Float2) {
        // Add new particles
        for (i in 0 until newParticles) {
            firstUnusedParticle().also { respawnParticle(it, gameObject, offset) }
        }

        // Update all particles
        particles.forEach { particle ->
            particle.life -= deltaSecs
            if (particle.life > 0.0f) {
                // Particle is alive, thus update
                particle.position.xy -= particle.velocity * deltaSecs
                particle.color.a -= deltaSecs * 2.5f
            }
        }
    }

    private fun firstUnusedParticle() = particles.find { it.life <= 0.0f } ?: particles.first()

    private fun respawnParticle(particle: Particle, gameObject: GameObject, offset: Float2) {
        val random = Random.nextDouble(0.0, 5.0).toFloat()
        val rColor = 0.5f + Random.nextDouble(0.0, 1.0).toFloat()
        particle.position.xy = gameObject.position + random + offset
        particle.color.rgba = Float4(rColor, rColor, rColor, 1.0f)
        particle.life = 1.0f
        particle.velocity.xy = gameObject.velocity * 0.1f
    }
}

data class Particle(
    val position: Float2 = Float2(),
    val velocity: Float2 = Float2(),
    val color: Float4 = Float4(1.0f),
    var life: Float = 0.0f
)
