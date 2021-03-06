package net.pters.learnopengl.android

import android.content.Context
import net.pters.learnopengl.android.scenes.advancedlighting.*
import net.pters.learnopengl.android.scenes.advancedopengl.*
import net.pters.learnopengl.android.scenes.gettingstarted.*
import net.pters.learnopengl.android.scenes.inpractice.Scene1TextRendering
import net.pters.learnopengl.android.scenes.inpractice.breakout.GameScene
import net.pters.learnopengl.android.scenes.lighting.*
import net.pters.learnopengl.android.scenes.modelloading.SceneBackpack
import net.pters.learnopengl.android.scenes.pbr.Scene11Lighting
import net.pters.learnopengl.android.scenes.pbr.Scene12LightingTextured
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
                    Chapter("Camera") { context -> Scene9Camera.create(context) }
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
                    Chapter("Multiple lights") { context -> Scene8MultipleLights.create(context) }
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
                    Chapter("Geometry shaders (ES 3.2 only): Houses") { context ->
                        Scene11GeometryShadersHouses.create(context)
                    },
                    Chapter("Geometry shaders (ES 3.2 only): Exploding objects") { context ->
                        Scene12GeometryShadersExplodingObjects.create(context)
                    },
                    Chapter("Geometry shaders (ES 3.2 only): Visualizing normals") { context ->
                        Scene13GeometryShadersVisualizingNormals.create(context)
                    },
                    Chapter("Instancing: Quads") { context -> Scene141InstancingQuads.create(context) },
                    Chapter("Instancing: Asteroid field") { context ->
                        Scene142InstancingAsteroidField.create(
                            context
                        )
                    },
                    Chapter("Anti-aliasing: Off-screen (ES 3.1 only)") { context ->
                        Scene15AntiAliasingOffScreen.create(
                            context
                        )
                    }
                )
            ),
            Section(
                "Advanced lighting",
                listOf(
                    Chapter("Blinn-Phong") { context -> Scene1BlinnPhong.create(context) },
                    Chapter("Gamma correction") { context -> Scene2GammaCorrection.create(context) },
                    Chapter("Shadows: Shadow mapping") { context ->
                        Scene31ShadowMapping.create(context)
                    },
                    Chapter("Shadows: Point shadows") { context ->
                        Scene32PointShadows.create(context)
                    },
                    Chapter("Normal mapping") { context -> Scene4NormalMapping.create(context) },
                    Chapter("Parallax mapping") { context -> Scene51ParallaxMapping.create(context) },
                    Chapter("Steep parallax mapping") { context ->
                        Scene52SteepParallaxMapping.create(
                            context
                        )
                    },
                    Chapter("Parallax occlusion mapping") { context ->
                        Scene53ParallaxOcclusionMapping.create(
                            context
                        )
                    },
                    Chapter("HDR") { context -> Scene6HDR.create(context) },
                    Chapter("Bloom") { context -> Scene7Bloom.create(context) },
                    Chapter("Deferred shading") { context -> Scene81DeferredShading.create(context) },
                    Chapter("Deferred shading: Volumes") { context ->
                        Scene82DeferredShadingVolumes.create(
                            context
                        )
                    },
                    Chapter("SSAO") { context -> Scene9SSAO.create(context) }
                )
            ),
            Section(
                "PBR",
                listOf(
                    Chapter("Lighting") { context -> Scene11Lighting.create(context) },
                    Chapter("Lighting: Textured") { context ->
                        Scene12LightingTextured.create(
                            context
                        )
                    }
                )
            ),
            Section(
                "In practice",
                listOf(
                    Chapter("Text rendering") { context -> Scene1TextRendering.create(context) },
                    Chapter("2D game: Breakout") { context -> GameScene.create(context) }
                )
            )
        )
    }
}

data class Chapter(val title: String, val createScene: (context: Context) -> Scene)

data class Section(val title: String, val chapters: List<Chapter>)
