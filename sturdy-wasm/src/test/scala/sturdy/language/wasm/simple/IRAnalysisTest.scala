package sturdy.language.wasm.simple

import cats.effect.{Blocker, IO}
import org.scalatest.Assertions.assertResult
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.*
import sturdy.effect.failure.{AFallible, FailureKind}
import sturdy.fix
import sturdy.fix.StackConfig
import sturdy.fix.context.Sensitivity
import sturdy.ir.*
import sturdy.language.wasm
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.abstractions.{CfgConfig, ControlFlow}
import sturdy.language.wasm.analyses.IRAnalysis.Value
import sturdy.language.wasm.analyses.{CallSites, FixpointConfig, IRAnalysis, WasmConfig}
import sturdy.language.wasm.generic.{FixIn, FixOut, FrameData, WasmFailure}
import sturdy.language.wasm.{ConcreteInterpreter, testCfgDifference}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.integer.{IntegerDivisionByZero, NumericIntervalAbstractly}
import sturdy.values.{Abstractly, Topped}
import swam.syntax.Module
import swam.text.*

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.reflect.{ClassTag, TypeTest}


class IRAnalysisTest extends AnyFlatSpec, Matchers:
  behavior of "Wasm constant analysis"

  val uriSimple = this.getClass.getResource("/sturdy/language/wasm/simple.wast").toURI;
  val uriFact = this.getClass.getResource("/sturdy/language/wasm/fact.wast").toURI;
  val simple = Paths.get(uriSimple)
  val fact = Paths.get(uriFact)

  val uriSimpleTest = this.getClass.getResource("/sturdy/language/wasm/simple_test.wast").toURI;
  val simpleTest = Paths.get(uriSimpleTest)

//  it must s"execute most general client for simple with stacked states" in {
//    runConstantAnalysis(simple, "", List(), StackConfig.StackedStates(), mostGeneralClient = true)
//  }

//  it must s"execute most general client for simple with stacked frames" in {
//    runConstantAnalysis(simple, "", List(), StackConfig.StackedStates(), mostGeneralClient = true)
//  }

//  it must s"execute most general client for fact with stacked states" in {
//    runConstantAnalysis(fact, "", List(), StackConfig.StackedStates(), mostGeneralClient = true)
//  }

//  it must s"execute most general client for fact with stacked frames" in {
//    runConstantAnalysis(fact, "", List(), StackConfig.StackedStates(), mostGeneralClient = true)
//  }

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
//    testFunctionConstantArgs(simple, "nesting", List(Value.Float32(0), Value.Float32(2)), List(Value.Float32(0)))
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
    testFailingFunction(simple, "division", List(IRAnalysis.Value.Int32(IR.Const(1)),
      IRAnalysis.Value.Int32(IR.Const(0))), IntegerDivisionByZero)
    testFunctionConstantArgs(simple, "effects", List(Value.Int32(1)), List(Value.Int32(-14)))

//    testFunctionConstantArgs(fact, "fac-rec", List(Value.Int64(0)), List(Value.Int64(1)))
  }


  testFunction(simple, "const", List(Value.Int32(IRAnalysis.topI32)), List(Value.Int32(IRAnalysis.topI32)))
  testFunction(simple, "first", List(Value.Int32(IR.Const(1)), Value.Int32(IRAnalysis.topI32)), List(Value.Int32(IR.Const(1))))
  testFunction(simple, "first", List(Value.Int32(IRAnalysis.topI32), Value.Int32(IR.Const(2))), List(Value.Int32(IRAnalysis.topI32)))
  testFunction(simple, "second", List(Value.Int32(IR.Const(1)), Value.Int32(IRAnalysis.topI32)), List(Value.Int32(IRAnalysis.topI32)))
  testFunction(simple, "second", List(Value.Int32(IRAnalysis.topI32), Value.Int32(IR.Const(2))), List(Value.Int32(IR.Const(2))))
  testFunction(simple, "test-mem", List(Value.Int32(IRAnalysis.topI32)), List(Value.Int32(IRAnalysis.topI32)))
