package sturdy.ir

import sturdy.values.{Join, MaybeChanged}

trait IROperator

enum IR:
  private val uid = new IR_UID
  case Unknonwn()
  case Const[C](c: C)
  case Op(op: IROperator, args: Seq[IR])
  case Join(left: IR, right: IR)

  def predecessors: Seq[IR] = this match
    case IR.Unknonwn() => Seq.empty
    case IR.Const(c) => Seq.empty
    case IR.Op(op, args) => args
    case IR.Join(left, right) => Seq(left, right)

  override def toString: String = this match
    case IR.Unknonwn() => s"Unknown@$uid"
    case IR.Const(c) => s"Const($c)@$uid"
    case IR.Op(op, args) => s"Op($op)@$uid"
    case IR.Join(left, right) => s"Join@$uid"


object IR:
  def Op(op: IROperator, arg: IR, args: IR*): IR.Op =
    IR.Op(op, arg +: args)


given Join[IR] with
  override def apply(v1: IR, v2: IR): MaybeChanged[IR] = MaybeChanged.Changed(IR.Join(v1, v2))


class IR_UID:
  override def toString: String = Integer.toHexString(hashCode)