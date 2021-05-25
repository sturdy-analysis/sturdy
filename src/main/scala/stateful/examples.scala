package stateful

import stateful.JoinUnit.joinUnit
import stateful.SignEnum.Neg

object examples extends App {
  def tick[V](c: Val[V] with State[V]): Unit = {
    import c._
    setCount(add(getCount, int(1)))
  }
  println(Concrete.run(tick))
  println(Abstract.run(tick))
  println()

  def tick3[V](c: Val[V] with State[V]): Unit = { tick(c); tick(c); tick(c) }
  println(Concrete.run(tick3, -1, -1))
  println(Abstract.run(tick3, Neg, Neg))
  println()

  def tock[V](c: Val[V] with State[V] with ReadImpl[V]): Unit = {
    import c._
    provide(int(-5)) {
      setCount(add(getCount, read))
    }
  }
  println(Concrete.run(tock))
  println(Abstract.run(tock))
  println()

  def checkPos[V](c: Val[V] with State[V] with Fail)(implicit j: c.TValJoin[Unit]): Unit = {
    import c._
    tick3(c)
    ifNeg(getCount,
      fail("Found negative count"),
      els = ())
  }
  println(Concrete.run(checkPos(_)(joinUnit), -1, -5))
  println(Abstract.run(checkPos(_)(joinUnit), Neg, Neg))
  println()

  def reset[V](c: Val[V] with State[V]): Unit = {
    import c._
    setCount(int(0))
  }
  def ensurePos[V](c: Val[V] with State[V])(implicit j: c.TValJoin[V]): Unit = {
    import c._
    val count = getCount
    ifNeg(count,
      {reset(c); getCount},
      els = count)
  }
  println(Concrete.run{c => tock(c); ensurePos(c)()})
  println(Abstract.run{c => tock(c); ensurePos(c)(SignEnum.Join)})
}
