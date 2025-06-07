package sturdy.language.bytecode.abstractions

import org.opalj.br.{Method, ObjectType}
import sturdy.effect.store.ManageableAddr
import sturdy.values.{Finite, Join}
import sturdy.values.references.{AbstractAddr, PowersetAddr}

// type to represent addresses.
// addresses are somewhat typed, but this is currently not enforced through the type system,
// this may change in the future
// variants try to make use of sturdy_core and/or contain what they replace
enum Addr extends ManageableAddr(true) with AbstractAddr[Addr]{
  case Array(site: Site)
  case ArrayElement(site: Site, index: Int)
  case Object(site: Site)
  case Field(site: Site, name: String, cls: ObjectType)
  case Static(id: (ObjectType, String))
  
  // implementation without much thought put into it currently
  override def isEmpty: Boolean = false
  override def isStrong: Boolean = false
  override def reduce[A](f: Addr => A)(using Join[A]): A = f(this)
  override def iterator: Iterator[Addr] = Iterator(this)
}

given Finite[Addr] with {}

type AddrSet = PowersetAddr[Addr, Addr]

enum Site {
  case Instruction(mth: Method, pc: Int, variant: Int = 0)
  case ArrayElementInitialization(s: Site, ix: Int)
  case FieldInitialization(s: Site, name: String, cls: ObjectType)
  case StaticInitialization(obj: ObjectType, name: String)
}
