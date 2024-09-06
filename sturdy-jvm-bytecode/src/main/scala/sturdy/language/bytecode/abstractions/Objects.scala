package sturdy.language.bytecode.abstractions

import org.opalj.br.analyses.Project
import org.opalj.br.{ClassFile, Method, MethodDescriptor}
import org.opalj.br.{ArrayType, BooleanType, ByteType, CharType, DoubleType, FieldType, FloatType, IntegerType, LongType, ObjectType, ReferenceType, ShortType}
import org.opalj.constraints.NullValue
import sturdy.data
import sturdy.data.{JOption, JOptionA, JOptionC, MayJoin}
import sturdy.data.MayJoin.WithJoin
import sturdy.data.MakeJoined
import sturdy.effect.EffectStack
import sturdy.effect.allocation.Allocation
import sturdy.effect.failure.Failure
import sturdy.effect.store.{ManageableAddr, Store}
import sturdy.language.bytecode.ConcreteInterpreter.Instance
import sturdy.language.bytecode.generic.BytecodeFailure.MethodNotFound
import sturdy.language.bytecode.{AuxillaryFunctions, ConcreteInterpreter, Interpreter}
import sturdy.language.bytecode.generic.{ArrayElemInitSite, FieldInitSite, InstructionSite}
import sturdy.values.{Combine, Finite, MaybeChanged, Powerset, Topped, Widening}
import sturdy.values.arrays.{Array, ArrayOps}
import sturdy.values.objects.{Object, ObjectOps}
import sturdy.values.references.AllocationSiteAddr
import sturdy.language.bytecode.AuxillaryFunctions.*
import sturdy.values.Topped.Actual

import java.net.URL

