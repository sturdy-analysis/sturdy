package sturdy.language.jimple

import scala.language.postfixOps
import sturdy.data.{JOption, MayJoin}
import sturdy.effect.callframe.MutableCallFrame
import sturdy.effect.failure.Failure
import sturdy.effect.symboltable.SymbolTable
import sturdy.values.config
import sturdy.values.convert.*
import sturdy.language.jimple.ClassBodyElement.GlobalVarCB

// operations on classes
trait ClassOps[C, V]:
  def classValue(c: C): V

// operations on strings
trait StringOps[S, V]:
  def stringValue(s: S): V

// operations on objects
trait ObjectOps[F, V, O, T]:
  def makeObject(fields: Seq[(F, V)], objectType: T): O
  def lookupObjectField(obj: O, field: F): V
  def updateObjectField(obj: O, field: F, newVal: V): O
  def nullObject: V

type QName = (String, String) // fully qualified class name + field/method name
case class RuntimeUnit(name: String, fields: Map[QName, ClassBodyElement.GlobalVarCB], methods: Map[QName, ClassBodyElement.MethodCB], isAbstract: Boolean) // interfaces are just like abstract classes
type RTU = RuntimeUnit

// the generic interpreter for jimple
trait GenericInterpreter[V, J[_] <: MayJoin[_]]:

  // defintions for join behavior on values, unit and runtimeunits
  implicit def jvV: J[V]
  implicit def jvUnit: J[Unit]
  implicit def jvRTU: J[RTU]

  /* Value components */
  val jimpleOps: JimpleOps[V, Type, J]

  // helper function needed for turning array types to strings
  def addBrackets(s: String, i: Integer): String =
    var ret = ""
    for(j <- 0 until i)
      ret = s.concat("[]")
    ret


  /* Effect components */
  val failure: Failure
  import failure.*
  type CallFrameData = Unit
  val callFrame: MutableCallFrame[CallFrameData, Identifier, V, J]
  val classTable: SymbolTable[String, String, Container, J]
  val globalsTable: SymbolTable[String, Identifier, V, J]

  // helper functions for interacting with the class table
  def getClassOption(name: String): JOption[J, Container] =
    classTable.get(name, name)
  def getClass(name: String): Container =
    val optionClass = getClassOption(name)
    optionClass match
      case c: Container => c
      case _ => throw new ClassNotFoundException(name)

  val runTimeTable: SymbolTable[String, String, RuntimeUnit, J]
  // helper functions for interacting with the runtime table
  def getMethod(name: QName): ClassBodyElement.MethodCB =
    val optionRunTimeUnit = runTimeTable.get(name(0), name(0)).getOrElse(fail(RuntimeUnitNotLoaded, "oh no"))
    optionRunTimeUnit match
      case RuntimeUnit(_, _, methods, _) => methods.getOrElse(name, fail(MethodNotLoaded, name(1)))
  def getField(name: QName): ClassBodyElement.GlobalVarCB =
    val optionRunTimeUnit = runTimeTable.get(name(0), name(0)).getOrElse(fail(RuntimeUnitNotLoaded, "oh no"))
    optionRunTimeUnit match
      case RuntimeUnit(_, fields, _, _) => fields.getOrElse(name, fail(UnboundField, name(1)))
  def getFields(name: String): Map[QName, ClassBodyElement.GlobalVarCB] =
    runTimeTable.get(name, name).getOrElse(fail(RuntimeUnitNotLoaded, name)).fields

  // helper function since booleans are represented as integers in Jimple
  def boolToV(b: Boolean): V =
    if !b then constant(Constant.IntC(0))
    else constant(Constant.IntC(1))

  // evaluation of constants with help of jimpleOps
  def constant(c: Constant): V = c match
    case Constant.IntC(v) => jimpleOps.intOps.integerLit(v)
    case Constant.LongC(v) => jimpleOps.longOps.integerLit(v)
    case Constant.FloatC(v) => jimpleOps.floatOps.floatingLit(v)
    case Constant.DoubleC(v) => jimpleOps.doubleOps.floatingLit(v)

    case Constant.FloatInfinityC => jimpleOps.floatOps.floatingLit(Float.PositiveInfinity)
    case Constant.FloatNegInfinityC => jimpleOps.floatOps.floatingLit(Float.NegativeInfinity)
    case Constant.FloatNanC => jimpleOps.floatOps.floatingLit(Float.NaN)

    case Constant.InfinityC => jimpleOps.doubleOps.floatingLit(Double.PositiveInfinity)
    case Constant.NegInfinityC => jimpleOps.doubleOps.floatingLit(Double.NegativeInfinity)
    case Constant.NanC => jimpleOps.doubleOps.floatingLit(Double.NaN)

    case Constant.StringC(v) => jimpleOps.stringOps.stringValue(v)
    case Constant.NullC => jimpleOps.objectOps.nullObject

  // evaluation of immediates, either by further evaluation or reading from callframe/class table
  def evalImmediate(i: Immediate): V = i match
    case Immediate.ConstI(c) => constant(c)
    case Immediate.LocalI(l) => callFrame.getLocalByName(l.id).getOrElse(fail(UnboundLocal, l.id))
    case Immediate.ClassI(name) => getClassOption(name).map(jimpleOps.classOps.classValue).getOrElse(fail(UnboundClass, name))

  // turning types into a String representation
  def typeToString(t: Type): String = t match
    case Type.IntT => "Int"
    case Type.LongT => "Long"
    case Type.FloatT => "Float"
    case Type.DoubleT => "Double"
    case Type.RefT(s) => s
    case Type.VoidT => "Unit"
    case Type.ArrayT(t, dim) => addBrackets(typeToString(t), dim)

  // evaluation of the right side of assignments
  def evalRVal(r: RVal): V = r match
    case RVal.ArrayRefR(i1, i2) => ???
    case RVal.ConstR(c) => constant(c)
    case RVal.ExpressionR(e) => evalExp(e)
    case RVal.InstanceFieldRefR(i, f) => // reading field from an object
      val obj = evalImmediate(i)
      jimpleOps.objectOps.lookupObjectField(obj, f.id)
    case RVal.StaticFieldRefR(f) =>
      f.t match
        case Type.RefT(s) =>
          callFrame.getLocalByName(s)
            .getOrElse(fail(UnboundLocal, s))
        case _ => throw new IllegalArgumentException("Need a ref type for this")
    case RVal.LocalR(l) => callFrame.getLocalByName(l.id).getOrElse(fail(UnboundLocal, l.id))
    case RVal.ClassR(s) => getClassOption(s).map(jimpleOps.classOps.classValue).getOrElse(fail(UnboundClass, s))

  // evaluation of binary operations with jimpleOps
  def evalBinop(v1: V, v2: V, op: BinOp): V = op match {
    case BinOp.Add => jimpleOps.typeOfOps.typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => jimpleOps.intOps.add(v1, v2)
      case (Type.IntT, Type.LongT) => jimpleOps.longOps.add(jimpleOps.convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.FloatT) => jimpleOps.floatOps.add(jimpleOps.convertIntFloat(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.DoubleT) => jimpleOps.doubleOps.add(jimpleOps.convertIntDouble(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => jimpleOps.longOps.add(v1, jimpleOps.convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => jimpleOps.longOps.add(v1, v2)
      case (Type.LongT, Type.FloatT) => jimpleOps.floatOps.add(jimpleOps.convertLongFloat(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.DoubleT) => jimpleOps.doubleOps.add(jimpleOps.convertLongDouble(v1, config.Bits.Signed), v2)
      case (Type.FloatT, Type.IntT) => jimpleOps.floatOps.add(v1, jimpleOps.convertIntFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.LongT) => jimpleOps.floatOps.add(v1, jimpleOps.convertLongFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.FloatT) => jimpleOps.floatOps.add(v1, v2)
      case (Type.FloatT, Type.DoubleT) => jimpleOps.doubleOps.add(jimpleOps.convertFloatDouble(v1, NilCC), v2)
      case (Type.DoubleT, Type.IntT) => jimpleOps.doubleOps.add(v1, jimpleOps.convertIntFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.LongT) => jimpleOps.doubleOps.add(v1, jimpleOps.convertLongFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.FloatT) => jimpleOps.doubleOps.add(v1, jimpleOps.convertFloatDouble(v2, NilCC))
      case (Type.DoubleT, Type.DoubleT) => jimpleOps.doubleOps.add(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot add inputs of kinds, $v1 and $v2")
    }
    case BinOp.And => jimpleOps.typeOfOps.typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => jimpleOps.intOps.bitAnd(v1, v2)
      case (Type.IntT, Type.LongT) => jimpleOps.longOps.bitAnd(jimpleOps.convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => jimpleOps.longOps.bitAnd(v1, jimpleOps.convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => jimpleOps.longOps.bitAnd(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot use bitwise and on inputs of kinds, $v1 and $v2")
    }
    case BinOp.Cmp => jimpleOps.typeOfOps.typeOf(v1, v2) {
//      case (Type.IntT, Type.IntT) => jjimpleOps.compareIntOps.cmp(v1, v2)
//      case (Type.IntT, Type.LongT) => jimpleOps.compareLongOps.cmp(jimpleOps.convertIntLong(v1, config.Bits.Signed), v2)
//      case (Type.LongT, Type.IntT) => jimpleOps.compareLongOps.cmp(v1, jimpleOps.convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => jimpleOps.compareLongOps.cmp(v1, v2)
//      case (Type.FloatT, Type.FloatT) => jjimpleOps.compareFloatOps.cmp(v1, v2)
//      case (Type.FloatT, Type.DoubleT) => jimpleOps.compareDoubleOps.cmp(jimpleOps.convertFloatDouble(v1, config.Bits.Signed), v2)
//      case (Type.DoubleT, Type.FloatT) => jimpleOps.compareDoubleOps.cmp(v1, jimpleOps.convertFloatDouble(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.DoubleT) => jimpleOps.compareDoubleOps.cmp(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot compare inputs of kinds, $v1 and $v2")
    }
    case BinOp.Cmpg => jimpleOps.typeOfOps.typeOf(v1, v2) {
      case (Type.DoubleT, Type.DoubleT) =>
        jimpleOps.compareDoubleOps.cmp(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot compare inputs of kinds, $v1 and $v2")
    }
    case BinOp.Cmpl => jimpleOps.typeOfOps.typeOf(v1, v2) {
      case (Type.DoubleT, Type.DoubleT) =>
        jimpleOps.compareDoubleOps.cmp(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot compare inputs of kinds, $v1 and $v2")
    }
    case BinOp.Div => jimpleOps.typeOfOps.typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => jimpleOps.intOps.div(v1, v2)
      case (Type.IntT, Type.LongT) => jimpleOps.longOps.div(jimpleOps.convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.FloatT) => jimpleOps.floatOps.div(jimpleOps.convertIntFloat(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.DoubleT) => jimpleOps.doubleOps.div(jimpleOps.convertIntDouble(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => jimpleOps.longOps.div(v1, jimpleOps.convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => jimpleOps.longOps.div(v1, v2)
      case (Type.LongT, Type.FloatT) => jimpleOps.floatOps.div(jimpleOps.convertLongFloat(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.DoubleT) => jimpleOps.doubleOps.div(jimpleOps.convertLongDouble(v1, config.Bits.Signed), v2)
      case (Type.FloatT, Type.IntT) => jimpleOps.floatOps.div(v1, jimpleOps.convertIntFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.LongT) => jimpleOps.floatOps.div(v1, jimpleOps.convertLongFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.FloatT) => jimpleOps.floatOps.div(v1, v2)
      case (Type.FloatT, Type.DoubleT) => jimpleOps.doubleOps.div(jimpleOps.convertFloatDouble(v1, NilCC), v2)
      case (Type.DoubleT, Type.IntT) => jimpleOps.doubleOps.div(v1, jimpleOps.convertIntFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.LongT) => jimpleOps.doubleOps.div(v1, jimpleOps.convertLongFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.FloatT) => jimpleOps.doubleOps.div(v1, jimpleOps.convertFloatDouble(v2, NilCC))
      case (Type.DoubleT, Type.DoubleT) => jimpleOps.doubleOps.div(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot divide inputs of kinds, $v1 and $v2")
    }
    case BinOp.Mul => jimpleOps.typeOfOps.typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => jimpleOps.intOps.mul(v1, v2)
      case (Type.IntT, Type.LongT) => jimpleOps.longOps.mul(jimpleOps.convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.FloatT) => jimpleOps.floatOps.mul(jimpleOps.convertIntFloat(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.DoubleT) => jimpleOps.doubleOps.mul(jimpleOps.convertIntDouble(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => jimpleOps.longOps.mul(v1, jimpleOps.convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => jimpleOps.longOps.mul(v1, v2)
      case (Type.LongT, Type.FloatT) => jimpleOps.floatOps.mul(jimpleOps.convertLongFloat(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.DoubleT) => jimpleOps.doubleOps.mul(jimpleOps.convertLongDouble(v1, config.Bits.Signed), v2)
      case (Type.FloatT, Type.IntT) => jimpleOps.floatOps.mul(v1, jimpleOps.convertIntFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.LongT) => jimpleOps.floatOps.mul(v1, jimpleOps.convertLongFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.FloatT) => jimpleOps.floatOps.mul(v1, v2)
      case (Type.FloatT, Type.DoubleT) => jimpleOps.doubleOps.mul(jimpleOps.convertFloatDouble(v1, NilCC), v2)
      case (Type.DoubleT, Type.IntT) => jimpleOps.doubleOps.mul(v1, jimpleOps.convertIntFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.LongT) => jimpleOps.doubleOps.mul(v1, jimpleOps.convertLongFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.FloatT) => jimpleOps.doubleOps.mul(v1, jimpleOps.convertFloatDouble(v2, NilCC))
      case (Type.DoubleT, Type.DoubleT) => jimpleOps.doubleOps.mul(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot multiply inputs of kinds, $v1 and $v2")
    }
    case BinOp.Or => jimpleOps.typeOfOps.typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => jimpleOps.intOps.bitOr(v1, v2)
      case (Type.IntT, Type.LongT) => jimpleOps.longOps.bitOr(jimpleOps.convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => jimpleOps.longOps.bitOr(v1, jimpleOps.convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => jimpleOps.longOps.bitOr(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot use bitwise or on inputs of kinds, $v1 and $v2")
    }
    case BinOp.Rem => jimpleOps.typeOfOps.typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => jimpleOps.intOps.remainder(v1, v2)
      case (Type.IntT, Type.LongT) => jimpleOps.longOps.remainder(jimpleOps.convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.FloatT) => jimpleOps.floatOps.remainder(jimpleOps.convertIntFloat(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.DoubleT) => jimpleOps.doubleOps.remainder(jimpleOps.convertIntDouble(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => jimpleOps.longOps.remainder(v1, jimpleOps.convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => jimpleOps.longOps.remainder(v1, v2)
      case (Type.LongT, Type.FloatT) => jimpleOps.floatOps.remainder(jimpleOps.convertLongFloat(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.DoubleT) => jimpleOps.doubleOps.remainder(jimpleOps.convertLongDouble(v1, config.Bits.Signed), v2)
      case (Type.FloatT, Type.IntT) => jimpleOps.floatOps.remainder(v1, jimpleOps.convertIntFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.LongT) => jimpleOps.floatOps.remainder(v1, jimpleOps.convertLongFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.FloatT) => jimpleOps.floatOps.remainder(v1, v2)
      case (Type.FloatT, Type.DoubleT) => jimpleOps.doubleOps.remainder(jimpleOps.convertFloatDouble(v1, NilCC), v2)
      case (Type.DoubleT, Type.IntT) => jimpleOps.doubleOps.remainder(v1, jimpleOps.convertIntFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.LongT) => jimpleOps.doubleOps.remainder(v1, jimpleOps.convertLongFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.FloatT) => jimpleOps.doubleOps.remainder(v1, jimpleOps.convertFloatDouble(v2, NilCC))
      case (Type.DoubleT, Type.DoubleT) => jimpleOps.doubleOps.remainder(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot calculate remainder of inputs of kinds, $v1 and $v2")
    }
    case BinOp.Shl => jimpleOps.typeOfOps.typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => jimpleOps.intOps.shiftLeft(v1, v2)
      case (Type.LongT, Type.IntT) => jimpleOps.longOps.shiftLeft(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot use left shift inputs of kinds, $v1 and $v2")
    }
    case BinOp.Shr => jimpleOps.typeOfOps.typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => jimpleOps.intOps.shiftRight(v1, v2)
      case (Type.LongT, Type.IntT) => jimpleOps.longOps.shiftRight(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot use right shift inputs of kinds, $v1 and $v2")
    }
    case BinOp.Sub => jimpleOps.typeOfOps.typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => jimpleOps.intOps.sub(v1, v2)
      case (Type.IntT, Type.LongT) => jimpleOps.longOps.sub(jimpleOps.convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.FloatT) => jimpleOps.floatOps.sub(jimpleOps.convertIntFloat(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.DoubleT) => jimpleOps.doubleOps.sub(jimpleOps.convertIntDouble(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => jimpleOps.longOps.sub(v1, jimpleOps.convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => jimpleOps.longOps.sub(v1, v2)
      case (Type.LongT, Type.FloatT) => jimpleOps.floatOps.sub(jimpleOps.convertLongFloat(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.DoubleT) => jimpleOps.doubleOps.sub(jimpleOps.convertLongDouble(v1, config.Bits.Signed), v2)
      case (Type.FloatT, Type.IntT) => jimpleOps.floatOps.sub(v1, jimpleOps.convertIntFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.LongT) => jimpleOps.floatOps.sub(v1, jimpleOps.convertLongFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.FloatT) => jimpleOps.floatOps.sub(v1, v2)
      case (Type.FloatT, Type.DoubleT) => jimpleOps.doubleOps.sub(jimpleOps.convertFloatDouble(v1, NilCC), v2)
      case (Type.DoubleT, Type.IntT) => jimpleOps.doubleOps.sub(v1, jimpleOps.convertIntFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.LongT) => jimpleOps.doubleOps.sub(v1, jimpleOps.convertLongFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.FloatT) => jimpleOps.doubleOps.sub(v1, jimpleOps.convertFloatDouble(v2, NilCC))
      case (Type.DoubleT, Type.DoubleT) => jimpleOps.doubleOps.sub(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot perform subtraction on inputs of kinds, $v1 and $v2")
    }
    case BinOp.Ushr => jimpleOps.typeOfOps.typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => jimpleOps.intOps.shiftRightUnsigned(v1, v2)
      case (Type.LongT, Type.IntT) => jimpleOps.longOps.shiftRightUnsigned(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot use unsigned right shift inputs of kinds, $v1 and $v2")
    }
    case BinOp.Xor => jimpleOps.typeOfOps.typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => jimpleOps.intOps.bitXor(v1, v2)
      case (Type.IntT, Type.LongT) => jimpleOps.longOps.bitXor(jimpleOps.convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => jimpleOps.longOps.bitXor(v1, jimpleOps.convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => jimpleOps.longOps.bitXor(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot use bitwise xor on inputs of kinds, $v1 and $v2")
    }
  }

  // evaluation of conditional operations
  def evalCondOp(v1: V, v2: V, op: CondOp): V = op match { // Only used on integer and diverse
    case CondOp.Eq => jimpleOps.typeOfOps.typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => boolToV(jimpleOps.intOps.eq(v1, v2))
      case (Type.IntT, Type.LongT) => boolToV(jimpleOps.longOps.eq(jimpleOps.convertIntLong(v1, config.Bits.Signed), v2))
      case (Type.IntT, Type.FloatT) => boolToV(jimpleOps.floatOps.eq(jimpleOps.convertIntFloat(v1, config.Bits.Signed), v2))
      case (Type.IntT, Type.DoubleT) => boolToV(jimpleOps.doubleOps.eq(jimpleOps.convertIntDouble(v1, config.Bits.Signed), v2))
      case (Type.LongT, Type.IntT) => boolToV(jimpleOps.longOps.eq(v1, jimpleOps.convertIntLong(v2, config.Bits.Signed)))
      case (Type.LongT, Type.LongT) => boolToV(jimpleOps.longOps.eq(v1, v2))
      case (Type.LongT, Type.FloatT) => boolToV(jimpleOps.floatOps.eq(jimpleOps.convertLongFloat(v1, config.Bits.Signed), v2))
      case (Type.LongT, Type.DoubleT) => boolToV(jimpleOps.doubleOps.eq(jimpleOps.convertLongDouble(v1, config.Bits.Signed), v2))
      case (Type.FloatT, Type.IntT) => boolToV(jimpleOps.floatOps.eq(v1, jimpleOps.convertIntFloat(v2, config.Bits.Signed)))
      case (Type.FloatT, Type.LongT) => boolToV(jimpleOps.floatOps.eq(v1, jimpleOps.convertLongFloat(v2, config.Bits.Signed)))
      case (Type.FloatT, Type.FloatT) => boolToV(jimpleOps.floatOps.eq(v1, v2))
      case (Type.FloatT, Type.DoubleT) => boolToV(jimpleOps.doubleOps.eq(jimpleOps.convertFloatDouble(v1, NilCC), v2))
      case (Type.DoubleT, Type.IntT) => boolToV(jimpleOps.doubleOps.eq(v1, jimpleOps.convertIntFloat(v2, config.Bits.Signed)))
      case (Type.DoubleT, Type.LongT) => boolToV(jimpleOps.doubleOps.eq(v1, jimpleOps.convertLongFloat(v2, config.Bits.Signed)))
      case (Type.DoubleT, Type.FloatT) => boolToV(jimpleOps.doubleOps.eq(v1, jimpleOps.convertFloatDouble(v2, NilCC)))
      case (Type.DoubleT, Type.DoubleT) => boolToV(jimpleOps.doubleOps.eq(v1, v2))
      case (_, _) => throw new IllegalArgumentException(s"Cannot add inputs of kinds, $v1 and $v2")
    }
    case CondOp.Ge => ???
    case CondOp.Le => ???
    case CondOp.Ne => ???
    case CondOp.Gt => ???
    case CondOp.Lt => ???
  }

  // evaluation of class body elements
  def evalClassBodyElement(el: ClassBodyElement): V = el match {
    // adding to the globals table
    case ClassBodyElement.GlobalVarCB(_, _, _, _, _, _, _, _, t, id) =>
      globalsTable.set(id, id, constant(t.getEmpty))
      globalsTable.get(id, id).getOrElse(fail(UnboundGlobal, id))
    case ClassBodyElement.NativeCallCB(isPublic, isPrivate, isProtected, isStatic, isFinal, isSynchronized, isNative, t, id, params, except) =>
      ???
    // evaluating the identity statements and statements and lastly returning the value of the local "return" from the call frame
    case ClassBodyElement.MethodCB(_, _, idStmts, stmts, _) =>
      idStmts.foreach(s => evalStmt(s))
      stmts.foreach(s => evalStmt(s))
      callFrame.getLocalByName("return").getOrElse(constant(Constant.NanC))
    case  ClassBodyElement.MethodHeaderCB(header) =>
      ???
  }

  // evaluation of expressions
  def evalExp(e: Exp): V = e match
    case Exp.UnopE(i, op) => ???
    case Exp.BinopE(i1, i2, op: BinOp) => // furhter evaluation
      val v1 = evalImmediate(i1)
      val v2 = evalImmediate(i2)
      evalBinop(v1, v2, op)
    case Exp.ConditionE(i1, i2, op) => // further evaluation
      val v1 = evalImmediate(i1)
      val v2 = evalImmediate(i2)
      evalCondOp(v1, v2, op)
    case Exp.CastE(t, i) => ???
    case Exp.InstanceOfE(i, ref) => ???
    case Exp.StaticInvokeE(s, l) => // creating a new callframe and calling the method from it
      val methodInfos = evalMethodSignature(s)
      val locals = createLocals(methodInfos, l)
      val method = getMethod(methodInfos(0))
      callFrame.withNew((), locals) {
        evalClassBodyElement(method)
      }
    case Exp.InvokeE(_, _, s, l) => // creating a new callframe and calling the method from it
      val methodInfos = evalMethodSignature(s)
      val locals = createLocals(methodInfos, l)
      val method = getMethod(methodInfos(0))
      callFrame.withNew((), locals) {
        constant(Type.VoidT.getEmpty)
      }
    case Exp.NewE(t) => // creating new object based on the type
      val newType = typeToString(t)
      val containerItem = getClassOption(newType).map(jimpleOps.classOps.classValue).getOrElse(fail(UnboundClass, newType))
      containerItem match
        case Container.ClassC(_, _, _, _, _, _, _, _, _, body) =>
          val bodyIDs = body.map(el => el.getID)
          val bodyElements = body.map(el => evalClassBodyElement(el))
          val fields = makeIdTuple(bodyIDs, bodyElements)
          jimpleOps.objectOps.makeObject(fields, t)
        case Container.InterfaceC(_, _, _, _, _, body) =>
          val bodyIDs = body.map(el => el.getID)
          val bodyElements = body.map(el => evalClassBodyElement(el))
          val fields = makeIdTuple(bodyIDs, bodyElements)
          jimpleOps.objectOps.makeObject(fields, t)
    case Exp.NewArrayE(t, i) => ???
    case Exp.NewMultArrE(t, dims) => ???

  // helper function for creating all locals needed for a new call frame
  def createLocals(methodInfos: ((String, String), Type), l: scala.collection.Seq[Immediate]): Map[Identifier, V] =
    val methodName = methodInfos(0)
    val methodRetType = methodInfos(1)
    var locals: Map[Identifier, V] = Map()
    locals += "return" -> constant(methodRetType.getEmpty)
    locals += "@this" -> callFrame.getLocalByName("@this").getOrElse(fail(UnboundLocal, "this is not known"))
    for i <- l.indices do
      locals += "@parameter"+i -> evalImmediate(l(i))
    val method = getMethod(methodName)
    method.locals.foreach(l =>
      val evaluatedDec = evalLocalDec(l)
      locals += evaluatedDec(0) -> evaluatedDec(1)
      //adding the globals of all called classes to the call frame
      var globals: Map[(String,String), GlobalVarCB] = Map()
        l.t match
      case Type.RefT(s) =>
        globals = getFields(s)
        for ((_, g) <- globals)
          locals += s -> constant(g.t.getEmpty)
      case _ => ()
    )
    locals

  // helper function for creating tuples needed for object creation
  def makeIdTuple(ids: scala.collection.Seq[String], els: scala.collection.Seq[V]): Seq[(String, V)] =
    val res = Seq.empty
    for i <- ids.indices do res.appended((ids(i), els(i)))
    res

  // evaluation of invocation types
  def evalInvokeType(t: InvokeType): String = t match
    case InvokeType.InterfaceI => "interface"
    case InvokeType.SpecialI => "special"
    case InvokeType.VirtualI => "virtual"

  // evaluation of statements
  def evalStmt(s: Stmt): Unit = s match
    case Stmt.AssignS(v, r) => // further evaluation and interaction with the call frame
      val rVal = evalRVal(r)
      v match
        case Var.LocalV(l) =>
          callFrame.getLocalByName(l.id).getOrElse(fail(UnboundLocal, l.id))
          callFrame.setLocalByName(l.id, rVal)
        case Var.InstanceFieldRefV(i, f) =>
          val obj = evalImmediate(i)
          jimpleOps.objectOps.updateObjectField(obj, f.id, rVal)
        case Var.ArrayRefV(i1, i2) => ???
        case Var.StaticFieldRefV(f) => ???
    case Stmt.IdentityS(l, identityVal) => callFrame.setLocalByName(l.id, evalIdentityVal(identityVal)) //setting the call frame
    case Stmt.BreakpointS => ???
    case Stmt.ExceptionIdentityS(l, identityVal) => ???
    case Stmt.EnterMonitorS(i) => ???
    case Stmt.ExitMonitorS(i) => ???
    case Stmt.GotoS(l) => ???
    case Stmt.IfS(cond, l) => ???
    case Stmt.InvokeS(e) => evalExp(e)
    case Stmt.LookupSwitchS(i, cases, l) => ???
    case Stmt.NopS => ???
    case Stmt.RetS(l) => // setting the value of the "return" variable in the call frame
      callFrame.setLocalByName("return", callFrame.getLocalByName(l.id)
        .getOrElse(fail(UnboundLocal, l.id)))
    case Stmt.ReturnS(i) => callFrame.setLocalByName("return", evalImmediate(i)) // setting the value of the "return" variable in the call frame
    case Stmt.ReturnVoidS => () // setting the value of the "return" variable in the call frame
    case Stmt.TableSwitchS(i, cases, l) => ???
    case Stmt.ThrowS(i) => ???
    case Stmt.LabelS(l) => ???
    case Stmt.CatchS(excRange) => ???

  // evaluation of classes and interfaces
  def evalContainer(c: Container): String = c match
    case Container.ClassC(_, _, isAbstract, _, _, _, id, _, _, body) =>
      val bodyEls = splitUpBody(c.getID, body)
      val runtimeUnit = RuntimeUnit(id, bodyEls(0), bodyEls(1), isAbstract)
      //adding class to all tables
      classTable.set(id, id, c)
      runTimeTable.set(id, id, runtimeUnit)
      val fields: Seq[(String, V)] = Seq.empty
      for ((name, global) <- bodyEls(0))
        fields.appended((name(1), evalClassBodyElement(global)))
      //filling callFrame to start with method "main"
      val mainMethod = getMethod((id,"main"))
      var locals: Map[Identifier, V] = Map()
      locals += "@this" -> jimpleOps.objectOps.makeObject(fields, Type.RefT(id))
      for i <- mainMethod.header.params.indices do
        locals += "@parameter"+i -> constant(mainMethod.header.params(i).getEmpty)
      mainMethod.locals.foreach(l =>
        val evaluatedDec = evalLocalDec(l)
        locals += evaluatedDec(0) -> evaluatedDec(1)
        //adding the globals of all called classes to the call frame
        var globals: Map[(String,String), GlobalVarCB] = Map()
        l.t match
          case Type.RefT(s) =>
            globals = getFields(s)
            for ((_, g) <- globals) {
              locals += s -> constant(g.t.getEmpty)
            }
          case _ => ()
      )
      var state = callFrame.getState
      callFrame.withNew((), locals) {
        evalClassBodyElement(mainMethod)
        state = callFrame.getState
      }
      state.toString
    case Container.InterfaceC(isPublic, isPrivate, id, extend, implement, body) => ???


  // helper function for retrieving fields and methods from a class
  def splitUpBody(className: String, body: collection.Seq[ClassBodyElement]): (Map[QName, ClassBodyElement.GlobalVarCB], Map[QName, ClassBodyElement.MethodCB]) =
    var fields = Map[QName, ClassBodyElement.GlobalVarCB]()
    var methods = Map[QName, ClassBodyElement.MethodCB]()

    body.foreach{ bodyEl =>
      bodyEl match
        case c:ClassBodyElement.GlobalVarCB => c match
          case ClassBodyElement.GlobalVarCB(_, _, _, _, _, _, _, _, _, id) =>
            fields = fields + ((className, id) -> c)
        case c:ClassBodyElement.MethodCB => c match
          case ClassBodyElement.MethodCB(_, _, _, _, _) =>
            methods = methods + ((className, bodyEl.getID) -> c)
        case ClassBodyElement.MethodHeaderCB(_) =>
          ()
        case ClassBodyElement.NativeCallCB(_,_,_,_,_,_,_,_,_,_,_) =>
          ()
        case null =>
          println(bodyEl.toString)
          throw new IllegalArgumentException(s"A ClassBodyElement is expected.")

    }
    (fields, methods)

  // helper function that returns the name of a local and its empty value
  def evalLocalDec(l: LocalDec): (Identifier, V) =
    (l.name, constant(l.t.getEmpty))

  // evaluation of identity values
  def evalIdentityVal(iv: IdentityVal): V = iv match {
    case IdentityVal.CaughtExcRef() => ???
    case IdentityVal.ParamRef(Constant.IntC(v), _) =>
      callFrame.getLocalByName("@parameter"+v).getOrElse(fail(UnboundLocal, "@parameter"+v))
    case IdentityVal.ThisRef(_) => callFrame.getLocalByName("@this").getOrElse(fail(UnboundLocal, "@this"))
  }

  // evaluation of method header
  def evalMethodHeader(mh: MethodHeader): String = mh.id

  // evaluation of the method signature
  def evalMethodSignature(ms: MethodSignature): (QName, Type) =
    ((ms.classOrigin, ms.id), ms.ret)

  // evaluation of a program by evaluating its containers
  def evalProgram(p: Program): String =
    var res = ""
    p.funs.foreach(c =>
      val tmp: String = evalContainer(c)
      res = res + tmp
    )
    res

