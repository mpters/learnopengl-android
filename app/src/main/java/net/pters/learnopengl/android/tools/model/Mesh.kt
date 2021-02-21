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
        data.addAttribute(locations.attribPosition, 3, 0)
        data.addAttribute(locations.attribNormal, 3, 3)
        data.addAttribute(locations.attribTexCoords, 2, 6)
        data.bind()

        program.use()

        material.diffuseTexture?.load()
        material.specularTexture?.load()

        material.diffuseTexture?.also {
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, it.getId())
            program.setInt(locations.uniformDiffuseTexture, 0)
        }
        material.specularTexture?.also {
            glActiveTexture(GL_TEXTURE1)
            glBindTexture(GL_TEXTURE_2D, it.getId())
            program.setInt(locations.uniformSpecularTexture, 1)

        }

        program.setFloat(locations.uniformShininess, 32.0f)
    }

    fun draw() {
        glBindVertexArray(data.getVaoId())
        glDrawElements(GL_TRIANGLES, indices.capacity(), GL_UNSIGNED_INT, 0)
    }
}
