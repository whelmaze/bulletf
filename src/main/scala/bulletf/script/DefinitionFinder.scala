package bulletf.script

object DefinitionFinder {

  implicit class SymToStr(val str: String) extends AnyVal {
    def sym: Symbol = Symbol(str)
  }

  final val spdparams = Set("absolute", "relative")
  final val dirparams = Set("absolute", "aim", "relative")
  final val stateparams = Set("x", "y", "aim", "speed", "angle", "px", "py")
  
  def findSpecialValue(name: String, args: Seq[Container]): Container = (name, args) match {
    case ("rnd", Seq( DVar(fst), DVar(snd) )) =>
      if (fst < snd) RandomVar(fst, snd) else RandomVar(snd, fst)
    case ("rnd", Seq( DVar(fst)) ) =>
      RandomVar(0, fst)
    case (n, Seq()) if stateparams(n) =>
      StateVar( n.sym )
    case _ => DVar(0.0)
  }

  def findFunction(name: String, args: Seq[Container]): Op = (name, args) match {
    case ("fire", args: Seq[_]) => args match {
      case Seq( StrVar(action), StrVar(kind), dirCon: Container, speedCon: Container ) =>
        Fire(action.sym, kind.sym, dirCon, speedCon)
      case Seq( StrVar(action), StrVar(kind) ) =>
        Fire(action.sym, kind.sym, StateVar('aim), StateVar('speed))
      case Seq( StrVar(action) ) =>
        Fire(action.sym, 'DEFAULT, StateVar('aim), StateVar('speed))
      case Nil =>
        Fire('simple, 'DEFAULT, StateVar('aim), StateVar('speed))
      case _ => Nop
    }

    case ("appear", args: Seq[_]) => args match {
      case Seq( StrVar(action), StrVar(kind), xCon: Container, yCon: Container ) =>
        GenEnemy(action.sym, kind.sym, xCon, yCon)
      case Seq( StrVar(action), StrVar(kind) ) =>
        GenEnemy(action.sym, kind.sym, StateVar('x), StateVar('y))
      case _ => Nop
    }

    case ("emit", args: Seq[_]) => args match {
      case Seq( StrVar(action), xCon, yCon) =>
        Emit(action.sym, xCon, yCon)
      case Seq( StrVar(action) ) => // 指定がない場合親の位置を受け継ぐ
        Emit(action.sym, StateVar('x), StateVar('y))
      case _ => Nop
    }

    case ("fx", arg: Seq[_]) => args match {
      case Seq( StrVar(action), StrVar(kind), xCon, yCon) =>
        GenEffect(action.sym, kind.sym, xCon, yCon)
      case Seq( StrVar(action), StrVar(kind) ) =>
        GenEffect(action.sym, kind.sym, StateVar('x), StateVar('y))
      case _ => Nop
    }

    case ("wait", Seq( DVar(time) )) =>
      Wait(time.toInt)

    case ("vwait", Seq( timeCon: Container )) =>
      VWait(timeCon)
    
    case ("spd", Seq( valueCon: Container, StrVar(param) )) =>
      if ( spdparams(param) ) {
        SetSpeed(valueCon, param.sym )
      } else {
        println("%s is undefined param with `spd`, fixed to absolute." format param)
        SetSpeed(valueCon, 'absolute)
      }
    case ("spd", Seq( valueCon: Container )) =>
      SetSpeed(valueCon, 'absolute)
    
    case ("dir", Seq( valueCon: Container, StrVar(param) )) =>
      if ( dirparams(param) ) {
        SetDirection(valueCon, param.sym )
      } else {
        println("%s is undefined param with `spd`, fixed to absolute." format param)
        SetDirection(valueCon, 'absolute )
      }
    case ("dir", Seq( valueCon: Container )) =>
      SetDirection(valueCon, 'absolute)

    case ("se", args: Seq[_]) => args match {
      case Seq(StrVar(soundId)) =>
        PlaySound(soundId.sym, DVar(1.0), DVar(0.5))
      case Seq(StrVar(soundId), volCon: Container) =>
        PlaySound(soundId.sym, DVar(1.0), volCon)
      case Seq(StrVar(soundId), pitchCon: Container, volCon: Container) =>
        PlaySound(soundId.sym, pitchCon, volCon)
      case _ => Nop
    }

    case ("brake", s @ Seq(tCon: Container, _*)) =>
      val isAsync = s.drop(1).take(1).headOption.collect{ case StrVar(str) if str == "async" => true }.getOrElse(false)
      MoveTo("brake", DVar(0), DVar(0), tCon, async = isAsync, Array.empty[Container])

    case ("accel", s @ Seq(tCon: Container, spd: Container, _*)) =>
      val isAsync = s.drop(2).take(1).headOption.collect{ case StrVar(str) if str == "async" => true }.getOrElse(false)
      MoveTo("accel", DVar(0), DVar(0), tCon, async = isAsync, Array(spd))


    case ("move_to", s @ Seq(StrVar(handling), xCon: Container, yCon: Container, timeCon: Container, _*)) =>
      val opt = s.drop(4).take(10).toArray
      MoveTo(handling, xCon, yCon, timeCon, async = false, opt)

    case ("move_to_async", s @ Seq(StrVar(handling), xCon: Container, yCon: Container, timeCon: Container, _*)) =>
      val opt = s.drop(4).take(10).toArray
      MoveTo(handling, xCon, yCon, timeCon, async = true, opt)

    case ("scale", Seq(valueCon: Container)) =>
      SetScale(valueCon)

    case ("alpha", Seq(valueCon: Container)) =>
      SetAlpha(valueCon)

    case ("music_load", Seq(StrVar(id))) =>
      MusicLoad(id.sym)

    case ("music_release", Seq(StrVar(id))) =>
      MusicRelease(id.sym)

    case ("music_play", Seq(StrVar(id), vol: Container)) =>
      MusicPlay(id.sym, vol)

    case ("music_stop", _) =>
      MusicStop

    case _ => Nop
  }

}
