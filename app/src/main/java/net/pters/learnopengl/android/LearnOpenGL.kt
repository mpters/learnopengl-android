package net.pters.learnopengl.android

import android.content.Context
import net.pters.learnopengl.android.scenes.advancedopengl.*
import net.pters.learnopengl.android.scenes.gettingstarted.*
import net.pters.learnopengl.android.scenes.lighting.*
import net.pters.learnopengl.android.scenes.modelloading.SceneBackpack
import net.pters.learnopengl.android.tools.Scene

object LearnOpenGL {

    val content by lazy {
        listOf(
            Section(
                "Getting started",
                listOf(
                    Chapter("Hello window") { Scene1HelloWindow.create() },
                    Chapter("Hello triangle") { context -> Scene2HelloTriangle.create(context) },
                    Chapter("Working with indices") { context -> Scene3Indices.create(context) },
                    Chapter("Shaders: Uniforms") { context -> Scene4ShadersUniforms.create(context) },
                    Chapter("Shaders: More attributes") { context ->
                        Scene5ShadersMoreAttributes.create(
                            context
                        )
                    },
                    Chapter("Textures") { context -> Scene6Textures.create(context) },
                    Chapter("Transformations") { context -> Scene7Transformations.create(context) },
                    Chapter("Coordinate systems") { context ->
                        Scene8CoordinateSystems.create(
                            context
                        )
                    },
                    Chapter("Camera") { context -> Scene9Camera.create(context) },
                )
            ),
            Section(
                "Lighting",
                listOf(
                    Chapter("Colors") { context -> Scene1Colors.create(context) },
                    Chapter("Basic lighting") { context -> Scene2BasicLighting.create(context) },
                    Chapter("Materials") { context -> Scene3Materials.create(context) },
                    Chapter("Lighting maps") { context -> Scene4LightingMaps.create(context) },
                    Chapter("Lighting casters: Directional light") { context ->
                        Scene5LightCastersDirectional.create(
                            context
                        )
                    },
                    Chapter("Light casters: Point lights") { context ->
                        Scene6LightCastersPoint.create(
                            context
                        )
                    },
                    Chapter("Light casters: Spotlight") { context ->
                        Scene7LightCastersSpotlight.create(
                            context
                        )
                    },
                    Chapter("Multiple lights") { context -> Scene8MultipleLights.create(context) },
                )
            ),
            Section(
                "Model loading",
                listOf(
                    Chapter("Survival guitar backpack") { context -> SceneBackpack.create(context) }
                )
            ),
            Section(
                "Advanced OpenGL",
                listOf(
                    Chapter("Depth testing") { context -> Scene1DepthTesting.create(context) },
                    Chapter("Depth buffer visualized") { context ->
                        Scene2DepthBufferVisualized.create(
                            context
                        )
                    },
                    Chapter("Stencil testing") { context -> Scene3StencilTesting.create(context) },
                    Chapter("Blending") { context -> Scene4Blending.create(context) },
                    Chapter("Sorted blending") { context -> Scene5SortedBlending.create(context) },
                    Chapter("Face culling") { context -> Scene6FaceCulling.create(context) },
                    Chapter("Framebuffers") { context -> Scene7Framebuffers.create(context) },
                    Chapter("Cubemaps: Skybox") { context -> Scene8CubemapsSkybox.create(context) },
                    Chapter("Cubemaps: Environment mapping") { context ->
                        Scene9CubemapsEnvironmentMapping.create(context)
                    },
                    Chapter("Uniform buffer objects") { context ->
                        Scene10UniformBufferObjects.create(context)
                    },
                    Chapter("Geometry shaders: Houses (ES 3.2 only)") { context ->
                        Scene11GeometryShadersHouses.create(context)
                    },
                    Chapter("Geometry shaders: Exploding objects (ES 3.2 only)") { context ->
                        Scene12GeometryShadersExplodingObjects.create(context)
                    }
                )
            )
        )
    }
}

data class Chapter(val title: String, val createScene: (context: Context) -> Scene)

data class Section(val title: String, val chapters: List<Chapter>)
