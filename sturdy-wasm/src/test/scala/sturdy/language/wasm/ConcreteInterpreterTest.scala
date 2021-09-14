package sturdy.language.wasm

import cats.effect.Blocker
import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.wasm.generic.GenericInterpreter.FrameData
import ConcreteInterpreter.Value

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.io.Source
import scala.jdk.StreamConverters.*
import swam.syntax.Module
import swam.text.*


class ConcreteInterpreterTest extends AnyFlatSpec, Matchers:
  behavior of "Wasm concrete interpreter"

  val uri = classOf[ConcreteInterpreterTest].getResource("/sturdy/language/wasm").toURI();

  Files.list(Paths.get(uri)).toScala(List).sorted.filter(p => p.toString.endsWith(".wast")).foreach { p =>
    it must s"parse ${p.getFileName}" in {
      val path = Path.of(p.toUri)
      val res = runWasmFunction(path, "first", List(Value.Int32(1), Value.Int32(2)))
      print(res)
    }
  }

def runWasmFunction(path: Path, funName: String, args: List[Value]): List[Value] =
  val module = parse(path)
  val interp = ConcreteInterpreter(FrameData(0, null), Iterable.empty)
  val modInst = interp.initializeModule(module)
  interp.effects.inNewFrameNoIndex(FrameData(0, modInst), Iterable.empty) {
    interp.invokeExported(funName, args)
  }
