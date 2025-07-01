package sturdy.language.bytecode.abstractions

import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, BooleanType, ByteType, CharType, ClassFile, DoubleType, FloatType, IntegerType, LongType, Method, MethodDescriptor, ObjectType, ReferenceType, ShortType, Type}
import sturdy.data
import sturdy.data.{JOption, JOptionA, MayJoin}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.EffectStack
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.Failure
import sturdy.effect.store.Store
import sturdy.language.bytecode.{AuxillaryFunctions, Interpreter}
import sturdy.language.bytecode.generic.FieldInitSite
import sturdy.values.arrays.{Array, ArrayOps}
import sturdy.values.{Combine, Finite, MaybeChanged, Structural, Topped, Widening}
import sturdy.values.integer.NumericInterval
import sturdy.values.objects.{Object, ObjectOps}
import sturdy.values.Topped.Actual

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
  override type FieldName = (ObjectType, String)

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
      import AbstractReferenceValue.*
      if (v1.isActual && v2.isActual)
        val tmp1 = v1.get
        val tmp2 = v2.get
        (tmp1, tmp2) match
          case (tmp1: maybeNullObject[constantArray, constantObj], tmp2: NullValue[Array[ArrayAddr, ArrayElemAddr, AType, Value], Object[ObjAddr, ClassFile, FieldAddr, FieldName]]) =>
            MaybeChanged.Changed(Topped.Actual(maybeNullObject(tmp1.obj, true)))
          case (tmp1: NullValue[Array[ArrayAddr, ArrayElemAddr, AType, Value], Object[ObjAddr, ClassFile, FieldAddr, FieldName]], tmp2: maybeNullObject[constantArray, constantObj]) =>
            MaybeChanged.Changed(Topped.Actual(maybeNullObject(tmp2.obj, true)))
          case (tmp1: maybeNullObject[constantArray, constantObj], tmp2: maybeNullObject[Array[ArrayAddr, ArrayElemAddr, AType, Value], Object[ObjAddr, ClassFile, FieldAddr, FieldName]]) =>
            MaybeChanged.Changed(topRef)
          case (tmp1: maybeNullArray[constantArray, constantObj], tmp2: maybeNullArray[Array[ArrayAddr, ArrayElemAddr, AType, Value], Object[ObjAddr, ClassFile, FieldAddr, FieldName]]) =>
            MaybeChanged.Changed(topRef)
          case (tmp1: maybeNullArray[constantArray, constantObj], tmp2: NullValue[Array[ArrayAddr, ArrayElemAddr, AType, Value], Object[ObjAddr, ClassFile, FieldAddr, FieldName]]) =>
            MaybeChanged.Changed(Topped.Actual(maybeNullArray(tmp1.array, true)))
          case (tmp1: NullValue[Array[ArrayAddr, ArrayElemAddr, AType, Value], Object[ObjAddr, ClassFile, FieldAddr, FieldName]], tmp2: maybeNullArray[constantArray, constantObj]) =>
            MaybeChanged.Changed(Topped.Actual(maybeNullArray(tmp2.array, true)))
          case (tmp1: NullValue[constantArray, constantObj], tmp2: NullValue[Array[ArrayAddr, ArrayElemAddr, AType, Value], Object[ObjAddr, ClassFile, FieldAddr, FieldName]]) =>
            MaybeChanged.Changed(topRef)
          case _ => ???
      else
        MaybeChanged.Changed(topRef)

  given structuralRef[A, O]: Structural[AbstractReferenceValue[A, O]] with {}

  // helper functions in a singleton objects to avoid name space issues
  object Helper:
    def topOpalVal(ty: Type): Value =
      ty match
        case fieldType: ByteType => Value.Int32(topI32)
        case fieldType: ShortType => Value.Int32(topI32)
        case fieldType: IntegerType => Value.Int32(topI32)
        case fieldType: FloatType => Value.Float32(topF32)
        case fieldType: LongType => Value.Int64(topI64)
        case fieldType: DoubleType => Value.Float64(topF64)
        case fieldType: BooleanType => Value.Int32(topI32)
        case fieldType: CharType => Value.Int32(topI32)
        case fieldType: ObjectType => Value.ReferenceValue(topRef)
        case fieldType: ArrayType => Value.ReferenceValue(topRef)
        case _ => ??? // TODO: not implemented

    def makeObjOps(using interpreter: Interpreter)(getFieldNonActual: JOptionA[Value], setFieldNonActual: JOptionA[Unit], isNullFn: Int => interpreter.I32)(using alloc: Allocator[FieldAddr, Site], store: Store[FieldAddr, Value, WithJoin], project: Project[URL], f: Failure, eff: EffectStack) = new ObjectOps[FieldName, ObjAddr, Value, ClassFile, RefValue, FieldInitSite, Method, String, MethodDescriptor, interpreter.I32, WithJoin] {
      override def makeObject(oid: ObjAddr, cfs: ClassFile, vals: Seq[(Value, Site, FieldName)]): RefValue =
        val fieldAddrs = vals.map { (v, site, name) =>
          val addr = alloc(site)
          store.write(addr, v)
          (name, addr)
        }.toVector.toMap
        Topped.Actual(AbstractReferenceValue.maybeNullObject(Object(oid, cfs, fieldAddrs), false))

      override def getField(ref: RefValue, name: FieldName): JOption[MayJoin.WithJoin, Value] =
        if (ref.isActual)
          val tmp = ref.get
          tmp match
            case tmp: AbstractReferenceValue.maybeNullObject[constantArray, constantObj] =>
              val obj: Object[ObjAddr, ClassFile, FieldAddr, FieldName] = tmp.obj
              if (!obj.fields.contains(name))
                JOptionA.none
              else
                store.read(obj.fields(name))
            case _ => ???
        else
          getFieldNonActual

      override def setField(ref: RefValue, name: FieldName, v: Value): JOption[MayJoin.WithJoin, Unit] =
        if (ref.isActual)
          val tmp = ref.get
          tmp match
            case tmp: AbstractReferenceValue.maybeNullObject[constantArray, constantObj] =>
              val obj: Object[ObjAddr, ClassFile, FieldAddr, FieldName] = tmp.obj
              if (!obj.fields.contains(name))
                JOptionA.none
              else
                store.write(obj.fields(name), v)
                JOptionA.some(())
            case _ => ???
        else
          setFieldNonActual

      override def invokeFunctionCorrect(ref: RefValue, mthName: String, sig: MethodDescriptor, args: Seq[Value])(invoke: (RefValue, Method, Seq[Value]) => Value): Value =
        if (ref.isActual)
          val tmp = ref.get
          tmp match
            case tmp: AbstractReferenceValue.maybeNullObject[constantArray, constantObj] =>
              val obj: Object[ObjAddr, ClassFile, FieldAddr, FieldName] = tmp.obj
              val mth = AuxillaryFunctions.findMethodOfSuperclass(obj.cls, mthName, sig, project)
              invoke(ref, mth, args)
            case _ => ???
        else
          topOpalVal(sig.returnType)

      override def makeNull(): RefValue = Topped.Actual(AbstractReferenceValue.NullValue())

      override def isNull(ref: RefValue): interpreter.I32 =
        if (ref.isActual)
          val tmp = ref.get
          tmp match
            case tmp: AbstractReferenceValue.maybeNullObject[constantArray, constantObj] =>
              if (tmp.maybeNull)
                ???
              else
                isNullFn(0)
            case tmp: AbstractReferenceValue.NullValue[constantArray, constantObj] =>
              isNullFn(1)
            case _ => ???
        else
          interpreter.topI32
    }

    def makeArray(aid: ArrayAddr, vals: Seq[(Value, Site)], arrayType: AType, arraySize: Value)(using alloc: Allocator[ArrayElemAddr, Site], store: Store[ArrayElemAddr, Value, WithJoin], jvV: WithJoin[Value]): RefValue =
      val valAddrs = vals.map { (v, site) =>
        val addr = alloc(site)
        store.write(addr, v)
        addr
      }.toVector
      Topped.Actual(AbstractReferenceValue.maybeNullArray(Array(aid, valAddrs, arrayType, arraySize), false))

    def arrayLength(ref: RefValue): Value =
      if (ref.isActual)
        val tmp = ref.get
        tmp match
          case tmp: AbstractReferenceValue.maybeNullArray[constantArray, constantObj] =>
            val array: Array[ArrayAddr, ArrayElemAddr, AType, Value] = tmp.array
            array.arraySize
          case _ => ???
      else
        Value.Int32(topI32)

    def copyArrayAfterChecks(using i: Interpreter)(src: RefValue, srcPos: i.I32, dest: RefValue, destPos: i.I32, length: i.I32)(lenFn: i.I32 => Int)(using store: Store[FieldAddr, Value, WithJoin], jvV: WithJoin[Value]): JOption[WithJoin, Unit] =
      val tmp1 = src.get
      val tmp2 = dest.get
      (tmp1, tmp2) match
        case (tmp1: AbstractReferenceValue.maybeNullArray[constantArray, constantObj], tmp2: AbstractReferenceValue.maybeNullArray[Array[ArrayAddr, ArrayElemAddr, AType, Value], Object[ObjAddr, ClassFile, FieldAddr, FieldName]]) =>
          val srcArray: Array[ArrayAddr, ArrayElemAddr, AType, Value] = tmp1.array
          val destArray: Array[ArrayAddr, ArrayElemAddr, AType, Value] = tmp2.array
          boundary:
            for (i <- 0 until lenFn(length))
              if (lenFn(srcPos) + i >= srcArray.vals.size || lenFn(destPos) + i >= destArray.vals.size)
                break(JOptionA.none)
              else
                val toCopy = store.read(srcArray.vals(lenFn(srcPos) + i)).get
                store.write(destArray.vals(lenFn(destPos) + i), toCopy)
          JOptionA.some(())
        case _ => ???

