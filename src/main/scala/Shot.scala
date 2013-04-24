package com.github.whelmaze.bulletf

class Shot(val action: Behavior, val resource: Symbol, var pos: Position, var angle: Angle, var speed: Double)
  extends BulletLike with HasCollision
{

  val sprite: Sprite = Sprite.get(resource)

  var waitCount = -1
  var waitingNest = 0

  val pc = copyPc
  val lc = copyLc
  val vars = copyVars
  var enable = true
  var time = 0

  val power = 2

  override def disable() {

  }

  // 当たり判定の半径
  val radius = sprite.rect.w / 4.0

  // スクリプトの実行が終わったら等速直線運動へシフト
  def onEndScript(delta: Int) {
    BasicBehavior.run(delta)(this)
  }

  def update(delta: Int) {
    if (enable) action.run(delta)(this)
    if (!inside) disable()
  }

  def draw() {
    if (enable) sprite.draw(pos, angle.dir-90)
  }

  def inside = (0-(radius*2) <= pos.x  && pos.x <= constants.screenWidth+(radius*2) && 0-(radius*2) <= pos.y && pos.y <= constants.screenHeight+(radius*2))

}