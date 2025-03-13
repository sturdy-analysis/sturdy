package sturdy.language.tip.oldBackward.abstractions

import sturdy.language.tip.oldBackward.BackwardsInterpreter
import sturdy.language.tip.{Function, Interpreter}
import sturdy.values.Powerset as PSet

object Functions:
  trait Powerset extends BackwardsInterpreter :
    final type VFun = PSet[Function]

    final def topFun(using self: Instance): VFun = PSet(self.getFunctions.toSet)
