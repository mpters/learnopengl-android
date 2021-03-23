package net.pters.learnopengl.android.scenes.advancedlighting

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.scenes.Vertices.ndcCubeWithNormalsAndTexture
import net.pters.learnopengl.android.tools.*
import kotlin.math.cos
import kotlin.math.sin

class Scene32PointShadows private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val depthVertexShaderCode: String,
    private val depthFragmentShaderCode: String,
    private val woodTexture: Texture
) : Scene() {

    private val camera = Camera(eye = Float3(z = 3.0f))

    private val cubeModels = listOf(
        translation(Float3(4.0f, -3.5f, 0.0f)) * scale(Float3(0.5f)), // Cube 1
        translation(Float3(2.0f, 3.0f, 1.0f)) * scale(Float3(0.75f)), // Cube 2
        translation(Float3(-3.0f, -1.0f, 0.0f)) * scale(Float3(0.5f)), // Cube 3
        translation(Float3(-1.5f, 1.0f, 1.5f)) * scale(Float3(0.5f)), // Cube 4
        translation(Float3(-1.5f, 2.0f, -3.0f)) * // Cube 5
                rotation(normalize(Float3(1.0f, 0.0f, 1.0f)), 60.0f) *
                scale(Float3(0.75f))
    )

    private val lightPosition = Float3(0.0f, 0.0f, 0.0f)

    private var depthFboId: Int = -1

    private var depthTextureId: Int = -1

    private var width: Int = -1

    private var height: Int = -1

    private lateinit var depthProgram: Program

    private lateinit var program: Program

    private lateinit var cubeVertexData: VertexData

    override fun draw() {
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        lightPosition.x = sin(timer.sinceStartSecs()) * 1.0f
        lightPosition.z = cos(timer.sinceStartSecs()) * 1.0f

        // Create depth cubemap transformation matrices
        val nearPlane = 1.0f
        val farPlane = 25.0f
        val projection = perspective(90.0f, 1.0f, nearPlane, farPlane)
        val shadowTransforms = mutableMapOf<Int, Mat4>().apply {
            put(
                GL_TEXTURE_CUBE_MAP_POSITIVE_X,
                projection * inverse(
                    lookAt(
                        lightPosition,
                        lightPosition + Float3(1.0f, 0.0f, 0.0f),
                        Float3(0.0f, -1.0f, 0.0f)
                    )
                )
            )
            put(
                GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
                projection * inverse(
                    lookAt(
                        lightPosition,
                        lightPosition + Float3(-1.0f, 0.0f, 0.0f),
                        Float3(0.0f, -1.0f, 0.0f)
                    )
                )
            )
            put(
                GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
                projection * inverse(
                    lookAt(
                        lightPosition,
                        lightPosition + Float3(0.0f, 1.0f, 0.0f),
                        Float3(0.0f, 0.0f, 1.0f)
                    )
                )
            )
            put(
                GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
                projection * inverse(
                    lookAt(
                        lightPosition,
                        lightPosition + Float3(0.0f, -1.0f, 0.0f),
                        Float3(0.0f, 0.0f, -1.0f)
                    )
                )
            )
            put(
                GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
                projection * inverse(
                    lookAt(
                        lightPosition,
                        lightPosition + Float3(0.0f, 0.0f, 1.0f),
                        Float3(0.0f, -1.0f, 0.0f)
                    )
                )
            )
            put(
                GL_TEXTURE_CUBE_MAP_NEGATIVE_Z,
                projection * inverse(
                    lookAt(
                        lightPosition,
                        lightPosition + Float3(0.0f, 0.0f, -1.0f),
                        Float3(0.0f, -1.0f, 0.0f)
                    )
                )
            )
        }

        // Render scene to depth cubemap
        glBindFramebuffer(GL_FRAMEBUFFER, depthFboId)
        glViewport(0, 0, 512, 512)

        depthProgram.use()
        depthProgram.setFloat("far_plane", farPlane)
        depthProgram.setFloat3("lightPos", lightPosition)

        // Some devices and also the emulator on macOS only support OpenGL ES 3.0. That's why we
        // render each face of the cube individually instead of using a geometry shader to render
        // everything in one run.
        for (i in GL_TEXTURE_CUBE_MAP_POSITIVE_X..GL_TEXTURE_CUBE_MAP_NEGATIVE_Z) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, i, depthTextureId, 0)
            glClear(GL_DEPTH_BUFFER_BIT)

            shadowTransforms[i]?.also {
                depthProgram.setMat4("lightSpaceMatrix", it)
                renderScene(depthProgram, false)
            }
        }

        // Switch to default framebuffer & reset viewport
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glViewport(0, 0, width, height)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Render scene as normal using the generated depth map
        program.use()
        program.setFloat("far_plane", farPlane)
        program.setFloat3("lightPos", lightPosition)
        program.setMat4("view", camera.getViewMatrix())
        program.setFloat3("viewPos", camera.getEye())
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, woodTexture.getId())
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_CUBE_MAP, depthTextureId)
        renderScene(program, true)
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        this.width = width
        this.height = height

        glEnable(GL_CULL_FACE)
        glEnable(GL_DEPTH_TEST)

        woodTexture.load()

        depthProgram =
            Program.create(depthVertexShaderCode, depthFragmentShaderCode)
        program = Program.create(vertexShaderCode, fragmentShaderCode)

        cubeVertexData = VertexData(ndcCubeWithNormalsAndTexture, null, 8)
        cubeVertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        cubeVertexData.addAttribute(program.getAttributeLocation("aNormal"), 3, 3)
        cubeVertexData.addAttribute(program.getAttributeLocation("aTexCoords"), 2, 6)
        cubeVertexData.bind()

        program.use()
        program.setInt("diffuseTexture", 0)
        program.setInt("depthMap", 1)
        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)
        program.setMat4("projection", projection)

        initCubeDepthMap()
    }

    private fun initCubeDepthMap() {
        depthFboId = genId { glGenFramebuffers(1, it) }

        // Create depth map texture
        depthTextureId = genId { glGenTextures(1, it) }
        glBindTexture(GL_TEXTURE_CUBE_MAP, depthTextureId)
        for (i in GL_TEXTURE_CUBE_MAP_POSITIVE_X..GL_TEXTURE_CUBE_MAP_NEGATIVE_Z) {
            glTexImage2D(
                i,
                0,
                GL_DEPTH_COMPONENT32F,
                512,
                512,
                0,
                GL_DEPTH_COMPONENT,
                GL_FLOAT,
                null
            )
        }
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE)

        // Attach depth texture as FBO's depth buffer
        glBindFramebuffer(GL_FRAMEBUFFER, depthFboId)
        for (i in GL_TEXTURE_CUBE_MAP_POSITIVE_X..GL_TEXTURE_CUBE_MAP_NEGATIVE_Z) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, i, depthTextureId, 0)
        }

        // Check that framebuffer is complete
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Framebuffer is not complete")
        }

        // Switch back to default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    private fun renderScene(program: Program, reverseNormals: Boolean) {
        glBindVertexArray(cubeVertexData.getVaoId())

        // Room cube
        program.setMat4("model", scale(Float3(5.0f)))
        // Note that we disable culling here since we render inside the cube which throws off the
        // normal culling methods.
        glDisable(GL_CULL_FACE)
        if (reverseNormals) {
            // Invert normals when drawing cube from the inside so lighting still works
            program.setInt("reverse_normals", 1)
        }
        glDrawArrays(GL_TRIANGLES, 0, 36)
        if (reverseNormals) {
            // And of course disable it
            program.setInt("reverse_normals", 0)
        }
        glEnable(GL_CULL_FACE)

        // Cubes inside the room
        cubeModels.forEach {
            program.setMat4("model", it)
            glDrawArrays(GL_TRIANGLES, 0, 36)
        }
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene32PointShadows(
                vertexShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene32_point_shadows_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene32_point_shadows_frag),
                depthVertexShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene32_point_shadows_depth_vert),
                depthFragmentShaderCode = resources.readRawTextFile(R.raw.advancedlighting_scene32_point_shadows_depth_frag),
                woodTexture = Texture(loadBitmap(context, R.raw.texture_wood))
            )
        }
    }
}
