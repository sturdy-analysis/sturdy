package sturdy.language.jimple

import sturdy.data.MayJoin
import sturdy.values.relational.CompareOps
import sturdy.values.relational.LiftedCompareOps
import sturdy.values.*
import sturdy.values.floating.*
import sturdy.values.integer.*
import sturdy.values.convert.*
import sturdy.values.records.RecordOps
import sturdy.values.types.TypeOfOps
import sturdy.effect.failure.Failure
import sturdy.language.jimple.*

// Interpreter as a more fine grained interface for concrete and abstract interpreters
trait Interpreter:

  // Defining all types that a value can take
  type J[A] <: MayJoin[A]
  type VIntC
  type VLongC
  type VFloatC
  type VDoubleC
  type VNullC
  type VStringC
  type VClass
  type VObject
  type VRTU

  // Defining all possible Values
  enum Value:
    case TopValue
    case IntConstValue(i: VIntC)
    case LongConstValue(l: VLongC)
    case FloatConstValue(f: VFloatC)
    case DoubleConstValue(d: VDoubleC)
    case NullConstValue(n: VNullC)
    case ClassValue(c: VClass)
    case ObjectValue(o: VObject)
    case StringValue(s: VStringC)

    // Methods to retrieve the element from a value
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
    def asString(using f: Failure): VStringC = this match
      case StringValue(s) => s
      case TopValue => topString
      case _ => f.fail(TypeError, s"Expected String but got $this")

  // Defining the types of top-instances for all possible values
  def topInt: VIntC
  def topDouble: VDoubleC
  def topFloat: VFloatC
  def topLong: VLongC
  def topNull: VNullC
  def topClass: VClass
  def topObject: VObject
  def topString: VStringC

  given Top[Value] with
    def top: Value = Value.TopValue

  import Value.*

  // Function to calculate the syntax type of a given value
  def evalTypes(v: Value): Type =
    v match
      case IntConstValue(_) => Type.IntT
      case LongConstValue(_) => Type.LongT
      case FloatConstValue(_) => Type.FloatT
      case DoubleConstValue(_) => Type.DoubleT
      case _ => throw new IllegalArgumentException(s"Can't type this input")

  // Defining value operations with the new value types
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
   vcompareDoubleOps: CompareOps[VDoubleC, VIntC],
   vstringOps: StringOps[String, VStringC]
  ): JimpleOps[Value, Type, J] with

    // Defining behaviour of value operations
    final val intOps: IntegerOps[Int, Value] = new LiftedIntegerOps(_.asInt, IntConstValue.apply)
    final val longOps: IntegerOps[Long, Value] = new LiftedIntegerOps(_.asLong, LongConstValue.apply)
    final val floatOps: FloatOps[Float, Value] = new LiftedFloatOps(_.asFloat, FloatConstValue.apply)
    final val doubleOps: FloatOps[Double, Value] = new LiftedFloatOps(_.asDouble, DoubleConstValue.apply)
    final val classOps = new ClassOps[Container, Value]:
      def classValue(c: Container): Value = ClassValue(vclassOps.classValue(c))
    final val typeOfOps: TypeOfOps[Value, Type, J] = new TypeOfOps[Value, Type, J]:
      override def typeOf[A](v: Value)(f: Type => A): J[A] ?=> A =
        v match
          case IntConstValue(_) => f(Type.IntT)
          case LongConstValue(_) => f(Type.LongT)
          case FloatConstValue(_) => f(Type.FloatT)
          case DoubleConstValue(_) => f(Type.DoubleT)
          case ObjectValue(o) => f(Type.RefT(o.toString))
          case _ => throw new IllegalArgumentException(s"Can't type this input")
    final val objectOps: ObjectOps[String, Value, Value, Type] = new ObjectOps[String, Value, Value, Type]:
      def makeObject(fields: Seq[(String, Value)], oType: Type): Value = ObjectValue(vobjectOps.makeObject(fields, oType))
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
    final val stringOps: StringOps[String, Value] = new StringOps[String, Value]:
      def stringValue(s: String): Value = StringValue(vstringOps.stringValue(s))

  // Creating in instance of the GenericInterpreter with the newly defined values
  type Instance <: GenericInstance

  abstract class GenericInstance
    extends GenericInterpreter[Value, J]
