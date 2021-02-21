package net.pters.learnopengl.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import net.pters.learnopengl.android.LearnOpenGL
import net.pters.learnopengl.android.tools.Scene
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SceneActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sectionIndex = intent.getIntExtra(EXTRA_SECTION_INDEX, -1)
        val chapterIndex = intent.getIntExtra(EXTRA_CHAPTER_INDEX, -1)
        val chapter = LearnOpenGL.content[sectionIndex].chapters[chapterIndex]

        title = chapter.title

        val glView = object : GLSurfaceView(this) {
            init {
                val scene = chapter.createScene(context)
                setEGLContextClientVersion(3)
                setOnTouchListener(TouchListener(context, scene))
                setRenderer(GLRenderer(scene))
            }
        }

        setContentView(glView)
    }

    private class GLRenderer(private val scene: Scene) : GLSurfaceView.Renderer {

        override fun onSurfaceCreated(unused: GL10, config: EGLConfig) = Unit

        override fun onDrawFrame(unused: GL10) {
            scene.preDraw()
            scene.draw()
        }

        override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
            scene.init(width, height)
            scene.postInit()
        }
    }

    private class TouchListener(
        context: Context,
        private val scene: Scene
    ) : View.OnTouchListener {

        private var isScaling: Boolean = false

        private val scaleDetector =
            ScaleGestureDetector(context, object : ScaleGestureDetector.OnScaleGestureListener {
                override fun onScale(detector: ScaleGestureDetector?): Boolean {
                    detector?.scaleFactor?.also { scene.getCamera()?.move(it - 1.0f) }
                    return true
                }

                override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                    isScaling = true
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector?) {
                    isScaling = false
                }
            })

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            scaleDetector.onTouchEvent(event)

            return when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    scene.getCamera()?.startLooking(event.x, event.y)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isScaling) {
                        scene.getCamera()?.lookAround(event.x, event.y)
                    }
                    true
                }
                MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_UP -> {
                    scene.getCamera()?.halt()
                    true
                }
                else -> false
            }
        }
    }

    companion object {

        private const val EXTRA_SECTION_INDEX = "section_index"

        private const val EXTRA_CHAPTER_INDEX = "chapter_index"

        fun createIntent(context: Context, sectionIndex: Int, chapterIndex: Int) =
            Intent(context, SceneActivity::class.java).apply {
                putExtra(EXTRA_SECTION_INDEX, sectionIndex)
                putExtra(EXTRA_CHAPTER_INDEX, chapterIndex)
            }
    }
}