//  testFunction(simple, "nesting", List(Value.Float32(IRAnalysis.topF32), Value.Float32(IR.Const(2))), List(Value.Float32(IRAnalysis.topF32)))
//  testFunction(simple, "nesting", List(Value.Float32(IR.Const(1)), Value.Float32(IRAnalysis.topF32)), List(Value.Float32(IRAnalysis.topF32)))
  testFunction(simple, "test-br3", List(Value.Int32(IRAnalysis.topI32)), List(Value.Int32(IRAnalysis.topI32)))
  testFunction(simple, "test-br-and-return", List(Value.Int32(IRAnalysis.topI32)), List(Value.Int32(IRAnalysis.topI32)))
  testFunction(simple, "test-br-and-return2", List(Value.Int32(IRAnalysis.topI32)), List(Value.Int32(IRAnalysis.topI32)))
  testFunction(simple, "test-br-and-return3", List(Value.Int32(IRAnalysis.topI32)), List(Value.Int32(IRAnalysis.topI32)))
  testFunction(simple, "test-br-and-return4", List(Value.Int32(IRAnalysis.topI32)), List(Value.Int32(IR.Const(42))))
  testFunction(simple, "test-unreachable5", List(Value.Int32(IRAnalysis.topI32)), List(Value.Int32(IRAnalysis.topI32)))
  testFunction(simple, "test-global", List(Value.Int32(IRAnalysis.topI32)), List(Value.Int32(IRAnalysis.topI32)))
  testFunction(simple, "test-call-indirect-parametric", List(Value.Int32(IRAnalysis.topI32)), List(Value.Int32(IR.Const(0))))
  testFailingFunction(simple, "division", List(Value.Int32(IR.Const(1)), Value.Int32(IRAnalysis.topI32)), IntegerDivisionByZero)
  testFunction(simple, "effects", List(Value.Int32(IRAnalysis.topI32)), List(Value.Int32(IRAnalysis.topI32)))

//  testFunction(fact, "fac-rec", List(Value.Int64(IR.Const(1))), List(Value.Int64(IRAnalysis.topI64)), List(Value.Int64(IR.Const(1))))
//  (2 to 8).foreach { arg =>
//    testFunction(fact, "fac-rec", List(Value.Int64(IR.Const(arg))), List(Value.Int64(IRAnalysis.topI64)))
//  }
//  testFunction(fact, "fac-rec", List(Value.Int64(IR.Const(25))), List(Value.Int64(IRAnalysis.topI64)))
//  testFunction(fact, "fac-iter", List(Value.Int64(IR.Const(25))), List(Value.Int64(IRAnalysis.topI64)))
//  testFunction(fact, "fac-rec-named", List(Value.Int64(IR.Const(25))), List(Value.Int64(IRAnalysis.topI64)))
//  testFunction(fact, "fac-iter-named", List(Value.Int64(IR.Const(25))), List(Value.Int64(IRAnalysis.topI64)))
//  testFunction(fact, "fac-opt", List(Value.Int64(IR.Const(25))), List(Value.Int64(IRAnalysis.topI64)))
//
//  testFunction(fact, "fac-rec", List(Value.Int64(IRAnalysis.topI64)), List(Value.Int64(IRAnalysis.topI64)))
//  testFunction(fact, "fac-iter", List(Value.Int64(IRAnalysis.topI64)), List(Value.Int64(IRAnalysis.topI64)))
//  testFunction(fact, "fac-rec-named", List(Value.Int64(IRAnalysis.topI64)), List(Value.Int64(IRAnalysis.topI64)))
//  testFunction(fact, "fac-iter-named", List(Value.Int64(IRAnalysis.topI64)), List(Value.Int64(IRAnalysis.topI64)))
//  testFunction(fact, "fac-opt", List(Value.Int64(IRAnalysis.topI64)), List(Value.Int64(IRAnalysis.topI64)))

  testFunction(simpleTest, "main", List(Value.Int32(IR.Const(0))), List(Value.Int32(IR.Const(42))))
  testFunction(simpleTest, "main", List(Value.Int32(IR.Const(1))), List(Value.Int32(IR.Const(42))))
  testFunction(simpleTest, "main", List(Value.Int32(IRAnalysis.topI32)), List(Value.Int32(IRAnalysis.topI32)))


  def testFunctionConstantArgs(path: Path, funcName: String, args: List[ConcreteInterpreter.Value], expectedResult: List[ConcreteInterpreter.Value]) =
    testFunction(path, funcName, args.map(Abstractly.apply), expectedResult.map(Abstractly.apply))

  def testFunction(path: Path, funcName: String, args: List[Value], expected: List[Value], expectedFrames: List[Value] = null) =
    it must s"execute $funcName withs args $args with result $expected with stacked states" in {
      val res = runIRAnalysis(path, funcName, args, StackConfig.StackedStates())
      res match
        case AFallible.Unfailing(vals) =>
          assertResult(expected.size)(vals.size)
          expected.zip(vals).foreach(p => assert(IRAnalysis.structuralEquals(p._1, p._2), s"Expected ${p._1}, but got ${p._2}"))
        case AFallible.MaybeFailing(vals, _) =>
          assertResult(expected.size)(vals.size)
          expected.zip(vals).foreach(p => assert(IRAnalysis.structuralEquals(p._1, p._2), s"Expected ${p._1}, but got ${p._2}"))
        case AFallible.Failing(fails) => assert(false, s"Expected $expected but execution failed: $fails")
        case AFallible.Diverging(recur) => assert(false, s"Expected $expected but execution diverged: $recur")
    }
    val expected2 = Option(expectedFrames).getOrElse(expected)