trait ConstantObjects extends Interpreter, Numbers:

  type ObjType = ClassFile
  case class ObjAddr(site: InstructionSite) extends ManageableAddr(true)
  type FieldName = (ObjectType, String)
  case class FieldAddr(site: InstructionSite, name: String, cls: ObjectType) extends ManageableAddr(true)
  given FiniteFieldAddr: Finite[FieldAddr] with {}
  type ObjRep = Topped[Object[ObjAddr, ObjType, FieldAddr, FieldName]]
  final def topObj: ObjRep = Topped.Top

  final type ArrayRep = Topped[Array[ArrayAddr, ArrayElemAddr, ArrayType, Value]]
  case class ArrayAddr(site: InstructionSite) extends ManageableAddr(true)
  case class ArrayElemAddr(site: InstructionSite, ix: Int) extends ManageableAddr(true)
  given FiniteArrayAddr: Finite[ArrayElemAddr] with {}

  type TypeRep = ReferenceType
  type AType = ArrayType
  final def topArray: ArrayRep = Topped.Top

  final type NullVal = Null
  final def topNull: NullVal = null

  given combineNull[W <: Widening]: Combine[Null, W] with
    override def apply(v1: Null, v2: Null): MaybeChanged[Null] = MaybeChanged.Unchanged(null)

  given constObjOps(using alloc: Allocation[FieldAddr, FieldInitSite], store: Store[FieldAddr, Value, WithJoin], project: Project[URL], f: Failure, eff: EffectStack): ObjectOps[FieldName, ObjAddr, Value, ClassFile, ObjRep, FieldInitSite, Method, String, MethodDescriptor, NullVal, WithJoin] with
    override def makeObject(oid: ObjAddr, cfs: ClassFile, vals: Seq[(Value, FieldInitSite, FieldName)]): ObjRep =
      val fieldAddrs = vals.map { (v, site, name) =>
        val addr = alloc(site)
        store.write(addr, v)
        (name, addr)
      }.toVector.toMap
      Topped.Actual(Object(oid, cfs, fieldAddrs))

    override def getField(obj: ObjRep, name: FieldName): JOption[MayJoin.WithJoin, Value] =
      if(obj.isActual){
        if (!obj.get.fields.contains(name))
          JOptionA.none
        else
          store.read(obj.get.fields(name))
      }
      else{
        ???
      }

    override def setField(obj: ObjRep, name: FieldName, v: Value): JOption[MayJoin.WithJoin, Unit] =
      if(obj.isActual){
        if (!obj.get.fields.contains(name))
          JOptionA.none
        else {
          store.write(obj.get.fields(name), v)
          JOptionA.some(())
        }
      }
      else {
        ???
      }

    override def invokeFunctionCorrect(obj: ObjRep, mthName: String, sig: MethodDescriptor, args: Seq[Value])(invoke: (ObjRep, Method, Seq[Value]) => Value): Value =
      if(obj.isActual){
        val mth = AuxillaryFunctions.findMethodOfSuperclass(obj.get.cls, mthName, sig, project)
        invoke(obj, mth, args)
      }
      else{
        ???
      }

    override def makeNull(): Null = null

  given constArrayOps(using alloc: Allocation[ArrayElemAddr, ArrayElemInitSite], store: Store[ArrayElemAddr, Value, WithJoin], jvV: WithJoin[Value]): ArrayOps[ArrayAddr, I32, Value, ArrayRep, ArrayType, ArrayElemInitSite, WithJoin] with
    override def makeArray(aid: ArrayAddr, vals: Seq[(Value, ArrayElemInitSite)], arrayType: AType, arraySize: Value): ArrayRep =
      val valAddrs = vals.map { (v, site) =>
        val addr = alloc(site)
        store.write(addr, v)
        addr
      }.toVector
      Topped.Actual(Array(aid, valAddrs, arrayType, arraySize))
  
    override def getVal(array: ArrayRep, idx: I32): JOption[WithJoin, Value] =
      if(array.isActual){
        if (idx.get >= array.get.vals.size)
          JOptionA.none
        else
          store.read(array.get.vals(idx.get))
      }
      else{
        ???
      }


    override def setVal(array: ArrayRep, idx: I32, v: Value): JOption[WithJoin, Unit] =
      if(array.isActual){
        if (idx.get >= array.get.vals.size)
          JOptionA.none
        else {
          store.write(array.get.vals(idx.get), v)
          JOptionA.some(())
        }
      }
      else{
        ???
      }
      
  
    override def arrayLength(array: ArrayRep): Value =
      if(array.isActual){
        array.get.arraySize
      }
      else{
        ???
      }
  
    override def initArray(size: I32): Seq[Any] =
      Seq.fill(size.get) {}
  
    override def arraycopy(src: ArrayRep, srcPos: I32, dest: ArrayRep, destPos: I32, length: I32): JOption[WithJoin, Unit] =
      if(src.isActual && dest.isActual){
        for (i <- 0 until length.get) {
          if (srcPos.get + i >= src.get.vals.size || destPos.get + i >= dest.get.vals.size) {
            return JOptionA.none
          }
          else {
            val toCopy = store.read(src.get.vals(srcPos.get + i)).get
            store.write(dest.get.vals(destPos.get + i), toCopy)
          }
        }
        JOptionA.some(())
      }
      else{
        ???
      }

    override def getArray(array: ArrayRep): Seq[JOption[WithJoin, Value]] =
      val arrayVals = array.get.vals.map(addr => getVal(array, Topped.Actual(array.get.vals.indexOf(addr))))
      arrayVals

