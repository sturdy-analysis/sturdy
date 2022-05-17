package sturdy

trait Executor:
  def newInstance: Executor
  def copyState(from: Executor): Unit = {}
  def fork: Executor =
    val e = this.newInstance
    e.copyState(this)
    e

trait Executable[+B] extends Function1[Executor, B]:
  def executor: Executor
  def run: B = apply(executor)
  def runWith(exec: Executor): B = apply(exec)
