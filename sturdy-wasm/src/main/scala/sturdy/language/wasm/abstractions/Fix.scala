package sturdy.language.wasm.abstractions

import sturdy.data.CombineEquiList
import sturdy.effect.ObservableJoin
import sturdy.effect.callframe.CallFrame
import sturdy.fix
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.{FixIn, FixOut, FrameData, FuncId, FunctionInstance, InstLoc, ModuleInstance}
import sturdy.values.Finite
import sturdy.values.{Combine, MaybeChanged, Unchanged, Widening}
import swam.FuncIdx
import swam.syntax.{Block, Call, CallIndirect, If, Inst, Loop}

import scala.collection.mutable

trait Fix extends Interpreter:
  final def isFunOrWhile(dom: FixIn[Value]): Boolean = dom match
    case _: FixIn.EnterWasmFunction[Value] => true
    case FixIn.Eval(_: Loop, _) => true
    case _ => false

  final def casesFunOrWhile(dom: FixIn[Value]): Int = dom match
    case _: FixIn.EnterWasmFunction[Value] => 0
    case FixIn.Eval(_: Loop, _) => 1
    case _ => -1


  final def frameSensitive(using frame: CallFrame[FrameData, _, _, _]): Sensitivity[FixIn[Value], FrameData] = new Sensitivity {
    override def emptyContext: FrameData = FrameData.empty

    override def switchCall(dom: FixIn[Value]): Boolean = dom match
      case _: FixIn.EnterWasmFunction[Value] => true // called by invoke and invokeExported
      case _ => false

    override def apply(dom: FixIn[Value]): FrameData = frame.getFrameData
  }

  final def callSitesLogger() = fix.context.callSites[FixIn[Value], Call | CallIndirect] {
    case FixIn.Eval(c: Call, _) => Some(c)
    case FixIn.Eval(c: CallIndirect, _) => Some(c)
    case _ => None
  }
  type CallString = fix.context.CallString[Call | CallIndirect]
  given Finite[CallString] = fix.context.FiniteCallString

  given CombineFixOut[W <: Widening] (using w: Combine[Value, W]): Combine[FixOut[Value], W] with
    override def apply(out1: FixOut[Value], out2: FixOut[Value]): MaybeChanged[FixOut[Value]] = (out1, out2) match
      case (FixOut.Eval(), FixOut.Eval()) => Unchanged(FixOut.Eval())
      case (FixOut.ExitWasmFunction(vs1), FixOut.ExitWasmFunction(vs2)) => Combine[List[Value], W](vs1, vs2).map(FixOut.ExitWasmFunction.apply)
      case _ => throw new IllegalArgumentException(s"Cannot join outputs of different kind, $out1 and $out2")

  enum CfgNode:
    case Instruction(inst: Inst, loc: InstLoc)
    case Call(inst: swam.syntax.Call | CallIndirect, loc: InstLoc)
    case CallReturn(callNode: Call) extends CfgNode, fix.CallReturnNode[Call]
    case Enter(funId: FuncId) extends CfgNode, fix.ImportantControlNode
    case Exit(funId: FuncId) extends CfgNode, fix.ImportantControlNode

    override def toString: String = this match
      case Instruction(inst, loc) => inst match
        case Block(_, _) => s"Block @$loc"
        case Loop(_, _) => s"Loop @$loc"
        case If(_, _, _) => s"If @$loc"
        case _ => s"$inst @$loc"
      case Call(inst, loc) => s"$inst @$loc"
      case CallReturn(call) => s"CallReturn(${call.inst}) @${call.loc}"
      case Enter(funId) => s"enter $funId"
      case Exit(funId) => s"exit $funId"

  def control[Ctx](sensitive: Boolean, onlyCalls: Boolean)(using effect: ObservableJoin) = fix.control[Ctx, FixIn[Value], FixOut[Value], CfgNode](sensitive) {
    case FixIn.Eval(c: Call, loc) => Some(CfgNode.Call(c, loc))
    case FixIn.Eval(c: CallIndirect, loc) => Some(CfgNode.Call(c, loc))
    case FixIn.Eval(inst, loc) => if (onlyCalls) None else Some(CfgNode.Instruction(inst, loc))
    case FixIn.EnterWasmFunction(id, _, _) => Some(CfgNode.Enter(id))
  } {
    case (FixIn.EnterWasmFunction(id, _, _), FixOut.ExitWasmFunction(_)) => Some(CfgNode.Exit(id))
    case (FixIn.Eval(c: (Call | CallIndirect), loc), _) => Some(CfgNode.CallReturn(CfgNode.Call(c, loc)))
    case _ => None
  }


  def allCfgNodes(modules: List[ModuleInstance]): Set[CfgNode] =
    val nodes: mutable.Set[CfgNode] = mutable.Set.empty
    modules.flatMap(_.functions).foreach {
      case f@FunctionInstance.Wasm(modInst, funcIx, func, ft) =>
        println(s"in function $f.")
        nodes.add(CfgNode.Enter(FuncId(modInst, funcIx)))
        nodes.add(CfgNode.Exit(FuncId(modInst, funcIx)))
        val (_,body) = withLocations(func.body, InstLoc.InFunction(FuncId(modInst, funcIx), 0))
        println(body)
        nodes.addAll(body.flatMap(instToCfgNode(_)))
      case FunctionInstance.Host(hostF) => ???
    }
    Set.from(nodes)

  def withLocations(instr: Vector[Inst], startLoc: InstLoc): (InstLoc, Vector[(Inst, InstLoc)]) =
    instr.foldLeft[(InstLoc, Vector[(Inst, InstLoc)])]((startLoc, Vector.empty)) { case ((loc, res), next) =>
      println(s"visiting $next with loc $loc")
      next match
        case Block(_, body) =>
          val (nestedLoc, nestedRes) = withLocations(body, loc+1)
          (nestedLoc, res ++: (next, loc + 1) +: nestedRes)
        case Loop(_, body) =>
          val (nestedLoc, nestedRes) = withLocations(body, loc+1)
          (nestedLoc, res ++: (next, loc + 1) +: nestedRes)
        case If(_, thenInstr, elseInstr) =>
          val (nestedLocThen, nestedResThen) = withLocations(thenInstr, loc+1)
          val (nestedLocElse, nestedResElse) = withLocations(elseInstr, nestedLocThen)
          (nestedLocElse, res ++: (next, loc+1) +: nestedResThen ++: nestedResElse)
        case _ => (loc+1, res :+ (next, loc+1))
    }

  def instToCfgNode(inst: (Inst, InstLoc)): Set[CfgNode] = inst match
    case (inst: swam.syntax.Call, loc) => Set(CfgNode.Call(inst, loc), CfgNode.CallReturn(CfgNode.Call(inst, loc)))
    case (inst: swam.syntax.CallIndirect, loc) => Set(CfgNode.Call(inst, loc), CfgNode.CallReturn(CfgNode.Call(inst, loc)))
    case (inst, loc) => Set(CfgNode.Instruction(inst, loc))