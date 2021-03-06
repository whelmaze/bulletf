package bulletf



import scala.collection.mutable

object Sprite {

  private[this] val cache = mutable.HashMap.empty[Symbol, Sprite]

  def get(resource: Symbol): Sprite = cache.get(resource) match {
    case Some(cachedSprite) => cachedSprite
    case None =>
      val newSprite: Sprite = if (resource.name.toLowerCase.substring(0, 2) == "a_") {
        new AnimationSprite(resource)
      } else {
        new SpriteImpl(resource)
      }
      cache.update(resource, newSprite)
      newSprite
  }

  def count = cache.size

}

trait Sprite {
  val rect: Rect

  def draw(pos: Position) {
    draw(pos, 0)
  }
  def draw(pos: Position, angle: Double) {
    draw(pos, angle, 1.0)
  }
  def draw(pos: Position, angle: Double, scale: Double) {
    draw(pos, angle, scale, 1.0)
  }
  def draw(pos: Position, angle: Double, scale: Double, alpha: Double) {
    draw(pos, angle, scale, alpha, 0)
  }

  def draw(pos: Position, angle: Double, scale: Double, alpha: Double, time: Int)

  def draw(custom_rect: Rect, pos: Position, angle: Double)
}

class SpriteImpl(resourceId: Symbol) extends Sprite {

  val (texture, rect) = TextureFactory.get(resourceId)

  // 普通のSpriteはtime無視
  def draw(pos: Position, angle: Double, scale: Double, alpha: Double, time: Int) {
    DrawerPlus.draw(texture, rect, pos, angle, scale, alpha)
  }

  def draw(custom_rect: Rect, pos: Position, angle: Double) {
    DrawerPlus.draw(texture, custom_rect, pos, angle, scale = 1f, alpha = 1f)
  }

}