trait ConstantObjects extends Objects, Numbers:
  given objOps(using alloc: Allocator[FieldAddr, Site], store: Store[FieldAddr, Value, WithJoin], project: Project[URL], f: Failure, eff: EffectStack): ObjectOps[FieldName, ObjAddr, Value, ClassFile, RefValue, FieldInitSite, Method, String, MethodDescriptor, I32, WithJoin] =
    Helper.makeObjOps(using this)(JOptionA.none, JOptionA.some(Value.TopValue), Topped.Actual.apply)

  given constArrayOps(using alloc: Allocator[ArrayElemAddr, Site], store: Store[ArrayElemAddr, Value, WithJoin], jvV: WithJoin[Value]): ArrayOps[ArrayAddr, I32, Value, RefValue, ArrayType, Site, WithJoin] with
    override def makeArray(aid: ArrayAddr, vals: Seq[(Value, Site)], arrayType: AType, arraySize: Value): RefValue = {
      // TODO: can this be curried better?
      Helper.makeArray(aid, vals, arrayType, arraySize)
    }

    override def getVal(ref: RefValue, idx: I32): JOption[WithJoin, Value] =
      if (ref.isActual && idx.isActual)
        val tmp = ref.get
        tmp match
          case tmp: AbstractReferenceValue.maybeNullArray[constantArray, constantObj] =>
            val array: Array[ArrayAddr, ArrayElemAddr, AType, Value] = tmp.array
            if (idx.get >= array.vals.size)
              JOptionA.none
            else
              store.read(array.vals(idx.get))
          case _ => ???
      else
        JOptionA.some(Value.TopValue)

    override def setVal(ref: RefValue, idx: I32, v: Value): JOption[WithJoin, Unit] =
      if (ref.isActual && idx.isActual)
        val tmp = ref.get
        tmp match
          case tmp: AbstractReferenceValue.maybeNullArray[constantArray, constantObj] =>
            val array: Array[ArrayAddr, ArrayElemAddr, AType, Value] = tmp.array
            if (idx.get >= array.vals.size)
              JOptionA.none
            else
              store.write(array.vals(idx.get), v)
              JOptionA.some(())
          case _ => ???
      else
        JOptionA.none

    override def arrayLength(ref: RefValue): Value =
      Helper.arrayLength(ref)

    override def initArray(size: I32): Seq[Any] =
      Seq.fill(size.get) {}

    override def arraycopy(src: RefValue, srcPos: I32, dest: RefValue, destPos: I32, length: I32): JOption[WithJoin, Unit] =
      // TODO: these checks can probably be refined with better matches
      if (src.isActual && dest.isActual && srcPos.isActual && destPos.isActual && length.isActual)
        Helper.copyArrayAfterChecks(using ConstantObjects.this)(src, srcPos, dest, destPos, length)(_.get)
      else
        JOptionA.none

    override def getArray(ref: RefValue): Seq[JOption[WithJoin, Value]] =
      if (ref.isActual)
        val tmp = ref.get
        tmp match
          case tmp: AbstractReferenceValue.maybeNullArray[constantArray, constantObj] =>
            val array: Array[ArrayAddr, ArrayElemAddr, AType, Value] = tmp.array
            val arrayVals = array.vals.map(addr => getVal(ref, Topped.Actual(array.vals.indexOf(addr))))
            arrayVals
          case _ => ???
      else
        ???

    override def printString(letters: Seq[Topped[Int]]): Unit =
      println(letters.map(l => l.get.toChar))

