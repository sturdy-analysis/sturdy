package sturdy.language.bytecode.abstractions

import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, BooleanType, ByteType, CharType, ClassFile, DoubleType, FloatType, IntegerType, LongType, Method, MethodDescriptor, ClassType, ReferenceType, ShortType, Type}
import sturdy.data.{JOption, JOptionA, MayJoin}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.EffectStack
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.Failure
import sturdy.effect.store.Store
import sturdy.language.bytecode.{AuxillaryFunctions, Interpreter}
import sturdy.language.bytecode.generic.{BytecodeFailure, FieldInitSite}
import sturdy.values.arrays.{Array, ArrayOps}
import sturdy.values.{Combine, Finite, Join, MaybeChanged, Structural, Topped, Widening}
import sturdy.values.integer.NumericInterval
import sturdy.values.objects.{Object, ObjectOps}

import java.net.URL
import scala.util.boundary
import scala.util.boundary.break

enum AbstractReferenceValue[A, O]:
  case maybeNullObject(obj: O, maybeNull: Boolean)
  case maybeNullArray(array: A, maybeNull: Boolean)
  case NullValue()

trait Objects extends Interpreter:
  override type ArrayAddr = AddrSet
  override type ArrayElemAddr = AddrSet
  override type ObjAddr = AddrSet
  override type FieldAddr = AddrSet
  override type StaticAddr = AddrSet

  override type ObjType = ClassFile
  override type FieldName = (ClassType, String)

  type Obj = Object[ObjAddr, ObjType, FieldAddr, FieldName]
  type Arr = Array[ArrayAddr, ArrayElemAddr, AType, Value]
  override type RefValue = Topped[AbstractReferenceValue[Arr, Obj]]
  override type TypeRep = ReferenceType
  override type AType = ArrayType

  override final def topRef: RefValue = Topped.Top

  given FiniteFieldAddr: Finite[FieldAddr] with {}

  // TODO: are these needed?
  final type NullVal = Null

  final def topNull: NullVal = null

  given combineRef[W <: Widening]: Combine[RefValue, W] with
    override def apply(v1: RefValue, v2: RefValue): MaybeChanged[RefValue] =
      import AbstractReferenceValue.{maybeNullArray, maybeNullObject, NullValue}
      (v1, v2) match
        case (Topped.Actual(v1), Topped.Actual(v2)) => (v1, v2) match
          case (maybeNullObject(obj, _), NullValue()) =>
            MaybeChanged.Changed(Topped.Actual(maybeNullObject(obj, true)))
          case (NullValue(), maybeNullObject(obj, _)) =>
            MaybeChanged.Changed(Topped.Actual(maybeNullObject(obj, true)))
          case (maybeNullObject(_, _), maybeNullObject(_, _)) =>
            MaybeChanged.Changed(topRef)
          case (maybeNullArray(_, _), maybeNullArray(_, _)) =>
            MaybeChanged.Changed(topRef)
          case (maybeNullArray(array, _), NullValue()) =>
            MaybeChanged.Changed(Topped.Actual(maybeNullArray(array, true)))
          case (NullValue(), maybeNullArray(array, _)) =>
            MaybeChanged.Changed(Topped.Actual(maybeNullArray(array, true)))
          case (NullValue(), NullValue()) =>
            MaybeChanged.Changed(topRef)
          case _ => ???
        case _ => MaybeChanged.Changed(topRef)

  given structuralRef[A, O]: Structural[AbstractReferenceValue[A, O]] with {}

  // helper functions in a singleton objects to avoid name space issues
  object Helper:
    def topOpalVal(ty: Type): Value =
      ty match
        case ByteType => Value.Int32(topI32)
        case ShortType => Value.Int32(topI32)
        case IntegerType => Value.Int32(topI32)
        case FloatType => Value.Float32(topF32)
        case LongType => Value.Int64(topI64)
        case DoubleType => Value.Float64(topF64)
        case BooleanType => Value.Int32(topI32)
        case CharType => Value.Int32(topI32)
        case _: ClassType => Value.ReferenceValue(topRef)
        case _: ArrayType => Value.ReferenceValue(topRef)
        case _ => ??? // TODO: not implemented

    def makeObjOps(using interpreter: Interpreter)(getFieldNonActual: JOptionA[Value], setFieldNonActual: JOptionA[Unit], isNullFn: Int => interpreter.I32)(using alloc: Allocator[FieldAddr, Site], store: Store[FieldAddr, Value, WithJoin], project: Project[URL], f: Failure): ObjectOps[FieldName, ObjAddr, Value, ClassFile, RefValue, FieldInitSite, Method, String, MethodDescriptor, interpreter.I32, WithJoin] = new ObjectOps[FieldName, ObjAddr, Value, ClassFile, RefValue, FieldInitSite, Method, String, MethodDescriptor, interpreter.I32, WithJoin] {
      override def makeObject(oid: ObjAddr, cfs: ClassFile, vals: Seq[(Value, Site, FieldName)]): RefValue =
        val fieldAddrs = vals.map { (v, site, name) =>
          val addr = alloc(site)
          store.write(addr, v)
          (name, addr)
        }.toMap
        Topped.Actual(AbstractReferenceValue.maybeNullObject(Object(oid, cfs, fieldAddrs), false))

      override def getField(ref: RefValue, name: FieldName)(using failure: Failure): Value =
        // TODO: fix
        // import sturdy.data.MakeJoined
        ref match
          case Topped.Top => ??? // getFieldNonActual
          case Topped.Actual(AbstractReferenceValue.maybeNullObject(obj, _)) => ???
            // store.read(obj.fields.getOrElse(name, failure.fail(BytecodeFailure.FieldNotFound, s"field $name not found"))).getOrElse(failure.fail(BytecodeFailure.UnboundField, s"$name not bound"))
          case Topped.Actual(AbstractReferenceValue.NullValue()) => throw NullPointerException()
          case Topped.Actual(_) => ???

      override def setField(ref: RefValue, name: FieldName, v: Value): JOption[WithJoin, Unit] =
        ref match
          case Topped.Top => setFieldNonActual
          case Topped.Actual(AbstractReferenceValue.maybeNullObject(obj, _)) =>
            if (!obj.fields.contains(name))
              JOptionA.none
            else
              store.write(obj.fields(name), v)
              JOptionA.some(())
          case Topped.Actual(_) => ???

      override def invokeFunctionCorrect(ref: RefValue, mthName: String, sig: MethodDescriptor, args: Seq[Value])(invoke: (RefValue, Method, Seq[Value]) => Value): Value =
        ref match
          case Topped.Top => topOpalVal(sig.returnType)
          case Topped.Actual(AbstractReferenceValue.maybeNullObject(obj, _)) =>
            val mth = AuxillaryFunctions.findMethodOfSuperclass(obj.cls, mthName, sig, project)
            invoke(ref, mth, args)
          case Topped.Actual(_) => ???

      override def makeNull(): RefValue = Topped.Actual(AbstractReferenceValue.NullValue())

      override def isNull(ref: RefValue): interpreter.I32 =
        ref match
          case Topped.Top => interpreter.topI32
          case Topped.Actual(AbstractReferenceValue.maybeNullObject(_, false)) => isNullFn(0)
          case Topped.Actual(AbstractReferenceValue.NullValue()) => isNullFn(1)
          case Topped.Actual(_) => ???
    }

    def makeArray(aid: ArrayAddr, vals: Seq[(Value, Site)], arrayType: AType, arraySize: Value)(using alloc: Allocator[ArrayElemAddr, Site], store: Store[ArrayElemAddr, Value, WithJoin], jvV: WithJoin[Value]): RefValue =
      val valAddrs = vals.map { (v, site) =>
        val addr = alloc(site)
        store.write(addr, v)
        addr
      }.toVector
      Topped.Actual(AbstractReferenceValue.maybeNullArray(Array(aid, valAddrs, arrayType, arraySize), false))

    def arrayLength(ref: RefValue): Value =
      ref match
        case Topped.Top => Value.Int32(topI32)
        case Topped.Actual(AbstractReferenceValue.maybeNullArray(array, _)) => array.arraySize
        case Topped.Actual(_) => ???

    def copyArray(src: Arr, srcPos: Int, dest: Arr, destPos: Int, length: Int)(using store: Store[ArrayElemAddr, Value, WithJoin])(using WithJoin[Value]): JOptionA[Unit] =
      boundary:
        for (i <- 0 until length)
          if (srcPos + i >= src.vals.size || destPos + i >= dest.vals.size)
            break(JOptionA.none)
          else
            val toCopy = store.read(src.vals(srcPos + i)).get
            store.write(dest.vals(destPos + i), toCopy)
      JOptionA.some(())

