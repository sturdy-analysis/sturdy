package sturdy.language.wasm.wasmbench

import sturdy.fix.Fixpoint
import sturdy.fix.cfg.ControlLogger
import sturdy.language.wasm
import sturdy.language.wasm.{Interpreter, Parsing}
import sturdy.language.wasm.abstractions.{CfgConfig, CfgNode, ControlFlow}
import sturdy.language.wasm.analyses.{ConstantAnalysis, ConstantTaintAnalysis, IntervalAnalysis, TypeAnalysis, WasmConfig}
import sturdy.language.wasm.generic.{FixIn, FixOut, FrameData, InstLoc, ModuleInstance}
import sturdy.util.Profiler
import swam.syntax.{CallIndirect, LoadInst, LoadNInst, StoreInst, StoreNInst}

import java.nio.file.{Files, Path}

trait AnalysisRunnable extends Runnable:
  def setRes(v: Either[Throwable, RRecord]): Unit
  def start(): RRecord

  def run(): Unit =
    try {
      val res = start()
      setRes(Right(res))
    }
    catch {
      case e: InterruptedException =>
        println("time limit reached, terminating analysis.")
        setRes(Left(e))
      case e =>
        setRes(Left(e))
    }

class TaintRunnable(set: Either[Throwable, RRecord] => Unit,
                    p: Path, scope: AnalysisScope, funcArgs: List[ConstantTaintAnalysis.Value],
                    config: WasmConfig,
                    binary: Boolean = false) extends AnalysisRunnable:
  override def setRes(v: Either[Throwable, RRecord]): Unit = set(v)
  override def start(): RRecord =
    Fixpoint.DEBUG = false
    val startTimeMillis = System.currentTimeMillis()

    val name = p.getFileName.toString
    val interp = new ConstantTaintAnalysis.Instance(FrameData.empty, Iterable.empty, config)
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)
    val memory = ConstantTaintAnalysis.taintedMemoryAccessLogger(interp)

    val modInst = interp.instantiateModule(module)
    interp.failure.fallible({
      scope match
        case AnalysisScope.SingleFunction(id) =>
          interp.invokeExported(modInst, id, funcArgs)
        case AnalysisScope.MostGeneralClient =>
          interp.runMostGeneralClient(modInst, ConstantTaintAnalysis.typedTop)
    })

    val allMemoryInstructions = memory.memoryInstructions
    val taintedAccesses = memory.taintedMemoryInstructions
    val taintedAccessesPercent = (10000.0 * taintedAccesses.size / allMemoryInstructions.size.toDouble).round / 100.0

    val endTimeMillis = System.currentTimeMillis()
    val duration = endTimeMillis - startTimeMillis

    println(s"Found ${taintedAccesses.size} tainted memory accesses, $taintedAccessesPercent% of all load and store instructions in $name.")
    println(s"  This means, ${100.0 - taintedAccessesPercent}% of all load and store instructions in $name are safe.")

    RRecord(
      "hash" -> name,
      "duration" -> duration,
      "memoryAccesses" -> allMemoryInstructions.size,
      "taintedAccesses" -> taintedAccesses.size,
      "taintedAccessesPercent" -> taintedAccessesPercent,
    )
object TaintRunnable:
  def getCsvHeadders: String =
    RRecord(
      "hash" -> 0,
      "duration" -> 0,
      "memoryAccesses" -> 0,
      "taintedAccesses" -> 0,
      "taintedAccessesPercent" -> 0,
    ).getCsvHeaders


