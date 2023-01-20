package sturdy.apron

import apron.Interval
import apron.Texpr1CstNode
import apron.Texpr1Node
import apron.Texpr1VarNode
import apron.{Var, Abstract1, Environment, Manager}

enum ApronAllocationSite:
  case LocalVar(name: String)
  case TemporaryVar

  override def toString: String = this match
    case LocalVar(name) => name
    case TemporaryVar => "$$temporary"

object ApronAlloc:
  def default(manager: Manager) =
//    new ApronAllocSingle(manager)
    new ApronAllocBoundPerSite(manager)

trait ApronAlloc:
  type Var <: ApronVar

  def allocateDoubleVariable(site: ApronAllocationSite, apron: Apron): Var
  def allocateIntVariable(site: ApronAllocationSite, apron: Apron): Var

  /** returns true if the constraint variable should be freed as well */
  def freeVariable(v: Var, apron: Apron): Boolean
  def useStrongUpdate(v: Var): Boolean
  
  def freshReference(v: Var): Var

  def boundIntVars: Iterable[Var]
  def boundDoubleVars: Iterable[Var]
