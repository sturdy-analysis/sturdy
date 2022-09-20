//package sturdy.language.wasm.abstractions
//
//import sturdy.apron.ApronCons
//import sturdy.apron.ApronExpr
//import sturdy.data.CombineEquiList
//import sturdy.effect.TrySturdy
//import sturdy.effect.failure.Failure
//import sturdy.effect.operandstack.DecidableOperandStack
//import sturdy.fix
//import sturdy.fix.Logger
//import sturdy.language.wasm.generic.WasmFailure
//import sturdy.language.wasm.generic.{FixIn, FixOut, InstLoc}
//import sturdy.language.wasm.{Interpreter, ConcreteInterpreter}
//import sturdy.values.booleans.given
//import sturdy.values.floating.given
//import sturdy.values.{Finite, Join, Topped, given}
//import sturdy.values.integer.{NumericInterval, given}
//import swam.{OpCode, syntax}
//import swam.syntax.{StoreNInst, StoreInst, LoadInst, LoadNInst}
//
//import scala.collection.MapView
//
//trait RelationalValues extends Interpreter:
//  final type I32 = Topped[ApronExpr]
//  final type I64 = Topped[ApronExpr]
//  final type F32 = Topped[ApronExpr]
//  final type F64 = Topped[ApronExpr]
//  final type Bool = Topped[ApronCons]
//
//  final def topI32: I32 = Topped.Top
//  final def topI64: I64 = Topped.Top
//  final def topF32: F32 = Topped.Top
//  final def topF64: F64 = Topped.Top
//
//  final def asBoolean(v: Value)(using Failure): Bool = v match
//    case Value.Int32(toppedInt) => toppedInt.map(ApronCons.neq(_, ApronExpr.num(0)))
//    case Value.TopValue => Topped.Top
//    case _ => Failure(WasmFailure.TypeError, s"Expected Int but got $this")
//
//  final def boolean(b: Bool)(using inst: Instance): Value =
//    b.flatMap { bv =>
//      import inst.{given_EffectStack, apron, failure}
//      given Failure = failure
//
//      val vIntOps = summon[IntegerOps[Int, VInt]]
//      val i = inst.apron.ifThenElsePure(bv, widen = false)(vIntOps.integerLit(1))(vIntOps.integerLit(0))
//      Value.Int32(i)
//    }
//
//  def constantInstructions(analysis: Instance): ConstantInstructionsLogger =
//    val constants = new ConstantInstructionsLogger(analysis.stack)(using analysis.failure)
//    analysis.fixpoint.addContextFreeLogger(constants)
//    constants
//
//  class ConstantInstructionsLogger(stack: DecidableOperandStack[Value])(using Failure) extends InstructionResultLogger[Value](stack):
//    override def boolValue(v: Value): Value = boolean(asBoolean(v))
//    override def dummyValue: Value = Value.Int32(NumericInterval(0, 0))
//
//    def get: Map[InstLoc, List[Value]] = instructionInfo.filter(_._2.forall {
//      case Value.TopValue => false
//      case Value.Int32(v) => v.isConstant
//      case Value.Int64(v) => v.isConstant
//      case Value.Float32(v) => v.isActual
//      case Value.Float64(v) => v.isActual
//    })
//
//    def grouped: Map[String, Map[InstLoc, List[Value]]] =
//      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName)
//
//    def groupedCount: Map[String, Int] =
//      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName).view.mapValues(_.size).toMap
