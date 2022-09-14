package sturdy.language.jimple

import sturdy.data.{*, given}
import sturdy.data.MayJoin
import sturdy.data.MayJoin.NoJoin
import sturdy.effect.callframe.{ConcreteCallFrame, MutableCallFrame}
import sturdy.effect.symboltable.ConcreteSymbolTable
import sturdy.language.jimple.Interpreter
import sturdy.language.jimple.GenericInterpreter
import sturdy.fix
import sturdy.effect.failure.CFailure
import sturdy.effect.failure.Failure


object ConcreteInterpreter extends Interpreter:
  override type J[A] = NoJoin[A]
  override type VIntC = Int
  override type VLongC = Long
  override type VFloatC = Float
  override type VDoubleC = Double
  //  type VStringC
//  override type VNullC = ???
  //  type VIntT //FIXME: Do I need Type-Values?
  //  type VDoubleT
  //  type VFloatT
  //  type VLongT
  //  type VRefT
  //  type VVoidT
  //  type VExcRef
  //  type VParamRef
  //  type VThisRef
  override type VClass = Value
  override type VObject = Object[Type, String, Value]

  override def topInt: Int = throw new UnsupportedOperationException
  override def topDouble: Double = throw new UnsupportedOperationException
  override def topFloat: Float = throw new UnsupportedOperationException
  override def topLong: Long = throw new UnsupportedOperationException
  override def topNull = throw new UnsupportedOperationException
  override def topClass = throw new UnsupportedOperationException
  override def topObject = throw new UnsupportedOperationException

  class Instance(rootFrameValues: Iterable[Value])
    extends GenericInstance with fix.Concrete[FixIn, FixOut[Value]]:

    override def jvV: NoJoin[Value] = implicitly

    val callFrame: ConcreteCallFrame[CallFrameData, Identifier, Value] = new ConcreteCallFrame[CallFrameData, Identifier, Value]((), rootFrameValues.view.zipWithIndex.map(_.swap))
    val classTable: ConcreteSymbolTable[Unit, String, Container] = new ConcreteSymbolTable[Unit, String, Container]
    val runTimeTable: ConcreteSymbolTable[Unit, String, RuntimeUnit] = new ConcreteSymbolTable[Unit, String, RuntimeUnit]
    val failure: CFailure = new CFailure
    private given Failure = failure

//    val jimpleOps: JimpleOps[Value, Type, NoJoin] = implicitly
//    val objects: Object[Type, String, Value] = implicitly
