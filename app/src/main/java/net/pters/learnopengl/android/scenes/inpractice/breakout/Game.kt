package net.pters.learnopengl.android.scenes.inpractice.breakout

class Game(
    contextProvider: ResourceManager.ContextProvider,
    private val width: Int,
    private val height: Int
) {

    private val resourceManager = ResourceManager(contextProvider)

    private var state = State.WIN

    fun init() {}

    fun processInput(deltaSecs: Float) {}

    fun update(deltaSecs: Float) {}

    fun render() {}

    enum class State { ACTIVE, MENU, WIN }
}
