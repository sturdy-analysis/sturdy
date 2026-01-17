package sturdy.language.bytecode

import org.opalj.bi.ACC_SUPER
import org.opalj.br.*
import org.opalj.br.analyses.Project
import sturdy.data.{*, given}
import sturdy.effect.allocation.{Allocator, CAllocatorIntIncrement}
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.except.{ConcreteExcept, Except}
import sturdy.effect.failure.{ConcreteFailure, Failure}
import sturdy.effect.operandstack.ConcreteOperandStack
import sturdy.effect.store.{CStore, Store}
import sturdy.effect.symboltable.ConcreteSymbolTable
import sturdy.fix
import sturdy.fix.ConcreteFixpoint
import sturdy.language.bytecode.abstractions.{ArrayOpContext, FieldAccessContext, FieldIdent, InvokeContext, InvokeType, Site, StaticMethodDeclaration, getIdent}
import sturdy.language.bytecode.generic.*
import sturdy.language.bytecode.util.ClassTypeValues
import sturdy.values.arrays.*
import sturdy.values.booleans.ConcreteBooleanBranching
import sturdy.values.config
import sturdy.values.convert.&&
import sturdy.values.exceptions.ConcreteExceptional
import sturdy.values.floating.given
import sturdy.values.integer.given
import sturdy.values.objects.*
import sturdy.values.ordering.EqOps

import java.net.URL
import scala.annotation.tailrec

enum ConcreteRefValues[ObjectAddr, Class, FieldName, ObjFieldAddr, ArrayAddr, ArrayElemAddr, AType, ASizeType]:
  case Object(oid: ObjectAddr, cls: Class, fields: Map[FieldName, ObjFieldAddr])
  case nonNullArray(aid: ArrayAddr, vals: Seq[ArrayElemAddr], arrayType: AType, arraySize: ASizeType)
  case NullValue()

