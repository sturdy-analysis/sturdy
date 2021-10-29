package sturdy.language.wasm.analyses.constant

import cats.effect.Blocker
import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFallible
import sturdy.fix.EndNode
import sturdy.fix.Fixpoint
import sturdy.language.wasm
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.abstractions.CfgConfig
import sturdy.language.wasm.abstractions.CfgNode
import sturdy.language.wasm.abstractions.ControlFlow
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.language.wasm.generic.FrameData
import sturdy.values.Topped
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.validation.Validator

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.jdk.StreamConverters.*

class BenchmarksgameConstantTest extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame"

  //val testcases = List("binarytrees", "fankuchredux", "mandelbrot", "nbody", "spectral-norm")
  val base = "/sturdy/language/wasm/benchmarksgame/"
  val funcName = "_start"

  val uri = classOf[BenchmarksgameConstantTest].getResource("/sturdy/language/wasm/benchmarksgame").toURI();

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wast")).sorted.foreach { p =>
    it must s"execute constant analysis on benchmark ${p.getFileName}" in {
      run(p)
    }
  }

  def run(p: Path, binary: Boolean = false) =
    val name = p.getFileName
    val module = if (binary) readBinaryModule(p) else wasm.parse(p)
    val interp = ConstantAnalysis(FrameData.empty, Iterable.empty, CfgConfig.AllNodes(false))
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

    val liveInstructions = allInstructions.size - deadInstructions.size
    val constantInstructions = interp.constantInstructions.get.size
    val constantInstructionPercent = (10000.0 * constantInstructions / liveInstructions.toDouble).round / 100.0
    println(s"Found $constantInstructions constant instructions, $constantInstructionPercent% of the $liveInstructions live instructions in $name")

    val eliminatable = deadInstructions.size + deadLabelsBlock.size + deadLabelLoop.size + constantInstructions
    val eliminatablePercent = (10000.0 * eliminatable / allInstructions.size.toDouble).round / 100.0
    println(s"This analysis can eliminate $eliminatable nodes, $eliminatablePercent% of the ${allInstructions.size} nodes in $name")

    // write CFG to .dot file
    val dotPath = p.getParent.resolve(p.getFileName.toString + ".dot")
    Files.writeString(dotPath, interp.cfg.toGraphViz)

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
