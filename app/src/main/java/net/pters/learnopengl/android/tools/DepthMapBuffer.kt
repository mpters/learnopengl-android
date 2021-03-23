package net.pters.learnopengl.android.tools

import android.opengl.GLES30.*

class DepthMapBuffer private constructor(private val sizePx: Int) {

    private val fboId: Int = genId { glGenFramebuffers(1, it) }

    private val textureId: Int = genId { glGenTextures(1, it) }

    init {
        // Create depth map texture
        glBindTexture(GL_TEXTURE_2D, textureId)
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_DEPTH_COMPONENT32F,
            sizePx,
            sizePx,
            0,
            GL_DEPTH_COMPONENT,
            GL_FLOAT,
            null
        )
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        // Over sampling issue must be addressed in fragment shader since GL_CLAMP_TO_BORDER is not
        // available in GLES 3.0.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        // Attach depth texture as FBO's depth buffer
        glBindFramebuffer(GL_FRAMEBUFFER, fboId)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, textureId, 0)

        // Check that framebuffer is complete
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Framebuffer is not complete")
        }

        // Switch back to default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, fboId)
        glClear(GL_DEPTH_BUFFER_BIT)
        glViewport(0, 0, sizePx, sizePx)
    }

    fun getTextureId() = textureId

    companion object {

        fun create(sizePx: Int = 1024) = DepthMapBuffer(sizePx)
    }
}
