package net.pters.learnopengl.android.tools.model

import android.opengl.GLES30.*
import net.pters.learnopengl.android.tools.Program
import net.pters.learnopengl.android.tools.VertexData
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Mesh(
    vertices: FloatBuffer,
    normals: FloatBuffer,
    texCoords: FloatBuffer,
    private val indices: IntBuffer,
    val material: Material
) {

    private val data: VertexData

    private var program: Program? = null

    private var locations: ProgramLocations? = null

    init {
        val capacity = vertices.capacity() + normals.capacity() + texCoords.capacity()
        val buffer = ByteBuffer.allocateDirect(capacity * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()

        while (vertices.hasRemaining()) {
            buffer.put(vertices.get())
            buffer.put(vertices.get())
            buffer.put(vertices.get())
            buffer.put(normals.get())
            buffer.put(normals.get())
            buffer.put(normals.get())
            buffer.put(texCoords.get())
            buffer.put(texCoords.get())
        }

        buffer.position(0)
        data = VertexData(buffer, indices, 8)
    }

    fun bind(program: Program, locations: ProgramLocations) {
        this.program = program
        this.locations = locations

        data.addAttribute(locations.attribPosition, 3, 0)
        locations.attribNormal?.also {
            data.addAttribute(it, 3, 3)
        }
        locations.attribTexCoords?.also {
            data.addAttribute(it, 2, 6)
        }
        data.bind()

        material.diffuseTexture?.load()
        material.specularTexture?.load()
    }

    fun draw() {
        glBindVertexArray(data.getVaoId())
        glDrawElements(GL_TRIANGLES, indices.capacity(), GL_UNSIGNED_INT, 0)
    }

    fun drawInstanced(count: Int) {
        glBindVertexArray(data.getVaoId())
        glDrawElementsInstanced(GL_TRIANGLES, indices.capacity(), GL_UNSIGNED_INT, 0, count)
    }

    fun getVaoId() = data.getVaoId()

    fun setTexturesAndUniforms() {
        val program = this.program
        val locations = this.locations
        if (program == null || locations == null) {
            throw IllegalStateException("Make sure you called bind(Program, ProgramLocations)")
        }

        program.use()

        material.diffuseTexture?.also { texture ->
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, texture.getId())
            locations.uniformDiffuseTexture?.let { program.setInt(it, 0) }
        }
        material.specularTexture?.also { texture ->
            glActiveTexture(GL_TEXTURE1)
            glBindTexture(GL_TEXTURE_2D, texture.getId())
            locations.uniformSpecularTexture?.also { program.setInt(it, 1) }
        }

        locations.uniformShininess?.also { program.setFloat(it, 32.0f) }
    }
}
