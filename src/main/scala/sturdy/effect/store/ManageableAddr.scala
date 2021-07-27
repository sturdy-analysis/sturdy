package sturdy.effect.store

trait ManageableAddr(_managed: Boolean):
  private val managed: Boolean = _managed
  def isManaged: Boolean = managed

