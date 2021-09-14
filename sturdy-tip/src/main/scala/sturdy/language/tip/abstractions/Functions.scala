package sturdy.language.tip.abstractions

import sturdy.language.tip.Interpreter
import sturdy.language.tip.Function
import sturdy.values.{Powerset => PSet}

object Functions:
  trait Powerset extends Interpreter :
    final type VFun = PSet[Function]

    final def topFun(using self: Instance): VFun = PSet(self.getFunctions.toSet)
