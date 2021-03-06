package net.pters.learnopengl.android.tools

import android.opengl.GLES30.*
import android.opengl.GLES32
import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.Float4
import com.curiouscreature.kotlin.math.Mat4
import java.nio.IntBuffer

class Program private constructor(
    private val vertexShaderId: Int,
    private val fragmentShaderId: Int,
    private val geometryShaderId: Int?
) {

    private var id: Int = -1

    private var attributes = mutableMapOf<String, Int>()

    private var uniforms = mutableMapOf<String, Int>()

    fun getAttributeLocation(name: String) = attributes.getValue(name)

    fun getId() = id

    fun getUniformLocation(name: String) = uniforms.getValue(name)

    fun set3f(uniformName: String, f1: Float, f2: Float, f3: Float) =
        glUniform3f(getUniformLocation(uniformName), f1, f2, f3)

    fun setBoolean(uniformName: String, b: Boolean) = if (b) {
        setInt(getUniformLocation(uniformName), 1)
    } else {
        setInt(getUniformLocation(uniformName), 0)
    }

    fun setFloat(location: Int, f: Float) = glUniform1f(location, f)

    fun setFloat(uniformName: String, f: Float) = glUniform1f(getUniformLocation(uniformName), f)

    fun setFloat2(uniformName: String, f2: Float2) =
        glUniform2f(getUniformLocation(uniformName), f2.x, f2.y)

    fun setFloat3(uniformName: String, f3: Float3) =
        glUniform3f(getUniformLocation(uniformName), f3.x, f3.y, f3.z)

    fun setFloat4(uniformName: String, f4: Float4) =
        glUniform4f(getUniformLocation(uniformName), f4.x, f4.y, f4.z, f4.w)

    fun setInt(location: Int, i: Int) = glUniform1i(location, i)

    fun setInt(uniformName: String, i: Int) = glUniform1i(getUniformLocation(uniformName), i)

    fun setMat4(uniformName: String, m4: Mat4, transpose: Boolean = true) =
        glUniformMatrix4fv(getUniformLocation(uniformName), 1, transpose, m4.toFloatArray(), 0)

    fun use() = glUseProgram(id)

    private fun fetchAttributes() {
        val count = IntBuffer.allocate(1)
        glGetProgramiv(id, GL_ACTIVE_ATTRIBUTES, count)
        for (i in 0 until count[0]) {
            val name = glGetActiveAttrib(id, i, IntBuffer.allocate(1), IntBuffer.allocate(1))
            val location = glGetAttribLocation(id, name)
            attributes[name] = location
        }
    }

    private fun fetchUniforms() {
        val count = IntBuffer.allocate(1)
        glGetProgramiv(id, GL_ACTIVE_UNIFORMS, count)
        for (i in 0 until count[0]) {
            val name = glGetActiveUniform(id, i, IntBuffer.allocate(1), IntBuffer.allocate(1))
            val location = glGetUniformLocation(id, name)
            uniforms[name] = location
        }
    }

    private fun link() {
        id = glCreateProgram()
        glAttachShader(id, vertexShaderId)
        glAttachShader(id, fragmentShaderId)
        geometryShaderId?.also { glAttachShader(id, it) }
        glLinkProgram(id)

        val linkStatus = IntBuffer.allocate(1)
        glGetProgramiv(id, GL_LINK_STATUS, linkStatus)
        if (linkStatus[0] == GL_FALSE) {
            throw RuntimeException("Linking program failed: ${glGetProgramInfoLog(id)}")
        }
    }

    companion object {

        fun create(vertexShaderCode: String, fragmentShaderCode: String) =
            create(vertexShaderCode, fragmentShaderCode, null)

        fun create(
            vertexShaderCode: String,
            fragmentShaderCode: String,
            geometryShaderCode: String? = null
        ): Program {
            val vertexShaderId = compileShader(GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShaderId = compileShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
            val geometryShaderId =
                geometryShaderCode?.let { compileShader(GLES32.GL_GEOMETRY_SHADER, it) }
            return Program(vertexShaderId, fragmentShaderId, geometryShaderId).apply {
                link()
                fetchAttributes()
                fetchUniforms()
                glDeleteShader(vertexShaderId)
                glDeleteShader(fragmentShaderId)
                geometryShaderId?.also { glDeleteShader(it) }
            }
        }
    }
}
