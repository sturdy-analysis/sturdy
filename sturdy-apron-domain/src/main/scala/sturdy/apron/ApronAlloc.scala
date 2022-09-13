package sturdy.apron

import apron.Interval
import apron.Texpr1CstNode
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
  protected var freed: Boolean = false
  private var bound: Interval = _
  protected def av: apron.Var
  def getOrElse(f: => apron.Var): apron.Var =
    if (freed)
      f
    else
      av
  def free(manager: Manager, state: Abstract1): Unit =
    if (!freed) {
      bound = state.getBound(manager, av)
      freed = true
    }

  def node: Texpr1Node =
    if (freed)
      new Texpr1CstNode(bound)
    else
      new Texpr1VarNode(av)