class TypeRunnable(set: Either[Throwable, RRecord] => Unit,
                   p: Path, scope: AnalysisScope, funcArgs: List[TypeAnalysis.Value],
                   config: WasmConfig,
                   binary: Boolean = false) extends AnalysisRunnable:
  override def setRes(v: Either[Throwable, RRecord]): Unit = set(v)
  override def start(): RRecord =
    Fixpoint.DEBUG = false

    val startTimeMillis = System.currentTimeMillis()

    val name = p.getFileName.toString
    val interp = new TypeAnalysis.Instance(FrameData.empty, Iterable.empty, config)
    val cfg = TypeAnalysis.controlFlow(CfgConfig.AllNodes(false), interp)
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    val modInst = interp.instantiateModule(module)
    interp.failure.fallible({
      scope match
        case AnalysisScope.SingleFunction(id) =>
          interp.invokeExported(modInst, id, funcArgs)
        case AnalysisScope.MostGeneralClient =>
          interp.runMostGeneralClient(modInst, TypeAnalysis.typedTop)
    })

    val allNodes = ControlFlow.allCfgNodes(List(modInst))
    val allInstructions = allNodes.filter(_.isInstruction)
    val deadInstructions = ControlFlow.deadInstruction(cfg, List(modInst))
    val deadInstructionPercent = (10000.0 * deadInstructions.size / allInstructions.size.toDouble).round / 100.0

    val allLabels = allNodes.filter(_.isInstanceOf[CfgNode.Labled])
    val deadLabels = ControlFlow.deadLabels(cfg)
    val deadLabelsPercent = (10000.0 * deadLabels.size / allLabels.size.toDouble).round / 100.0
    val deadLabelsGrouped = deadLabels.groupBy(_.inst.getClass.getSimpleName)

    val deadLabelsIf = deadLabelsGrouped.getOrElse("If", Set())
    val deadLabelsBlock = deadLabelsGrouped.getOrElse("Block", Set())
    val deadLabelLoop = deadLabelsGrouped.getOrElse("Loop", Set())

    val eliminatable = deadInstructions.size + deadLabelsBlock.size + deadLabelLoop.size
    val eliminatablePercent = (10000.0 * eliminatable / allInstructions.size.toDouble).round / 100.0

    val endTimeMillis = System.currentTimeMillis()
    val duration = endTimeMillis - startTimeMillis

    println(s"Found ${deadInstructions.size} dead instructions, $deadInstructionPercent% of the ${allInstructions.size} instructions in $name")
    println(s"Found ${deadLabels.size} dead labels, $deadLabelsPercent% of the ${allLabels.size} labels in $name.")
    println(s"  Can optimize ${deadLabelsIf.size} if instructions; can eliminate ${deadLabelsBlock.size} block and ${deadLabelLoop.size} loop instructions.")
    println(s"This analysis can eliminate $eliminatable nodes, $eliminatablePercent% of the ${allInstructions.size} nodes in $name")

    // write CFG to .dot file
    val dotPath = p.getParent.resolve(p.getFileName.toString + ".types.dot")
    Files.writeString(dotPath, cfg.toGraphViz)

    RRecord(
      "hash" -> name,
      "duration" -> duration,
      "allInstructions" -> allInstructions.size,
      "deadInstructions" -> deadInstructions.size,
      "deadInstructionPercent" -> deadInstructionPercent,
      "deadLabels" -> deadLabels.size,
      "deadLabelsPercent" -> deadLabelsPercent,
      "allLabels" -> allLabels.size,
      "deadLabelsBlock" -> deadLabelsBlock.size,
      "deadLabelLoop" -> deadLabelLoop.size,
      "deadLabelsIf" -> deadLabelsIf.size,
      "eliminatable" -> eliminatable,
      "eliminatablePercent" -> eliminatablePercent,
    )
object TypeRunnable:
  def getCsvHeadders: String =
    RRecord(
      "hash" -> 0,
      "duration" -> 0,
      "allInstructions" -> 0,
      "deadInstructions" -> 0,
      "deadInstructionPercent" -> 0,
      "deadLabels" -> 0,
      "deadLabelsPercent" -> 0,
      "allLabels" -> 0,
      "deadLabelsBlock" -> 0,
      "deadLabelLoop" -> 0,
      "deadLabelsIf" -> 0,
      "eliminatable" -> 0,
      "eliminatablePercent" -> 0,
    ).getCsvHeaders