trait ConstantObjects extends Objects, Numbers:
  given objOps(using alloc: Allocator[FieldAddr, Site], store: Store[FieldAddr, Value, WithJoin], project: Project[URL], f: Failure, eff: EffectStack): ObjectOps[FieldName, ObjAddr, Value, ClassFile, RefValue, FieldInitSite, Method, String, MethodDescriptor, I32, WithJoin] =
    Helper.makeObjOps(using this)(JOptionA.none, JOptionA.some(Value.TopValue), Topped.Actual.apply)

  given constArrayOps(using alloc: Allocator[ArrayElemAddr, Site], store: Store[ArrayElemAddr, Value, WithJoin], jvV: WithJoin[Value]): ArrayOps[ArrayAddr, I32, Value, RefValue, ArrayType, Site, WithJoin] with
    override def makeArray(aid: ArrayAddr, vals: Seq[(Value, Site)], arrayType: AType, arraySize: Value): RefValue =
      // TODO: can this be curried better?
      Helper.makeArray(aid, vals, arrayType, arraySize)

    override def getVal(ref: RefValue, idx: I32): JOption[WithJoin, Value] =
      (ref, idx) match
        case (Topped.Actual(AbstractReferenceValue.maybeNullArray(array, _)), Topped.Actual(idx)) =>
          if (idx >= array.vals.size)
            JOptionA.none
          else
            store.read(array.vals(idx))
        case (Topped.Actual(_), Topped.Actual(_)) => ???
        case _ => JOptionA.some(Value.TopValue)

    override def setVal(ref: RefValue, idx: I32, v: Value): JOption[WithJoin, Unit] =
      (ref, idx) match
        case (Topped.Actual(AbstractReferenceValue.maybeNullArray(array, _)), Topped.Actual(idx)) =>
          if (idx >= array.vals.size)
            JOptionA.none
          else
            store.write(array.vals(idx), v)
            JOptionA.some(())
        case (Topped.Actual(_), Topped.Actual(_)) => ???
        case _ => JOptionA.none

    override def arrayLength(ref: RefValue): Value =
      Helper.arrayLength(ref)

    override def initArray(size: I32): Seq[Any] =
      Seq.fill(size.get) {}

    override def arraycopy(src: RefValue, srcPos: I32, dest: RefValue, destPos: I32, length: I32): JOption[WithJoin, Unit] =
      import Topped.Actual
      (src, dest, srcPos, destPos, length) match
        case (Actual(AbstractReferenceValue.maybeNullArray(src, _)), Actual(AbstractReferenceValue.maybeNullArray(dest, _)), Actual(srcPos), Actual(destPos), Actual(length)) =>
          Helper.copyArray(src, srcPos, dest, destPos, length)
        case (Actual(_), Actual(_), Actual(_), Actual(_), Actual(_)) => ???
        case _ => JOptionA.none

    override def getArray(ref: RefValue): Seq[JOption[WithJoin, Value]] =
      ref match
        case Topped.Actual(AbstractReferenceValue.maybeNullArray(array, _)) =>
          array.vals.map(addr => getVal(ref, Topped.Actual(array.vals.indexOf(addr))))
        case _ => ???

    override def printString(letters: Seq[Topped[Int]]): Unit =
      println(letters.map(l => l.get.toChar))

