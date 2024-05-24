package sturdy.language.bytecode.analyses

import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, ClassFile, Method, MethodDescriptor, ObjectType, ReferenceType}
import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.EffectStack
import sturdy.effect.allocation.Allocation
import sturdy.effect.callframe.DecidableMutableCallFrame
import sturdy.effect.except.{Except, JoinedExcept}
import sturdy.effect.failure.{CollectedFailures, Failure}
import sturdy.effect.operandstack.{DecidableOperandStack, JoinableDecidableOperandStack}
import sturdy.effect.store.Store
import sturdy.fix
import sturdy.fix.StackConfig.StackedStates
import sturdy.fix.context.Sensitivity
import sturdy.fix.{ContextualFixpoint, Fixpoint}
import sturdy.language.bytecode.{ConcreteInterpreter, Interpreter}
import sturdy.language.bytecode.abstractions.{ConstantValues, Exceptions}
import sturdy.language.bytecode.generic.{AllocationSite, BytecodeFailure, BytecodeOps, FixIn, FixOut, JvmExcept, given}
import sturdy.values.{Abstractly, Topped, given}
import sturdy.values.integer.given
import sturdy.values.floating.given
import sturdy.values.objects.{Object, ObjectOps, given}
import sturdy.values.arrays.{Array, ArrayOps, given}

import java.net.URL

object ConstantAnalysis extends Interpreter, ConstantValues, Exceptions:
  override type J[A] = WithJoin[A]
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
  override type ExcV = JvmExceptAbstract[Value]


//  given valuesAbstractly: Abstractly[ConcreteInterpreter.Value, Value] with
//    override def apply(c: ConcreteInterpreter.Value): Value = c match
//      case ConcreteInterpreter.Value.TopValue => Value.TopValue
//      case ConcreteInterpreter.Value.Int32(i) => Value.Int32(Topped.Actual(i))
//      case ConcreteInterpreter.Value.Int64(l) => Value.Int64(Topped.Actual(l))
//      case ConcreteInterpreter.Value.Float32(f) => Value.Float32(Topped.Actual(f))
//      case ConcreteInterpreter.Value.Float64(d) => Value.Float64(Topped.Actual(d))
//      //case ConcreteInterpreter.Value.Obj(o) => Value.Obj(Topped.Actual(o))
//      //case ConcreteInterpreter.Value.Array(a) => Value.Array(Topped.Actual(a))

  class Instance extends GenericInstance:

    private given Instance = this

    override val fixpoint: fix.Fixpoint[FixIn, FixOut] = ???
//      fix.notContextSensitive(fix.iter.innermost(StackedStates())).fixpoint

    override val fixpointSuper = fixpoint

    val joinUnit: WithJoin[Unit] = implicitly
    val jvV: WithJoin[ConstantAnalysis.Value] = implicitly

    override val stack = new JoinableDecidableOperandStack
    override val failure = new CollectedFailures[BytecodeFailure]
    override val except = new JoinedExcept()
    override val objFieldAlloc = implicitly
    override val objAlloc = implicitly
    override val arrayValAlloc = implicitly
    override val arrayAlloc = implicitly
    override val objFieldStore = implicitly
    override val arrayValStore = implicitly
    override val staticVarStore = implicitly
    override val frame = implicitly
    override val project: Project[URL] = ???
    override val projectSource: String = ???

    override val bytecodeOps: BytecodeOps[Topped[FrameData], Topped[FrameData], ConstantAnalysis.Value, ReferenceType] = ???
    override val objectOps: ObjectOps[Topped[FrameData], String, Topped[FrameData], ConstantAnalysis.Value, ClassFile, Object[Topped[FrameData], ClassFile, Topped[FrameData], String], ConstantAnalysis.Value, AllocationSite, Method, String, MethodDescriptor, ConstantAnalysis.Value, MayJoin.NoJoin] = ???
    override val arrayOps: ArrayOps[Topped[FrameData], Topped[FrameData], ConstantAnalysis.Value, ConstantAnalysis.Value, Array[Topped[FrameData], Topped[FrameData], ArrayType], ConstantAnalysis.Value, ArrayType, AllocationSite, MayJoin.NoJoin] = ???



