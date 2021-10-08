package sturdy.language.wasm

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFallible
import sturdy.language.wasm
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.language.wasm.generic.FrameData
import sturdy.values.Topped
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.validation.Validator

import java.nio.file.{Path, Paths}

class BenchmarksgameTest extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame"

  val testcases = List("binarytrees", "fankuchredux", "mandelbrot", "nbody", "spectral-norm")
  //val testcases = List("fankuchredux")
  val base = "/sturdy/language/wasm/benchmarksgame/src/"
  val funcName = "_start"

  testcases.foreach { testfile =>
    val uri = classOf[BenchmarksgameTest].getResource(base ++ testfile ++ ".wasm").toURI();
    val path = Paths.get(uri)

    // run concrete interpeter
    it must s"execute $funcName in $testfile with concrete interpreter" in {
      val res = runWasmFunction(path, funcName, List.empty)
      println(res)
    }

    // run constant analysis and print CFG
    it must s"execute $funcName in $testfile with constant analysis" in {
      val module = readBinaryModule(path)
      val onlyCalls = false
      val interp = ConstantAnalysis(FrameData.empty, Iterable.empty, onlyCalls)
      val modInst = interp.initializeModule(module)
      val result = interp.effects.fallible(
        interp.invokeExported(modInst, funcName, List.empty)
      )
      println(result)
      println(interp.cfg.toGraphViz)
    }

  }

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

  def runWasmFunction(path: Path, funName: String, args: List[ConcreteInterpreter.Value]): CFallible[Iterable[ConcreteInterpreter.Value]] =
    val module = readBinaryModule(path)
    val interp = ConcreteInterpreter(FrameData.empty, Iterable.empty)
    val modInst = interp.initializeModule(module)
    interp.effects.fallible(
      interp.invokeExported(modInst, funName, args)
    )
