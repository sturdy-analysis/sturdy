package sturdy.language.wasm.analyses

import cats.effect.Blocker
import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.effect.failure.FailureKind
import sturdy.language.wasm
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.analyses.ConstantAnalysis.Value
import sturdy.language.wasm.generic.GenericInterpreter.FrameData
import sturdy.language.wasm.generic.UnreachableInstruction
import sturdy.values.Topped

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.io.Source
import scala.jdk.StreamConverters.*
import swam.syntax.Module
import swam.text.*

import scala.reflect.ClassTag
import scala.reflect.TypeTest


class ConstantAnalysisTest extends AnyFlatSpec, Matchers:
  behavior of "Wasm constant analysis"

  val uriSimple = classOf[ConstantAnalysisTest].getResource("/sturdy/language/wasm/simple.wast").toURI();
  val uriFact = classOf[ConstantAnalysisTest].getResource("/sturdy/language/wasm/fact.wast").toURI();
  val simple = Paths.get(uriSimple)
  val fact = Paths.get(uriFact)


  {
    import sturdy.language.wasm.ConcreteInterpreter.Value
    testFunctionConstantArgs(simple, "noop", List.empty, List(Value.Int32(0)))
    testFunctionConstantArgs(simple, "const", List(Value.Int32(5)), List(Value.Int32(5)))
    testFunctionConstantArgs(simple, "first", List(Value.Int32(1), Value.Int32(2)), List(Value.Int32(1)))
    testFunctionConstantArgs(simple, "second", List(Value.Int32(1), Value.Int32(2)), List(Value.Int32(2)))

    (0 to 8).zip(List(1, 1, 2, 6, 24, 120, 720, 5040, 40320)).foreach { (arg, res) =>
      testFunctionConstantArgs(fact, "fac-rec", List(Value.Int64(arg)), List(Value.Int64(res)))
    }

    testFunctionConstantArgs(fact, "fac-rec", List(Value.Int64(25)), List(Value.Int64(7034535277573963776)))
    testFunctionConstantArgs(fact, "fac-iter", List(Value.Int64(25)), List(Value.Int64(7034535277573963776)))
    testFunctionConstantArgs(fact, "fac-rec-named", List(Value.Int64(25)), List(Value.Int64(7034535277573963776)))
    testFunctionConstantArgs(fact, "fac-iter-named", List(Value.Int64(25)), List(Value.Int64(7034535277573963776)))
    testFunctionConstantArgs(fact, "fac-opt", List(Value.Int64(25)), List(Value.Int64(7034535277573963776)))
    testFunctionConstantArgs(simple, "test-mem", List(Value.Int32(42)), List(Value.Int32(43)))
    testFunctionConstantArgs(simple, "test-size", List.empty, List(Value.Int32(1)))
    testFunctionConstantArgs(simple, "test-memgrow", List.empty, List(Value.Int32(1), Value.Int32(2)))
    testFunctionConstantArgs(simple, "test-call-indirect", List.empty, List(Value.Int32(0)))
    testFunctionConstantArgs(simple, "call-first", List.empty, List(Value.Int32(0)))
    testFunctionConstantArgs(simple, "nesting", List(Value.Float32(1), Value.Float32(2)), List(Value.Float32(2)))
    testFunctionConstantArgs(simple, "as-br_table-index", List.empty, List.empty)
    testFunctionConstantArgs(simple, "test-br1", List.empty, List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-br2", List.empty, List(Value.Int32(43)))
    testFunctionConstantArgs(simple, "test-br3", List(Value.Int32(0)), List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-br3", List(Value.Int32(1)), List(Value.Int32(43)))
    testFunctionConstantArgs(simple, "test-br-and-return", List(Value.Int32(0)), List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-br-and-return", List(Value.Int32(1)), List(Value.Int32(43)))
    testFunctionConstantArgs(simple, "test-br-and-return2", List(Value.Int32(0)), List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-br-and-return2", List(Value.Int32(1)), List(Value.Int32(43)))
    testFunctionConstantArgs(simple, "test-br-and-return3", List(Value.Int32(0)), List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-br-and-return3", List(Value.Int32(1)), List(Value.Int32(43)))
    testFunctionConstantArgs(simple, "test-unreachable", List.empty, List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-unreachable2", List.empty, List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-unreachable3", List.empty, List(Value.Int32(42)))
    testFailingFunction[CFailureException](simple, "test-unreachable4", List.empty, UnreachableInstruction)
    testFunctionConstantArgs(simple, "test-unreachable5", List(Value.Int32(0)), List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-unreachable5", List(Value.Int32(1)), List(Value.Int32(43)))
  }

  def testFunctionConstantArgs(path: Path, funcName: String, args: List[ConcreteInterpreter.Value], expectedResult: List[ConcreteInterpreter.Value]) =
    it must s"execute $funcName withs args $args with result $expectedResult" in {
      val res = runConstantAnalysis(path, funcName, args.map(ConstantAnalysis.liftConcreteValue))
      assertResult(expectedResult.map(ConstantAnalysis.liftConcreteValue))(res)
    }

  def testFailingFunction[E <: CFailureException](path: Path, funcName: String, args: List[Value], failureKind: FailureKind)(using ClassTag[E]) =
    it must s"execute $funcName with args $args throwing exception $failureKind" in {
      val caught = intercept[E] {
        runConstantAnalysis(path, funcName, args)
      }
      assertResult(failureKind)(caught.kind)
    }


def runConstantAnalysis(path: Path, funName: String, args: List[Value]): List[Value] =
  val module = wasm.parse(path)
  val interp = ConstantAnalysis(FrameData(0, null), Iterable.empty)
  val modInst = interp.initializeModule(module)
  interp.effects.inNewFrameNoIndex(FrameData(0, modInst), Iterable.empty) {
    interp.invokeExported(funName, args)
  }