trait TypeObjects extends Interpreter:
  final type NullVal = Null
  final def topNull: NullVal = null


  given combineNull[W <: Widening]: Combine[Null, W] with
    override def apply(v1: Null, v2: Null): MaybeChanged[Null] = MaybeChanged.Unchanged(null)
  
  type ObjRep = ClassFile
  given CombineTypeObj[W <: Widening](using project: Project[URL]): Combine[ObjRep, W] with
    override def apply(v1: ClassFile, v2: ClassFile): MaybeChanged[ClassFile] = 
      // super type of v1 v2
      val lcSuperType = project.classHierarchy.joinObjectTypes(v1.thisType, v2.thisType, true).head
      MaybeChanged.Changed(project.classFile(lcSuperType).get)

  given typeObjects(using project: Project[URL], f: Failure, effects: EffectStack, jvV: WithJoin[Value]): ObjectOps[String, InstructionSite, Value, ClassFile, ObjRep, InstructionSite, Method, String, MethodDescriptor, NullVal, WithJoin] with
    override def makeObject(oid: InstructionSite, cfs: ClassFile, vals: Seq[(Value, InstructionSite, String)]): ClassFile =
      cfs

    override def getField(obj: ClassFile, name: String): JOptionA[Value] =
      val fieldType = obj.findField(name).head.fieldType
      val toppedType: Value = topOpalVal(fieldType)
      JOptionA.noneSome(toppedType)

    override def setField(obj: ObjRep, name: String, v: Value): JOptionA[Unit] =
      JOptionA.noneSome(())

    override def invokeFunctionCorrect(obj: ClassFile, mthName: String, sig: MethodDescriptor, args: Seq[Value])(invoke: (ClassFile, Method, Seq[Value]) => Value): Value =
      val allSubMths = project.classHierarchy.allSubclassTypes(obj.thisType, true)
        .map(obj => project.classFile(obj))
        .map(cfs => cfs.get.findMethod(mthName, sig).get).toSeq
      // search method and superClasses for first occurence of method
      val supMth = findMethodOfSuperclass(obj, mthName, sig, project)

      sturdy.data.mapJoin(supMth +: allSubMths, invoke(obj, _, args))

    override def makeNull(): Null = null

    type ArrayRep = ArrayType
    given typeArrays: ArrayOps[InstructionSite, Int, Value, ArrayRep, ArrayType, InstructionSite, WithJoin] with
      override def makeArray(aid: InstructionSite, vals: Seq[(Value, InstructionSite)], arrayType: ArrayRep, arraySize: Value): ArrayRep =
        arrayType

      override def getVal(array: ArrayRep, idx: Int): JOption[MayJoin.WithJoin, Value] =
        val toppedType = topOpalVal(array.elementType)
        JOptionA.noneSome(toppedType)

      override def setVal(array: ArrayRep, idx: Int, v: Value): JOption[MayJoin.WithJoin, Unit] =
        JOptionA.noneSome(())

      override def arrayLength(array: ArrayRep): Value =
        Value.Int32(topI32)

      override def initArray(size: Int): Seq[Any] =
        Seq()

      override def arraycopy(src: ArrayRep, srcPos: Int, dest: ArrayRep, destPos: Int, length: Int): JOption[MayJoin.WithJoin, Unit] =
        JOptionA.noneSome(())

      override def getArray(array: ArrayRep): Seq[JOption[MayJoin.WithJoin, Value]] =
        Seq(JOptionA.noneSome(topOpalVal(array.elementType)))

  def topOpalVal(ty: FieldType): Value =
    ty match
      case fieldType: ByteType => Value.Int32(topI32)
      case fieldType: ShortType => Value.Int32(topI32)
      case fieldType: IntegerType => Value.Int32(topI32)
      case fieldType: FloatType => Value.Float32(topF32)
      case fieldType: LongType => Value.Int64(topI64)
      case fieldType: DoubleType => Value.Float64(topF64)
      case fieldType: BooleanType => Value.Int32(topI32)
      case fieldType: CharType => Value.Int32(topI32)
      case fieldType: ObjectType => Value.Obj(topObj)
      case fieldType: ArrayType => Value.Array(topArray)

/*
val typeObjects = new ObjectOps[String, InstructionSite, Value, ClassFile, ObjRep, ObjRep, InstructionSite, Method, String, MethodDescriptor, NullVal, WithJoin] {
  override def makeObject(oid: InstructionSite, cfs: ClassFile, vals: Seq[(Value, InstructionSite, String)]): ClassFile =
    val pc = oid.pc
    // oid.mth.body.get
    ???

  override def getField(obj: ClassFile, name: String): JOptionA[Value] =
    JOptionA.noneSome(Value.TopValue)

  override def setField(obj: ObjRep, name: String, v: Value): JOptionA[Unit] =
    JOptionA.noneSome(())

  override def invokeFunctionCorrect(obj: ClassFile, mthName: String, sig: MethodDescriptor, args: Seq[Value])(invoke: (ClassFile, Method, Seq[Value]) => JOption[MayJoin.WithJoin, Value]): JOption[MayJoin.WithJoin, Value] =
    ???
  /*override def invokeFunctionCorrect(obj: ClassFile, mth: String, sig: MethodDescriptor, args: Seq[Value])(invoke: (ClassFile, Method, Seq[Value]) => JOptionA[Value]): JOptionA[Value] =
    // for c subtype of obj
    //   for m in c.methods if m.name == mth && m.sig == sig
    val ms: Seq[Method] = ???
    sturdy.data.mapJoin(ms, invoke(obj, _, arg))*/

  override def invokeFunction(obj: ClassFile, mth: Method, args: Seq[Value])(invoke: (ClassFile, Method, Seq[Value]) => Value): Value = ???

  override def findFunction(obj: ClassFile, name: String, sig: MethodDescriptor)(find: (ClassFile, String, MethodDescriptor) => Method): Method = ???

  override def makeNull(): Null = null
}*/




