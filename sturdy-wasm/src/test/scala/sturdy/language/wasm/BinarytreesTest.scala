package sturdy.language.wasm

import cats.effect.{IO, Blocker}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFallible
import sturdy.language.wasm
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.generic.FrameData
import sturdy.values.Topped
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.validation.Validator

import java.nio.file.Files
import java.nio.file.{Path, Paths}

import scala.jdk.StreamConverters.*

class BinarytreesTest extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame"

  //val testcases = List("binarytrees", "fankuchredux", "mandelbrot", "nbody", "spectral-norm")
  val base = "/sturdy/language/wasm/benchmarksgame/"
  val funcName = "_start"

  it must s"execute shortened $funcName in binarytrees_repo with concrete interpreter" in {
    val uri = classOf[BinarytreesTest].getResource(base ++ "src/binarytrees_shortened.wast").toURI();
    val path = Paths.get(uri)
    val module = wasm.parse(path)
    val interp = ConcreteInterpreter(FrameData.empty, Iterable.empty)
    val modInst = interp.initializeModule(module)
    val res = interp.effects.fallible(
      interp.invokeExported(modInst, funcName, List())
    )
    println(res)
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
