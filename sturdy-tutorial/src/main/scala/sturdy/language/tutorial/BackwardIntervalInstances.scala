package sturdy.language.tutorial

import sturdy.effect.EffectStack
import sturdy.effect.failure.{AFallible, FailureKind}
import sturdy.language.tutorial.BackwardIntervalInstances.Interval.*
import sturdy.values.{CombineMayMust, finitely}

object BackwardIntervalInstances:

  enum Interval:
    case ITop
    case I(lower: Int, upper: Int)

    override def toString: String = this match
      case ITop => "⊤"
      case I(lower, upper) => s"[$lower, $upper]"


  class IntervalUnifiable extends Unifiable[Interval]:
    override def canUnify(v1: Interval, v2: Interval): Boolean = (v1, v2) match
      case (I(a1, b1), I(a2, b2)) =>
        val gtCond = a1 <= a2 && b1 >= b2
        val ltCond = a2 <= a1 && b2 >= b1
        ltCond || gtCond
      case _ => true  // Assuming ITop can unify with any Interval

  class IntervalBackJoin extends BackJoin[Interval]:
    override def join(v1: Interval, v2: Interval): Interval = (v1, v2) match
      case (ITop, _) | (_, ITop) => ITop
      case (I(a1, b1), I(a2, b2)) => I(math.min(a1, a2), math.max(b1, b2))

  class IntervalInvertOps(using f: Failure, j: EffectStack) extends InvertOps[Interval]:
    override def trueVal: Interval = I(1, 1)
    override def falseVal: Interval = I(0, 0)
    override def topVal: Interval = ITop

    override def invConst(c: Int, a: Interval): Boolean = a match
      case I(l, u) => l <= c && c <= u
      case ITop => true

    override def invAdd(a1: Interval, a2: Interval, a3: Interval): (Interval, Interval, Interval) = (a1, a2, a3) match
      case (ITop, I(x, y), I(rx, ry)) => (I(rx - y, ry - x), I(x, y), I(rx, ry))
      case (I(x, y), ITop, I(rx, ry)) => (I(x, y), I(rx - y, ry - x), I(rx, ry))
      case (I(x, y), I(a, b), ITop)   => (I(x, y), I(a, b), I(x + a, y + b))
      case _                          => (a1, a2, a3)

    override def invSub(a1: Interval, a2: Interval, a3: Interval): (Interval, Interval, Interval) = (a1, a2, a3) match
      case (ITop, I(x, y), I(rx, ry)) => (I(rx + x, ry + y), I(x, y), I(rx, ry))
      case (I(x, y), ITop, I(rx, ry)) => (I(x, y), I(x - ry, y - rx), I(rx, ry))
      case (I(x, y), I(a, b), ITop)   => (I(x, y), I(a, b), I(x - b, y - a))
      case _                          => (a1, a2, a3)

    override def invMul(a1: Interval, a2: Interval, a3: Interval): (Interval, Interval, Interval) = ???
    override def invDiv(a1: Interval, a2: Interval, a3: Interval): (Interval, Interval, Interval) = ???

    override def invGt(a1: Interval, a2: Interval, res: Interval): (Interval, Interval, Interval) =
      val t = (a1,a2,res)
      t match
        case (ITop, I(x, y), I(1, 1)) => (I(y+1,Int.MaxValue), a2, res)
        case (I(x, y), ITop, I(1, 1)) => (a1, I(Int.MinValue,x-1), res)
        case (ITop, I(x, y), I(0, 0)) => (I(Int.MinValue,x), a2, res)
        case (I(x, y), ITop, I(0, 0)) => (a1,I(y,Int.MaxValue), res)
        case (I(x,y),I(a,b),ITop)     => if a > y then (a1,a2,I(1,1)) else (a1,a2,I(0,0))
        case (I(x,y),I(a,b),I(0,0))   => x <= b match
          case true => t
          case false => throw new RuntimeException(s"Expected ${a1} <= ${a2} which is wrong!!!")
        case (I(x,y),I(a,b),I(1,1))   => x > b match
          case true  => t
          case false => throw new RuntimeException(s"Expected ${a1} > ${a2} which is wrong!!!")
        case _ => (a1, a2, res)

    override def invLt(a1: Interval, a2: Interval, res: Interval): (Interval, Interval, Interval) = (a1, a2, res) match
      case (ITop, I(x, y), I(1, 1)) => (I(Int.MinValue, y), I(x, Int.MaxValue), res)
      case (I(x, y), ITop, I(1, 1)) => (I(Int.MinValue, y), I(x, Int.MaxValue), res)
      case (ITop, I(x, y), I(0, 0)) => (I(x, Int.MaxValue), I(Int.MinValue, y), res)
      case (I(x, y), ITop, I(0, 0)) => (I(x, Int.MaxValue), I(Int.MinValue, y), res)
      case (I(x, y), I(a, b), ITop) => if (b < x) then (a1, a2, I(1, 1)) else (a1, a2, I(0, 0))
      case _ => (a1, a2, res)


  class IntervalNumericOps(using f: Failure, j: EffectStack) extends NumericOps[Interval]:
    override def lit(i: Int): Interval = Interval.I(i, i)

    override def add(v1: Interval, v2: Interval): Interval = (v1, v2) match
      case (Interval.I(a1, b1), Interval.I(a2, b2)) => Interval.I(a1 + a2, b1 + b2)
      case _ => Interval.ITop

    override def sub(v1: Interval, v2: Interval): Interval = (v1, v2) match
      case (Interval.I(a1, b1), Interval.I(a2, b2)) => Interval.I(a1 - b2, b1 - a2)
      case _ => Interval.ITop

    override def mul(v1: Interval, v2: Interval): Interval = (v1, v2) match
      case (Interval.I(a1, b1), Interval.I(a2, b2)) =>
        val products = List(a1 * a2, a1 * b2, b1 * a2, b1 * b2)
        Interval.I(products.min, products.max)
      case _ => Interval.ITop

    override def div(v1: Interval, v2: Interval): Interval = v2 match
      case Interval.I(a, b) if a <= 0 && b >= 0 =>
        f.fail(Failures.DivisionByZero, s"$v1 / $v2")
        Interval.ITop
      case Interval.I(a, b) => ???
        //mul(v1, Interval.I(1 / b.toFloat, 1 / a.toFloat))
      case _ => Interval.ITop

    override def lt(v1: Interval, v2: Interval): Interval = (v1, v2) match
      case (Interval.I(a1, _), Interval.I(_, b2)) if a1 < b2 => Interval.I(1, 1)
      case _ => Interval.I(0, 0)

    override def gt(v1: Interval, v2: Interval): Interval = (v1, v2) match
      case (Interval.I(_, b1), Interval.I(a2, _)) if b1 > a2 => Interval.I(1, 1)
      case _ => Interval.I(0, 0)

  class IntervalWidener extends MyWiden[Interval]:
    override def mywiden(prevState: ST[Interval], newState: ST[Interval]): ST[Interval] =
      newState.map {
        case (varName, newInterval) =>
          val prevInterval = prevState.getOrElse(varName, ITop)
          val widenedInterval = (prevInterval, newInterval) match
            case (I(prevLower, prevUpper), I(newLower, newUpper)) =>
              val lower = if newLower < prevLower then Int.MinValue else prevLower
              val upper = if newUpper > prevUpper then Int.MaxValue else prevUpper
              I(lower, upper)
            case (_, ITop) | (ITop, _) => ITop
            case _ => newInterval
          (varName, widenedInterval)
      }

