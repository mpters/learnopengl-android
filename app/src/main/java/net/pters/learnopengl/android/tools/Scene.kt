package net.pters.learnopengl.android.tools

abstract class Scene {

    protected val timer = Timer()

    abstract fun draw()

    abstract fun init(width: Int, height: Int)

    open fun getCamera(): Camera? = null

    open fun getInputTracker(): InputTracker? = null

    fun postInit() {
        timer.tick()
    }

    open fun preDraw() {
        getCamera()?.advance(timer.sinceLastFrameSecs())
        timer.tick()
    }
}