object ConcreteInterpreter extends Interpreter:
  override type J[A] = NoJoin[A]
  override type I8 = Byte
  override type I16 = Short
  override type U16 = Char
  override type I32 = Int
  override type I64 = Long
  override type F32 = Float
  override type F64 = Double
  override type Bool = Boolean

  override type Addr = (Site, Int)

  override type Mth = Method
  override type StaticMth = StaticMethodDeclaration
  override type ObjType = ClassFile
  override type TypeRep = ReferenceType
  //override type NullVal = Null
  override type FieldName = FieldIdent
  //override type ObjRep = Object[ObjAddr, ClassFile, FieldAddr, FieldName]
  override type AType = ArrayType
  //override type ArrayRep = Array[ArrayAddr, FieldAddr, ArrayType, Value]

  override type RefValue = ConcreteRefValues[Addr, ClassFile, FieldName, Addr, Addr, Addr, AType, Int]

  override type ExcV = JvmExcept[Value]

  override implicit val except: Except[JvmExcept[Value], ExcV, J] = ConcreteExcept[ExcV]()

  override def topI32: Int = throw UnsupportedOperationException()

  override def topI64: Long = throw UnsupportedOperationException()

  override def topF32: Float = throw UnsupportedOperationException()

  override def topF64: Double = throw UnsupportedOperationException()

  //override def topObj: Object[ConcreteInterpreter.ObjAddr, ClassFile, ConcreteInterpreter.FieldAddr, ConcreteInterpreter.FieldName] = throw new UnsupportedOperationException
  //override def topArray: Array[ConcreteInterpreter.ArrayAddr, ConcreteInterpreter.FieldAddr, ConcreteInterpreter.AType, ConcreteInterpreter.Value] = throw new UnsupportedOperationException
  //override def topNull: Null = throw new UnsupportedOperationException
  override def topRef: RefValue = throw UnsupportedOperationException()

  override def asBoolean(v: Value)(using Failure): Boolean = v.asInt32 != 0

  override def boolean(b: Boolean): Value =
    if (b)
      Value.Int32(1)
    else
      Value.Int32(0)

  given ConcreteTypeOps(using project: Project[URL]): TypeOps[RefValue, TypeRep] with
    // null is currently not supported since java lacks a bottom type we could use here
    override def typeOf(v: RefValue): ReferenceType = v match
      case ConcreteRefValues.Object(_, cls, _) => cls.thisType
      case ConcreteRefValues.nonNullArray(_, _, arrayType, _) => arrayType
      case ConcreteRefValues.NullValue() => throw IllegalArgumentException("can't get type of null")

    override def ifInstanceOf[A](v: RefValue, target: ReferenceType)(ifTrue: => A)(ifFalse: => A): A = v match
      case ConcreteRefValues.Object(_, cf, _) =>
        if checkInstanceOf(cf.thisType, target) then ifTrue else ifFalse
      case ConcreteRefValues.nonNullArray(_, _, ty, _) =>
        if checkInstanceOf(ty, target) then ifTrue else ifFalse
      case ConcreteRefValues.NullValue() =>
        ifTrue

    @tailrec
    private def checkInstanceOf(objRef: ReferenceType, t: ReferenceType)(using ClassHierarchy): Boolean = objRef match
      case c: ClassType =>
        c.isSubtypeOf(t.mostPreciseClassType)
      case ArrayType(sc) => t match
        case t: ClassType =>
          Set(ClassType.Object, ClassType.Cloneable, ClassType.Serializable).contains(t)
        case t: ArrayType =>
          val tc = t.componentType
          tc == sc || tc == ClassType.Object || (sc match
            case sc: ClassType => tc.isClassType && sc.isSubtypeOf(tc.asClassType)
            case sc: ArrayType => tc.isReferenceType && checkInstanceOf(sc, tc.asReferenceType)
            case _: BaseType => false
            )
      case x: ArrayType => throw UnsupportedOperationException(s"ArrayType deconstruction failed unexpectedly with value $x")

    given ClassHierarchy = project.classHierarchy

  given intSizeOps: SizeOps[I32, Boolean] with
    override def is32Bit(v: I32): Boolean = true

  given floatSizeOps: SizeOps[F32, Boolean] with
    override def is32Bit(v: F32): Boolean = true

  given longSizeOps: SizeOps[I64, Boolean] with
    override def is32Bit(v: I64): Boolean = false

  given doubleSizeOps: SizeOps[F64, Boolean] with
    override def is32Bit(v: F64): Boolean = false

  given refSizeOps: SizeOps[RefValue, Boolean] with
    override def is32Bit(v: RefValue): Boolean = true

  given ConcreteObjectOps
  (using alloc: Allocator[Addr, Site], store: Store[Addr, Value, NoJoin], project: Project[URL], failure: Failure, throwClass: ThrowClass): ObjectOps[FieldName, Addr, Value, ClassFile, RefValue, Site, Mth, StaticMth, I32, InvokeContext, FieldAccessContext, NoJoin] with
    given hierarchy: ClassHierarchy = project.classHierarchy

    override def makeObject(oid: Addr, c: ClassFile, fields: Seq[(Value, Site, FieldName)]): RefValue =
      val fieldAddrs = fields.map { (v, site, name) =>
        val addr = alloc(site)
        store.write(addr, v)
        (name, addr)
      }.toMap
      ConcreteRefValues.Object(oid, c, fieldAddrs)

    override def getField(context: FieldAccessContext)(obj: RefValue, identifier: FieldName): Value = obj match
      case ConcreteRefValues.Object(_, _, fields) =>
        val resolvedField = resolveField(context._2.thisType, identifier)(using project, except, throwClass(context._1))
        val addr = fields.getOrElse(resolvedField.getIdent, failure.fail(BytecodeFailure.FieldNotFound, s"field $identifier not found"))
        store.read(addr).getOrElse(failure.fail(BytecodeFailure.UnboundField, s"$identifier not bound"))
      case ConcreteRefValues.NullValue() =>
        throwClass(context._1)(ClassType.NullPointerException)
      case ConcreteRefValues.nonNullArray(_, _, _, _) =>
        throwClass(context._1)(ClassTypeValues.LinkageError)

    override def setField(context: FieldAccessContext)(obj: RefValue, identifier: FieldName, v: Value): JOptionC[Unit] = obj match
      case ConcreteRefValues.Object(_, _, fields) =>
        val resolvedField = resolveField(context._2.thisType, identifier)(using project, except, throwClass(context._1))
        if !fields.contains(resolvedField.getIdent) then
          JOptionC.none
        else
          store.write(fields(resolvedField.getIdent), v)
          JOptionC.some(())
      case ConcreteRefValues.NullValue() => throwClass(context._1)(ClassType.NullPointerException)
      case _ =>
        throw UnsupportedOperationException(s"attempted object operations on $obj")

    override def invokeMethod(context: InvokeContext)(staticMethod: StaticMth, obj: RefValue, args: Seq[Value])(invoke: (RefValue, Mth, Seq[Value]) => Value): Value = obj match
      case ConcreteRefValues.NullValue() => throwClass(context._1)(ClassType.NullPointerException)
      case ConcreteRefValues.Object(_, cf, _) => context match
        case (site, InvokeType.Interface, callingClass) =>
          if !hierarchy.isSubtypeOf(cf.thisType, staticMethod.declaringClass) then
            throwClass(context._1)(ClassTypeValues.IncompatibleClassChangeError)
          val resolvedMethod = resolveInterfaceMethod(callingClass.thisType, staticMethod.declaringClass, staticMethod.name, staticMethod.descriptor)(using hierarchy, project, except, throwClass(context._1))
          if resolvedMethod.isStatic then
            throwClass(context._1)(ClassTypeValues.IncompatibleClassChangeError)
          val selectedMethod = selectMethod(cf.thisType, resolvedMethod)(using hierarchy, project, except, throwClass(context._1))
          if !(selectedMethod.isPublic || selectedMethod.isPrivate) then
            throwClass(context._1)(ClassTypeValues.IllegalAccessError)
          if selectedMethod.isAbstract || selectedMethod.isStatic then
            throwClass(context._1)(ClassTypeValues.AbstractMethodError)
          invoke(obj, selectedMethod, args)

        case (site, InvokeType.Virtual, callingClass) =>
          val resolvedMethod = resolveMethod(callingClass.thisType, staticMethod.declaringClass, staticMethod.name, staticMethod.descriptor)(using hierarchy, project, except, throwClass(context._1))
          if resolvedMethod.isStatic then
            throwClass(context._1)(ClassTypeValues.IncompatibleClassChangeError)
          val selectedMethod = selectMethod(cf.thisType, resolvedMethod)(using hierarchy, project, except, throwClass(context._1))
          if selectedMethod.isAbstract then
            throwClass(context._1)(ClassTypeValues.AbstractMethodError)
          invoke(obj, selectedMethod, args)

        case (site, InvokeType.Special(isInterfaceCall), callingClass) =>
          val resolvedMethod = if isInterfaceCall then
            resolveInterfaceMethod(callingClass.thisType, staticMethod.declaringClass, staticMethod.name, staticMethod.descriptor)(using hierarchy, project, except, throwClass(context._1))
          else
            resolveMethod(callingClass.thisType, staticMethod.declaringClass, staticMethod.name, staticMethod.descriptor)(using hierarchy, project, except, throwClass(context._1))
          val c = if !resolvedMethod.isConstructor && !isInterfaceCall && callingClass.thisType.isSubtypeOf(staticMethod.declaringClass) && callingClass.superclassType.isDefined && ACC_SUPER.isSet(callingClass.accessFlags) then
            callingClass.superclassType.get
          else
            staticMethod.declaringClass
          val selectedMethod = selectSpecial(c, resolvedMethod)(using hierarchy, project, except, throwClass(context._1))
          if selectedMethod.isAbstract then
            throwClass(context._1)(ClassTypeValues.AbstractMethodError)
          invoke(obj, selectedMethod, args)

      case _ =>
        throw UnsupportedOperationException(s"attempted object operations on $obj")

    override def makeNull(): RefValue = ConcreteRefValues.NullValue()

    override def isNull(obj: RefValue): I32 = obj match
      case ConcreteRefValues.NullValue() => 1
      case _ => 0

  given ConcreteArrayOps
  (using alloc: Allocator[Addr, Site], store: Store[Addr, Value, NoJoin], project: Project[URL], f: Failure, throwClass: ThrowClass): ArrayOps[Addr, Int, Value, RefValue, AType, ArrayOpContext, NoJoin] with
    override def makeArray(ctx: ArrayOpContext)(aid: Addr, defaultValue: => Value, arrayType: AType, arraySize: Int): RefValue =
      val values = Range.Int(0, arraySize, 1).map: index =>
        val addr = alloc(Site.ArrayElementInitialization(ctx, index))
        store.write(addr, defaultValue)
        addr
      ConcreteRefValues.nonNullArray(aid, values, arrayType, arraySize)

    override def get(ctx: ArrayOpContext)(array: RefValue, idx: Int): JOption[NoJoin, Value] = array match
      case ConcreteRefValues.nonNullArray(_, vals, _, _) =>
        if idx >= vals.size || idx < 0 then
          JOptionC.none
        else
          store.read(vals(idx))
      case ConcreteRefValues.NullValue() =>
        throwClass(ctx)(ClassType.NullPointerException)
      case _ =>
        throw UnsupportedOperationException(s"attempted array operations on $array")

    // returns some if setting the value was successful, none otherwise
    // it can only fail by being out of bounds
    override def set(ctx: ArrayOpContext)(array: RefValue, idx: Int, v: Value): JOptionC[Unit] = array match
      case ConcreteRefValues.nonNullArray(_, vals, arrayType, _) =>
        if idx >= vals.size || idx < 0 then
          JOptionC.none
        else
          // reference types need to be checked, null should pass the type check so no need for special handling
          if arrayType.componentType.isReferenceType then
            val cType = arrayType.componentType.asReferenceType
            ConcreteTypeOps(using project).ifInstanceOf(v.asRef, cType) {} {
              throwClass(ctx)(ClassType.ArrayStoreException)
            }

          store.write(vals(idx), v)
          JOptionC.some(())
      case ConcreteRefValues.NullValue() =>
        throwClass(ctx)(ClassType.NullPointerException)
      case _ =>
        throw UnsupportedOperationException(s"attempted array operations on $array")

    override def length(ctx: ArrayOpContext)(array: RefValue): Value = array match
      case ConcreteRefValues.nonNullArray(_, _, _, size: I32) =>
        Value.Int32(size)
      case ConcreteRefValues.NullValue() => throwClass(ctx)(ClassType.NullPointerException)
      case _ =>
        throw UnsupportedOperationException(s"attempted array operations on $array")

  given RefEqOps[AID, OID, ASize]: EqOps[RefValue, Boolean] with
    override def equ(v1: RefValue, v2: RefValue): Boolean = (v1, v2) match
      case (ConcreteRefValues.Object(oid1, _, _), ConcreteRefValues.Object(oid2, _, _)) =>
        oid1 == oid2
      case (ConcreteRefValues.nonNullArray(aid1, _, _, _), ConcreteRefValues.nonNullArray(aid2, _, _, _)) =>
        aid1 == aid2
      case (ConcreteRefValues.NullValue(), ConcreteRefValues.NullValue()) =>
        true
      // each remaining case (comparing anything to null or objects to arrays) must be false
      case _ =>
        false

    override def neq(v1: RefValue, v2: RefValue): Boolean =
      !this.equ(v1, v2)

  class Instance(proj: Project[URL], initStore: Map[Addr, Value]) extends GenericInstance:
    val newFrameData: FrameData = 0
    val args: List[Value] = List()

    override val joinUnit: MayJoin.NoJoin[Unit] = implicitly
    override val jvV: MayJoin.NoJoin[Value] = implicitly
    override val joinAddr: MayJoin.NoJoin[Addr] = implicitly

    override val stack: ConcreteOperandStack[Value] = ConcreteOperandStack[Value]
    override implicit val failure: ConcreteFailure = ConcreteFailure()
    override val frame: ConcreteCallFrame[FrameData, Int, Value, Site] = ConcreteCallFrame[FrameData, Int, Value, Site](newFrameData, args.view.zipWithIndex.map((x, y) => (y, Some(x))))
    override val except: Except[JvmExcept[Value], JvmExcept[Value], MayJoin.NoJoin] = ConcreteExcept[JvmExcept[Value]]
    override val objAlloc: Allocator[Addr, Site] = CAllocatorIntIncrement[Site]
    override val objFieldAlloc: CAllocatorIntIncrement[Site] = CAllocatorIntIncrement[Site]
    override val arrayAlloc: CAllocatorIntIncrement[Site] = CAllocatorIntIncrement[Site]
    override val arrayValAlloc: CAllocatorIntIncrement[Site] = CAllocatorIntIncrement[Site]
    override val staticAlloc: CAllocatorIntIncrement[Site] = CAllocatorIntIncrement[Site]
    override val store: CStore[Addr, Value] = CStore(initStore)

    override val classInitializationState: ConcreteSymbolTable[Unit, ClassType, InitializationResult] = ConcreteSymbolTable()
    override val staticFieldTable: ConcreteSymbolTable[Unit, FieldName, Addr] = ConcreteSymbolTable()

    override implicit val project: Project[URL] = proj

    // adjust the given instances, the current sturdy-core implementation does not reflect the required semantics
    // config can be ignored here
    given ConcreteConvertFloatInt: ConcreteConvertFloatInt with
      override def apply(f: Float, conf: config.Overflow && config.Bits): Int = f.toInt

    given ConcreteConvertFloatLong: ConcreteConvertFloatLong with
      override def apply(f: Float, conf: config.Overflow && config.Bits): Long = f.toLong

    given ConcreteConvertDoubleInt: ConcreteConvertDoubleInt with
      override def apply(d: Double, conf: config.Overflow && config.Bits): Int = d.toInt

    given ConcreteConvertDoubleLong: ConcreteConvertDoubleLong with
      override def apply(d: Double, conf: config.Overflow && config.Bits): Long = d.toLong

    // jvm floating point remainder is different from IEEE754 remainder, the current standard implementation of the concrete float ops
    // delegate the calculation to the jvm this code is running on
    given ConcreteFloatOps: ConcreteFloatOps with
      override def remainder(dividend: Float, divisor: Float): Float = dividend % divisor

    given ConcreteDoubleOps: ConcreteDoubleOps with
      override def remainder(dividend: Double, divisor: Double): Double = dividend % divisor

    override val bytecodeOps: BytecodeOps[Value, TypeRep] = implicitly
    override val objectOps: ObjectOps[FieldName, Addr, Value, ObjType, Value, Site, Mth, StaticMth, Value, InvokeContext, FieldAccessContext, MayJoin.NoJoin] =
      LiftedObjectOps[FieldName, Addr, Value, ObjType, Value, Site, Mth, StaticMth, Value, InvokeContext, FieldAccessContext, MayJoin.NoJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using ConcreteObjectOps(using objFieldAlloc, store, project, failure, this.throwClass)
      )
    override val arrayOps: ArrayOps[Addr, Value, Value, Value, AType, ArrayOpContext, MayJoin.NoJoin] =
      LiftedArrayOps[Addr, Value, Value, Value, AType, ArrayOpContext, MayJoin.NoJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using ConcreteArrayOps(using arrayValAlloc, store, project, failure, this.throwClass)
      )

    override val fixpoint: ConcreteFixpoint[FixIn, FixOut] = ConcreteFixpoint[FixIn, FixOut]

    override def exceptionHandler(mth: Method)(using Fixed): JvmExcept[ConcreteInterpreter.Value] => Unit  =
      case JvmExcept.Jump(targetPC) =>
        enterMethod(targetPC, mth)
      case JvmExcept.Ret(_) =>
        ??? // TODO
      case JvmExcept.Return(_) =>
        ()
      case JvmExcept.ThrowObject(exception) =>
        // otherwise, handle the exception
        val currPC = frame.data
        val body = mth.body.get
        val handler = body.exceptionHandlersFor(currPC)
          // try to find handler for exception; get is safe to use since finally handlers are not included
          .find(handlerException => bytecodeOps.typeOps.ifInstanceOf(exception, handlerException.catchType.get)(true)(false))
          // try to find finally clause to invoke if no handler was found
          .orElse(body.handlersFor(currPC).find(_.catchType.isEmpty))
          .getOrElse:
            // no handler found, throw the exception again
            stack.clearCurrentOperandFrame()
            except.throws(JvmExcept.ThrowObject(exception))
        // handler has been found
        stack.push(exception)
        enterMethod(handler.handlerPC, mth)
