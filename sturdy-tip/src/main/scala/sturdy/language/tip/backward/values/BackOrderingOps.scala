package sturdy.language.tip.backward.values

import sturdy.values.Topped
import sturdy.values.integer.IntSign
import sturdy.values.integer.IntSign.*
import sturdy.values.integer.SignOrderingOps

trait BackOrderingOps[V, B]:
  def lt(v1: V => V, v2: V => V, r: B): B
  def le(v1: V => V, v2: V => V, r: B): B

  def ge(v1: V => V, v2: V => V, r: B): B = le(v2, v1, r)
  def gt(v1: V => V, v2: V => V, r: B): B = lt(v2, v1, r)

class LiftedBackOrderingOps[V,B,UV,UB](extract: V => UV, inject: UV => V, extractB: B => UB, injectB: UB => B)(using ops: BackOrderingOps[UV,UB]) extends BackOrderingOps[V,B]:
  import scala.language.implicitConversions
  private implicit def exB(v: B): UB = extractB(v)
  private implicit def inB(i: UB): B = injectB(i)
  private implicit def wrap(f: V => V): UV => UV = i => extract(f(inject(i)))

  override def lt(v1: V => V, v2: V => V, r: B): B = ops.lt(v1, v2, r)
  override def le(v1: V => V, v2: V => V, r: B): B = ops.le(v1, v2, r)

given SignBackOrderingOps: BackOrderingOps[IntSign, Topped[Boolean]] with
  override def lt(v1: IntSign => IntSign, v2: IntSign => IntSign, r: Topped[Boolean]): Topped[Boolean] = r match
    case Topped.Top =>
      val a2 = v2(TopSign)
      val a1 = v1(TopSign)
      SignOrderingOps.lt(a1, a2)
    case Topped.Actual(true) => v2(TopSign) match
      case Neg | NegOrZero | Zero => v1(Neg); r
      case ZeroOrPos | Pos | TopSign => v1(TopSign); r
    case Topped.Actual(false) => v2(TopSign) match
      case Zero | ZeroOrPos => v2(ZeroOrPos); r
      case Pos => v2(Pos); r
      case Neg | NegOrZero | TopSign => v1(TopSign); r

    override def le(v1: IntSign => IntSign, v2: IntSign => IntSign, r: Topped[Boolean]): Topped[Boolean] = r match
      case Topped.Top =>
        val a2 = v2(TopSign)
        val a1 = v1(TopSign)
        SignOrderingOps.le(a1, a2)
      case Topped.Actual(true) => v2(TopSign) match
        case NegOrZero | Zero => v1(NegOrZero); r
        case Neg => v1(Neg); r
        case ZeroOrPos | Pos | TopSign => v1(TopSign); r
      case Topped.Actual(false) => v2(TopSign) match
        case Zero | ZeroOrPos | Pos => v2(Pos); r
        case Neg | NegOrZero | TopSign => v1(TopSign); r



