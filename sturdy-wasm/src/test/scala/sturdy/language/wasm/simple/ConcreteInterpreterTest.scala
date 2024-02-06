package sturdy.language.wasm.simple

import cats.effect.Blocker
import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.PrintingControlObserver
import sturdy.effect.failure.AFallible
import sturdy.effect.failure.FailureKind
import sturdy.language.wasm.generic.FrameData
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.ConcreteInterpreter.Value
import sturdy.effect.failure.CFallible
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.generic.WasmFailure

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.io.Source
import scala.jdk.StreamConverters.*
import swam.syntax.Module
import swam.text.*


class ConcreteInterpreterTest extends AnyFlatSpec, Matchers:
  behavior of "Wasm concrete interpreter"

  val uriSimple = this.getClass.getResource("/sturdy/language/wasm/simple.wast").toURI;
  val uriFact = this.getClass.getResource("/sturdy/language/wasm/fact.wast").toURI;
  val simple = Paths.get(uriSimple)
  val fact = Paths.get(uriFact)

  testFunction(simple, "noop", List.empty, List(Value.Int32(0)))
  testFunction(simple, "const", List(Value.Int32(5)), List(Value.Int32(5)))
  testFunction(simple, "first", List(Value.Int32(1), Value.Int32(2)), List(Value.Int32(1)))
  testFunction(simple, "second", List(Value.Int32(1), Value.Int32(2)), List(Value.Int32(2)))

  (0 to 8).zip(List(1,1,2,6,24,120,720,5040,40320)).foreach { (arg,res) =>
    testFunction(fact, "fac-rec", List(Value.Int64(arg)), List(Value.Int64(res)))
  }

  testFunction(fact, "fac-rec", List(Value.Int64(25)), List(Value.Int64(7034535277573963776)))
  testFunction(fact, "fac-iter", List(Value.Int64(25)), List(Value.Int64(7034535277573963776)))
  testFunction(fact, "fac-rec-named", List(Value.Int64(25)), List(Value.Int64(7034535277573963776)))
  testFunction(fact, "fac-iter-named", List(Value.Int64(25)), List(Value.Int64(7034535277573963776)))
  testFunction(fact, "fac-opt", List(Value.Int64(25)), List(Value.Int64(7034535277573963776)))
  testFunction(simple, "test-mem", List(Value.Int32(42)), List(Value.Int32(43)))
  testFunction(simple, "test-size", List.empty, List(Value.Int32(1)))
  testFunction(simple, "test-memgrow", List.empty, List(Value.Int32(1), Value.Int32(2)))
  testFunction(simple, "test-call-indirect", List.empty, List(Value.Int32(0)))
  testFunction(simple, "call-first", List.empty, List(Value.Int32(0)))
  testFunction(simple, "nesting", List(Value.Float32(0), Value.Float32(2)), List(Value.Float32(0)))
  testFunction(simple, "nesting", List(Value.Float32(1), Value.Float32(2)), List(Value.Float32(2)))
  testFunction(simple, "nesting", List(Value.Float32(4), Value.Float32(2)), List(Value.Float32(3.4166665)))
  testFunction(simple, "as-br_table-index", List.empty, List.empty)
  testFunction(simple, "test-br1", List.empty, List(Value.Int32(42)))
  testFunction(simple, "test-br2", List.empty, List(Value.Int32(43)))
  testFunction(simple, "test-br3", List(Value.Int32(0)), List(Value.Int32(42)))
  testFunction(simple, "test-br3", List(Value.Int32(1)), List(Value.Int32(43)))
  testFunction(simple, "test-br-and-return", List(Value.Int32(0)), List(Value.Int32(42)))
  testFunction(simple, "test-br-and-return", List(Value.Int32(1)), List(Value.Int32(43)))
  testFunction(simple, "test-br-and-return2", List(Value.Int32(0)), List(Value.Int32(42)))
  testFunction(simple, "test-br-and-return2", List(Value.Int32(1)), List(Value.Int32(43)))
  testFunction(simple, "test-br-and-return3", List(Value.Int32(0)), List(Value.Int32(42)))
  testFunction(simple, "test-br-and-return3", List(Value.Int32(1)), List(Value.Int32(43)))
  testFunction(simple, "test-unreachable", List.empty, List(Value.Int32(42)))
  testFunction(simple, "test-unreachable2", List.empty, List(Value.Int32(42)))
  testFunction(simple, "test-unreachable3", List.empty, List(Value.Int32(42)))
  testFailingFunction(simple, "test-unreachable4", List.empty, WasmFailure.UnreachableInstruction)
  testFunction(simple, "test-unreachable5", List(Value.Int32(0)), List(Value.Int32(42)))
  testFunction(simple, "test-unreachable5", List(Value.Int32(1)), List(Value.Int32(43)))

  def testFunction(path: Path, funcName: String, args: List[Value], expectedResult: List[Value]) =
    it must s"execute $funcName withs args $args with result $expectedResult" in {
      val res = runWasmFunction(path, funcName, args)
      assertResult(CFallible.Unfailing(expectedResult))(res)
    }

  def testFailingFunction(path: Path, funcName: String, args: List[Value], failureKind: FailureKind) =
    it must s"execute $funcName with args $args throwing exception $failureKind" in {
      val res = runWasmFunction(path, funcName, args)
      assert(res.isFailing)
      val kind = res.asInstanceOf[CFallible.Failing[_]].kind
      assert(kind == failureKind)
    }


def runWasmFunction(path: Path, funName: String, args: List[Value]): CFallible[Iterable[Value]] =
  val module = Parsing.fromText(path)
  val interp = new ConcreteInterpreter.Instance(FrameData.empty, Iterable.empty)
  interp.addControlObserver(new PrintingControlObserver("  ", "\n")(println))
  val modInst = interp.initializeModule(module)
  interp.failure.fallible(
    interp.invokeExported(modInst, funName, args)
  )
