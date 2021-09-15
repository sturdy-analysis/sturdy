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

  //val uri = classOf[ConcreteInterpreterTest].getResource("/sturdy/language/wasm").toURI();
  val uriSimple = classOf[ConcreteInterpreterTest].getResource("/sturdy/language/wasm/simple.wast").toURI();
  val uriFact = classOf[ConcreteInterpreterTest].getResource("/sturdy/language/wasm/fact.wast").toURI();
  val simple = Paths.get(uriSimple)
  val fact = Paths.get(uriFact)

  testFunction(simple, "noop", List.empty, List(Value.Int32(0)))
  testFunction(simple, "const", List(Value.Int32(5)), List(Value.Int32(5)))

  (0 to 8).zip(List(1,1,2,6,24,120,720,5040,40320)).foreach { (arg,res) =>
    testFunction(fact, "fac-rec", List(Value.Int64(arg)), List(Value.Int64(res)))
  }

  testFunction(fact, "fac-rec", List(Value.Int64(25)), List(Value.Int64(7034535277573963776)))
  testFunction(fact, "fac-iter", List(Value.Int64(25)), List(Value.Int64(7034535277573963776)))
  testFunction(fact, "fac-rec-named", List(Value.Int64(25)), List(Value.Int64(7034535277573963776)))
  testFunction(fact, "fac-iter-named", List(Value.Int64(25)), List(Value.Int64(7034535277573963776)))
  testFunction(fact, "fac-opt", List(Value.Int64(25)), List(Value.Int64(7034535277573963776)))
  
  def testFunction(path: Path, funcName: String, args: List[Value], expectedResult: List[Value]) =
    it must s"execute $funcName withs args $args with result $expectedResult" in {
      val res = runWasmFunction(path, funcName, args)
      assertResult(expectedResult)(res)
    }

//  Files.list(Paths.get(uri)).toScala(List).sorted.filter(p => p.toString.endsWith(".wast")).foreach { p =>
//    it must s"parse ${p.getFileName}" in {
//      //val path = Path.of(p.toUri)
//      val res = runWasmFunction(p, "first", List(Value.Int32(1), Value.Int32(2)))
//      print(res)
//    }
//  }

def runWasmFunction(path: Path, funName: String, args: List[Value]): List[Value] =
  val module = parse(path)
  val interp = ConcreteInterpreter(FrameData(0, null), Iterable.empty)
  val modInst = interp.initializeModule(module)
  interp.effects.inNewFrameNoIndex(FrameData(0, modInst), Iterable.empty) {
    interp.invokeExported(funName, args)
  }
