package sturdy

trait Executor:
  def newInstance: Executor
  def copyState(from: Executor): Unit = {}
  def fork: Executor =
    val e = this.newInstance
    e.copyState(this)
    e