//    it must s"execute $funcName withs args $args with result $expected2 with stacked frames" in {
//      val res = runConstantAnalysis(path, funcName, args, StackConfig.StackedCfgNodes())
//      res match
//        case AFallible.Unfailing(vals) => assertResult(expected2)(vals)
//        case AFallible.MaybeFailing(vals, _) => assertResult(expected2)(vals)
//        case AFallible.Failing(fails) => assert(false, s"Expected $expected2 but execution failed: $fails")
//        case AFallible.Diverging(recur) => assert(false, s"Expected $expected2 but execution diverged: $recur")
//    }

  def testFailingFunction(path: Path, funcName: String, args: List[Value], failureKind: FailureKind): Unit =
    it must s"execute $funcName with args $args throwing exception $failureKind with stacked states" in {
      val res = runIRAnalysis(path, funcName, args, StackConfig.StackedStates())
      res match
        case AFallible.Unfailing(vals) => assert(false, s"Expected $failureKind but execution succeeded: $vals")
        case AFallible.MaybeFailing(_, fails) => assert(fails.set.exists(_._1 == failureKind))
        case AFallible.Failing(fails) => assert(fails.set.exists(_._1 == failureKind))
        case AFallible.Diverging(recur) => assert(false, s"Expected $failureKind but execution diverged: $recur")
    }
//    it must s"execute $funcName with args $args throwing exception $failureKind with stacked frames" in {
//      val res = runConstantAnalysis(path, funcName, args, StackConfig.StackedCfgNodes())
//      res match
//        case AFallible.Unfailing(vals) => assert(false, s"Expected $failureKind but execution succeeded: $vals")
//        case AFallible.MaybeFailing(_, fails) => assert(fails.set.exists(_._1 == failureKind))
//        case AFallible.Failing(fails) => assert(fails.set.exists(_._1 == failureKind))
//        case AFallible.Diverging(recur) => assert(false, s"Expected $failureKind but execution diverged: $recur")
//        case AFallible.Diverging(recur) => assert(false, s"Expected $failureKind but execution diverged: $recur")
//    }


def runIRAnalysis(path: Path, funName: String, args: List[Value], stackConfig: StackConfig, mostGeneralClient: Boolean = false): AFallible[List[Value]] =
  val module = wasm.Parsing.fromText(path)

  val interp = new IRAnalysis.Instance(FrameData.empty, Iterable.empty,
    WasmConfig(FixpointConfig(fix.iter.Config.Innermost(stackConfig))))

  val graphBuilder = interp.addControlObserver(new ControlEventGraphBuilder)

  val modInst = interp.initializeModule(module)
  val result = interp.failure.fallible(
    if (!mostGeneralClient)
      interp.invokeExported(modInst, funName, args)
    else {
      interp.runMostGeneralClient(modInst, IRAnalysis.typedTop)
      List()
    }
  )
  println(s"$funName($args) = $result")

  val cfg = graphBuilder.get
  println(cfg.toGraphViz)

  LinearStateOperationCounter.addToListAndReset()
  println(s"${LinearStateOperationCounter.toString} in the last tests")
  println(s"#linear state operations in the last tests: ${LinearStateOperationCounter.getSummedOperationsPerTest}")
  Profiler.printLastMeasured()
  result
