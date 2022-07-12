package sturdy.language.jimple

import scala.language.postfixOps

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.effect.callframe.MutableCallFrame
import sturdy.effect.failure.Failure
import sturdy.effect.symboltable.SymbolTable
import sturdy.values.config
import sturdy.values.booleans.BooleanOps
import sturdy.values.floating.*
import sturdy.values.integer.*
import sturdy.values.convert.*
import sturdy.values.records.RecordOps
import sturdy.values.types.TypeOfOps

trait ClassOps[C, V]:
  def classValue(c: C): V

trait ObjectOps[F, V, O, T]:
  def makeObject(fields: Seq[(F, V)], objectType: T): O
  def lookupObjectField(obj: O, field: F): V
  def updateObjectField(obj: O, field: F, newVal: V): O
  def nullObject: V

trait Object[F, V, T]:
  def thisObject: (T, Seq[(F,V)])
  def objectType: T
  def objectFields: Seq[(F,V)]

type QName = (String, String) // fully qualified class name + field/method name
case class RuntimeUnit(name: String, fields: Map[QName, ClassBodyElement.GlobalVarCB], methods: Map[QName, ClassBodyElement.MethodCB], isAbstract: Boolean) // interfaces are just like abstract classes

trait CompareLongOps[V, R]:
  def cmp(v1: V, v2: V): R

trait CompareFloatingOps[V, R]:
  def cmpl(v1: V, v2: V): R
  def cmpg(v1: V, v2: V): R

trait GenericInterpreter[V, J[_] <: MayJoin[_]]:
  implicit def jvV: J[V]
  /* Value components */

  val objects: Object[Type, String, V]

  def addBrackets(s: String, i: Integer): String =
    var ret = ""
    for(j <- 0 until i)
      ret = s.concat("[]")
    ret

  val intOps: IntegerOps[Int, V]
  val longOps: IntegerOps[Long, V]
  val floatOps: FloatOps[Float, V]
  val doubleOps: FloatOps[Double, V]
  val classOps: ClassOps[Container, V]
  val typeOfOps: TypeOfOps[V, Type, J]
  val objectOps: ObjectOps[String, V, Object[Type, String, V], Type]

  val convertIntLong: ConvertIntLong[V, V]
  val convertIntFloat: ConvertIntFloat[V, V]
  val convertIntDouble: ConvertIntDouble[V, V]
  val convertLongFloat: ConvertLongFloat[V, V]
  val convertLongDouble: ConvertLongDouble[V, V]
  val convertFloatDouble: ConvertFloatDouble[V, V]
  val compareLongOps: CompareLongOps[V, V]
  val compareFloatingOps: CompareFloatingOps[V, V]


  import classOps.*
  import typeOfOps.*
  val booleanOps: BooleanOps[V]

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



  def constant(c: Constant): V = c match
    case Constant.IntC(v) => longOps.integerLit(v)
    case Constant.LongC(v) => longOps.integerLit(v)
    case Constant.FloatC(v) => floatOps.floatingLit(v)
    case Constant.DoubleC(v) => doubleOps.floatingLit(v)

    case Constant.FloatInfinityC => floatOps.floatingLit(Float.PositiveInfinity)
    case Constant.FloatNegInfinityC => floatOps.floatingLit(Float.NegativeInfinity)
    case Constant.FloatNanC => floatOps.floatingLit(Float.NaN)

    case Constant.InfinityC => doubleOps.floatingLit(Double.PositiveInfinity)
    case Constant.NegInfinityC => doubleOps.floatingLit(Double.NegativeInfinity)
    case Constant.NanC => doubleOps.floatingLit(Double.NaN)

    case Constant.StringC(v) => ???
    case Constant.NullC => objectOps.nullObject

  def evalImmediate(i: Immediate): V = i match
    case Immediate.ConstI(c) => constant(c)
    case Immediate.LocalI(l) => callFrame.getLocalByName(l.id).getOrElse(fail(UnboundLocal, l.id))
    case Immediate.ClassI(name) => getClassOption(name).map(classValue).getOrElse(fail(UnboundClass, name))

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
    case RVal.InstanceFieldRefR(i, f) => ???