class IntervalRunnable(set: Either[Throwable, RRecord] => Unit,
                       p: Path, scope: AnalysisScope, funcArgs: List[IntervalAnalysis.Value],
                       config: WasmConfig,
                       binary: Boolean = false) extends AnalysisRunnable{

  override def setRes(v: Either[Throwable, RRecord]): Unit = set(v)

  override def start(): RRecord =
    Fixpoint.DEBUG = false

    val name = p.getFileName.toString

    val startTimeMillis = System.currentTimeMillis()
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    val interp = new IntervalAnalysis.Instance(FrameData.empty, Iterable.empty, config)
    val cfg = IntervalAnalysis.controlFlow(CfgConfig.AllNodes(false), interp)
    val constants = IntervalAnalysis.constantInstructions(interp)

    val modInst = interp.instantiateModule(module)
    interp.failure.fallible({
      scope match
        case AnalysisScope.SingleFunction(id) =>
          interp.invokeExported(modInst, id, funcArgs)
        case AnalysisScope.MostGeneralClient =>
          interp.runMostGeneralClient(modInst, IntervalAnalysis.typedTop)
    })

    val allNodes = ControlFlow.allCfgNodes(List(modInst))
    val allInstructions = allNodes.filter(_.isInstruction)
    val deadInstructions = ControlFlow.deadInstruction(cfg, List(modInst))
    val deadInstructionPercent = (10000.0 * deadInstructions.size / allInstructions.size.toDouble).round / 100.0

    val allLabels = allNodes.filter(_.isInstanceOf[CfgNode.Labled])
    val deadLabels = ControlFlow.deadLabels(cfg)
    val deadLabelsPercent = (10000.0 * deadLabels.size / allLabels.size.toDouble).round / 100.0
    val deadLabelsGrouped = deadLabels.groupBy(_.inst.getClass.getSimpleName)

    val deadLabelsIf = deadLabelsGrouped.getOrElse("If", Set())
    val deadLabelsBlock = deadLabelsGrouped.getOrElse("Block", Set())
    val deadLabelLoop = deadLabelsGrouped.getOrElse("Loop", Set())

    val liveInstructions = allInstructions.size - deadInstructions.size
    val constantInstructions = constants.get.size
    val constantInstructionPercent = (10000.0 * constantInstructions / liveInstructions.toDouble).round / 100.0

    val eliminatable = deadInstructions.size + deadLabelsBlock.size + deadLabelLoop.size + constantInstructions
    val eliminatablePercent = (10000.0 * eliminatable / allInstructions.size.toDouble).round / 100.0

    val endTimeMillis = System.currentTimeMillis()
    val duration = endTimeMillis - startTimeMillis

    println(s"Found ${deadInstructions.size} dead instructions, $deadInstructionPercent% of the ${allInstructions.size} instructions in $name")
    println(s"Found ${deadLabels.size} dead labels, $deadLabelsPercent% of the ${allLabels.size} labels in $name.")
    println(s"Can optimize ${deadLabelsIf.size} if instructions; can eliminate ${deadLabelsBlock.size} block and ${deadLabelLoop.size} loop instructions.")
    println(s"Found $constantInstructions constant instructions, $constantInstructionPercent% of the $liveInstructions live instructions in $name")
    println(s"This analysis can eliminate $eliminatable instructions, $eliminatablePercent% of the ${allInstructions.size} instructions in $name")

    // write CFG to .dot file
    val dotPath = p.getParent.resolve(p.getFileName.toString + ".dot")
    val blockCfg = cfg.withBlocks(shortLabels = true)
    Files.writeString(dotPath, blockCfg.toGraphViz)

    RRecord(
      "hash" -> name,
      "duration" -> duration,
      "allInstructions" -> allInstructions.size,
      "deadInstructions" -> deadInstructions.size,
      "deadInstructionPercent" -> deadInstructionPercent,
      "deadLabels" -> deadLabels.size,
      "deadLabelsPercent" -> deadLabelsPercent,
      "allLabels" -> allLabels.size,
      "deadLabelsBlock" -> deadLabelsBlock.size,
      "deadLabelLoop" -> deadLabelLoop.size,
      "deadLabelsIf" -> deadLabelsIf.size,
      "eliminatable" -> eliminatable,
      "eliminatablePercent" -> eliminatablePercent,
      "constantInstructions" -> constantInstructions,
      "constantInstructionPercent" -> constantInstructionPercent,
      "liveInstructions" -> liveInstructions,
    )
}
object IntervalRunnable:
  def getCsvHeadders: String =
    RRecord(
      "hash" -> 0,
      "duration" -> 0,
      "allInstructions" -> 0,
      "deadInstructions" -> 0,
      "deadInstructionPercent" -> 0,
      "deadLabels" -> 0,
      "deadLabelsPercent" -> 0,
      "allLabels" -> 0,
      "deadLabelsBlock" -> 0,
      "deadLabelLoop" -> 0,
      "deadLabelsIf" -> 0,
      "eliminatable" -> 0,
      "eliminatablePercent" -> 0,
      "constantInstructions" -> 0,
      "constantInstructionPercent" -> 0,
      "liveInstructions" -> 0,
    ).getCsvHeaders

