package sturdy.language.bytecode.abstractions

import org.opalj.br.{Method, ClassType}
import sturdy.effect.failure.Failure
import sturdy.effect.store.ManageableAddr
import sturdy.language.bytecode.generic.BytecodeFailure.IncorrectSiteVariant
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
  case Field(site: Site, name: String, cls: ClassType)
  case Static(id: (ClassType, String))

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
  case FieldInitialization(s: Site, name: String, cls: ClassType)
  case StaticInitialization(obj: ClassType, name: String)

  // deconstructs the instruction into a tuple containing the fields of the Instruction variant or fails using Failure
  def deconstructInstruction()(using failure: Failure): (Method, Int, Int) =
    this match
      case Site.Instruction(mth, pc, variant) => (mth, pc, variant)
      case _ => failure.fail(IncorrectSiteVariant, s"expected an instruction site, got $this")
