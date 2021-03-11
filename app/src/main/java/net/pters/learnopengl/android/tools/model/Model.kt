package net.pters.learnopengl.android.tools.model

import net.pters.learnopengl.android.tools.Program

class Model(private val meshes: List<Mesh>) {

    fun bind(program: Program, locations: ProgramLocations) {
        meshes.forEach { it.bind(program, locations) }
    }

    fun draw() {
        meshes.forEach {
            it.setTexturesAndUniforms()
            it.draw()
        }
    }

    fun getPrimaryMaterial() = meshes.first().material
}
