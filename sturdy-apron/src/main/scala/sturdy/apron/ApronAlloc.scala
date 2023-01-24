package sturdy.apron

import apron.*

enum ApronAllocationSite:
  case LocalVar(name: String)
  case TemporaryVar

  override def toString: String = this match
    case LocalVar(name) => name
    case TemporaryVar => "$$temporary"

object ApronAlloc:
  def default(manager: Manager) =
    new ApronAllocScoped(manager)

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
