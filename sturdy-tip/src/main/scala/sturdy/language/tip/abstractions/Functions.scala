package sturdy.language.tip.abstractions

import sturdy.ir.IR
import sturdy.language.tip.Interpreter
import sturdy.language.tip.Function
import sturdy.values.Powerset as PSet

object Functions:
  trait Powerset extends Interpreter:
    final type VFun = PSet[Function]

    final def topFun(using self: Instance): VFun = PSet(self.getFunctions.toSet)

  trait IRFun extends Interpreter:
    override final type VFun = IR
    override def topFun(using Instance): IR = IR.Unknonwn()
