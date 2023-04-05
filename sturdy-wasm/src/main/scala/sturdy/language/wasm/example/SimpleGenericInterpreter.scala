//package sturdy.language.wasm.example
//
//import sturdy.data.{unit, NoJoin, WithJoin}
//import sturdy.effect.Effectful
//import sturdy.effect.except.{Except, ConcreteExcept, JoinedExcept}
//import sturdy.effect.failure.Failure
//import sturdy.effect.operandstack.{DecidableOperandStack, ConcreteOperandStack, JoinedDecidableOperandStack}
//import sturdy.language.wasm.generic.FrameData
//import sturdy.language.wasm.generic.UnreachableInstruction
//import sturdy.language.wasm.generic.WasmException
//import sturdy.values.{*, given}
//import sturdy.values.exceptions.{*, given}
//import sturdy.values.floating.{*, given}
//import sturdy.values.integer.{*, given}
//import sturdy.values.types.BaseType
//import swam.syntax.*
//
//trait SimpleGenericInterpreter[V, ExcV, MayJoin[_]]:
//  /** Effects are stacked so that their behavior gets interleaved. */
//  val effects: DecidableOperandStack[V] & Except[WasmException[V], ExcV, MayJoin]
//
//  val i32ops: IntegerOps[Int, V]
//  val i64ops: IntegerOps[Long, V]
//  val f32ops: FloatOps[Float, V]
//  val f64ops: FloatOps[Double, V]
//
//  def evalInst(inst: Inst): Unit = inst match
//    case i32.Sub =>
//      val v2 = effects.popOrFail()
//      val v1 = effects.popOrFail()
//      effects.push(i32ops.sub(v1, v2))
//    case f64.Abs =>
//      val v = effects.popOrFail()
//      effects.push(f64ops.absolute(v))
//    case Return =>
//      val operands = effects.popNOrFail(getFrameData.returnArity)
//      effects.throws(WasmException.Return(operands))
//    case _ => ???
//
//
//  protected given Failure = ???
//
//  def getFrameData: FrameData = ???
//
//
//enum Value:
//  case I32(i: Int);   case I64(l: Long)
//  case F32(f: Float); case F64(d: Double)
//
//class SimpleConcreteInterpreter extends SimpleGenericInterpreter[Value, WasmException[Value], NoJoin]:
//  val effects = new ConcreteOperandStack[Value] with ConcreteExcept[WasmException[Value]] {}
//  val i32ops: IntegerOps[Int, Value] = new LiftedIntegerOps({case Value.I32(i) => i}, Value.I32.apply)
//  val i64ops: IntegerOps[Long, Value] = new LiftedIntegerOps({case Value.I64(l) => l}, Value.I64.apply)
//  val f32ops: FloatOps[Float, Value] = new LiftedFloatOps({case Value.F32(f) => f}, Value.F32.apply)
//  val f64ops: FloatOps[Double, Value] = new LiftedFloatOps({case Value.F64(i) => i}, Value.F64.apply)
//
//enum Type:
//  case Top; case I32; case I64; case F32; case F64
//
//given Join[Type] with
//  override def apply(v1: Type, v2: Type): MaybeChanged[Type] =
//    if (v1 == Type.Top || v1 == v2) MaybeChanged.Unchanged(v1)
//    else MaybeChanged.Changed(Type.Top)
//
//class SimpleAbstractInterpreter extends SimpleGenericInterpreter[Type, Powerset[WasmException[Type]], WithJoin]:
//  val effects = new JoinedDecidableOperandStack[Type] with JoinedExcept[WasmException[Type], Powerset[WasmException[Type]]] {}
//  val i32ops: IntegerOps[Int, Type] = new LiftedIntegerOps({case Type.I32 => BaseType[Int]}, _ => Type.I32)
//  val i64ops: IntegerOps[Long, Type] = new LiftedIntegerOps({case Type.I64 => BaseType[Long]}, _ => Type.I64)
//  val f32ops: FloatOps[Float, Type] = new LiftedFloatOps({case Type.F32 => BaseType[Float]}, _ => Type.F32)
//  val f64ops: FloatOps[Double, Type] = new LiftedFloatOps({case Type.F64 => BaseType[Double]}, _ => Type.F64)
//
//  protected given Effectful = effects
//
