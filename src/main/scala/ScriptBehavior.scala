package com.github.whelmaze.bulletf

import com.github.whelmaze.bulletf.script._
import scala.collection.mutable.ListBuffer

import scala.annotation.tailrec

class ScriptBehavior(val manager: BehaviorManager, val topseq: Array[Op]) extends Behavior {
  import implicits.angle2double

  def extract(manager: BehaviorManager, unit: ScriptedMove)(c: Container): Double = c match {

    case StateVar(p) => p match {
      case 'x => unit.pos.x
      case 'y => unit.pos.y
      case 'aim => manager.getAimToShip(unit.pos)
      case 'speed => unit.speed
      case 'angle => unit.angle
    }

    // StrVarは解析時点でSymbolに変換するからこの時点では存在しない
    case EVar(getvar) => unit.vars(getvar.idx)
    case DVar(value) => value
    case RandomVar(begin, end) => scala.util.Random.nextDouble()*(end-begin)+begin
    case Negate(in) => extract(manager, unit)(in) * -1
    
  }
  
  def run(delta: Int)(implicit unit: ScriptedMove) {
    lazy val ex = extract(manager, unit) _

    unit.time += 1
    
    @tailrec
    def recur(nestLevel: Int, seq: Array[Op]) {
      def incPC() {
        unit.pc(nestLevel) += 1
      }

      //println("w:"+bullet.waitCount+",nest:"+nestLevel+",pc:"+bullet.pc(nestLevel))
      
      if (0 <= unit.waitCount) { // wait中
        
        unit.waitCount -= 1
        if (unit.waitCount == -1) {
          unit.pc(unit.waitingNest) += 1
          recur(0, topseq)
        } else {
          BasicBehavior.run(delta)(unit)
        }
        
      } else if (unit.pc(nestLevel) < seq.length) { // 次のOpを実行
        
        seq(unit.pc(nestLevel)) match {
          case Wait(time) =>
            unit.waitCount = if (time < 1) 1 else time
            //println("wait:" + bullet.waitCount)
            unit.waitingNest = nestLevel
            recur(nestLevel, seq)

          case Fire(action, kind, dir, speed) =>
            unit.produce(manager.get(action), kind, unit.pos, ex(speed), Angle( ex(dir) ))
            //println("fire")
            incPC()
            recur(nestLevel, seq)

          case Repeat(time, childs) =>
            if ( unit.lc(nestLevel+1) == -1) {
              unit.lc(nestLevel+1) = time
              //println("repeat:" + time +", into NestLevel:"+(nestLevel+1))
            }
            recur(nestLevel+1, childs) // 1段階ネスト

          case SetVar(idx, value) =>
            unit.vars(idx) = ex(value)
            incPC()
            recur(nestLevel, seq)

          case GetVar(idx) => throw new Exception("変数取得してどうすんの？")

          case Nop =>
            incPC()
            recur(nestLevel, seq)

          case UpdateVar(idx, value) =>
            unit.vars(idx) += ex(value)
            incPC()
            recur(nestLevel, seq)

          case SetDirection(dir, param) => param match {
            case 'absolute =>
              unit.angle = Angle( ex(dir) )
            case 'aim =>
              unit.angle = Angle( manager.getAimToShip(unit.pos) )
            case 'relative =>
              unit.angle = Angle( ex(dir) )
          }; incPC(); recur(nestLevel, seq)

          case SetSpeed(spd, param) => param match {
            case 'absolute =>
              unit.speed = ex(spd)
            case 'relative =>
              unit.speed += ex(spd)
          }; incPC(); recur(nestLevel, seq)

          case PlaySound(param) => {
            SoundEffect.playSymbol(param)
          }; incPC(); recur(nestLevel, seq)

        }

      } else if (nestLevel != 0) {
        //println("owatteru")
        unit.lc(nestLevel) match {
          case 1 => // 回数分が終了していればループを終了
            unit.lc(nestLevel) = -1
            unit.pc(nestLevel) = 0
            unit.pc(nestLevel - 1) += 1
            // また頭からたどる．親も保持した方がいいのか……
            //println("[repeat end] NestLevel:"+nestLevel)
            recur(0, topseq)
          case 0 => // 無限ループなのでpcリセットして続行
            unit.pc(nestLevel) = 0
            recur(nestLevel, seq)
          case n => // ループカウンタ減らしてpcリセットして続行
            //println("dec-LC :"+(bullet.lc(nestLevel))+"["+nestLevel+"]")
            unit.lc(nestLevel) -= 1
            unit.pc(nestLevel) = 0
            recur(nestLevel, seq)
        }
      } else { // もう全部終わってるならScriptedMove側に移譲
        unit.onEndScript(delta)
        //BasicBehavior.run(delta)(unit)
      }
    }
    
    recur(0, topseq) // 頭からたどる
  }

  def evaluate(seq: Array[Op], op: Op)(implicit bullet: Bullet) {
    //def incPC() = bullet.pc += 1
  }
  
}