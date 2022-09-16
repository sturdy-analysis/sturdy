package sturdy.language.jimple

import scala.language.postfixOps

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.effect.callframe.MutableCallFrame
import sturdy.effect.failure.Failure
import sturdy.effect.symboltable.SymbolTable
import sturdy.values.config
import sturdy.values.floating.*
import sturdy.values.integer.*
import sturdy.values.convert.*
import sturdy.values.records.RecordOps
import sturdy.values.types.TypeOfOps
import sturdy.fix

trait ClassOps[C, V]:
  def classValue(c: C): V

trait ObjectOps[F, V, O, T]:
  def makeObject(fields: Seq[(F, V)], objectType: T): O
  def lookupObjectField(obj: O, field: F): V
  def updateObjectField(obj: O, field: F, newVal: V): O
  def nullObject: V

type QName = (String, String) // fully qualified class name + field/method name
case class RuntimeUnit(name: String, fields: Map[QName, ClassBodyElement.GlobalVarCB], methods: Map[QName, ClassBodyElement.MethodCB], isAbstract: Boolean) // interfaces are just like abstract classes

trait GenericInterpreter[V, T, J[_] <: MayJoin[_]]:
//  extends fix.Fixpoint[FixIn, FixOut[V]]:
  implicit def jvV: J[V]

  /* Value components */
  val jimpleOps: JimpleOps[V, Type, J]


