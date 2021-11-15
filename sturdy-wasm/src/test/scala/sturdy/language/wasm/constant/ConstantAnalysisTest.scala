package sturdy.language.wasm.constant

import cats.effect.Blocker
import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.AFallible
import sturdy.effect.failure.FailureKind
import sturdy.language.wasm
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.abstractions.CfgConfig
import sturdy.language.wasm.abstractions.ControlFlow
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.language.wasm.analyses.ConstantAnalysis.Value
import sturdy.language.wasm.analyses.CallSites
import sturdy.language.wasm.analyses.WasmConfig
import sturdy.language.wasm.generic.FrameData
import sturdy.language.wasm.generic.UnreachableInstruction
import sturdy.values.Topped
import sturdy.values.integer.IntegerDivisionByZero

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
    testFunctionConstantArgs(simple, "test-mem", List(Value.Int32(42)), List(Value.Int32(43)))
    testFunctionConstantArgs(simple, "test-size", List.empty, List(Value.Int32(1)))
    testFunctionConstantArgs(simple, "test-memgrow", List.empty, List(Value.Int32(1), Value.Int32(2)))
    testFunctionConstantArgs(simple, "test-call-indirect", List.empty, List(Value.Int32(0)))
    testFunctionConstantArgs(simple, "call-first", List.empty, List(Value.Int32(0)))
    testFunctionConstantArgs(simple, "nesting", List(Value.Float32(0), Value.Float32(2)), List(Value.Float32(0)))
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
    testFailingFunction(simple, "test-unreachable4", List.empty, UnreachableInstruction)
    testFunctionConstantArgs(simple, "test-unreachable5", List(Value.Int32(0)), List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-unreachable5", List(Value.Int32(1)), List(Value.Int32(43)))
    testFunctionConstantArgs(simple, "test-global", List(Value.Int32(0)), List(Value.Int32(1)))
    testFunctionConstantArgs(simple, "test-global", List(Value.Int32(1)), List(Value.Int32(2)))
    testFunctionConstantArgs(simple, "test-call-indirect-parametric", List(Value.Int32(0)), List(Value.Int32(0)))
    testFailingFunction(simple, "division", List(ConstantAnalysis.Value.Int32(Topped.Actual(1)),
      ConstantAnalysis.Value.Int32(Topped.Actual(0))), IntegerDivisionByZero)
    testFunctionConstantArgs(simple, "effects", List(Value.Int32(1)), List(Value.Int32(-14)))

    testFunctionConstantArgs(fact, "fac-rec", List(Value.Int64(0)), List(Value.Int64(1)))
  }


  testFunction(simple, "const", List(Value.Int32(Topped.Top)), List(Value.Int32(Topped.Top)))
  testFunction(simple, "first", List(Value.Int32(Topped.Actual(1)), Value.Int32(Topped.Top)), List(Value.Int32(Topped.Actual(1))))
  testFunction(simple, "first", List(Value.Int32(Topped.Top), Value.Int32(Topped.Actual(2))), List(Value.Int32(Topped.Top)))
  testFunction(simple, "second", List(Value.Int32(Topped.Actual(1)), Value.Int32(Topped.Top)), List(Value.Int32(Topped.Top)))
  testFunction(simple, "second", List(Value.Int32(Topped.Top), Value.Int32(Topped.Actual(2))), List(Value.Int32(Topped.Actual(2))))
  testFunction(simple, "test-mem", List(Value.Int32(Topped.Top)), List(Value.Int32(Topped.Top)))
  testFunction(simple, "nesting", List(Value.Float32(Topped.Top), Value.Float32(Topped.Actual(2))), List(Value.Float32(Topped.Top)))
  testFunction(simple, "nesting", List(Value.Float32(Topped.Actual(1)), Value.Float32(Topped.Top)), List(Value.Float32(Topped.Top)))
  testFunction(simple, "test-br3", List(Value.Int32(Topped.Top)), List(Value.Int32(Topped.Top)))
  testFunction(simple, "test-br-and-return", List(Value.Int32(Topped.Top)), List(Value.Int32(Topped.Top)))
  testFunction(simple, "test-br-and-return2", List(Value.Int32(Topped.Top)), List(Value.Int32(Topped.Top)))
  testFunction(simple, "test-br-and-return3", List(Value.Int32(Topped.Top)), List(Value.Int32(Topped.Top)))
  testFunction(simple, "test-br-and-return4", List(Value.Int32(Topped.Top)), List(Value.Int32(Topped.Actual(42))))
  testFunction(simple, "test-unreachable5", List(Value.Int32(Topped.Top)), List(Value.Int32(Topped.Top)))
  testFunction(simple, "test-global", List(Value.Int32(Topped.Top)), List(Value.Int32(Topped.Top)))
  testFunction(simple, "test-call-indirect-parametric", List(Value.Int32(Topped.Top)), List(Value.Int32(Topped.Actual(0))))
  testFailingFunction(simple, "division", List(Value.Int32(Topped.Actual(1)), Value.Int32(Topped.Top)), IntegerDivisionByZero)
  testFunction(simple, "effects", List(Value.Int32(Topped.Top)), List(Value.Int32(Topped.Top)))

  // these two are precise due to call-site sensitivity
  testFunction(fact, "fac-rec", List(Value.Int64(Topped.Actual(1))), List(Value.Int64(Topped.Actual(1))))
  testFunction(fact, "fac-rec", List(Value.Int64(Topped.Actual(2))), List(Value.Int64(Topped.Actual(2))))

  (3 to 8).foreach { arg =>
    testFunction(fact, "fac-rec", List(Value.Int64(Topped.Actual(arg))), List(Value.Int64(Topped.Top)))
  }
  testFunction(fact, "fac-rec", List(Value.Int64(Topped.Actual(25))), List(Value.Int64(Topped.Top)))
  testFunction(fact, "fac-iter", List(Value.Int64(Topped.Actual(25))), List(Value.Int64(Topped.Top)))
  testFunction(fact, "fac-rec-named", List(Value.Int64(Topped.Actual(25))), List(Value.Int64(Topped.Top)))
  testFunction(fact, "fac-iter-named", List(Value.Int64(Topped.Actual(25))), List(Value.Int64(Topped.Top)))
  testFunction(fact, "fac-opt", List(Value.Int64(Topped.Actual(25))), List(Value.Int64(Topped.Top)))

  testFunction(fact, "fac-rec", List(Value.Int64(Topped.Top)), List(Value.Int64(Topped.Top)))
  testFunction(fact, "fac-iter", List(Value.Int64(Topped.Top)), List(Value.Int64(Topped.Top)))
  testFunction(fact, "fac-rec-named", List(Value.Int64(Topped.Top)), List(Value.Int64(Topped.Top)))
  testFunction(fact, "fac-iter-named", List(Value.Int64(Topped.Top)), List(Value.Int64(Topped.Top)))
  testFunction(fact, "fac-opt", List(Value.Int64(Topped.Top)), List(Value.Int64(Topped.Top)))



  def testFunctionConstantArgs(path: Path, funcName: String, args: List[ConcreteInterpreter.Value], expectedResult: List[ConcreteInterpreter.Value]) =
    testFunction(path, funcName, args.map(ConstantAnalysis.liftConcreteValue), expectedResult.map(ConstantAnalysis.liftConcreteValue))

  def testFunction(path: Path, funcName: String, args: List[Value], expected: List[Value]) =
    it must s"execute $funcName withs args $args with result $expected" in {
      val res = runConstantAnalysis(path, funcName, args)
      res match
        case AFallible.Unfailing(vals) => assertResult(expected)(vals)
        case AFallible.MaybeFailing(vals, _) => assertResult(expected)(vals)
        case AFallible.Failing(fails) => assert(false, s"Expected $expected but execution failed: $fails")
    }

  def testFailingFunction(path: Path, funcName: String, args: List[Value], failureKind: FailureKind) =
    it must s"execute $funcName with args $args throwing exception $failureKind" in {
      val res = runConstantAnalysis(path, funcName, args)//args.map(ConstantAnalysis.liftConcreteValue))
      res match
        case AFallible.Unfailing(vals) => assert(false, s"Expected $failureKind but execution succeeded: $vals")
        case AFallible.MaybeFailing(_, fails) => assert(fails.set.exists(_._1 == failureKind))
        case AFallible.Failing(fails) => assert(fails.set.exists(_._1 == failureKind))
    }


def runConstantAnalysis(path: Path, funName: String, args: List[Value]): AFallible[List[Value]] =
  val module = wasm.parse(path)

  val interp = ConstantAnalysis(FrameData.empty, Iterable.empty)(WasmConfig(ctx = CallSites(3)))
  val cfg = ConstantAnalysis.controlFlow(CfgConfig.AllNodes(true), interp)
  val constants = ConstantAnalysis.constantInstructions(interp)

  val modInst = interp.initializeModule(module)
  val result = interp.effects.fallible(
    interp.invokeExported(modInst, funName, args)
  )
//  println(cfg.toGraphViz)

  val deadInstructions = ControlFlow.deadInstruction(cfg, List(modInst))
  val deadLabels = ControlFlow.deadLabels(cfg)
  val constantInstructions = constants.get
  println(s"Found ${deadInstructions.size} dead instructions")
  println(s"Found ${deadLabels.size} dead labels")
  println(s"Found ${constantInstructions.size} constant instructions")
  result
