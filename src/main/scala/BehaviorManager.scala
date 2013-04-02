package com.github.whelmaze.bulletf

import scala.io.Source
import script.DSFParser
import scala.collection.mutable

class BehaviorManager(ship: Ship) {

  val map = mutable.HashMap.empty[Symbol, Behavior]
  init()

  def init() = {
    map += ('simple -> BasicBehavior)
  }

  def build(ref: String) {
    val src = Source.fromFile( Resource.scriptPath + ref + ".dsf" ).mkString("")
    
    DSFParser.parse(src) foreach { case (name, ops) =>
      val action = new ScriptBehavior(this, ops)
      println("action[" + name.name + "] created.")
      map += ( name -> action )
    }
  }
  
  def get(ref: Symbol): Behavior = map.get(ref) match {
    // 目的のアクションがなかった場合どうする？
    // →buildの時点で依存するアクションはロードしておくべき．
    // あとで．
    case Some(x) => x
    case None => throw new Exception//build(ref.name)
  }

  def clear() = {
    map.clear()
    init()
  }

  def getAimToShip(from: Position) = {
    import scala.math._
    toDegrees( atan2(ship.pos.y - from.y, ship.pos.x - from.x) )
  }
  
}