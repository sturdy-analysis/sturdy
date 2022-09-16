package sturdy.language.jimple

import sturdy.data.MayJoin
import sturdy.data.MayJoin.NoJoin
import sturdy.values.relational.CompareOps
import sturdy.values.relational.LiftedCompareOps
import sturdy.values.types.ConcreteTypeOfOps
//import sturdy.language.jimple.ObjectOps//, ClassOps, GenericInterpreter}
import sturdy.values.*
import sturdy.values.floating.*
import sturdy.values.integer.*
import sturdy.values.convert.*
import sturdy.values.records.RecordOps
import sturdy.values.types.TypeOfOps
//import sturdy.language.jimple.ClassOps
import sturdy.effect.failure.{Failure, FailureKind}
//import sturdy.language.jimple.JimpleOps
import sturdy.language.jimple.*


trait Interpreter:
  type J[A] <: MayJoin[A]
  type VIntC
  type VLongC
  type VFloatC
  type VDoubleC
  //  type VStringC
  type VNullC
//  type VIntT //FIXME: Do I need Type-Values?
//  type VDoubleT
//  type VFloatT
//  type VLongT
//  type VRefT
//  type VVoidT
//  type VExcRef
//  type VParamRef
//  type VThisRef
  type VClass
  type VObject

  enum Value:
    case TopValue
    case IntConstValue(i: VIntC)
    case LongConstValue(l: VLongC)
    case FloatConstValue(f: VFloatC)
    case DoubleConstValue(d: VDoubleC)
    //    case StringConstValue(s: VStringC)
    case NullConstValue(n: VNullC)
//    case IntTypeValue(t: VIntT)
//    case DoubleTypeValue(t: VDoubleT)
//    case FloatTypeValue(t: VFloatT)
//    case LongTypeValue(t: VLongT)
//    case RefTypeValue(t: VRefT)
//    case VoidTypeValue(t: VVoidT)
//    case ExcRefValue(exc: VExcRef)
//    case ParamRefValue(param: VParamRef)
//    case ThisRefValue(th: VThisRef)
    case ClassValue(c: VClass)
    case ObjectValue(o: VObject)

    def asInt(using f: Failure): VIntC = this match
      case IntConstValue(i) => i
      case TopValue => topInt
      case _ => f.fail(TypeError, s"Expected Int but got $this")
    def asLong(using f: Failure): VLongC = this match
      case LongConstValue(l) => l
      case TopValue => topLong
      case _ => f.fail(TypeError, s"Expected Long but got $this")
    def asFloat(using f: Failure): VFloatC = this match
      case FloatConstValue(f) => f
      case TopValue => topFloat
      case _ => f.fail(TypeError, s"Expected Float but got $this")
    def asDouble(using f: Failure): VDoubleC = this match
      case DoubleConstValue(d) => d
      case TopValue => topDouble
      case _ => f.fail(TypeError, s"Expected Double but got $this")
    def asClass(using f: Failure): VClass = this match
      case ClassValue(c) => c
      case TopValue => topClass
      case _ => f.fail(TypeError, s"Expected Class but got $this")
    def asObject(using f: Failure): VObject = this match
      case ObjectValue(o) => o
      case TopValue => topObject
      case _ => f.fail(TypeError, s"Expected Object but got $this")

  def topInt: VIntC
  def topDouble: VDoubleC
  def topFloat: VFloatC
  def topLong: VLongC
  def topNull: VNullC
  def topClass: VClass
  def topObject: VObject

  given Top[Value] with
    def top = Value.TopValue

  import Value.*

