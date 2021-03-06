package bulletf.script

/**
* Op
* 弾の振る舞いを定義するクラス群の親トレイト．
* 
*/
sealed trait Op

/** 
* Repeat
* subseq以下をtime回数繰り返す．timeが0なら無限ループ．
* 
*/
case class Repeat(time: Int, subseq: Array[Op]) extends Op {
  override def toString = "Repeat(%d, %s)" format (time, subseq.deep.toString())
} 

/** 
* Wait
* 指定したフレーム数だけアクションを実行せず待機する．
*   
*/
case class Wait(time: Int) extends Op

/**
 * VWait (Variable Wait)
 * こちらは実行時に解釈が入るので遅くなる可能性
 * 定数以外で指定したい場合はこっち使えばおｋ
 */
case class VWait(time: Container) extends Op

/** 
* Nop
* パースに失敗した残骸．
*   
*/
case object Nop extends Op

/** 
* Fire
* 弾を撃つ．弾はフレームの終わりに登録され，次のフレームから動き出す．
* 
*/
case class Fire(action: Symbol, kind: Symbol, dir: Container, speed: Container) extends Op

/**
 * GenEnemy
 * Enemyを生成する．
 */
case class GenEnemy(action: Symbol, kind: Symbol, x: Container, y: Container) extends Op

/**
 * Emit
 * Emitterを生成する．
 */
case class Emit(action: Symbol, x: Container, y: Container) extends Op

/**
 * Effect
 * Effectを生成する．
 */
case class GenEffect(action: Symbol, kind: Symbol, x: Container, y: Container) extends Op

/**
* SetVar
* Double型の変数をセットする．$0 - $9までが使用可能で，アクションごとに共有される．
*
*/
case class SetVar(idx: Int, value: Container) extends Op

/** 
* GetVar
* Double型の変数を取得する．$0 - $9までが使用可能で，アクションごとに共有される．
* 
*/
case class GetVar(idx: Int) extends Op

/** 
* UpdateVar
* 変数にvalueの値を加算して更新する．
*   
*/
case class UpdateVar(idx: Int, value: Container) extends Op 

/** 
* SetSpeed
* スピードを変える．
* サポートするparamの値は absolute(絶対指定), relative(現在速度を0とする)
* paramを省略した場合absoluteを指定したことになる．
*/
case class SetSpeed(value: Container, param: Symbol) extends Op

/** 
* SetDirection
* 方向を変える．
* サポートするparamの値は absolute(絶対指定), aim(自機狙い基準)
* paramを省略した場合absoluteを指定したことになる．
*/
case class SetDirection(value: Container, param: Symbol) extends Op

/**
 * 音再生
 * @param soundId 再生する音のid
 */
case class PlaySound(soundId: Symbol, pitch: Container, vol: Container) extends Op

/**
 * 大きさ変更
 * @param value 変更する大きさ(1.0が通常)
 */
case class SetScale(value: Container) extends Op

/**
 * alpha値変更
 * @param value 変更するalpha値
 */
case class SetAlpha(value: Container) extends Op

// 曲データロード
case class MusicLoad(id: Symbol) extends Op
// 曲データ開放
case class MusicRelease(id: Symbol) extends Op
// 曲再生
case class MusicPlay(id: Symbol, volume: Container) extends Op
// 曲停止
case object MusicStop extends Op

case class DataSet(name: Symbol, data: String) extends Op

/**
 * 指定座標への移動
 * @param handling 移動方法
 * @param x 移動先のX座標
 * @param y 移動先Y座標
 * @param time 移動にかける時間(フレーム)
 * @param async 非同期かどうか
 * @param option オプション引数(最大10)
 */
case class MoveTo(handling: String, x: Container, y: Container, time: Container, async: Boolean, option: Array[Container] = Array.empty[Container]) extends Op