//      val obj = evalImmediate(i)
//      obj match
//        case objects =>
//          objectOps.lookupObjectField(obj, f.id)
    case RVal.StaticFieldRefR(f) => ???
    case RVal.LocalR(l) => callFrame.getLocalByName(l.id).getOrElse(fail(UnboundLocal, l.id))
    case RVal.ClassR(s) => getClassOption(s).map(classValue).getOrElse(fail(UnboundClass, s))

  def evalBinop(v1: V, v2: V, op: BinOp): V = op match {
    case BinOp.Add => typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => intOps.add(v1, v2)
      case (Type.IntT, Type.LongT) => longOps.add(convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.FloatT) => floatOps.add(convertIntFloat(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.DoubleT) => doubleOps.add(convertIntDouble(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => longOps.add(v1, convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => longOps.add(v1, v2)
      case (Type.LongT, Type.FloatT) => floatOps.add(convertLongFloat(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.DoubleT) => doubleOps.add(convertLongDouble(v1, config.Bits.Signed), v2)
      case (Type.FloatT, Type.IntT) => floatOps.add(v1, convertIntFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.LongT) => floatOps.add(v1, convertLongFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.FloatT) => floatOps.add(v1, v2)
      case (Type.FloatT, Type.DoubleT) => doubleOps.add(convertFloatDouble(v1, NilCC), v2)
      case (Type.DoubleT, Type.IntT) => doubleOps.add(v1, convertIntFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.LongT) => doubleOps.add(v1, convertLongFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.FloatT) => doubleOps.add(v1, convertFloatDouble(v2, NilCC))
      case (Type.DoubleT, Type.DoubleT) => doubleOps.add(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot add inputs of kinds, $v1 and $v2")
    }
    case BinOp.And => typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => intOps.bitAnd(v1, v2)
      case (Type.IntT, Type.LongT) => longOps.bitAnd(convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => longOps.bitAnd(v1, convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => longOps.bitAnd(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot use bitwise and on inputs of kinds, $v1 and $v2")
    }
    case BinOp.Cmp => typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => compareLongOps.cmp(convertIntLong(v1, config.Bits.Signed), convertIntLong(v2, config.Bits.Signed))
      case (Type.IntT, Type.LongT) => compareLongOps.cmp(convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => compareLongOps.cmp(v1, convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => compareLongOps.cmp(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot compare inputs of kinds, $v1 and $v2")
    }
    case BinOp.Cmpg => typeOf(v1, v2) {
      case (Type.FloatT, Type.FloatT) => compareFloatingOps.cmpg(v1, v2)
      case (Type.FloatT, Type.DoubleT) => compareFloatingOps.cmpg(convertFloatDouble(v1, NilCC), v2)
      case (Type.DoubleT, Type.FloatT) => compareFloatingOps.cmpg(v1, convertFloatDouble(v2, NilCC))
      case (Type.DoubleT, Type.DoubleT) => compareFloatingOps.cmpg(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot compare inputs of kinds, $v1 and $v2")
    }
    case BinOp.Cmpl => typeOf(v1, v2) {
      case (Type.FloatT, Type.FloatT) => compareFloatingOps.cmpl(v1, v2)
      case (Type.FloatT, Type.DoubleT) => compareFloatingOps.cmpl(convertFloatDouble(v1, NilCC), v2)
      case (Type.DoubleT, Type.FloatT) => compareFloatingOps.cmpl(v1, convertFloatDouble(v2, NilCC))
      case (Type.DoubleT, Type.DoubleT) => compareFloatingOps.cmpl(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot compare inputs of kinds, $v1 and $v2")
    }
    case BinOp.Div => typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => intOps.div(v1, v2)
      case (Type.IntT, Type.LongT) => longOps.div(convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.FloatT) => floatOps.div(convertIntFloat(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.DoubleT) => doubleOps.div(convertIntDouble(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => longOps.div(v1, convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => longOps.div(v1, v2)
      case (Type.LongT, Type.FloatT) => floatOps.div(convertLongFloat(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.DoubleT) => doubleOps.div(convertLongDouble(v1, config.Bits.Signed), v2)
      case (Type.FloatT, Type.IntT) => floatOps.div(v1, convertIntFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.LongT) => floatOps.div(v1, convertLongFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.FloatT) => floatOps.div(v1, v2)
      case (Type.FloatT, Type.DoubleT) => doubleOps.div(convertFloatDouble(v1, NilCC), v2)
      case (Type.DoubleT, Type.IntT) => doubleOps.div(v1, convertIntFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.LongT) => doubleOps.div(v1, convertLongFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.FloatT) => doubleOps.div(v1, convertFloatDouble(v2, NilCC))
      case (Type.DoubleT, Type.DoubleT) => doubleOps.div(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot divide inputs of kinds, $v1 and $v2")
    }
    case BinOp.Mul => typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => intOps.mul(v1, v2)
      case (Type.IntT, Type.LongT) => longOps.mul(convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.FloatT) => floatOps.mul(convertIntFloat(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.DoubleT) => doubleOps.mul(convertIntDouble(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => longOps.mul(v1, convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => longOps.mul(v1, v2)
      case (Type.LongT, Type.FloatT) => floatOps.mul(convertLongFloat(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.DoubleT) => doubleOps.mul(convertLongDouble(v1, config.Bits.Signed), v2)
      case (Type.FloatT, Type.IntT) => floatOps.mul(v1, convertIntFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.LongT) => floatOps.mul(v1, convertLongFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.FloatT) => floatOps.mul(v1, v2)
      case (Type.FloatT, Type.DoubleT) => doubleOps.mul(convertFloatDouble(v1, NilCC), v2)
      case (Type.DoubleT, Type.IntT) => doubleOps.mul(v1, convertIntFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.LongT) => doubleOps.mul(v1, convertLongFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.FloatT) => doubleOps.mul(v1, convertFloatDouble(v2, NilCC))
      case (Type.DoubleT, Type.DoubleT) => doubleOps.mul(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot multiply inputs of kinds, $v1 and $v2")
    }
    case BinOp.Or => typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => intOps.bitOr(v1, v2)
      case (Type.IntT, Type.LongT) => longOps.bitOr(convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.IntT, _) => ???
      case (Type.LongT, Type.IntT) => longOps.bitOr(v1, convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => longOps.bitOr(v1, v2)
      case (Type.LongT, _) => ???
      case (_, _) => throw new IllegalArgumentException(s"Cannot us bitwise or on inputs of kinds, $v1 and $v2")
    }
    case BinOp.Rem => typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => intOps.remainder(v1, v2)
      case (Type.IntT, Type.LongT) => longOps.remainder(convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.FloatT) => floatOps.remainder(convertIntFloat(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.DoubleT) => doubleOps.remainder(convertIntDouble(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => longOps.remainder(v1, convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => longOps.remainder(v1, v2)
      case (Type.LongT, Type.FloatT) => floatOps.remainder(convertLongFloat(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.DoubleT) => doubleOps.remainder(convertLongDouble(v1, config.Bits.Signed), v2)
      case (Type.FloatT, Type.IntT) => floatOps.remainder(v1, convertIntFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.LongT) => floatOps.remainder(v1, convertLongFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.FloatT) => floatOps.remainder(v1, v2)
      case (Type.FloatT, Type.DoubleT) => doubleOps.remainder(convertFloatDouble(v1, NilCC), v2)
      case (Type.DoubleT, Type.IntT) => doubleOps.remainder(v1, convertIntFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.LongT) => doubleOps.remainder(v1, convertLongFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.FloatT) => doubleOps.remainder(v1, convertFloatDouble(v2, NilCC))
      case (Type.DoubleT, Type.DoubleT) => doubleOps.remainder(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot calculate remainder of inputs of kinds, $v1 and $v2")
    }
    case BinOp.Shl => typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => intOps.shiftLeft(v1, v2)
      case (Type.LongT, Type.IntT) => longOps.shiftLeft(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot use left shift inputs of kinds, $v1 and $v2")
    }
    case BinOp.Shr => typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => intOps.shiftRight(v1, v2)
      case (Type.LongT, Type.IntT) => longOps.shiftRight(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot use right shift inputs of kinds, $v1 and $v2")
    }
    case BinOp.Sub => typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => intOps.sub(v1, v2)
      case (Type.IntT, Type.LongT) => longOps.sub(convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.FloatT) => floatOps.sub(convertIntFloat(v1, config.Bits.Signed), v2)
      case (Type.IntT, Type.DoubleT) => doubleOps.sub(convertIntDouble(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => longOps.sub(v1, convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => longOps.sub(v1, v2)
      case (Type.LongT, Type.FloatT) => floatOps.sub(convertLongFloat(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.DoubleT) => doubleOps.sub(convertLongDouble(v1, config.Bits.Signed), v2)
      case (Type.FloatT, Type.IntT) => floatOps.sub(v1, convertIntFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.LongT) => floatOps.sub(v1, convertLongFloat(v2, config.Bits.Signed))
      case (Type.FloatT, Type.FloatT) => floatOps.sub(v1, v2)
      case (Type.FloatT, Type.DoubleT) => doubleOps.sub(convertFloatDouble(v1, NilCC), v2)
      case (Type.DoubleT, Type.IntT) => doubleOps.sub(v1, convertIntFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.LongT) => doubleOps.sub(v1, convertLongFloat(v2, config.Bits.Signed))
      case (Type.DoubleT, Type.FloatT) => doubleOps.sub(v1, convertFloatDouble(v2, NilCC))
      case (Type.DoubleT, Type.DoubleT) => doubleOps.sub(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot perform subtraction on inputs of kinds, $v1 and $v2")
    }
    case BinOp.Ushr => typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => intOps.shiftRightUnsigned(v1, v2)
      case (Type.LongT, Type.IntT) => longOps.shiftRightUnsigned(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot use unsigned right shift inputs of kinds, $v1 and $v2")
    }
    case BinOp.Xor => typeOf(v1, v2) {
      case (Type.IntT, Type.IntT) => intOps.bitXor(v1, v2)
      case (Type.IntT, Type.LongT) => longOps.bitXor(convertIntLong(v1, config.Bits.Signed), v2)
      case (Type.LongT, Type.IntT) => longOps.bitXor(v1, convertIntLong(v2, config.Bits.Signed))
      case (Type.LongT, Type.LongT) => longOps.bitXor(v1, v2)
      case (_, _) => throw new IllegalArgumentException(s"Cannot use bitwise xor on inputs of kinds, $v1 and $v2")
    }
  }

  def evalCondOp(v1: V, v2: V, op: CondOp): V = op match { // Only used on integer and diverse
    case CondOp.Eq => ???
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
      callFrame.getLocalByName("return").getOrElse(fail(UnboundLocal, "return"))
    case  ClassBodyElement.MethodHeaderCB(header) =>
      ???
  }

  def evalExp(e: Exp): V = e match
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
      for i <- 0 to l.size do
        callFrame.setLocalByName("@parameter"+i, evalImmediate(l(i))) //FIXME: Fully qualified param-name??
      val method = getMethod(methodName)
      evalClassBodyElement(method)
    case Exp.NewArrayE(t, i) => ???
    case Exp.NewE(t) => //FIXME: muss hier ein vor-laden aller Library Klassen geschehen?
      val newType = typeToString(t)
      val containerItem = getClassOption(newType).map(classValue).getOrElse(fail(UnboundClass, newType))
      containerItem match
        case Container.ClassC(isPublic, isPrivate, isAbstract, isStatic, isFinal, isEnum, id, extend, implement, body) => ???
//          val bodyIDs = body.map(el => el.getID)
//          val bodyElements = body.map(el => evalClassBodyElement(el))
//          objectOps.makeObject(makeIdTuple(bodyIDs, bodyElements), t)
        case Container.InterfaceC(isPublic, isPrivate, id, extend, implement, body) => ???
//          val bodyIDs = body.map(el => el.getID)
//          val bodyElements = body.map(el => evalClassBodyElement(el))
//          objectOps.makeObject(makeIdTuple(bodyIDs, bodyElements), t)

    case Exp.NewMultArrE(t, dims) => ???
    case Exp.UnopE(i, op) => ???

  def makeIdTuple(ids: scala.collection.Seq[String], els: scala.collection.Seq[V]): Seq[(String, V)] =
    val res = Seq.empty
    for i <- 0 to ids.size do res.appended((ids(i), els(i)))
    res

  def evalInvokeType(t: InvokeType): String = t match
    case InvokeType.InterfaceI => "interface"
    case InvokeType.SpecialI => "special"
    case InvokeType.VirtualI => "virtual"

  def evalStmt(s: Stmt): Unit = s match
    case Stmt.BreakpointS => ???
    case Stmt.AssignS(v, r) =>
      val rVal = evalRVal(r)
      v match
        case Var.ArrayRefV(i1, i2) => ???
        case Var.InstanceFieldRefV(i, f) => ???
//          val obj = evalImmediate(i)
//          objectOps.updateObjectField(obj, f.id, rVal)
        case Var.StaticFieldRefV(f) => ???
        case Var.LocalV(l) =>
          callFrame.getLocalByName(l.id).getOrElse(fail(UnboundLocal, l.id))
          callFrame.setLocalByName(l.id, rVal)
    case Stmt.IdentityS(l, identityVal) => callFrame.setLocalByName(l.id, evalIdentityVal(identityVal))
    case Stmt.ExceptionIdentityS(l, identityVal) => ???
    case Stmt.EnterMonitorS(i) => ???
    case Stmt.ExitMonitorS(i) => ???
    case Stmt.GotoS(l) => ???
    case Stmt.IfS(cond, l) => ???
    case Stmt.InvokeS(e) => evalExp(e)
    case Stmt.LookupSwitchS(i, cases, l) => ???
    case Stmt.NopS => ???
    case Stmt.RetS(l) => ???
    case Stmt.ReturnS(i) => ???
    case Stmt.ReturnVoidS => ??? //TODO: Will need this one
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
    case IdentityVal.ParamRef(c, t) =>
      c match
        case Constant.IntC(v) => callFrame.getLocalByName("@parameter"+v).getOrElse(fail(UnboundLocal, "@parameter"+v))
    case IdentityVal.ThisRef(t) => callFrame.getLocalByName("@this").getOrElse(fail(UnboundLocal, "@this"))
  }

  def evalMethodHeader(mh: MethodHeader): V = ??? //TODO: Will need this one

  def evalMethodSignature(ms: MethodSignature): QName =
    (ms.id, ms.classOrigin)







