package net.pters.learnopengl.android.scenes.inpractice.breakout

import android.content.Context
import androidx.annotation.RawRes
import net.pters.learnopengl.android.tools.Program
import net.pters.learnopengl.android.tools.Texture
import net.pters.learnopengl.android.tools.loadBitmap
import net.pters.learnopengl.android.tools.readRawTextFile

class ResourceManager(private val contextProvider: ContextProvider) {

    private val programs = mutableMapOf<String, Program>()

    private val textures = mutableMapOf<String, Texture>()

    fun getProgram(name: String) = programs.getValue(name)

    fun getTexture(name: String) = textures.getValue(name)

    fun loadProgram(name: String, @RawRes vertShaderId: Int, @RawRes fragShaderId: Int): Program {
        val resources = contextProvider.getContext().resources
        val program = Program.create(
            resources.readRawTextFile(vertShaderId),
            resources.readRawTextFile(fragShaderId)
        )
        programs[name] = program
        return program
    }

    fun loadTexture(name: String, @RawRes textureId: Int): Texture {
        val bitmap = loadBitmap(contextProvider.getContext(), textureId)
        val texture = Texture(bitmap)
        texture.load()
        textures[name] = texture
        return texture
    }

    fun readTextFile(@RawRes textFileId: Int) =
        contextProvider.getContext().resources.readRawTextFile(textFileId)

    interface ContextProvider {
        fun getContext(): Context
    }
}
