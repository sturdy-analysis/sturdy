package sturdy.language.wasm.simple

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.{ControlEventGraphBuilder, ControlGraph, PrintingControlObserver}
import sturdy.effect.failure.{AFallible, FailureKind}
import sturdy.fix
import sturdy.fix.StackConfig
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm
import sturdy.language.wasm.abstractions.Control.{Atom, Section}
import sturdy.language.wasm.abstractions.{CfgConfig, ControlFlow}
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.analyses.IntervalAnalysis.Value
import sturdy.language.wasm.analyses.{CallSites, ConstantAnalysis, FixpointConfig, IntervalAnalysis, WasmConfig}
import sturdy.language.wasm.generic.{FixIn, FixOut, FrameData, WasmFailure}
import sturdy.language.wasm.{ConcreteInterpreter, testCfgDifference, compareControlGraphs}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.Topped.Top
import sturdy.values.{Abstractly, Topped}
import sturdy.values.integer.{IntegerDivisionByZero, NumericInterval}
import swam.syntax.Module
import swam.text.*

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.reflect.{ClassTag, TypeTest}

class IntervalAnalysisCFGTest extends AnyFlatSpec, Matchers:
  behavior of "Wasm interval analysis"

  val uriSimple = this.getClass.getResource("/sturdy/language/wasm/simple.wast").toURI;
  val uriFact = this.getClass.getResource("/sturdy/language/wasm/fact.wast").toURI;
  val simple = Paths.get(uriSimple)
  val fact = Paths.get(uriFact)

  val uriSimpleTest = this.getClass.getResource("/sturdy/language/wasm/simple_test.wast").toURI;
  val simpleTest = Paths.get(uriSimpleTest)

  {
    import sturdy.language.wasm.ConcreteInterpreter.Value
    testFunctionConstantArgs(simple, "noop", List.empty, List(Value.Int32(0)))
    testFunctionConstantArgs(simple, "const", List(Value.Int32(5)), List(Value.Int32(5)))
    testFunctionConstantArgs(simple, "first", List(Value.Int32(1), Value.Int32(2)), List(Value.Int32(1)))
    testFunctionConstantArgs(simple, "second", List(Value.Int32(1), Value.Int32(2)), List(Value.Int32(2)))
    testFunctionConstantArgs(simple, "test-mem0", List(Value.Int32(42)), List(Value.Int32(42)))
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
    testFailingFunction(simple, "test-unreachable4", List.empty, WasmFailure.UnreachableInstruction)
    testFunctionConstantArgs(simple, "test-unreachable5", List(Value.Int32(0)), List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-unreachable5", List(Value.Int32(1)), List(Value.Int32(43)))
    testFunctionConstantArgs(simple, "test-global", List(Value.Int32(0)), List(Value.Int32(1)))
    testFunctionConstantArgs(simple, "test-global", List(Value.Int32(1)), List(Value.Int32(2)))
    testFunctionConstantArgs(simple, "test-call-indirect-parametric", List(Value.Int32(0)), List(Value.Int32(0)))
    testFailingFunction(simple, "division",
      List(IntervalAnalysis.Value.Int32(NumericInterval.constant(1)),
      IntervalAnalysis.Value.Int32(NumericInterval.constant(0))),
      IntegerDivisionByZero)
    testFunctionConstantArgs(simple, "effects", List(Value.Int32(1)), List(Value.Int32(-14)))

    testFunctionConstantArgs(fact, "fac-rec", List(Value.Int64(0)), List(Value.Int64(1)))
  }
  val top32 = Value.Int32(NumericInterval(Integer.MIN_VALUE, Integer.MAX_VALUE))
  val top64 = Value.Int64(NumericInterval(Long.MinValue, Long.MaxValue))
  testFunction(simple, "const", List(top32), List(top32))
  testFunction(simple, "first", List(Value.Int32(NumericInterval.constant(1)), top32), List(Value.Int32(NumericInterval.constant(1))))
  testFunction(simple, "first", List(top32, Value.Int32(NumericInterval.constant(2))), List(top32))
  testFunction(simple, "second", List(Value.Int32(NumericInterval.constant(1)), top32), List(top32))
  testFunction(simple, "second", List(top32, Value.Int32(NumericInterval.constant(2))), List(Value.Int32(NumericInterval.constant(2))))
  testFunction(simple, "test-mem0", List(top32), List(top32))
  testFunction(simple, "test-mem", List(top32), List(top32))
  testFunction(simple, "nesting", List(Value.Float32(Topped.Top), Value.Float32(Topped.Actual(2))), List(Value.Float32(Topped.Top)))
  testFunction(simple, "nesting", List(Value.Float32(Topped.Actual(1)), Value.Float32(Topped.Top)), List(Value.Float32(Topped.Top)))
  testFunction(simple, "test-br3", List(top32), List(Value.Int32(NumericInterval(42, 43))))
  testFunction(simple, "test-br-and-return", List(top32), List(Value.Int32(NumericInterval(42, 43))))
  testFunction(simple, "test-br-and-return2", List(top32), List(Value.Int32(NumericInterval(42, 43))))
  testFunction(simple, "test-br-and-return3", List(top32), List(Value.Int32(NumericInterval(42, 43))))
  testFunction(simple, "test-br-and-return4", List(top32), List(Value.Int32(NumericInterval.constant(42))))
  testFunction(simple, "test-unreachable5", List(top32), List(Value.Int32(NumericInterval(42, 43))))
  testFunction(simple, "test-global", List(top32), List(Value.Int32(NumericInterval(1, 2))))
  testFunction(simple, "test-call-indirect-parametric", List(top32), List(Value.Int32(NumericInterval.constant(0))))
  testFailingFunction(simple, "division", List(Value.Int32(NumericInterval.constant(1)), top32), IntegerDivisionByZero)
  testFunction(simple, "effects", List(top32), List(Value.Int32(NumericInterval(-14, -6))))

//  testFunction(fact, "fac-rec", List(Value.Int64(NumericInterval.constant(1))), List(Value.Int64(NumericInterval.constant(1))))
  (2 to 8).foreach { arg =>
    testFunction(fact, "fac-rec", List(Value.Int64(NumericInterval.constant(arg))), List(top64))
  }
  testFunction(fact, "fac-rec", List(Value.Int64(NumericInterval.constant(25))), List(top64))
  testFunction(fact, "fac-iter", List(Value.Int64(NumericInterval.constant(25))), List(top64))
  testFunction(fact, "fac-rec-named", List(Value.Int64(NumericInterval.constant(25))), List(top64))
  testFunction(fact, "fac-iter-named", List(Value.Int64(NumericInterval.constant(25))), List(top64))
  testFunction(fact, "fac-opt", List(Value.Int64(NumericInterval.constant(25))), List(top64))
  testFunction(fact, "fac-rec", List(top64), List(top64))
  testFunction(fact, "fac-iter", List(top64), List(top64))
  testFunction(fact, "fac-rec-named", List(top64), List(top64))
  testFunction(fact, "fac-iter-named", List(top64), List(top64))
  testFunction(fact, "fac-opt", List(top64), List(top64))

  testFunction(simpleTest, "main", List(Value.Int32(NumericInterval(0,1))), List(Value.Int32(NumericInterval(42, 42))))
  testFunction(simpleTest, "main", List(Value.Int32(NumericInterval(1,5))), List(Value.Int32(NumericInterval(42, 42))))
  testFunction(simpleTest, "main", List(top32), List(Value.Int32(NumericInterval(42,42))))


  def testFunctionConstantArgs(path: Path, funcName: String, args: List[ConcreteInterpreter.Value], expectedResult: List[ConcreteInterpreter.Value]) =
    testFunction(path, funcName, args.map(Abstractly.apply), expectedResult.map(Abstractly.apply))

  def testFunction(path: Path, funcName: String, args: List[Value], expected: List[Value]) =
    it must s"execute $funcName withs args $args with result $expected with stacked states" in {
      val res = runIntervalAnalysisCFG(path, funcName, args, StackConfig.StackedStates())
      res match
        case AFallible.Unfailing(vals) => assertResult(expected)(vals)
        case AFallible.MaybeFailing(vals, _) => assertResult(expected)(vals)
        case AFallible.Failing(fails) => assert(false, s"Expected $expected but execution failed: $fails")
        case AFallible.Diverging(recur) => assert(false, s"Expected $expected but execution diverged: $recur")
    }
//    it must s"execute $funcName withs args $args with result $expected with stacked frames" in {
//      val res = runIntervalAnalysisCFG(path, funcName, args, StackConfig.StackedCfgNodes())
//      res match
//        case AFallible.Unfailing(vals) => assertResult(expected)(vals)
//        case AFallible.MaybeFailing(vals, _) => assertResult(expected)(vals)
//        case AFallible.Failing(fails) => assert(false, s"Expected $expected but execution failed: $fails")
//        case AFallible.Diverging(recur) => assert(false, s"Expected $expected but execution diverged: $recur")
//    }

  def testFailingFunction(path: Path, funcName: String, args: List[Value], failureKind: FailureKind): Unit =
    it must s"execute $funcName with args $args throwing exception $failureKind with stacked states" in {
      val res = runIntervalAnalysisCFG(path, funcName, args, StackConfig.StackedStates())
      res match
        case AFallible.Unfailing(vals) => assert(false, s"Expected $failureKind but execution succeeded: $vals")
        case AFallible.MaybeFailing(_, fails) => assert(fails.set.exists(_._1 == failureKind))
        case AFallible.Failing(fails) => assert(fails.set.exists(_._1 == failureKind))
        case AFallible.Diverging(recur) => assert(false, s"Expected $failureKind but execution diverged: $recur")
    }
//    it must s"execute $funcName with args $args throwing exception $failureKind with stacked frames" in {
//      val res = runIntervalAnalysisCFG(path, funcName, args, StackConfig.StackedCfgNodes())
//      res match
//        case AFallible.Unfailing(vals) => assert(false, s"Expected $failureKind but execution succeeded: $vals")
//        case AFallible.MaybeFailing(_, fails) => assert(fails.set.exists(_._1 == failureKind))
//        case AFallible.Failing(fails) => assert(fails.set.exists(_._1 == failureKind))
//        case AFallible.Diverging(recur) => assert(false, s"Expected $failureKind but execution diverged: $recur")
//    }


def runIntervalAnalysisCFG(path: Path, funName: String, args: List[Value], stackConfig: StackConfig): AFallible[List[Value]] =
  val module = wasm.Parsing.fromText(path)

  val interp = new IntervalAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig(FixpointConfig(stackConfig)))
  val oldCfg = IntervalAnalysis.controlFlow(CfgConfig.AllNodes(true), interp)
  val graphBuilder = interp.addControlObserver(new ControlEventGraphBuilder)

  val modInst = interp.initializeModule(module, moduleId = Some("mod"))
  val result = interp.failure.fallible(
    interp.invokeExported(modInst, funName, args)
  )

  val intervalCfg = graphBuilder.get.withName(s"intervalCFG-$funName")

  // compares old (unsound) CFG construction to new (sound) CFG construction 
  testCfgDifference(oldCfg, intervalCfg)
  
  // compares interval-based CFG to constant-based CFG
  val (constantRes, constantCfg) = runConstantAnalysisForIntervalArgs(module, funName, args, stackConfig)
  compareControlGraphs(intervalCfg, constantCfg)
  result

def runConstantAnalysisForIntervalArgs(module: Module, funName: String, args: List[Value], stackConfig: StackConfig): (AFallible[List[ConstantAnalysis.Value]], ControlGraph[Atom, Section]) =
  val interp = new ConstantAnalysis.Instance(FrameData.empty, Iterable.empty,
    WasmConfig(FixpointConfig(stackConfig)))
  val graphBuilder = interp.addControlObserver(new ControlEventGraphBuilder)
  val modInst = interp.initializeModule(module, moduleId = Some("mod"))
  val constArgs: List[ConstantAnalysis.Value] = args.map {
    case Value.TopValue => ConstantAnalysis.Value.TopValue
    case Value.Int32(iv) => ConstantAnalysis.Value.Int32(iv.toConstant)
    case Value.Int64(iv) => ConstantAnalysis.Value.Int64(iv.toConstant)
    case Value.Float32(v) => ConstantAnalysis.Value.Float32(v)
    case Value.Float64(v) => ConstantAnalysis.Value.Float64(v)
  }
  val result = interp.failure.fallible(
    interp.invokeExported(modInst, funName, constArgs)
  )
  (result, graphBuilder.get.withName(s"constantCFG-$funName"))
