package sturdy.language.wasm.wasmbench

import sturdy.fix.Fixpoint
import sturdy.language.wasm
import sturdy.language.wasm.{Interpreter, Parsing}
import sturdy.language.wasm.abstractions.{CfgConfig, CfgNode, ControlFlow}
import sturdy.language.wasm.analyses.{ConstantAnalysis, ConstantTaintAnalysis, TypeAnalysis, WasmConfig}
import sturdy.language.wasm.generic.FrameData
import swam.syntax.{LoadInst, LoadNInst, StoreInst, StoreNInst}

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
        println("time limited reached, terminating analysis.")
        setRes(Left(e))
      case e =>
        setRes(Left(e))
    }

class TaintRunnable(set: Either[Throwable, RRecord] => Unit,
                    p: Path, funcName: String, funcArgs: List[ConstantTaintAnalysis.Value],
                    config: WasmConfig,
                    binary: Boolean = false) extends AnalysisRunnable:
  override def setRes(v: Either[Throwable, RRecord]): Unit = set(v)
  override def start(): RRecord =
    Fixpoint.DEBUG = false

    val name = p.getFileName.toString
    val startTimeMillis = System.currentTimeMillis()
    val module: swam.syntax.Module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)
    
    
    val interp = new ConstantTaintAnalysis.Instance(FrameData.empty, Iterable.empty, config)
    val cfg = ConstantTaintAnalysis.controlFlow(CfgConfig.AllNodes(false), interp)
    val constants = ConstantTaintAnalysis.constantInstructions(interp)
    val memory = ConstantTaintAnalysis.taintedMemoryAccessLogger(interp)

    val modInst = interp.initializeModule(module)
    interp.failure.fallible(
      interp.invokeExported(modInst, funcName, funcArgs)
    )

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

    val allMemoryInstructions = allNodes.filter{
      case CfgNode.Instruction(inst, _) => inst match
        case _: LoadInst | _: LoadNInst | _: StoreInst | _: StoreNInst => true
        case _ => false
      case _ => false
    }
    val taintedAccesses = memory.instructions
    val taintedAccessesPercent = (10000.0 * taintedAccesses.size / allMemoryInstructions.size.toDouble).round / 100.0

    val eliminatable = deadInstructions.size + deadLabelsBlock.size + deadLabelLoop.size + constantInstructions
    val eliminatablePercent = (10000.0 * eliminatable / allInstructions.size.toDouble).round / 100.0
    val endTimeMillis = System.currentTimeMillis()
    val duration = endTimeMillis - startTimeMillis

    println(s"Found ${deadInstructions.size} dead instructions, $deadInstructionPercent% of the ${allInstructions.size} instructions in $name")
    println(s"Found ${deadLabels.size} dead labels, $deadLabelsPercent% of the ${allLabels.size} labels in $name.")
    println(s"  Can optimize ${deadLabelsIf.size} if instructions; can eliminate ${deadLabelsBlock.size} block and ${deadLabelLoop.size} loop instructions.")
    println(s"Found $constantInstructions constant instructions, $constantInstructionPercent% of the $liveInstructions live instructions in $name")
    println(s"Found ${taintedAccesses.size} tainted memory accesses, $taintedAccessesPercent% of all load and store instructions in $name.")
    println(s"  This means, ${100.0 - taintedAccessesPercent}% of all load and store instructions in $name are safe.")
    println(s"This analysis can eliminate $eliminatable nodes, $eliminatablePercent% of the ${allInstructions.size} nodes in $name")

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
      "taintedAccesses" -> taintedAccesses.size,
      "taintedAccessesPercent" -> taintedAccessesPercent,
    )


class TypeRunnable(set: Either[Throwable, RRecord] => Unit,
                   p: Path, funcName: String, funcArgs: List[TypeAnalysis.Value],
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

    val modInst = interp.initializeModule(module)
    interp.failure.fallible(
      interp.invokeExported(modInst, funcName, funcArgs)
    )

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

class ConstantRunnable(set: Either[Throwable, RRecord] => Unit,
                       p: Path, funcName: String, funcArgs: List[ConstantAnalysis.Value],
                       config: WasmConfig,
                       binary: Boolean = false) extends AnalysisRunnable{

  override def setRes(v: Either[Throwable, RRecord]): Unit = set(v)

  override def start(): RRecord =
    Fixpoint.DEBUG = false

    val name = p.getFileName.toString

    val startTimeMillis = System.currentTimeMillis()
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    val interp = new ConstantAnalysis.Instance(FrameData.empty, Iterable.empty, config)
    val cfg = ConstantAnalysis.controlFlow(CfgConfig.AllNodes(false), interp)
    val constants = ConstantAnalysis.constantInstructions(interp)

    val modInst = interp.initializeModule(module)
    val res = interp.failure.fallible(
      interp.invokeExported(modInst, funcName, funcArgs)
    )

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
