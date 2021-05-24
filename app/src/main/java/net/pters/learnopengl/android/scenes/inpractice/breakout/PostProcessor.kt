package net.pters.learnopengl.android.scenes.inpractice.breakout

import android.opengl.GLES30.*
import net.pters.learnopengl.android.tools.Program
import net.pters.learnopengl.android.tools.VertexData
import net.pters.learnopengl.android.tools.genId

class PostProcessor(
    private val program: Program,
    private val width: Int,
    private val height: Int,
    var chaos: Boolean = false,
    var confuse: Boolean = false,
    var shake: Boolean = false,
) {

    private val msFboId: Int = genId { glGenFramebuffers(1, it) }

    private val fboId: Int = genId { glGenFramebuffers(1, it) }

    private val rboId: Int = genId { glGenRenderbuffers(1, it) }

    private val textureId: Int

    private val vertices: VertexData = VertexData(
        floatArrayOf(
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f
        ), null, 4
    )

    init {
        // initialize renderbuffer storage with a multisampled color buffer (don't need a depth/stencil buffer)
        glBindFramebuffer(GL_FRAMEBUFFER, msFboId)
        glBindRenderbuffer(GL_RENDERBUFFER, rboId)
        // allocate storage for render buffer object
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, 4, GL_RGBA8, width, height)
        // attach MS render buffer object to framebuffer
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, rboId)

        // Check that framebuffer is complete
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Framebuffer is not complete")
        }

        // also initialize the FBO/texture to blit multisampled color-buffer to; used for shader operations (for postprocessing effects)
        glBindFramebuffer(GL_FRAMEBUFFER, fboId)
        // create Texture
        textureId = genId { glGenTextures(1, it) }
        glBindTexture(GL_TEXTURE_2D, textureId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)
        // set Texture wrap and filter modes
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // attach texture to framebuffer as its color attachment
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0)

        // Check that framebuffer is complete
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Framebuffer is not complete")
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // initialize render data and uniforms
        vertices.addAttribute(program.getAttributeLocation("vertex"), 4, 0)
        vertices.bind()

        program.use()
        program.setInt("scene", 0)

        val offset = 1.0f / 300.0f
        val offsets = floatArrayOf(
            -offset, offset,  // top-left
            0.0f, offset,  // top-center
            offset, offset,  // top-right
            -offset, 0.0f,  // center-left
            0.0f, 0.0f,  // center-center
            offset, 0.0f,  // center - right
            -offset, -offset,  // bottom-left
            0.0f, -offset,  // bottom-center
            offset, -offset     // bottom-right
        )
        glUniform2fv(program.getUniformLocation("offsets[0]"), 9, offsets, 0)

        val edgeKernel = intArrayOf(
            -1, -1, -1,
            -1, 8, -1,
            -1, -1, -1
        )
        glUniform1iv(program.getUniformLocation("edge_kernel[0]"), 9, edgeKernel, 0)

        val blurKernel = floatArrayOf(
            1.0f / 16.0f, 2.0f / 16.0f, 1.0f / 16.0f,
            2.0f / 16.0f, 4.0f / 16.0f, 2.0f / 16.0f,
            1.0f / 16.0f, 2.0f / 16.0f, 1.0f / 16.0f
        )
        glUniform1fv(program.getUniformLocation("blur_kernel[0]"), 9, blurKernel, 0)
    }

    fun beginRender() {
        glBindFramebuffer(GL_FRAMEBUFFER, msFboId)
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)
    }

    fun endRender() {
        // now resolve multisampled color-buffer into intermediate FBO to store to texture
        glBindFramebuffer(GL_READ_FRAMEBUFFER, msFboId)
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboId)
        glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST)
        glBindFramebuffer(
            GL_FRAMEBUFFER,
            0
        ) // binds both READ and WRITE framebuffer to default framebuffer
    }

    fun render(time: Float) {
        // set uniforms/options
        program.use()
        program.setFloat("time", time)
        program.setBoolean("confuse", confuse)
        program.setBoolean("chaos", chaos)
        program.setBoolean("shake", shake)

        // render textured quad
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureId)
        glBindVertexArray(vertices.getVaoId())
        glDrawArrays(GL_TRIANGLES, 0, 6)
        glBindVertexArray(0)
    }
}