trait IntervalObjects extends Objects, IntervalNumbers:
  given objOps(using alloc: Allocator[FieldAddr, Site], store: Store[FieldAddr, Value, WithJoin], project: Project[URL], f: Failure, eff: EffectStack): ObjectOps[FieldName, ObjAddr, Value, ClassFile, RefValue, FieldInitSite, Method, String, MethodDescriptor, I32, WithJoin] =
    Helper.makeObjOps(using this)(JOptionA.some(Value.TopValue), JOptionA.none, NumericInterval.constant)

  given constArrayOps(using alloc: Allocator[ArrayElemAddr, Site], store: Store[ArrayElemAddr, Value, WithJoin], jvV: WithJoin[Value]): ArrayOps[ArrayAddr, I32, Value, RefValue, ArrayType, Site, WithJoin] with
    override def makeArray(aid: ArrayAddr, vals: Seq[(Value, Site)], arrayType: AType, arraySize: Value): RefValue =
      Helper.makeArray(aid, vals, arrayType, arraySize)

    override def getVal(ref: RefValue, idx: I32): JOption[WithJoin, Value] =
      ref match
        case Topped.Actual(AbstractReferenceValue.maybeNullArray(array, _)) if idx.isConstant =>
          if (idx.low >= array.vals.size)
            JOptionA.none
          else
            store.read(array.vals(idx.low))
        case Topped.Top if idx.isConstant => JOptionA.some(Value.TopValue)
        case _ => ???

    override def setVal(ref: RefValue, idx: I32, v: Value): JOption[WithJoin, Unit] =
      ref match
        case Topped.Actual(AbstractReferenceValue.maybeNullArray(array, _)) if idx.isConstant =>
          if (idx.low >= array.vals.size)
            JOptionA.none
          else
            store.write(array.vals(idx.low), v)
            JOptionA.some(())
        case Topped.Top if idx.isConstant => JOptionA.none
        case _ => ???

    override def arrayLength(ref: RefValue): Value =
      Helper.arrayLength(ref)

    override def initArray(size: I32): Seq[Any] =
      Seq.fill(size.low) {}

    override def arraycopy(src: RefValue, srcPos: I32, dest: RefValue, destPos: I32, length: I32): JOption[WithJoin, Unit] =
      (src, dest) match
        case (Topped.Actual(AbstractReferenceValue.maybeNullArray(src, _)), Topped.Actual(AbstractReferenceValue.maybeNullArray(dest, _))) if srcPos.isConstant && destPos.isConstant =>
          Helper.copyArray(src, srcPos.low, dest, destPos.low, length.low)
        case _ => ???

    override def getArray(ref: RefValue): Seq[JOption[WithJoin, Value]] =
      ref match
        case Topped.Actual(AbstractReferenceValue.maybeNullArray(array, _)) =>
          array.vals.map(addr => getVal(ref, NumericInterval.constant(array.vals.indexOf(addr))))
        case _ => ???

    override def printString(letters: Seq[NumericInterval[Int]]): Unit =
      println(letters.map(l => l.low.toChar))
