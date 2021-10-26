package sturdy.language.wasm.analyses.constant

import cats.effect.Blocker
import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFallible
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

class BinarytreesTest extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame"

  //val testcases = List("binarytrees", "fankuchredux", "mandelbrot", "nbody", "spectral-norm")
  val base = "/sturdy/language/wasm/benchmarksgame/"
  val funcName = "_start"

  val uri = classOf[BinarytreesTest].getResource("/sturdy/language/wasm/benchmarksgame").toURI();

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
    val result = interp.effects.fallible(
      interp.invokeExported(modInst, funcName, List.empty)
    )

    val allNodes = ControlFlow.allCfgNodes(List(modInst))
    val deadNodes = interp.cfg.filterDeadNodes(allNodes).size
    val deadNodesPercent = (10000.0 * deadNodes / allNodes.size.toDouble).round / 100.0
    println(s"Found $deadNodes dead nodes, $deadNodesPercent% of the ${allNodes.size} nodes in $name")

    val liveInstructions = interp.cfg.getNodes.view.map(_.node).flatMap {
      case CfgNode.Instruction(_, loc) => Some(loc)
      case CfgNode.Call(_, loc) => Some(loc)
      case _ => None
    }.toSet.size
    val constantInstructions = interp.constantInstructions.get.size
    val constantInstructionPercent = (10000.0 * constantInstructions / liveInstructions.toDouble).round / 100.0
    println(s"Found $constantInstructions constant instructions, $constantInstructionPercent% of the $liveInstructions live instructions in $name")

    val eliminatable = deadNodes + constantInstructions
    val eliminatablePercent = (10000.0 * eliminatable / allNodes.size.toDouble).round / 100.0
    println(s"This analysis can eliminate $eliminatable, $eliminatablePercent% of the ${allNodes.size} nodes in $name")

//  // run constant analysis and print CFG
//  it must s"execute $funcName in binarytrees_repo with constant analysis without throwing a recurrent call exception" in {
//    val uri = classOf[BinarytreesTest].getResource(base ++ "src/binarytrees.wasm").toURI;
//    val path = Paths.get(uri)
//    run(path, binary = true)
//  }
//
//  // run constant analysis and print CFG
//  it must s"execute shortened $funcName in binarytrees_repo with constant analysis without throwing a recurrent call exception" in {
//    val uri = classOf[BinarytreesTest].getResource(base ++ "src/binarytrees_shortened.wast").toURI;
//    val path = Paths.get(uri)
//    run(path)
//  }

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
