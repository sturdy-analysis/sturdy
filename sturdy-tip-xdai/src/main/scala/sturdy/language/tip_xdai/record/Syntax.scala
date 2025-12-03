package sturdy.language.tip_xdai.record

import sturdy.language.tip_xdai.core.{Assignable, Exp}

case class Record(fields: Seq[(String, Exp)]) extends Exp:
  override def toString: String = s"Record@${this.label}"
case class FieldAccess(rec: Exp, field: String) extends Exp:
  override def toString: String = s"FieldAccess@${this.label}"


case class AField(rec: String, field: String) extends Assignable
case class ADerefField(rec: Exp, field: String) extends Assignable