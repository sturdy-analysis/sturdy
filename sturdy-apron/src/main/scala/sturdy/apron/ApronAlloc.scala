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
  def default(manager: Manager) = new ApronAllocBoundPerSite(manager)

trait ApronAlloc:
  type Var <: ApronVar

  def addDoubleVariable(state: Abstract1, site: ApronAllocationSite): Var
  def addIntVariable(state: Abstract1, site: ApronAllocationSite): Var

  def freeVariable(v: Var, state: Abstract1): Unit
  def useStrongUpdate(v: Var): Boolean
  def freshReference(v: Var): Var

  scala.collection.immutable.ArraySeq