class ConstantRunnable(set: Either[Throwable, RRecord] => Unit,
                       p: Path, scope: AnalysisScope, funcArgs: List[ConstantAnalysis.Value],
                       config: WasmConfig,
                       binary: Boolean = false) extends AnalysisRunnable{

  override def setRes(v: Either[Throwable, RRecord]): Unit = set(v)

  override def start(): RRecord =
    Fixpoint.DEBUG = false

    val name = p.getFileName.toString

    val startTimeMillis = System.currentTimeMillis()
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    println(s"Analyzing $p")
    val interp = new ConstantAnalysis.Instance(FrameData.empty, Iterable.empty, config)
    val cfg = ConstantAnalysis.controlFlow(CfgConfig.AllNodes(false), interp)
    val constants = ConstantAnalysis.constantInstructions(interp)

    val modInst = interp.instantiateModule(module)
    interp.failure.fallible({
      scope match
        case AnalysisScope.SingleFunction(id) =>
          interp.invokeExported(modInst, id, funcArgs)
        case AnalysisScope.MostGeneralClient =>
          interp.runMostGeneralClient(modInst, ConstantAnalysis.typedTop)
    })
    println(s"Analyzed $p")

    val allNodes = ControlFlow.allCfgNodes(List(modInst))
    val allInstructions = allNodes.filter(_.isInstruction)
    val deadInstructions = ControlFlow.deadInstruction(cfg, List(modInst))
    val deadInstructionPercent = (10000.0 * deadInstructions.size / allInstructions.size.toDouble).round / 100.0

    val allLabels = allNodes.filter(_.isInstanceOf[CfgNode.Labled])
    val deadLabels = ControlFlow.deadLabels(cfg)
    val deadLabelsPercent = (10000.0 * deadLabels.size / allLabels.size.toDouble).round / 100.0
    val deadLabelsGrouped = deadLabels.groupBy(_.inst.getClass.getSimpleName)

    val deadLabelsIf = deadLabelsGrouped.getOrElse("If", Set())
    val deadLabelsBlock = deadLabelsGrouped.getOrElse("Block", Set())
    val deadLabelLoop = deadLabelsGrouped.getOrElse("Loop", Set())

    val liveInstructions = allInstructions.size - deadInstructions.size
    val constantInstructions = constants.get.size
    val constantInstructionPercent = (10000.0 * constantInstructions / liveInstructions.toDouble).round / 100.0

    val eliminatable = deadInstructions.size + deadLabelsBlock.size + deadLabelLoop.size + constantInstructions
    val eliminatablePercent = (10000.0 * eliminatable / allInstructions.size.toDouble).round / 100.0

    import ConstantAnalysis.{Value, NumValue}
    def isConstant(value: ConstantAnalysis.Value): Boolean =
      value match
        case Value.TopValue => false
        case Value.Num(NumValue.Int32(v)) => v.isActual
        case Value.Num(NumValue.Int64(v)) => v.isActual
        case Value.Num(NumValue.Float32(v)) => v.isActual
        case Value.Num(NumValue.Float64(v)) => v.isActual

    var indirectCalls: Set[InstLoc] = allInstructions.collect{ case CfgNode.Instruction(_: CallIndirect,loc) =>loc }
    val preciselyResolvedIndirectCalls = indirectCalls.count(loc =>
      constants.get.get(loc) match
        case Some(List(value)) => isConstant(value)
        case Some(_) => throw new Exception("indirect calls read exactly one argument from the stack")
        case None =>
          // Indirect call has not been logged. So we remove it from the set of all loads.
          indirectCalls -= loc
          false
    )
    val preciselyResolvedIndirectCallsPercentage = (10000.0 * preciselyResolvedIndirectCalls.toDouble / indirectCalls.size.toDouble) / 100.0

    var loads: Set[InstLoc] = allInstructions.collect{ case CfgNode.Instruction(_: LoadInst | _: LoadNInst, loc) =>loc }
    val constantLoads = loads.count(loc =>
      constants.get.get(loc) match
        case Some(List(value)) => isConstant(value)
        case Some(_) => throw new Exception("loads read exactly one argument from the stack")
        case None =>
          // Load has not been logged. So we remove it from the set of all loads.
          loads -= loc
          false
    )
    val constantLoadsPercentage = (10000.0 * constantLoads.toDouble / loads.size.toDouble) / 100.0


    val endTimeMillis = System.currentTimeMillis()
    val duration = endTimeMillis - startTimeMillis

    println(s"Found ${deadInstructions.size} dead instructions, $deadInstructionPercent% of the ${allInstructions.size} instructions in $name")
    println(s"Found ${deadLabels.size} dead labels, $deadLabelsPercent% of the ${allLabels.size} labels in $name.")
    println(s"Can optimize ${deadLabelsIf.size} if instructions; can eliminate ${deadLabelsBlock.size} block and ${deadLabelLoop.size} loop instructions.")
    println(s"Found $constantInstructions constant instructions, $constantInstructionPercent% of the $liveInstructions live instructions in $name")
    println(s"This analysis can eliminate $eliminatable instructions, $eliminatablePercent% of the ${allInstructions.size} instructions in $name")

    // write CFG to .dot file
    val dotPath = p.getParent.resolve(p.getFileName.toString + ".dot")
    val blockCfg = cfg.withBlocks(shortLabels = true)
    Files.writeString(dotPath, blockCfg.toGraphViz)

    RRecord(
      "hash" -> name,
      "duration" -> duration,
      "allInstructions" -> allInstructions.size,
      "deadInstructions" -> deadInstructions.size,
      "deadInstructionPercent" -> deadInstructionPercent,
      "deadLabels" -> deadLabels.size,
      "deadLabelsPercent" -> deadLabelsPercent,
      "allLabels" -> allLabels.size,
      "deadLabelsBlock" -> deadLabelsBlock.size,
      "deadLabelLoop" -> deadLabelLoop.size,
      "deadLabelsIf" -> deadLabelsIf.size,
      "eliminatable" -> eliminatable,
      "eliminatablePercent" -> eliminatablePercent,
      "constantInstructions" -> constantInstructions,
      "constantInstructionPercent" -> constantInstructionPercent,
      "liveInstructions" -> liveInstructions,
      "preciselyResolvedIndirectCalls" -> preciselyResolvedIndirectCalls,
      "preciselyResolvedIndirectCallsPercentage" -> preciselyResolvedIndirectCallsPercentage,
      "constantLoads" -> constantLoads,
      "constantLoadsPercent" -> constantLoadsPercentage
    )
}
object ConstantRunnable:
  def getCsvHeadders: String =
    RRecord(
      "hash" -> 0,
      "duration" -> 0,
      "allInstructions" -> 0,
      "deadInstructions" -> 0,
      "deadInstructionPercent" -> 0,
      "deadLabels" -> 0,
      "deadLabelsPercent" -> 0,
      "allLabels" -> 0,
      "deadLabelsBlock" -> 0,
      "deadLabelLoop" -> 0,
      "deadLabelsIf" -> 0,
      "eliminatable" -> 0,
      "eliminatablePercent" -> 0,
      "constantInstructions" -> 0,
      "constantInstructionPercent" -> 0,
      "liveInstructions" -> 0,
    ).getCsvHeaders