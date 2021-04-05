package net.pters.learnopengl.android.tools.model

import net.pters.learnopengl.android.tools.Program

data class ProgramLocations(
    val attribPosition: Int,
    val attribNormal: Int? = null,
    val attribTexCoords: Int,
    val uniformDiffuseTexture: Int?,
    val uniformSpecularTexture: Int? = null,
    val uniformShininess: Int? = null
)

internal object DefaultProgramLocations {

    fun resolve(program: Program): ProgramLocations = ProgramLocations(
        attribPosition = program.getAttributeLocation("aPos"),
        attribNormal = program.getAttributeLocation("aNormal"),
        attribTexCoords = program.getAttributeLocation("aTexCoords"),
        uniformDiffuseTexture = program.getUniformLocation("material.diffuseTexture"),
        uniformSpecularTexture = program.getUniformLocation("material.specularTexture"),
        uniformShininess = program.getUniformLocation("material.shininess")
    )
}
