package net.pters.learnopengl.android.tools.model

import net.pters.learnopengl.android.tools.Program

class Model(private val meshes: List<Mesh>) {

    fun bind(program: Program, locations: ProgramLocations) {
        meshes.forEach { it.bind(program, locations) }
    }

    fun getMeshes() = meshes

    fun draw() {
        meshes.forEach {
            it.setTexturesAndUniforms()
            it.draw()
        }
    }

    fun drawInstanced(count: Int) {
        meshes.forEach {
            it.setTexturesAndUniforms()
            it.drawInstanced(count)
        }
    }

    fun getPrimaryMaterial() = meshes.first().material
}
