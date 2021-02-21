package net.pters.learnopengl.android.tools.model

import android.content.Context
import android.graphics.BitmapFactory
import com.curiouscreature.kotlin.math.Float3
import de.javagl.obj.*
import net.pters.learnopengl.android.tools.Texture
import java.io.File

object ObjLoader {

    fun fromAssets(
        context: Context,
        directory: String,
        objFileName: String,
        mtlFileName: String,
        specularTextureFileName: String? = null
    ): Model {
        val obj = context.assets.open(File(directory, objFileName).path).use {
            ObjReader.read(it)
        }
        val materials = context.assets.open(File(directory, mtlFileName).path).use {
            MtlReader.read(it)
        }

        val meshes = mutableListOf<Mesh>()
        val textures = mutableMapOf<String, Texture>()

        ObjSplitting.splitByMaterialGroups(obj).forEach { (materialName, group) ->
            val material = materials.first { it.name == materialName }
            val renderableObj = ObjUtils.convertToRenderable(group)
            val diffuseTexture =
                material.mapKd?.let { createTexture(context, File(directory, it), textures) }
            val specularTexture = specularTextureFileName?.let {
                createTexture(
                    context,
                    File(directory, it),
                    textures
                )
            }

            val mesh = Mesh(
                vertices = ObjData.getVertices(renderableObj),
                normals = ObjData.getNormals(renderableObj),
                texCoords = ObjData.getTexCoords(renderableObj, 2),
                indices = ObjData.getFaceVertexIndices(renderableObj),
                material = Material(
                    ambientColor = material.ka.toFloat3(),
                    diffuseColor = material.kd.toFloat3(),
                    specularColor = material.ks.toFloat3(),
                    diffuseTexture = diffuseTexture,
                    specularTexture = specularTexture
                )
            )
            meshes.add(mesh)
        }

        return Model(meshes)
    }

    private fun createTexture(
        context: Context,
        file: File,
        knownTextures: MutableMap<String, Texture>
    ) = knownTextures[file.path] ?: run {
        context.assets.open(file.path).use {
            BitmapFactory.decodeStream(
                it,
                null,
                BitmapFactory.Options().apply { inScaled = false })?.let { bitmap ->
                Texture(bitmap).also { texture ->
                    knownTextures[file.path] = texture
                }
            }
        }
    }
}

private fun FloatTuple.toFloat3() = Float3(x, y, z)
