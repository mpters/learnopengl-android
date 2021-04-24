package net.pters.learnopengl.android.scenes.inpractice

import android.content.Context
import android.opengl.GLES30.*
import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.ortho
import net.pters.learnopengl.android.R
import net.pters.learnopengl.android.tools.*

class Scene1TextRendering private constructor(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val fontTexture: Texture
) : Scene() {

    private val characters = mutableMapOf<Char, CharacterInfo>()

    private var charVaoId = -1

    private var charVboId = -1

    private lateinit var program: Program

    override fun draw() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        renderText("This is sample text", Float2(100.0f, 100.0f), 2.0f, Float3(0.5f, 0.8f, 0.2f))
        renderText("(C) LearnOpenGL.com", Float2(500.0f, 500.0f), 1.0f, Float3(0.3f, 0.7f, 0.9f))
    }

    override fun init(width: Int, height: Int) {
        glEnable(GL_CULL_FACE)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glViewport(0, 0, width, height)

        program = Program.create(vertexShaderCode, fragmentShaderCode)
        program.use()
        val projection =
            ortho(0.0f, width.toFloat(), 0.0f, height.toFloat(), 0.0f, 10.0f)
        program.setMat4("projection", projection)

        fontTexture.load()

        initCharacters()

        charVaoId = genId { glGenVertexArrays(1, it) }
        charVboId = genId { glGenBuffers(1, it) }
        glBindVertexArray(charVaoId)
        glBindBuffer(GL_ARRAY_BUFFER, charVboId)
        glBufferData(GL_ARRAY_BUFFER, 6 * 4 * Float.SIZE_BYTES, null, GL_DYNAMIC_DRAW)
        glEnableVertexAttribArray(program.getAttributeLocation("vertex"))
        glVertexAttribPointer(
            program.getAttributeLocation("vertex"),
            4,
            GL_FLOAT,
            false,
            4 * Float.SIZE_BYTES,
            0
        )
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    private fun initCharacters() {
        fntInfo.lines().forEach { line ->
            val components = line.split(Regex("\\s+"))
            val char = components[1].split("=")[1].toInt().toChar()
            val x = components[2].split("=")[1].toFloat()
            val y = components[3].split("=")[1].toFloat()
            val height = components[4].split("=")[1].toFloat()
            val width = components[5].split("=")[1].toFloat()
            val xoffset = components[6].split("=")[1].toFloat()
            val yoffset = components[7].split("=")[1].toFloat()
            val xadvance = components[8].split("=")[1].toFloat()

            characters[char] = CharacterInfo(
                size = Float2(height, width),
                offset = Float2(xoffset, yoffset),
                advance = xadvance,
                texSize = Float2(256.0f),
                texPosition = Float2(x, y)
            )
        }
    }

    private fun renderText(text: String, position: Float2, scale: Float, color: Float3) {
        program.setFloat3("textColor", color)
        glBindVertexArray(charVaoId)

        text.forEach {
            val c = characters.getValue(it)
            val x = position.x + c.offset.x * scale
            val y = position.y - (c.size.y + c.offset.y) * scale
            val width = c.size.x * scale
            val height = c.size.y * scale

            val vertices = floatArrayOf(
                x, y + height, c.texCoordsLowerLeft.x, c.texCoordsLowerLeft.y,
                x, y, c.texCoordsUpperLeft.x, c.texCoordsUpperLeft.y,
                x + width, y, c.texCoordsUpperRight.x, c.texCoordsUpperRight.y,
                x, y + height, c.texCoordsLowerLeft.x, c.texCoordsLowerLeft.y,
                x + width, y, c.texCoordsUpperRight.x, c.texCoordsUpperRight.y,
                x + width, y + height, c.texCoordsLowerRight.x, c.texCoordsLowerRight.y,
            )

            val buffer = vertices.toFloatBuffer()
            glBindBuffer(GL_ARRAY_BUFFER, charVboId)
            glBufferSubData(GL_ARRAY_BUFFER, 0, Float.SIZE_BYTES * buffer.capacity(), buffer)
            glBindBuffer(GL_ARRAY_BUFFER, 0)
            glDrawArrays(GL_TRIANGLES, 0, 6)
            position.x = position.x + c.advance * scale
        }

        glBindVertexArray(0)
    }

    companion object {

        fun create(context: Context): Scene {
            val resources = context.resources
            return Scene1TextRendering(
                vertexShaderCode = resources.readRawTextFile(R.raw.inpractice_scene1_text_rendering_vert),
                fragmentShaderCode = resources.readRawTextFile(R.raw.inpractice_scene1_text_rendering_frag),
                Texture(loadBitmap(context, R.raw.texture_font_arial))
            )
        }
    }

    private data class CharacterInfo(
        val size: Float2,
        val offset: Float2,
        val advance: Float,
        private val texSize: Float2,
        private val texPosition: Float2,
    ) {

        val texCoordsLowerLeft: Float2 =
            Float2(texPosition.x / texSize.x, texPosition.y / texSize.y)
        val texCoordsUpperLeft: Float2 =
            Float2(texPosition.x / texSize.x, (texPosition.y + size.y) / texSize.y)
        val texCoordsUpperRight: Float2 =
            Float2((texPosition.x + size.x) / texSize.x, (texPosition.y + size.y) / texSize.y)
        val texCoordsLowerRight: Float2 =
            Float2((texPosition.x + size.x) / texSize.x, texPosition.y / texSize.y)
    }
}

