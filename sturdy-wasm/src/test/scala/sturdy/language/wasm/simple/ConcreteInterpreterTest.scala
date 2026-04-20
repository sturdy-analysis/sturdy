package sturdy.language.wasm.simple

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.*
import sturdy.effect.failure.{CFallible, FailureKind}
import sturdy.language.wasm.ConcreteInterpreter.{NumValue, Value}
import sturdy.language.wasm.generic.{FrameData, WasmFailure}
import sturdy.language.wasm.{ConcreteInterpreter, Parsing}

import java.net.URI
import java.nio.file.{Path, Paths}
import scala.jdk.StreamConverters.*


class ConcreteInterpreterTest extends AnyFlatSpec, Matchers:
  behavior of "Wasm concrete interpreter"

  val uriSimple: URI = this.getClass.getResource("/sturdy/language/wasm/simple.wast").toURI
  val uriFact: URI = this.getClass.getResource("/sturdy/language/wasm/fact.wast").toURI
  val simple: Path = Paths.get(uriSimple)
  val fact: Path = Paths.get(uriFact)

  val uriSimpleTest: URI = this.getClass.getResource("/sturdy/language/wasm/simple_test.wast").toURI
  val simpleTest: Path = Paths.get(uriSimpleTest)


  testFunction(simple, "noop", List.empty, List(Value.Num(NumValue.Int32(0))))
  testFunction(simple, "const", List(Value.Num(NumValue.Int32(5))), List(Value.Num(NumValue.Int32(5))))
  testFunction(simple, "first", List(Value.Num(NumValue.Int32(1)), Value.Num(NumValue.Int32(2))), List(Value.Num(NumValue.Int32(1))))
  testFunction(simple, "second", List(Value.Num(NumValue.Int32(1)), Value.Num(NumValue.Int32(2))), List(Value.Num(NumValue.Int32(2))))

  (0 to 8).zip(List(1, 1, 2, 6, 24, 120, 720, 5040, 40320)).foreach { (arg, res) =>
    testFunction(fact, "fac-rec", List(Value.Num(NumValue.Int64(arg))), List(Value.Num(NumValue.Int64(res))))
  }

  testFunction(fact, "fac-rec", List(Value.Num(NumValue.Int64(25))), List(Value.Num(NumValue.Int64(7034535277573963776L))))
  testFunction(fact, "fac-iter", List(Value.Num(NumValue.Int64(25))), List(Value.Num(NumValue.Int64(7034535277573963776L))))
  testFunction(fact, "fac-rec-named", List(Value.Num(NumValue.Int64(25))), List(Value.Num(NumValue.Int64(7034535277573963776L))))
  testFunction(fact, "fac-iter-named", List(Value.Num(NumValue.Int64(25))), List(Value.Num(NumValue.Int64(7034535277573963776L))))
  testFunction(fact, "fac-opt", List(Value.Num(NumValue.Int64(25))), List(Value.Num(NumValue.Int64(7034535277573963776L))))
  testFunction(simple, "test-mem", List(Value.Num(NumValue.Int32(42))), List(Value.Num(NumValue.Int32(43))))
  testFunction(simple, "test-size", List.empty, List(Value.Num(NumValue.Int32(1))))
  testFunction(simple, "test-memgrow", List.empty, List(Value.Num(NumValue.Int32(1)), Value.Num(NumValue.Int32(2))))
  testFunction(simple, "test-call-indirect", List.empty, List(Value.Num(NumValue.Int32(0))))
  testFunction(simple, "call-first", List.empty, List(Value.Num(NumValue.Int32(0))))
  testFunction(simple, "nesting", List(Value.Num(NumValue.Float32(0)), Value.Num(NumValue.Float32(2))), List(Value.Num(NumValue.Float32(0))))
  testFunction(simple, "nesting", List(Value.Num(NumValue.Float32(1)), Value.Num(NumValue.Float32(2))), List(Value.Num(NumValue.Float32(2))))
  testFunction(simple, "nesting", List(Value.Num(NumValue.Float32(4)), Value.Num(NumValue.Float32(2))), List(Value.Num(NumValue.Float32(3.4166665))))
  testFunction(simple, "as-br_table-index", List.empty, List.empty)
  testFunction(simple, "test-br1", List.empty, List(Value.Num(NumValue.Int32(42))))
  testFunction(simple, "test-br2", List.empty, List(Value.Num(NumValue.Int32(43))))
  testFunction(simple, "test-br3", List(Value.Num(NumValue.Int32(0))), List(Value.Num(NumValue.Int32(42))))
  testFunction(simple, "test-br3", List(Value.Num(NumValue.Int32(1))), List(Value.Num(NumValue.Int32(43))))
  testFunction(simple, "test-br-and-return", List(Value.Num(NumValue.Int32(0))), List(Value.Num(NumValue.Int32(42))))
  testFunction(simple, "test-br-and-return", List(Value.Num(NumValue.Int32(1))), List(Value.Num(NumValue.Int32(43))))
  testFunction(simple, "test-br-and-return2", List(Value.Num(NumValue.Int32(0))), List(Value.Num(NumValue.Int32(42))))
  testFunction(simple, "test-br-and-return2", List(Value.Num(NumValue.Int32(1))), List(Value.Num(NumValue.Int32(43))))
  testFunction(simple, "test-br-and-return3", List(Value.Num(NumValue.Int32(0))), List(Value.Num(NumValue.Int32(42))))
  testFunction(simple, "test-br-and-return3", List(Value.Num(NumValue.Int32(1))), List(Value.Num(NumValue.Int32(43))))
  testFunction(simple, "test-unreachable", List.empty, List(Value.Num(NumValue.Int32(42))))
  testFunction(simple, "test-unreachable2", List.empty, List(Value.Num(NumValue.Int32(42))))
  testFunction(simple, "test-unreachable3", List.empty, List(Value.Num(NumValue.Int32(42))))
  testFailingFunction(simple, "test-unreachable4", List.empty, WasmFailure.UnreachableInstruction)
  testFunction(simple, "test-unreachable5", List(Value.Num(NumValue.Int32(0))), List(Value.Num(NumValue.Int32(42))))
  testFunction(simple, "test-unreachable5", List(Value.Num(NumValue.Int32(1))), List(Value.Num(NumValue.Int32(43))))

  testFunction(simpleTest, "main", List(Value.Num(NumValue.Int32(0))), List(Value.Num(NumValue.Int32(42))))
  testFunction(simpleTest, "main", List(Value.Num(NumValue.Int32(1))), List(Value.Num(NumValue.Int32(42))))
  testFunction(simpleTest, "main", List(Value.Num(NumValue.Int32(1000))), List(Value.Num(NumValue.Int32(42))))


  def testFunction(path: Path, funcName: String, args: List[Value], expectedResult: List[Value]): Unit =
    it must s"execute $funcName withs args $args with result $expectedResult" in {
      val res = runWasmFunction(path, funcName, args)
      assertResult(CFallible.Unfailing(expectedResult))(res)
    }

  def testFailingFunction(path: Path, funcName: String, args: List[Value], failureKind: FailureKind): Unit =
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
  val recorder = interp.addControlObserver(new RecordingControlObserver)

  val modInst = interp.instantiateModule(module)
  val b: CFallible[Iterable[Value]] = interp.failure.fallible(
    interp.invokeExported(modInst, funName, args)
  )

  val originalSequence = recorder.events
  val tree = ControlEventParser.parse(originalSequence)
  val treeSequence = tree.print
  val tree2 = ControlEventParser.parse(treeSequence)
  val treeSequence2 = tree2.print

  assert(treeSequence == treeSequence2)
  assert(tree == tree2)

  println(tree.toGraph.toGraphViz)

  b

