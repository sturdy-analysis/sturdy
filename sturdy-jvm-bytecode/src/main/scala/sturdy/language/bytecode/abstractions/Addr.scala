package sturdy.language.bytecode.abstractions

import org.opalj.br.Method
import sturdy.effect.store.ManageableAddr
import sturdy.values.references.{AbstractAddr, PowersetAddr}
import sturdy.values.{Finite, Join}

// type to represent addresses.
// addresses are somewhat typed, but this is currently not enforced through the type system,
// this may change in the future
// variants try to make use of sturdy_core and/or contain what they replace
enum Addr extends ManageableAddr(true) with AbstractAddr[Addr]:
  case Array(site: Site)
  case ArrayElement(site: Site, index: Int)
  case Object(site: Site)
  case Field(site: Site, ident: FieldIdent)
  case Static(ident: FieldIdent)

  // implementation without much thought put into it currently
  override def isEmpty: Boolean = false

  override def isStrong: Boolean = false

  override def reduce[A](f: Addr => A)(using Join[A]): A = f(this)

  override def iterator: Iterator[Addr] = Iterator(this)

given Finite[Addr] with {}

// we need a powerset of addresses to join them
type AddrSet = PowersetAddr[Addr, Addr]

// enum to represent different sites, replacing the old case classes
enum Site:
  case Instruction(mth: Method, pc: Int, variant: Int = 0)
  case ArrayElementInitialization(s: Site, ix: Int)
  case FieldInitialization(s: Site, ident: FieldIdent)
  case StaticInitialization(ident: FieldIdent)
  // not created within the program
  case External
