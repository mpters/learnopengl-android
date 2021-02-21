package net.pters.learnopengl.android.tools.model

import com.curiouscreature.kotlin.math.Float3
import net.pters.learnopengl.android.tools.Texture

data class Material(
    val ambientColor: Float3,
    val diffuseColor: Float3,
    val specularColor: Float3,
    val diffuseTexture: Texture?,
    val specularTexture: Texture?
)
