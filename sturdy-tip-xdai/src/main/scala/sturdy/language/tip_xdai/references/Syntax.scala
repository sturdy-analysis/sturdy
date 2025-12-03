package sturdy.language.tip_xdai.references

import sturdy.language.tip_xdai.core.{Assignable, Exp}

case class Alloc(e: Exp) extends Exp:
  override def toString: String = s"Alloc@${this.label}"
case class VarRef(name: String) extends Exp:
  override def toString: String = s"&$name@${this.label}"
case class Deref(e: Exp) extends Exp:
  override def toString: String = s"Deref@${this.label}"
case class NullRef() extends Exp:
  override def toString: String = s"Null@${this.label}"


case class ADeref(e: Exp) extends Assignable