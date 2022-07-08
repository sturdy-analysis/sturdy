package sturdy.values.booleans

import sturdy.effect.EffectStack
import sturdy.values.Indent
import sturdy.values.TreeList
import sturdy.values.{TreeBuffer, Changed, Join, Tree}

enum BoolTree extends Tree:
  case BoolSelect(v: Tree, ifTrue: Tree, ifFalse: Tree)
  case BoolBranch(v: Tree, thn: Tree, els: Tree)

  override def prettyPrint(using Indent): String = this match
    case BoolSelect(v, thn, els) => s"($v ? $thn : $els)"
    case BoolBranch(v, thn, els) => s"if $v\n" + Indent.increasedLine(s"then $thn\n") + Indent.increasedLine(s"else $els\n")

given BoolTreesSelection: BooleanSelection[Tree, Tree] with
  def boolSelect(v: Tree, ifTrue: Tree, ifFalse: Tree): Tree =
    BoolTree.BoolSelect(v, ifTrue, ifFalse)

given BoolTreesBranching(using effects: EffectStack): BooleanBranching[Tree, Tree] with
  override def boolBranch(v: Tree, thn: => Tree, els: => Tree): Tree =
    given Join[Tree] = (t, e) => Changed(BoolTree.BoolBranch(v, t, e))
    effects.joinComputations(thn)(els)

given BoolTreesBranchingBuffer(using effects: EffectStack, buf: TreeBuffer): BooleanBranching[Tree, Unit] with
  override def boolBranch(v: Tree, thn: => Unit, els: => Unit): Unit =
    given Join[Tree] = (t, e) => Changed(BoolTree.BoolBranch(v, t, e))
    val cond = effects.joinComputations[Tree] {
      val mark = buf.mark
      thn
      TreeList(buf.cut(mark))
    } {
      val mark = buf.mark
      els
      TreeList(buf.cut(mark))
    }
    buf += cond
