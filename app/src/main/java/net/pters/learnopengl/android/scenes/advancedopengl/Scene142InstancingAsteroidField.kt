package net.pters.learnopengl.android.scenes.advancedopengl

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.*
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.*
import net.pters.learnopengl.android.tools.model.Model
import net.pters.learnopengl.android.tools.model.ObjLoader
import net.pters.learnopengl.android.tools.model.ProgramLocations
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Scene142InstancingAsteroidField private constructor(
    private val asteroidVertexShaderCode: String,
    private val planetVertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val asteroidModel: Model,
    private val planetModel: Model
) : Scene() {

    private val camera = Camera(eye = Float3(0.0f, 0.0f, 70.0f))

    private val asteroidModels = mutableListOf<Mat4>()

    private val numAsteroids = 10000

    private lateinit var asteroidProgram: Program

    private lateinit var planetProgram: Program

    override fun draw() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        val view = camera.getViewMatrix()
        asteroidProgram.use()
        asteroidProgram.setMat4("view", view)
        asteroidModel.drawInstanced(numAsteroids)

        planetProgram.use()
        planetProgram.setMat4("view", view)
        planetModel.draw()
    }

    override fun getCamera() = camera

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        glViewport(0, 0, width, height)

        val projection = perspective(45.0f, width.toFloat() / height.toFloat(), 0.1f, 100.0f)

        asteroidProgram = Program.create(asteroidVertexShaderCode, fragmentShaderCode)
        asteroidProgram.use()
        asteroidProgram.setMat4("projection", projection)

        asteroidModel.bind(
            asteroidProgram, ProgramLocations(
                attribPosition = asteroidProgram.getAttributeLocation("aPos"),
                attribNormal = asteroidProgram.getAttributeLocation("aNormal"),
                attribTexCoords = asteroidProgram.getAttributeLocation("aTexCoords"),
                uniformDiffuseTexture = asteroidProgram.getUniformLocation("texture1")
            )
        )

        generateAsteroids()

        val instanceData = ByteBuffer.allocateDirect(asteroidModels.size * Mat4.sizeBytes())
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        asteroidModels.forEach { model ->
            instanceData.put(transpose(model).toFloatArray())
        }
        instanceData.position(0)

        val instanceDataId = genId { glGenBuffers(1, it) }
        glBindBuffer(GL_ARRAY_BUFFER, instanceDataId)
        glBufferData(
            GL_ARRAY_BUFFER,
            instanceData.capacity() * Float.SIZE_BYTES,
            instanceData,
            GL_STATIC_DRAW
        )

        asteroidModel.getMeshes().forEach { mesh ->
            glBindVertexArray(mesh.getVaoId())

            glEnableVertexAttribArray(3)
            glVertexAttribPointer(3, 4, GL_FLOAT, false, Mat4.numFloats() * Float.SIZE_BYTES, 0)
            glEnableVertexAttribArray(4)
            glVertexAttribPointer(
                4,
                4,
                GL_FLOAT,
                false,
                Mat4.numFloats() * Float.SIZE_BYTES,
                4 * Float.SIZE_BYTES
            )
            glEnableVertexAttribArray(5)
            glVertexAttribPointer(
                5,
                4,
                GL_FLOAT,
                false,
                Mat4.numFloats() * Float.SIZE_BYTES,
                2 * 4 * Float.SIZE_BYTES
            )
            glEnableVertexAttribArray(6)
            glVertexAttribPointer(
                6,
                4,
                GL_FLOAT,
                false,
                Mat4.numFloats() * Float.SIZE_BYTES,
                3 * 4 * Float.SIZE_BYTES
            )

            glVertexAttribDivisor(3, 1)
            glVertexAttribDivisor(4, 1)
            glVertexAttribDivisor(5, 1)
            glVertexAttribDivisor(6, 1)

            glBindVertexArray(0)
        }.also {
            glBindBuffer(GL_ARRAY_BUFFER, 0)
        }

        planetProgram = Program.create(planetVertexShaderCode, fragmentShaderCode)
        planetProgram.use()
        planetProgram.setMat4("model", scale(Float3(4.0f)))
        planetProgram.setMat4("projection", projection)

        planetModel.bind(
            planetProgram, ProgramLocations(
                attribPosition = planetProgram.getAttributeLocation("aPos"),
                attribNormal = planetProgram.getAttributeLocation("aNormal"),
                attribTexCoords = planetProgram.getAttributeLocation("aTexCoords"),
                uniformDiffuseTexture = planetProgram.getUniformLocation("texture1")
            )
        )
    }

    private fun generateAsteroids() {
        val radius = 50.0f
        val offset = 10.0
        val nextOffset = { Random.nextDouble(-offset, offset).toFloat() }

        for (i in 1..numAsteroids) {
            // 1. Translation: Displace along circle with radius in range [-offset, offset]
            val angle = i / 1000.0f * 360.0f
            val x = sin(angle) * radius + nextOffset()
            val y = nextOffset()
            val z = cos(angle) * radius + nextOffset()
            var model = translation(Float3(x, y, z))

            // 2. Scale between 0.05 and 0.25
            val scale = Random.nextDouble(0.05, 0.25).toFloat()
            model *= scale(Float3(scale))

            // 3. Add random rotation around a semi-randomly picked rotation axis vector
            val rotAngle = Random.nextInt(0, 360).toFloat()
            model *= rotation(normalize(Float3(0.4f, 0.6f, 0.8f)), rotAngle)

            // Now add to list of matrices
            asteroidModels.add(model)
        }
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene142InstancingAsteroidField(
                asteroidVertexShaderCode = resources.readRawTextFile(R.raw.advancedopengl_scene142_instancing_asteroid_field_asteroid_vert),
                planetVertexShaderCode = resources.readRawTextFile(R.raw.simple_model_mvp_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.simple_texture_frag),
                asteroidModel = ObjLoader.fromAssets(
                    context,
                    directory = "rock",
                    objFileName = "rock.obj",
                    mtlFileName = "rock.mtl"
                ),
                planetModel = ObjLoader.fromAssets(
                    context,
                    directory = "planet",
                    objFileName = "planet.obj",
                    mtlFileName = "planet.mtl"
                )
            )
        }
    }
}
