package bulletf


import script.DSFParser
import scala.io.Source
import scala.collection.mutable

object BehaviorManager {

  val map = mutable.HashMap.empty[Symbol, Behavior]
  init()

  def init() = {
    map += ('simple -> BasicBehavior)
  }

  def build(ref: String) {
    val src = Source.fromFile( Resource.scriptPath + ref + ".dsf" ).getLines().mkString("\n")
    
    DSFParser.parse(src) foreach { case (name, blocks) =>
      val action = new ScriptBehavior(blocks)
      println("action[" + name.name + "] created.")
      map += ( name -> action )
    }
  }
  
  def get(ref: Symbol): Behavior = map.get(ref) match {
    // 目的のアクションがなかった場合どうする？
    // →buildの時点で依存するアクションはロードしておくべき．
    // あとで．
    case Some(x) => x
    case None => sys.error(s"BehaviorManager: no action [$ref]")
  }

  def clear() = {
    map.clear()
    init()
  }
  
}
