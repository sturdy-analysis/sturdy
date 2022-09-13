package sturdy.apron

import apron.Texpr1Node
import apron.Texpr1VarNode
import apron.{Var, Abstract1, Environment, Manager}

enum ApronAllocationSite:
  case LocalVar(name: String)
  case TemporaryVar

//  case Join(exp1: Texpr1Node, exp2: Texpr1Node, widen: Boolean)

object ApronAlloc:
  def default(manager: Manager) = new ApronAllocBoundPerSite(manager)

trait ApronAlloc:
  type ApronVar <: ApronVarOps

  def addDoubleVariable(name: String, state: Abstract1, site: ApronAllocationSite): ApronVar
  def addIntVariable(name: String, state: Abstract1, site: ApronAllocationSite): ApronVar

  def freeVariable(v: ApronVar, state: Abstract1): Unit
  
  def useStrongUpdate(v: ApronVar): Boolean

trait ApronVarOps:
  def av: apron.Var
  def node: Texpr1VarNode = new Texpr1VarNode(av)