//  type Instance <: GenericInstance
//  abstract class GenericInstance
//    extends GenericInterpreter[Value, J]:

  def evalTypes(v: Value): Type =
    v match
      case IntConstValue(_) => Type.IntT
      case LongConstValue(_) => Type.LongT
      case FloatConstValue(_) => Type.FloatT
      case DoubleConstValue(_) => Type.DoubleT
      case _ => throw new IllegalArgumentException(s"Can't type this input")
    //given Instance = this.asInstanceOf[Instance]
  given ValueJimpleOps
    (using failure: Failure,
     vintOps: IntegerOps[Int, VIntC],
     vlongOps: IntegerOps[Long, VLongC],
     vfloatOps: FloatOps[Float, VFloatC],
     vdoubleOps: FloatOps[Double, VDoubleC],
     vclassOps: ClassOps[Container, VClass],
     vobjectOps: ObjectOps[String, Value, VObject, Type],
     vconvertIntLong: ConvertIntLong[VIntC, VLongC],
     vconvertIntFloat: ConvertIntFloat[VIntC, VFloatC],
     vconvertIntDouble: ConvertIntDouble[VIntC, VDoubleC],
     vconvertLongFloat: ConvertLongFloat[VLongC, VFloatC],
     vconvertLongDouble: ConvertLongDouble[VLongC, VDoubleC],
     vconvertFloatDouble: ConvertFloatDouble[VFloatC, VDoubleC],
     vcompareLongOps: CompareOps[VLongC, VIntC],
     vcompareDoubleOps: CompareOps[VDoubleC, VIntC]
    ): JimpleOps[Value, Type, J] with

    final val intOps: IntegerOps[Int, Value] = new LiftedIntegerOps(_.asInt, IntConstValue.apply)
    final val longOps: IntegerOps[Long, Value] = new LiftedIntegerOps(_.asLong, LongConstValue.apply)
    final val floatOps: FloatOps[Float, Value] = new LiftedFloatOps(_.asFloat, FloatConstValue.apply)
    final val doubleOps: FloatOps[Double, Value] = new LiftedFloatOps(_.asDouble, DoubleConstValue.apply)
    final val classOps = new ClassOps[Container, Value]:
      def classValue(c: Container): Value = ClassValue(vclassOps.classValue(c))
    final val typeOfOps: TypeOfOps[Value, Type, J] = new TypeOfOps[Value, Type, J]:
      override def typeOf[A](v: Value)(f: Type => A): J[A] ?=> A =
        v match
          case (IntConstValue(_)) => f(Type.IntT)
          case LongConstValue(_) => f(Type.LongT)
          case FloatConstValue(_) => f(Type.FloatT)
          case DoubleConstValue(_) => f(Type.DoubleT)
          case ObjectValue(o) => f(Type.RefT(o.toString))
          case _ => throw new IllegalArgumentException(s"Can't type this input")
    final val objectOps: ObjectOps[String, Value, Value, Type] = new ObjectOps[String, Value, Value, Type]:
      def makeObject(fields: Seq[(String, Value)], oType: Type) = ObjectValue(vobjectOps.makeObject(fields, oType))
      def lookupObjectField(obj: Value, field: String): Value = vobjectOps.lookupObjectField(obj.asObject, field)
      def updateObjectField(obj: Value, field: String, newVal: Value): Value = ObjectValue(vobjectOps.updateObjectField(obj.asObject, field, newVal))
      def nullObject: Value = vobjectOps.nullObject

    final val convertIntLong: ConvertIntLong[Value, Value] = new LiftedConvert(_.asInt, LongConstValue.apply)
    final val convertIntFloat: ConvertIntFloat[Value, Value] = new LiftedConvert(_.asInt, FloatConstValue.apply)
    final val convertIntDouble: ConvertIntDouble[Value, Value] = new LiftedConvert(_.asInt, DoubleConstValue.apply)
    final val convertLongFloat: ConvertLongFloat[Value, Value] = new LiftedConvert(_.asLong, FloatConstValue.apply)
    final val convertLongDouble: ConvertLongDouble[Value, Value] = new LiftedConvert(_.asLong, DoubleConstValue.apply)
    final val convertFloatDouble: ConvertFloatDouble[Value, Value] = new LiftedConvert(_.asFloat, DoubleConstValue.apply)

    final val compareLongOps = new LiftedCompareOps(_.asLong, IntConstValue.apply)
    final val compareDoubleOps = new LiftedCompareOps(_.asDouble, IntConstValue.apply)

  type Instance <: GenericInstance

  abstract class GenericInstance
    extends GenericInterpreter[Value, Type, J]


