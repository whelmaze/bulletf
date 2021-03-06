package bulletf



import Constants.script._

object MoveStrategy {

  def move(unit: Enemy, status: MoveStrategyStatus) {
    status.handling match {
      case "normal" =>
        normal(unit, status)
      case "easing" =>
        easing(unit, status)
      case "accel" =>
        accel(unit, status)
      case "brake" =>
        brake(unit, status)
      case "testsin" =>
        sin(unit, status)
      case _ => // nop
    }
  }

  // nフレームで速度0になる
  private[this] def brake(unit: Enemy, status: MoveStrategyStatus) {
    import status._

    unit.speed -= (unit.speed / status.restFrame.toDouble)

    restFrame -= 1
    BasicBehavior.run(unit)
    posUpdated = true
  }

  // nフレームで速度Xになる (vars(0) : 速度X)
  private[this] def accel(unit: Enemy, status: MoveStrategyStatus) {
    import status._

    unit.speed += ((safetyVar(0,1.0) - unit.speed) / status.restFrame)

    restFrame -= 1
    BasicBehavior.run(unit)
    posUpdated = true
  }

  // vars(0) : 減衰率
  private[this] def easing(unit: Enemy, status: MoveStrategyStatus) {
    import status._
    //safetyVar(0, 0.2)
    val decel = 1.0 - math.exp(-6.0 * (16 / 1000.0))

    unit.pos.y += (target.y - unit.pos.y) * decel
    unit.pos.x += (target.x - unit.pos.x) * decel

    restFrame -= 1

    posUpdated = true
  }

  // sin移動
  private[this] def sin(unit: Enemy, status: MoveStrategyStatus) {
    import status._

    unit.pos.y += 1.0
    unit.pos.x = math.cos(unit.pos.y/13.0) * 6 * 16 + 300 // 16 = old_delta
    posUpdated = true
  }

  /**
   * 指定位置に等速直線運動
   * pos直接いじる系
   */
  private[this] def normal(unit: Enemy, status: MoveStrategyStatus) {
    import  status._

    val ax = target.x - unit.pos.x
    val ay = target.y - unit.pos.y
    unit.pos.x += ax / restFrame
    unit.pos.y += ay / restFrame
    restFrame -= 1
    posUpdated = true
  }

}

object MoveStrategyStatus {
  def nop = new MoveStrategyStatus(Position(0,0), "nop", 0, false)
}

// handlingはsealed traitにした方がよくね
class MoveStrategyStatus(val target: Position, var handling: String, var restFrame: Int, var async: Boolean, var posUpdated: Boolean = false) {

  val vars: Array[Double] = {
    val dest = Array.ofDim[Double](MAX_NEST)
    Array.copy(ScriptControlled.initialVars, 0, dest, 0, MAX_NEST)
    dest
  }

  def safetyVar(idx: Int, default: Double = 0.0): Double = {
    if (idx < 0 || vars.length <= idx) default
    else vars(idx)
  }

  def updated() {
    posUpdated = false
  }

  def clearVar() {
    var i = vars.length-1
    while(0 <= i) {
      vars(i) = 0.0
      i -= 1
    }
  }

  override def toString = s"MoveStrategyStatus($target, $handling, $restFrame, async:$async)"
}
