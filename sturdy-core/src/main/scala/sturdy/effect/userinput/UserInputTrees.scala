package sturdy.effect.userinput

import sturdy.effect.Stateless
import sturdy.values.Indent
import sturdy.values.Tree
import sturdy.values.TreeBuffer

case class UserInputTree() extends Tree:
  override def prettyPrint(using Indent): String = "input()"

class UserInputTrees(using buf: TreeBuffer) extends UserInput[Tree], Stateless:
  override def read(): Tree = buf += UserInputTree()