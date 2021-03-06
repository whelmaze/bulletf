package bulletf

import org.lwjgl.opengl._
import org.lwjgl.input.Controllers

object Game {

  private[this] var on3d: Boolean = false
  private[Game] var _width: Int = 0
  private[Game] var _height: Int = 0
  def width = _width
  def height = _height

  def clearScreen() {
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)
  }

}

class Game(_width: Int, _height: Int) {
  var fps: Int = 60
  var a: Int = 0
  private[this] var lastFrame: Long = 0
  private[this] var fpscount: Int = 0
  private[this] var lastFPS: Long = 0

  Game._width = _width
  Game._height = _height

  def start() {
    initGL()
    //DrawerPlus.setupOpenGL()
    DrawerPlus.setupShader()
    DrawerPlus.setupObjects()
    SceneController.init(new TestScene)
    SoundSystem.init()
    BGM.init()

    calcDelta()
    lastFPS = getTime

    GLUtil.printGLSpecs()

    while (true) {
      if (a%60==0) {
        val ta = System.nanoTime()
        SceneController.update()
        val elapse = (System.nanoTime() - ta) / 1000.0 / 1000.0
        println(f"update takes time: $elapse%e ms")
      } else {
        SceneController.update()
      }
      SoundSystem.update()
      
      updateFPS()

      Game.clearScreen()

      DrawerPlus.begin()

      if (a%60==0) {
        val ta = System.nanoTime()
        SceneController.draw()
        DrawerPlus.flush()
        val elasp = (System.nanoTime() - ta) / 1000.0 / 1000.0
        println(f"draw takes time: $elasp%e ms")
        println(s"sprite count: ${Sprite.count}")
        println(s"drawCalls: ${DrawerPlus.drawCalls}")
      } else {
        SceneController.draw()
      }

      DrawerPlus.end()

      Display.update()

      //sync(be)
      Display.sync(fps)
      a += 1; a %= 360
      if (Display.isCloseRequested) {
        println("free resources...")
        DrawerPlus.destroyAll()
        BGM.free()
        SoundSystem.free()
        TextureFactory.free()

        Display.destroy()

        System.exit(0)
      }
    }
  }

  final val WAIT: Int = 16650 // [us]
  // return overtime???
  def sync(before: Long) =  {
    var now = getTime
    while(now - before < WAIT) {
      Thread.sleep(0)
      now = getTime
    }
    //println("wait: " + (now - before))
  }

  private def initGL() {
    Display.setDisplayMode(new DisplayMode(Game.width,Game.height))
    //Display.setFullscreen(true);
    Display.setVSyncEnabled(true)
    Display.create()

    Controllers.create()
    val padCount = Controllers.getControllerCount
    println("padCount : " + padCount)
    (0 until padCount).foreach{ i =>
      val co = Controllers.getController(i)
      println("[%d] \"%s\" (%d)" format (i, co.getName, co.getButtonCount))
    }

    GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)          
    
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

  }

  def getTime: Long = System.nanoTime / 1000//(Sys.getTime() * 1000 / Sys.getTimerResolution())

  def calcDelta(): Int = {
    val time: Long = getTime
    val delta: Int = (time - lastFrame).toInt
    lastFrame = time
    delta
  }

  def updateFPS() {
    if (getTime - lastFPS > 1000000) {
      Display.setTitle("FPS: " + fpscount)
      fpscount = 0
      lastFPS += 1000000
    }
    fpscount += 1
  }

}
