package sturdy.language.bytecode.analyses

import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, ClassFile, Method, MethodDescriptor, ObjectType, ReferenceType}
import sturdy.data.MayJoin
import sturdy.effect.allocation.Allocation
import sturdy.effect.callframe.DecidableMutableCallFrame
import sturdy.effect.except.Except
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.effect.store.Store
import sturdy.fix
import sturdy.fix.context.Sensitivity
import sturdy.fix.{ContextualFixpoint, Fixpoint}
import sturdy.language.bytecode.{ConcreteInterpreter, Interpreter}
import sturdy.language.bytecode.abstractions.ConstantValues
import sturdy.language.bytecode.generic.{AllocationSite, BytecodeOps, FixIn, FixOut, JvmExcept}
import sturdy.values.{Abstractly, Topped}
import sturdy.values.objects.{Object, ObjectOps}
import sturdy.values.arrays.{Array, ArrayOps}

import java.net.URL

object ConstantAnalysis extends Interpreter, ConstantValues:
  type Mth = Method
  type MthName = String
  type MthSig = MethodDescriptor
  type Addr = I32
  type Idx = I32
  type TypeRep = ReferenceType
  type FieldName = String
  type OID = I32
  type ObjType = ClassFile
  type AID = I32
  type AType = ArrayType


  given valuesAbstractly: Abstractly[ConcreteInterpreter.Value, Value] with
    override def apply(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.TopValue => Value.TopValue
      case ConcreteInterpreter.Value.Int32(i) => Value.Int32(Topped.Actual(i))
      case ConcreteInterpreter.Value.Int64(l) => Value.Int64(Topped.Actual(l))
      case ConcreteInterpreter.Value.Float32(f) => Value.Float32(Topped.Actual(f))
      case ConcreteInterpreter.Value.Float64(d) => Value.Float64(Topped.Actual(d))
      //case ConcreteInterpreter.Value.Obj(o) => Value.Obj(Topped.Actual(o))
      //case ConcreteInterpreter.Value.Array(a) => Value.Array(Topped.Actual(a))

/*
  class Instance extends GenericInstance:

    private given Instance = this

    override val fixpoint: fix.ContextualFixpoint[FixIn, FixOut[ConstantAnalysis.Value]] = new fix.ContextualFixpoint {
      override type Ctx = config.ctx.Ctx
      val (contextPreparation, sensitivity) = config.ctx.make[ConstantAnalysis.Value]

      import config.ctx.finiteCtx

      override protected def contextFree = contextPreparation

      override protected def context: Sensitivity[FixIn, Ctx] = sensitivity

      override protected def contextSensitive = config.fix.get
    }

    override val fixpointSuper = fixpoint

    val joinUnit: MayJoin.NoJoin[Unit] = ???
    val jvV: MayJoin.NoJoin[ConstantAnalysis.Value] = ???
    override val stack: DecidableOperandStack[ConstantAnalysis.Value] = ???
    override val failure: Failure = ???
    override val except: Except[JvmExcept[ConstantAnalysis.Value], JvmExcept[ConstantAnalysis.Value], MayJoin.NoJoin] = ???
    override val objFieldAlloc: Allocation[Topped[FrameData], AllocationSite] = ???
    override val objAlloc: Allocation[Topped[FrameData], AllocationSite] = ???
    override val arrayValAlloc: Allocation[Topped[FrameData], AllocationSite] = ???
    override val arrayAlloc: Allocation[Topped[FrameData], AllocationSite] = ???
    override val objFieldStore: Store[Topped[FrameData], ConstantAnalysis.Value, MayJoin.NoJoin] = ???
    override val arrayValStore: Store[Topped[FrameData], ConstantAnalysis.Value, MayJoin.NoJoin] = ???
    override val staticVarStore: Store[(ObjectType, String), ConstantAnalysis.Value, MayJoin.NoJoin] = ???
    override val frame: DecidableMutableCallFrame[FrameData, FrameData, ConstantAnalysis.Value] = ???
    override val project: Project[URL] = ???
    override val projectSource: String = ???

    override val bytecodeOps: BytecodeOps[Topped[FrameData], Topped[FrameData], ConstantAnalysis.Value, ReferenceType] = ???
    override val objectOps: ObjectOps[Topped[FrameData], String, Topped[FrameData], ConstantAnalysis.Value, ClassFile, Object[Topped[FrameData], ClassFile, Topped[FrameData], String], ConstantAnalysis.Value, AllocationSite, Method, String, MethodDescriptor, ConstantAnalysis.Value, MayJoin.NoJoin] = ???
    override val arrayOps: ArrayOps[Topped[FrameData], Topped[FrameData], ConstantAnalysis.Value, ConstantAnalysis.Value, Array[Topped[FrameData], Topped[FrameData], ArrayType], ConstantAnalysis.Value, ArrayType, AllocationSite, MayJoin.NoJoin] = ???

    */