//  import jimpleOps.*

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
  val classTable: SymbolTable[Unit, String, Container, J]
  def getClassOption(name: String): JOption[J, Container] = classTable.get((), name)
  def getClass(name: String): Container =
    val optionClass = getClassOption(name)
    optionClass match
      case c: Container => c
      case _ => throw new ClassNotFoundException(name)


  val runTimeTable: SymbolTable[Unit, String, RuntimeUnit, J]
  def getMethod(name: QName): ClassBodyElement.MethodCB =
    val optionRunTimeUnit = runTimeTable.get((), name._1)
    optionRunTimeUnit match
      case RuntimeUnit(_, _, methods, _) => methods.get(name).getOrElse(fail(MethodNotLoaded, name._2))

  def boolToV(b: Boolean): V =
    if !b then constant(Constant.IntC(0))
    else constant(Constant.IntC(1))

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

    case Constant.StringC(v) => ???
    case Constant.NullC => jimpleOps.objectOps.nullObject

  def evalImmediate(i: Immediate): V = i match
    case Immediate.ConstI(c) => constant(c)
    case Immediate.LocalI(l) => callFrame.getLocalByName(l.id).getOrElse(fail(UnboundLocal, l.id))
    case Immediate.ClassI(name) => getClassOption(name).map(jimpleOps.classOps.classValue).getOrElse(fail(UnboundClass, name))

  def typeToString(t: Type): String = t match
    case Type.IntT => "Int"
    case Type.LongT => "Long"
    case Type.FloatT => "Float"
    case Type.DoubleT => "Double"
    case Type.RefT(s) => s
    case Type.VoidT => "Unit"
    case Type.ArrayT(t, dim) => addBrackets(typeToString(t), dim)

  def evalRVal(r: RVal): V = r match
    case RVal.ArrayRefR(i1, i2) => ???
    case RVal.ConstR(c) => constant(c)
    case RVal.ExpressionR(e) => evalExp(e)
    case RVal.InstanceFieldRefR(i, f) =>
      val obj = evalImmediate(i)
      jimpleOps.objectOps.lookupObjectField(obj, f.id)
    case RVal.StaticFieldRefR(f) => ???
    case RVal.LocalR(l) => callFrame.getLocalByName(l.id).getOrElse(fail(UnboundLocal, l.id))
    case RVal.ClassR(s) => getClassOption(s).map(jimpleOps.classOps.classValue).getOrElse(fail(UnboundClass, s))

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
        // TODO NaN values
        jimpleOps.compareDoubleOps.cmp(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot compare inputs of kinds, $v1 and $v2")
    }
    case BinOp.Cmpl => jimpleOps.typeOfOps.typeOf(v1, v2) {
      case (Type.DoubleT, Type.DoubleT) =>
        // TODO NaN values
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
//      case (Type.IntT, _) => ???
      case (Type.LongT, Type.IntT) => jimpleOps.longOps.bitOr(v1, jimpleOps.convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => jimpleOps.longOps.bitOr(v1, v2)
//      case (Type.LongT, _) => ???
      case (_, _) => throw new IllegalArgumentException(s"Cannot us bitwise or on inputs of kinds, $v1 and $v2")
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

  def evalClassBodyElement(el: ClassBodyElement): V = el match {
    case ClassBodyElement.GlobalVarCB(isPublic, isPrivate, isProtected, isStatic, isFinal, isEnum, isTransient, isVolatile, t, id) =>
      ???
    case ClassBodyElement.NativeCallCB(isPublic, isPrivate, isProtected, isStatic, isFinal, isSynchronized, isNative, t, id, params, except) =>
      ???
    case  ClassBodyElement.MethodCB(header, locals, idStmts, stmts, excRanges) =>
      evalMethodHeader(header)
      for i <- 0 to locals.size do
        evalLocalDec(locals(i))
      for i <- 0 to idStmts.size do
        evalStmt(idStmts(i))
      for i <- 0 to stmts.size do
        evalStmt(stmts(i))
      callFrame.getLocalByName("return").getOrElse(constant(Constant.NanC))
    case  ClassBodyElement.MethodHeaderCB(header) =>
      ???
  }

  def evalExp(e: Exp): V = e match
    case Exp.UnopE(i, op) => ???
    case Exp.BinopE(i1, i2, op: BinOp) =>
      val v1 = evalImmediate(i1)
      val v2 = evalImmediate(i2)
      evalBinop(v1, v2, op)
    case Exp.ConditionE(i1, i2, op) =>
      val v1 = evalImmediate(i1)
      val v2 = evalImmediate(i2)
      evalCondOp(v1, v2, op)
    case Exp.CastE(t, i) => ???
    case Exp.InstanceOfE(i, ref) => ???
    case Exp.StaticInvokeE(s, l) => ???
    case Exp.InvokeE(t, i, s, l) =>
      evalImmediate(i) //FIXME: Is this needed?
      val methodName = evalMethodSignature(s)
      var locals: Map[Identifier, V] = Map()
      for i <- 0 to l.size do
//        callFrame.setLocalByName("@parameter"+i, evalImmediate(l(i))) //FIXME: Fully qualified param-name??
        locals += "@parameter"+i -> evalImmediate(l(i)) //FIXME: Fully qualified param-name??
      val method = getMethod(methodName)
      callFrame.withNew((), locals) {
        evalClassBodyElement(method)
      }
    case Exp.NewE(t) => //FIXME: muss hier ein vor-laden aller Library Klassen geschehen?
      val newType = typeToString(t)
      val containerItem = getClassOption(newType).map(jimpleOps.classOps.classValue).getOrElse(fail(UnboundClass, newType))
      containerItem match
        case Container.ClassC(isPublic, isPrivate, isAbstract, isStatic, isFinal, isEnum, id, extend, implement, body) =>
          val bodyIDs = body.map(el => el.getID)
          val bodyElements = body.map(el => evalClassBodyElement(el))
          jimpleOps.objectOps.makeObject(makeIdTuple(bodyIDs, bodyElements), t)
        case Container.InterfaceC(isPublic, isPrivate, id, extend, implement, body) =>
          val bodyIDs = body.map(el => el.getID)
          val bodyElements = body.map(el => evalClassBodyElement(el))
          jimpleOps.objectOps.makeObject(makeIdTuple(bodyIDs, bodyElements), t)

    case Exp.NewArrayE(t, i) => ???
    case Exp.NewMultArrE(t, dims) => ???

  def makeIdTuple(ids: scala.collection.Seq[String], els: scala.collection.Seq[V]): Seq[(String, V)] =
    val res = Seq.empty
    for i <- 0 to ids.size do res.appended((ids(i), els(i)))
    res

  def evalInvokeType(t: InvokeType): String = t match
    case InvokeType.InterfaceI => "interface"
    case InvokeType.SpecialI => "special"
    case InvokeType.VirtualI => "virtual"

  def evalStmt(s: Stmt): Unit = s match
    case Stmt.AssignS(v, r) =>
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
    case Stmt.IdentityS(l, identityVal) => callFrame.setLocalByName(l.id, evalIdentityVal(identityVal))
    case Stmt.BreakpointS => ???
    case Stmt.ExceptionIdentityS(l, identityVal) => ???
    case Stmt.EnterMonitorS(i) => ???
    case Stmt.ExitMonitorS(i) => ???
    case Stmt.GotoS(l) => ???
    case Stmt.IfS(cond, l) => ???
    case Stmt.InvokeS(e) => evalExp(e)
    case Stmt.LookupSwitchS(i, cases, l) => ???
    case Stmt.NopS => ???
    case Stmt.RetS(l) =>
      callFrame.setLocalByName("return", callFrame.getLocalByName(l.id)
        .getOrElse(fail(UnboundLocal, l.id)))
    case Stmt.ReturnS(i) => callFrame.setLocalByName("return", evalImmediate(i))
    case Stmt.ReturnVoidS => () //TODO: Will need this one
    case Stmt.TableSwitchS(i, cases, l) => ???
    case Stmt.ThrowS(i) => ???
    case Stmt.LabelS(l) => ???
    case Stmt.CatchS(excRange) => ???

  def evalContainer(c: Container): Unit = c match {
    case Container.ClassC(isPublic, isPrivate, isAbstract, isStatic, isFinal, isEnum, id, extend, implement, body) =>
      evalContainer(getClass(extend.get.s)) //FIXME: is this necessary?
      for i <- 0 to implement.size do
        evalContainer(getClass(implement(i).s)) //FIXME: is this necessary?
      for i <- 0 to body.size do
        evalClassBodyElement(body(i))
        //FIXME: Do I need to start at main?
    case Container.InterfaceC(isPublic, isPrivate, id, extend, implement, body) => ???
  }

  def evalLocalDec(l: LocalDec): Unit =
    callFrame.setLocalByName(l.name, constant(l.t.getEmpty))

  def evalIdentityVal(iv: IdentityVal): V = iv match {
    case IdentityVal.CaughtExcRef() => ???
    case IdentityVal.ParamRef(Constant.IntC(v), t) =>
      callFrame.getLocalByName("@parameter"+v).getOrElse(fail(UnboundLocal, "@parameter"+v))
    case IdentityVal.ThisRef(t) => callFrame.getLocalByName("@this").getOrElse(fail(UnboundLocal, "@this"))
  }

  def evalMethodHeader(mh: MethodHeader): Unit = () //FIXME: Do I even need this?

  def evalMethodSignature(ms: MethodSignature): QName =
    (ms.id, ms.classOrigin)
