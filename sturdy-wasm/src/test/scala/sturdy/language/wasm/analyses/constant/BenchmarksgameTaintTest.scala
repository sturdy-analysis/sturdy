package sturdy.language.wasm.analyses.constant

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFallible
import sturdy.fix.Fixpoint
import sturdy.language.wasm
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.abstractions.{CfgConfig, CfgNode, ControlFlow}
import sturdy.language.wasm.analyses.ConstantTaintAnalysis
import sturdy.language.wasm.generic.FrameData
import sturdy.values.Topped
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.*
import swam.validation.Validator

import java.nio.file.{Files, Path, Paths}
import scala.jdk.StreamConverters.*

class BenchmarksgameTaintTest extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame"

  //val testcases = List("binarytrees", "fankuchredux", "mandelbrot", "nbody", "spectral-norm")
  val base = "/sturdy/language/wasm/benchmarksgame/"
  val funcName = "_start"

  val uri = classOf[BenchmarksgameTaintTest].getResource("/sturdy/language/wasm/benchmarksgame").toURI();

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wast")).sorted.foreach { p =>
    it must s"execute constant taint analysis on benchmark ${p.getFileName}" in {
      run(p)
    }
  }

  def run(p: Path, binary: Boolean = false) =
    val name = p.getFileName
    val module = if (binary) readBinaryModule(p) else wasm.parse(p)
    val interp = ConstantTaintAnalysis(FrameData.empty, Iterable.empty, CfgConfig.AllNodes(false))
    val modInst = interp.initializeModule(module)
    interp.effects.fallible(
      interp.invokeExported(modInst, funcName, List.empty)
    )

    val allNodes = ControlFlow.allCfgNodes(List(modInst))
    val allInstructions = allNodes.filter(_.isInstruction)
    val deadInstructions = ControlFlow.deadInstruction(interp.cfg, List(modInst))
    val deadInstructionPercent = (10000.0 * deadInstructions.size / allInstructions.size.toDouble).round / 100.0
    println(s"Found ${deadInstructions.size} dead instructions, $deadInstructionPercent% of the ${allInstructions.size} instructions in $name")

    val allLabels = allNodes.filter(_.isInstanceOf[CfgNode.Labeled])
    val deadLabels = ControlFlow.deadLabels(interp.cfg)
    val deadLabelsPercent = (10000.0 * deadLabels.size / allLabels.size.toDouble).round / 100.0
    val deadLabelsGrouped = deadLabels.groupBy(_.inst.getClass.getSimpleName)
    println(s"Found ${deadLabels.size} dead labels, $deadLabelsPercent% of the ${allLabels.size} labels in $name.")
    val deadLabelsIf = deadLabelsGrouped.getOrElse("If", Set())
    val deadLabelsBlock = deadLabelsGrouped.getOrElse("Block", Set())
    val deadLabelLoop = deadLabelsGrouped.getOrElse("Loop", Set())
    println(s"  Can optimize ${deadLabelsIf.size} if instructions; can eliminate ${deadLabelsBlock.size} block and ${deadLabelLoop.size} loop instructions.")

    // TODO constant instruction logger in ConstantTaintValues

    val allMemoryInstructions = allNodes.filter{
      case CfgNode.Instruction(inst, _) => inst match
        case _: LoadInst | _: LoadNInst | _: StoreInst | _: StoreNInst => true
        case _ => false
      case _ => false
    }
    val taintedAccesses = interp.taintedMemoryAccesses.taintedMemoryAccesses
    val taintedAccessesPercent = (10000.0 * taintedAccesses.size / allMemoryInstructions.size.toDouble).round / 100.0
    println(s"Found ${taintedAccesses.size} tainted memory accesses, $taintedAccessesPercent% of all load and store instructions in $name.")
    println(s"  This means, ${100.0 - taintedAccessesPercent}% of all load and store instructions in $name are safe.")

  def readBinaryModule(path: Path): Module =
    implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)
    Blocker[IO].use { blocker =>
      for {
        validator <- Validator[IO](blocker)
        loader = new ModuleLoader[IO]()
        binaryParser = new ModuleParser[IO](validator)
        mod <- binaryParser.parse(loader.sections(path, blocker))
      } yield mod
    }.unsafeRunSync()
