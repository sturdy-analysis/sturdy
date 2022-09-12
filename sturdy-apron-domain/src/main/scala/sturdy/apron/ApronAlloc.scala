package sturdy.apron

import apron.Texpr1Node
import apron.Texpr1VarNode
import apron.{Var, Abstract1, Environment}

enum ApronAllocationSite:
  case LocalIntVar(name: String)
  case LocalDoubleVar(name: String)
  case Join(exp1: Texpr1Node, exp2: Texpr1Node, widen: Boolean)

trait ApronAlloc:
  type ApronVar <: { 
    def av: apron.Var
    def node: Texpr1VarNode 
  }
  
  def addDoubleVariable(name: String, state: Abstract1, site: ApronAllocationSite): ApronVar
  def addIntVariable(name: String, state: Abstract1, site: ApronAllocationSite): ApronVar

  def freeVariable(v: ApronVar, state: Abstract1): Unit
  
  def useStrongUpdate(v: ApronVar): Boolean
