//package sturdy.language.wasm
//
//import sturdy.effect.failure.Failure
//import sturdy.effect.operandstack.OperandStack
//import sturdy.values.booleans.BooleanBranching
//import sturdy.values.ints.IntOps
//import sturdy.values.relational.EqOps
//
//import swam.syntax.*
//
///** Effects are stacked so that their behavior is interleaved. */
//type Effects[V, MayJoin[_]] = OperandStack[V, MayJoin] & Failure
//
//trait SimpleGenericInterpreter[V, MayJoin[_]]:
//  val effects: Effects[V, MayJoin]
//
//  val intOps: IntOps[V]
//  val eqOps: EqOps[V, V]
//  val branchOps: BooleanBranching[V, MayJoin]
//
//  def evalInst(inst: Inst): Unit = inst match
//    case i32.Const(i) => effects.push(intOps.intLit(i))
//    case i32.Add =>
//      val v1 = effects.safePop()
//      val v2 = effects.safePop()
//      effects.push(intOps.add(v1,v2))
//    case If(thn, els) =>
//      val c = stack.safePop()
//      val isZero = eqOps.equ(c, intOps.intLit(0))
//      branching.boolBranch(isZero) {
//        els.foreach(evalInst)
//      } {
//        thn.foreach(evalInst)
//      }
//    case Trap => fail.fail(FailTrap, "trap")
//