// Generated with BMFont (https://angelcode.com/products/bmfont/)
val fntInfo = """
    char id=32   x=79    y=24    width=3     height=1     xoffset=-1    yoffset=31    xadvance=8     page=0  chnl=15
    char id=33   x=103   y=66    width=2     height=20    xoffset=3     yoffset=6     xadvance=8     page=0  chnl=15
    char id=34   x=110   y=82    width=7     height=7     xoffset=1     yoffset=6     xadvance=10    page=0  chnl=15
    char id=35   x=236   y=21    width=14    height=20    xoffset=0     yoffset=6     xadvance=15    page=0  chnl=15
    char id=36   x=79    y=0     width=13    height=23    xoffset=1     yoffset=5     xadvance=15    page=0  chnl=15
    char id=37   x=141   y=0     width=20    height=20    xoffset=2     yoffset=6     xadvance=24    page=0  chnl=15
    char id=38   x=88    y=24    width=16    height=20    xoffset=1     yoffset=6     xadvance=18    page=0  chnl=15
    char id=39   x=118   y=82    width=2     height=7     xoffset=1     yoffset=6     xadvance=5     page=0  chnl=15
    char id=40   x=51    y=0     width=6     height=25    xoffset=2     yoffset=6     xadvance=9     page=0  chnl=15
    char id=41   x=44    y=0     width=6     height=25    xoffset=1     yoffset=6     xadvance=9     page=0  chnl=15
    char id=42   x=99    y=89    width=10    height=8     xoffset=1     yoffset=6     xadvance=11    page=0  chnl=15
    char id=43   x=61    y=89    width=12    height=12    xoffset=1     yoffset=10    xadvance=16    page=0  chnl=15
    char id=44   x=121   y=82    width=2     height=6     xoffset=2     yoffset=24    xadvance=8     page=0  chnl=15
    char id=45   x=162   y=79    width=7     height=2     xoffset=1     yoffset=18    xadvance=9     page=0  chnl=15
    char id=46   x=170   y=79    width=2     height=2     xoffset=2     yoffset=24    xadvance=8     page=0  chnl=15
    char id=47   x=71    y=68    width=8     height=20    xoffset=0     yoffset=6     xadvance=8     page=0  chnl=15
    char id=48   x=59    y=47    width=13    height=20    xoffset=1     yoffset=6     xadvance=15    page=0  chnl=15
    char id=49   x=248   y=42    width=7     height=20    xoffset=3     yoffset=6     xadvance=15    page=0  chnl=15
    char id=50   x=183   y=42    width=12    height=20    xoffset=1     yoffset=6     xadvance=15    page=0  chnl=15
    char id=51   x=73    y=47    width=13    height=20    xoffset=1     yoffset=6     xadvance=15    page=0  chnl=15
    char id=52   x=87    y=47    width=13    height=20    xoffset=1     yoffset=6     xadvance=15    page=0  chnl=15
    char id=53   x=101   y=45    width=13    height=20    xoffset=1     yoffset=6     xadvance=15    page=0  chnl=15
    char id=54   x=45    y=47    width=13    height=20    xoffset=1     yoffset=6     xadvance=15    page=0  chnl=15
    char id=55   x=170   y=42    width=12    height=20    xoffset=1     yoffset=6     xadvance=15    page=0  chnl=15
    char id=56   x=115   y=43    width=13    height=20    xoffset=1     yoffset=6     xadvance=15    page=0  chnl=15
    char id=57   x=129   y=42    width=13    height=20    xoffset=1     yoffset=6     xadvance=15    page=0  chnl=15
    char id=58   x=32    y=90    width=2     height=15    xoffset=3     yoffset=11    xadvance=8     page=0  chnl=15
    char id=59   x=106   y=66    width=2     height=19    xoffset=3     yoffset=11    xadvance=8     page=0  chnl=15
    char id=60   x=48    y=90    width=12    height=13    xoffset=2     yoffset=10    xadvance=16    page=0  chnl=15
    char id=61   x=85    y=89    width=13    height=8     xoffset=1     yoffset=12    xadvance=16    page=0  chnl=15
    char id=62   x=35    y=90    width=12    height=13    xoffset=2     yoffset=10    xadvance=16    page=0  chnl=15
    char id=63   x=143   y=42    width=13    height=20    xoffset=1     yoffset=6     xadvance=15    page=0  chnl=15
    char id=64   x=0     y=0     width=25    height=26    xoffset=1     yoffset=6     xadvance=27    page=0  chnl=15
    char id=65   x=18    y=27    width=17    height=20    xoffset=0     yoffset=6     xadvance=18    page=0  chnl=15
    char id=66   x=172   y=21    width=15    height=20    xoffset=2     yoffset=6     xadvance=18    page=0  chnl=15
    char id=67   x=182   y=0     width=18    height=20    xoffset=1     yoffset=6     xadvance=20    page=0  chnl=15
    char id=68   x=0     y=27    width=17    height=20    xoffset=2     yoffset=6     xadvance=20    page=0  chnl=15
    char id=69   x=156   y=21    width=15    height=20    xoffset=2     yoffset=6     xadvance=18    page=0  chnl=15
    char id=70   x=30    y=48    width=14    height=20    xoffset=2     yoffset=6     xadvance=17    page=0  chnl=15
    char id=71   x=201   y=0     width=18    height=20    xoffset=1     yoffset=6     xadvance=21    page=0  chnl=15
    char id=72   x=204   y=21    width=15    height=20    xoffset=2     yoffset=6     xadvance=19    page=0  chnl=15
    char id=73   x=97    y=68    width=2     height=20    xoffset=3     yoffset=6     xadvance=8     page=0  chnl=15
    char id=74   x=51    y=68    width=10    height=20    xoffset=1     yoffset=6     xadvance=13    page=0  chnl=15
    char id=75   x=54    y=26    width=16    height=20    xoffset=2     yoffset=6     xadvance=18    page=0  chnl=15
    char id=76   x=157   y=42    width=12    height=20    xoffset=2     yoffset=6     xadvance=15    page=0  chnl=15
    char id=77   x=220   y=0     width=17    height=20    xoffset=3     yoffset=6     xadvance=23    page=0  chnl=15
    char id=78   x=188   y=21    width=15    height=20    xoffset=2     yoffset=6     xadvance=19    page=0  chnl=15
    char id=79   x=162   y=0     width=19    height=20    xoffset=1     yoffset=6     xadvance=21    page=0  chnl=15
    char id=80   x=0     y=48    width=14    height=20    xoffset=2     yoffset=6     xadvance=17    page=0  chnl=15
    char id=81   x=93    y=0     width=19    height=21    xoffset=1     yoffset=6     xadvance=21    page=0  chnl=15
    char id=82   x=139   y=21    width=16    height=20    xoffset=2     yoffset=6     xadvance=20    page=0  chnl=15
    char id=83   x=122   y=21    width=16    height=20    xoffset=1     yoffset=6     xadvance=18    page=0  chnl=15
    char id=84   x=15    y=48    width=14    height=20    xoffset=1     yoffset=6     xadvance=16    page=0  chnl=15
    char id=85   x=220   y=21    width=15    height=20    xoffset=2     yoffset=6     xadvance=19    page=0  chnl=15
    char id=86   x=36    y=26    width=17    height=20    xoffset=0     yoffset=6     xadvance=17    page=0  chnl=15
    char id=87   x=113   y=0     width=27    height=20    xoffset=0     yoffset=6     xadvance=28    page=0  chnl=15
    char id=88   x=238   y=0     width=17    height=20    xoffset=0     yoffset=6     xadvance=17    page=0  chnl=15
    char id=89   x=105   y=22    width=16    height=20    xoffset=1     yoffset=6     xadvance=18    page=0  chnl=15
    char id=90   x=71    y=26    width=16    height=20    xoffset=0     yoffset=6     xadvance=17    page=0  chnl=15
    char id=91   x=64    y=0     width=5     height=25    xoffset=2     yoffset=6     xadvance=8     page=0  chnl=15
    char id=92   x=62    y=68    width=8     height=20    xoffset=0     yoffset=6     xadvance=8     page=0  chnl=15
    char id=93   x=58    y=0     width=5     height=25    xoffset=1     yoffset=6     xadvance=8     page=0  chnl=15
    char id=94   x=74    y=89    width=10    height=10    xoffset=1     yoffset=6     xadvance=12    page=0  chnl=15
    char id=95   x=145   y=79    width=16    height=2     xoffset=-1    yoffset=29    xadvance=15    page=0  chnl=15
    char id=96   x=139   y=79    width=5     height=4     xoffset=1     yoffset=6     xadvance=9     page=0  chnl=15
    char id=97   x=148   y=63    width=13    height=15    xoffset=1     yoffset=11    xadvance=15    page=0  chnl=15
    char id=98   x=196   y=42    width=12    height=20    xoffset=2     yoffset=6     xadvance=15    page=0  chnl=15
    char id=99   x=217   y=63    width=12    height=15    xoffset=1     yoffset=11    xadvance=14    page=0  chnl=15
    char id=100  x=209   y=42    width=12    height=20    xoffset=1     yoffset=6     xadvance=15    page=0  chnl=15
    char id=101  x=162   y=63    width=13    height=15    xoffset=1     yoffset=11    xadvance=15    page=0  chnl=15
    char id=102  x=80    y=68    width=8     height=20    xoffset=0     yoffset=6     xadvance=7     page=0  chnl=15
    char id=103  x=222   y=42    width=12    height=20    xoffset=1     yoffset=11    xadvance=15    page=0  chnl=15
    char id=104  x=39    y=69    width=11    height=20    xoffset=2     yoffset=6     xadvance=15    page=0  chnl=15
    char id=105  x=251   y=21    width=2     height=20    xoffset=2     yoffset=6     xadvance=6     page=0  chnl=15
    char id=106  x=70    y=0     width=5     height=25    xoffset=-1    yoffset=6     xadvance=6     page=0  chnl=15
    char id=107  x=235   y=42    width=12    height=20    xoffset=2     yoffset=6     xadvance=14    page=0  chnl=15
    char id=108  x=100   y=68    width=2     height=20    xoffset=2     yoffset=6     xadvance=6     page=0  chnl=15
    char id=109  x=129   y=63    width=18    height=15    xoffset=2     yoffset=11    xadvance=22    page=0  chnl=15
    char id=110  x=0     y=90    width=11    height=15    xoffset=2     yoffset=11    xadvance=15    page=0  chnl=15
    char id=111  x=190   y=63    width=13    height=15    xoffset=1     yoffset=11    xadvance=15    page=0  chnl=15
    char id=112  x=0     y=69    width=12    height=20    xoffset=2     yoffset=11    xadvance=15    page=0  chnl=15
    char id=113  x=13    y=69    width=12    height=20    xoffset=1     yoffset=11    xadvance=15    page=0  chnl=15
    char id=114  x=24    y=90    width=7     height=15    xoffset=2     yoffset=11    xadvance=9     page=0  chnl=15
    char id=115  x=243   y=63    width=12    height=15    xoffset=1     yoffset=11    xadvance=14    page=0  chnl=15
    char id=116  x=89    y=68    width=7     height=20    xoffset=0     yoffset=6     xadvance=8     page=0  chnl=15
    char id=117  x=12    y=90    width=11    height=15    xoffset=2     yoffset=11    xadvance=15    page=0  chnl=15
    char id=118  x=176   y=63    width=13    height=15    xoffset=0     yoffset=11    xadvance=13    page=0  chnl=15
    char id=119  x=109   y=66    width=19    height=15    xoffset=0     yoffset=11    xadvance=19    page=0  chnl=15
    char id=120  x=230   y=63    width=12    height=15    xoffset=0     yoffset=11    xadvance=12    page=0  chnl=15
    char id=121  x=26    y=69    width=12    height=20    xoffset=1     yoffset=11    xadvance=14    page=0  chnl=15
    char id=122  x=204   y=63    width=12    height=15    xoffset=0     yoffset=11    xadvance=13    page=0  chnl=15
    char id=123  x=26    y=0     width=8     height=25    xoffset=1     yoffset=6     xadvance=9     page=0  chnl=15
    char id=124  x=76    y=0     width=2     height=25    xoffset=2     yoffset=6     xadvance=6     page=0  chnl=15
    char id=125  x=35    y=0     width=8     height=25    xoffset=0     yoffset=6     xadvance=9     page=0  chnl=15
    char id=126  x=124   y=82    width=14    height=4     xoffset=1     yoffset=14    xadvance=16    page=0  chnl=15
""".trimIndent()