trait IntervalObjects extends Objects, IntervalNumbers:
  given objOps(using alloc: Allocator[FieldAddr, Site], store: Store[FieldAddr, Value, WithJoin], project: Project[URL], f: Failure, eff: EffectStack): ObjectOps[FieldName, ObjAddr, Value, ClassFile, RefValue, FieldInitSite, Method, String, MethodDescriptor, I32, WithJoin] =
    Helper.makeObjOps(using this)(JOptionA.some(Value.TopValue), JOptionA.none, NumericInterval.constant)

  given constArrayOps(using alloc: Allocator[ArrayElemAddr, Site], store: Store[ArrayElemAddr, Value, WithJoin], jvV: WithJoin[Value]): ArrayOps[ArrayAddr, I32, Value, RefValue, ArrayType, Site, WithJoin] with
    override def makeArray(aid: ArrayAddr, vals: Seq[(Value, Site)], arrayType: AType, arraySize: Value): RefValue =
      Helper.makeArray(aid, vals, arrayType, arraySize)

    override def getVal(ref: RefValue, idx: I32): JOption[WithJoin, Value] =
      if (idx.isConstant)
        if (ref.isActual)
          val tmp = ref.get
          tmp match
            case tmp: AbstractReferenceValue.maybeNullArray[constantArray, constantObj] =>
              val array: Array[ArrayAddr, ArrayElemAddr, AType, Value] = tmp.array
              if (idx.low >= array.vals.size)
                JOptionA.none
              else
                store.read(array.vals(idx.low))
            case _ => ???
        else
          JOptionA.some(Value.TopValue)
      else
        ???

    override def setVal(ref: RefValue, idx: I32, v: Value): JOption[WithJoin, Unit] =
      if (idx.isConstant)
        if (ref.isActual)
          val tmp = ref.get
          tmp match
            case tmp: AbstractReferenceValue.maybeNullArray[constantArray, constantObj] =>
              val array: Array[ArrayAddr, ArrayElemAddr, AType, Value] = tmp.array
              if (idx.low >= array.vals.size)
                JOptionA.none
              else
                store.write(array.vals(idx.low), v)
                JOptionA.some(())
            case _ => ???
        else
          JOptionA.none
      else
        ???

    override def arrayLength(ref: RefValue): Value =
      Helper.arrayLength(ref)

    override def initArray(size: I32): Seq[Any] =
      Seq.fill(size.low) {}

    override def arraycopy(src: RefValue, srcPos: I32, dest: RefValue, destPos: I32, length: I32): JOption[WithJoin, Unit] = {
      // TODO: these checks can probably be refined with better matches
      if (srcPos.isConstant && destPos.isConstant)
        if (src.isActual && dest.isActual)
          Helper.copyArrayAfterChecks(using IntervalObjects.this)(src, srcPos, dest, destPos, length)(_.low)
        else
          ???
      else
        ???
    }

    override def getArray(ref: RefValue): Seq[JOption[WithJoin, Value]] =
      if (ref.isActual)
        val tmp = ref.get
        tmp match
          case tmp: AbstractReferenceValue.maybeNullArray[constantArray, constantObj] =>
            val array: Array[ArrayAddr, ArrayElemAddr, AType, Value] = tmp.array
            val arrayVals = array.vals.map(addr => getVal(ref, NumericInterval.constant(array.vals.indexOf(addr))))
            arrayVals
          case _ => ???
      else
        ???

    override def printString(letters: Seq[NumericInterval[Int]]): Unit =
      println(letters.map(l => l.low.toChar))
