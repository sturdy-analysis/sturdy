package sturdy.language.jimple

import sturdy.data.MayJoin.NoJoin

enum Value:
  case TopValue

//class ConcreteInterpreter extends GenericInterpreter[Value, NoJoin]

//type Instance <: GenericInstance
abstract class GenericInstance extends GenericInterpreter[Value, Type, NoJoin]

abstract class Instance extends GenericInstance:
  
  
  
  
  /*val intOps: IntegerOps[Int, V]
  val longOps: IntegerOps[Long, V]
  val floatOps: FloatOps[Float, V]
  val doubleOps: FloatOps[Double, V]
  val classOps: ClassOps[Container, V]
  val typeOfOps: TypeOfOps[V, Type, J]
  val objectOps: ObjectOps[String, V, V, Type]*/
  
  /*
  *   // joins
  implicit def jv: J[V]

  // value components
  val intOps: IntegerOps[Int, V]; import intOps.*
  val compareOps: OrderingOps[V, V]; import compareOps.*
  val eqOps: EqOps[V, V]; import eqOps.*
  val functionOps: FunctionOps[Function, Seq[V], V, V]; import functionOps.*
  val refOps: ReferenceOps[Addr, V]; import refOps.*
  val recOps: RecordOps[Field, V, V]; import recOps.*
  val branchOps: BooleanBranching[V, Unit]; import branchOps.*

  // effect components
  val callFrame: DecidableCallFrame[Unit, String, Addr]
  val store: Store[Addr, V, J]
  val alloc: Allocation[Addr, AllocationSite]
  val print: Print[V]
  val input: UserInput[V]
  * */