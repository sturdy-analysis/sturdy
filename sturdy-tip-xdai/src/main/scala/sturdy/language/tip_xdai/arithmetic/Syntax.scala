package sturdy.language.tip_xdai.arithmetic

import sturdy.language.tip_xdai.core.Exp

case class NumLit(n: Int) extends Exp:
  override def toString: String = s"$n@${this.label}"
case class Add(e1: Exp, e2: Exp) extends Exp:
  override def toString: String = s"Add@${this.label}"
case class Sub(e1: Exp, e2: Exp) extends Exp:
  override def toString: String = s"Sub@${this.label}"
case class Mul(e1: Exp, e2: Exp) extends Exp:
  override def toString: String = s"Mul@${this.label}"
case class Div(e1: Exp, e2: Exp) extends Exp:
  override def toString: String = s"Div@${this.label}"
case class Gt(e1: Exp, e2: Exp) extends Exp:
  override def toString: String = s"Gt@${this.label}"
