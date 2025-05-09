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
import sturdy.language.wasm
import sturdy.language.wasm.analyses.ConstantAnalysis.NumValue
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.abstractions.{CfgConfig, ControlFlow}
import sturdy.language.wasm.analyses.ConstantAnalysis.Value
import sturdy.language.wasm.analyses.{CallSites, ConstantAnalysis, FixpointConfig, WasmConfig}
import sturdy.language.wasm.generic.{FixIn, FixOut, FrameData, WasmFailure}
import sturdy.language.wasm.{ConcreteInterpreter, testCfgDifference}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.integer.{IntegerDivisionByZero, NumericIntervalAbstractly}
import sturdy.values.{Abstractly, Topped}
import swam.syntax.Module
import swam.text.*

import java.net.URI
import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.reflect.{ClassTag, TypeTest}


class ConstantAnalysisCFGTest extends AnyFlatSpec, Matchers:
  behavior of "Wasm constant analysis"

  val uriSimple: URI = this.getClass.getResource("/sturdy/language/wasm/simple.wast").toURI
  val uriFact: URI = this.getClass.getResource("/sturdy/language/wasm/fact.wast").toURI
  val simple: Path = Paths.get(uriSimple)
  val fact: Path = Paths.get(uriFact)

  val uriSimpleTest: URI = this.getClass.getResource("/sturdy/language/wasm/simple_test.wast").toURI
  val simpleTest: Path = Paths.get(uriSimpleTest)

//  it must s"execute most general client for simple with stacked states" in {
//    runConstantAnalysisCFG(simple, "", List(), StackConfig.StackedStates(), mostGeneralClient = true)
//  }

//  it must s"execute most general client for simple with stacked frames" in {
//    runConstantAnalysisCFG(simple, "", List(), StackConfig.StackedStates(), mostGeneralClient = true)
//  }

//  it must s"execute most general client for fact with stacked states" in {
//    runConstantAnalysisCFG(fact, "", List(), StackConfig.StackedStates(), mostGeneralClient = true)
//  }

//  it must s"execute most general client for fact with stacked frames" in {
//    runConstantAnalysisCFG(fact, "", List(), StackConfig.StackedStates(), mostGeneralClient = true)
//  }

//  {
//    import sturdy.language.wasm.ConcreteInterpreter.Value
//    testFunctionConstantArgs(simple, "noop", List.empty, List(Value.Int32(0)))
//    testFunctionConstantArgs(simple, "const", List(Value.Int32(5)), List(Value.Int32(5)))
//    testFunctionConstantArgs(simple, "first", List(Value.Int32(1), Value.Int32(2)), List(Value.Int32(1)))
//    testFunctionConstantArgs(simple, "second", List(Value.Int32(1), Value.Int32(2)), List(Value.Int32(2)))
//    testFunctionConstantArgs(simple, "test-mem", List(Value.Int32(42)), List(Value.Int32(43)))
//    testFunctionConstantArgs(simple, "test-size", List.empty, List(Value.Int32(1)))
//    testFunctionConstantArgs(simple, "test-memgrow", List.empty, List(Value.Int32(1), Value.Int32(2)))
//    testFunctionConstantArgs(simple, "test-call-indirect", List.empty, List(Value.Int32(0)))
//    testFunctionConstantArgs(simple, "call-first", List.empty, List(Value.Int32(0)))
//    testFunctionConstantArgs(simple, "nesting", List(Value.Float32(0), Value.Float32(2)), List(Value.Float32(0)))
//    testFunctionConstantArgs(simple, "as-br_table-index", List.empty, List.empty)
//    testFunctionConstantArgs(simple, "test-br1", List.empty, List(Value.Int32(42)))
//    testFunctionConstantArgs(simple, "test-br2", List.empty, List(Value.Int32(43)))
//    testFunctionConstantArgs(simple, "test-br3", List(Value.Int32(0)), List(Value.Int32(42)))
//    testFunctionConstantArgs(simple, "test-br3", List(Value.Int32(1)), List(Value.Int32(43)))
//    testFunctionConstantArgs(simple, "test-br-and-return", List(Value.Int32(0)), List(Value.Int32(42)))
//    testFunctionConstantArgs(simple, "test-br-and-return", List(Value.Int32(1)), List(Value.Int32(43)))
//    testFunctionConstantArgs(simple, "test-br-and-return2", List(Value.Int32(0)), List(Value.Int32(42)))
//    testFunctionConstantArgs(simple, "test-br-and-return2", List(Value.Int32(1)), List(Value.Int32(43)))
//    testFunctionConstantArgs(simple, "test-br-and-return3", List(Value.Int32(0)), List(Value.Int32(42)))
//    testFunctionConstantArgs(simple, "test-br-and-return3", List(Value.Int32(1)), List(Value.Int32(43)))
//    testFunctionConstantArgs(simple, "test-unreachable", List.empty, List(Value.Int32(42)))
//    testFunctionConstantArgs(simple, "test-unreachable2", List.empty, List(Value.Int32(42)))
//    testFunctionConstantArgs(simple, "test-unreachable3", List.empty, List(Value.Int32(42)))
//    testFailingFunction(simple, "test-unreachable4", List.empty, WasmFailure.UnreachableInstruction)
//    testFunctionConstantArgs(simple, "test-unreachable5", List(Value.Int32(0)), List(Value.Int32(42)))
//    testFunctionConstantArgs(simple, "test-unreachable5", List(Value.Int32(1)), List(Value.Int32(43)))
//    testFunctionConstantArgs(simple, "test-global", List(Value.Int32(0)), List(Value.Int32(1)))
//    testFunctionConstantArgs(simple, "test-global", List(Value.Int32(1)), List(Value.Int32(2)))
//    testFunctionConstantArgs(simple, "test-call-indirect-parametric", List(Value.Int32(0)), List(Value.Int32(0)))
//    testFailingFunction(simple, "division", List(ConstantAnalysis.Value.Int32(Topped.Actual(1)),
//      ConstantAnalysis.Value.Int32(Topped.Actual(0))), IntegerDivisionByZero)
//    testFunctionConstantArgs(simple, "effects", List(Value.Int32(1)), List(Value.Int32(-14)))
//
//    testFunctionConstantArgs(fact, "fac-rec", List(Value.Int64(0)), List(Value.Int64(1)))
//  }


  testFunction(simple, "const", List(Value.Num(NumValue.Int32(Topped.Top))), List(Value.Num(NumValue.Int32(Topped.Top))))
  testFunction(simple, "first", List(Value.Num(NumValue.Int32(Topped.Actual(1))), Value.Num(NumValue.Int32(Topped.Top))), List(Value.Num(NumValue.Int32(Topped.Actual(1)))))
  testFunction(simple, "first", List(Value.Num(NumValue.Int32(Topped.Top)), Value.Num(NumValue.Int32(Topped.Actual(2)))), List(Value.Num(NumValue.Int32(Topped.Top))))
  testFunction(simple, "second", List(Value.Num(NumValue.Int32(Topped.Actual(1))), Value.Num(NumValue.Int32(Topped.Top))), List(Value.Num(NumValue.Int32(Topped.Top))))
  testFunction(simple, "second", List(Value.Num(NumValue.Int32(Topped.Top)), Value.Num(NumValue.Int32(Topped.Actual(2)))), List(Value.Num(NumValue.Int32(Topped.Actual(2)))))
  testFunction(simple, "test-mem", List(Value.Num(NumValue.Int32(Topped.Top))), List(Value.Num(NumValue.Int32(Topped.Top))))
  testFunction(simple, "nesting", List(Value.Num(NumValue.Float32(Topped.Top)), Value.Num(NumValue.Float32(Topped.Actual(2)))), List(Value.Num(NumValue.Float32(Topped.Top))))
  testFunction(simple, "nesting", List(Value.Num(NumValue.Float32(Topped.Actual(1))), Value.Num(NumValue.Float32(Topped.Top))), List(Value.Num(NumValue.Float32(Topped.Top))))
  testFunction(simple, "test-br3", List(Value.Num(NumValue.Int32(Topped.Top))), List(Value.Num(NumValue.Int32(Topped.Top))))
  testFunction(simple, "test-br-and-return", List(Value.Num(NumValue.Int32(Topped.Top))), List(Value.Num(NumValue.Int32(Topped.Top))))
  testFunction(simple, "test-br-and-return2", List(Value.Num(NumValue.Int32(Topped.Top))), List(Value.Num(NumValue.Int32(Topped.Top))))
  testFunction(simple, "test-br-and-return3", List(Value.Num(NumValue.Int32(Topped.Top))), List(Value.Num(NumValue.Int32(Topped.Top))))
  testFunction(simple, "test-br-and-return4", List(Value.Num(NumValue.Int32(Topped.Top))), List(Value.Num(NumValue.Int32(Topped.Actual(42)))))
  testFunction(simple, "test-unreachable5", List(Value.Num(NumValue.Int32(Topped.Top))), List(Value.Num(NumValue.Int32(Topped.Top))))
  testFunction(simple, "test-global", List(Value.Num(NumValue.Int32(Topped.Top))), List(Value.Num(NumValue.Int32(Topped.Top))))
  testFunction(simple, "test-call-indirect-parametric", List(Value.Num(NumValue.Int32(Topped.Top))), List(Value.Num(NumValue.Int32(Topped.Actual(0)))))
  testFailingFunction(simple, "division", List(Value.Num(NumValue.Int32(Topped.Actual(1))), Value.Num(NumValue.Int32(Topped.Top))), IntegerDivisionByZero)
  testFunction(simple, "effects", List(Value.Num(NumValue.Int32(Topped.Top))), List(Value.Num(NumValue.Int32(Topped.Top))))

  testFunction(fact, "fac-rec", List(Value.Num(NumValue.Int64(Topped.Actual(1)))), List(Value.Num(NumValue.Int64(Topped.Top))), List(Value.Num(NumValue.Int64(Topped.Actual(1)))))
  (2 to 8).foreach { arg =>
    testFunction(fact, "fac-rec", List(Value.Num(NumValue.Int64(Topped.Actual(arg)))), List(Value.Num(NumValue.Int64(Topped.Top))))
  }
  testFunction(fact, "fac-rec", List(Value.Num(NumValue.Int64(Topped.Actual(25)))), List(Value.Num(NumValue.Int64(Topped.Top))))
  testFunction(fact, "fac-iter", List(Value.Num(NumValue.Int64(Topped.Actual(25)))), List(Value.Num(NumValue.Int64(Topped.Top))))
  testFunction(fact, "fac-rec-named", List(Value.Num(NumValue.Int64(Topped.Actual(25)))), List(Value.Num(NumValue.Int64(Topped.Top))))
  testFunction(fact, "fac-iter-named", List(Value.Num(NumValue.Int64(Topped.Actual(25)))), List(Value.Num(NumValue.Int64(Topped.Top))))
  testFunction(fact, "fac-opt", List(Value.Num(NumValue.Int64(Topped.Actual(25)))), List(Value.Num(NumValue.Int64(Topped.Top))))

  testFunction(fact, "fac-rec", List(Value.Num(NumValue.Int64(Topped.Top))), List(Value.Num(NumValue.Int64(Topped.Top))))
  testFunction(fact, "fac-iter", List(Value.Num(NumValue.Int64(Topped.Top))), List(Value.Num(NumValue.Int64(Topped.Top))))
  testFunction(fact, "fac-rec-named", List(Value.Num(NumValue.Int64(Topped.Top))), List(Value.Num(NumValue.Int64(Topped.Top))))
  testFunction(fact, "fac-iter-named", List(Value.Num(NumValue.Int64(Topped.Top))), List(Value.Num(NumValue.Int64(Topped.Top))))
  testFunction(fact, "fac-opt", List(Value.Num(NumValue.Int64(Topped.Top))), List(Value.Num(NumValue.Int64(Topped.Top))))

  testFunction(simpleTest, "main", List(Value.Num(NumValue.Int32(Topped.Actual(0)))), List(Value.Num(NumValue.Int32(Topped.Actual(42)))))
  testFunction(simpleTest, "main", List(Value.Num(NumValue.Int32(Topped.Actual(1)))), List(Value.Num(NumValue.Int32(Topped.Actual(42)))))
  testFunction(simpleTest, "main", List(Value.Num(NumValue.Int32(Topped.Top))), List(Value.Num(NumValue.Int32(Topped.Top))))


  def testFunctionConstantArgs(path: Path, funcName: String, args: List[ConcreteInterpreter.Value], expectedResult: List[ConcreteInterpreter.Value]) =
    testFunction(path, funcName, args.map(Abstractly.apply), expectedResult.map(Abstractly.apply))

  def testFunction(path: Path, funcName: String, args: List[Value], expected: List[Value], expectedFrames: List[Value] = null) =
    it must s"execute $funcName withs args $args with result $expected with stacked states" in {
      val res = runConstantAnalysisCFG(path, funcName, args, StackConfig.StackedStates())
      res match
        case AFallible.Unfailing(vals) => assertResult(expected)(vals)
        case AFallible.MaybeFailing(vals, _) => assertResult(expected)(vals)
        case AFallible.Failing(fails) => assert(false, s"Expected $expected but execution failed: $fails")
        case AFallible.Diverging(recur) => assert(false, s"Expected $expected but execution diverged: $recur")
    }
    val expected2 = Option(expectedFrames).getOrElse(expected)
//    it must s"execute $funcName withs args $args with result $expected2 with stacked frames" in {
//      val res = runConstantAnalysisCFG(path, funcName, args, StackConfig.StackedCfgNodes())
//      res match
//        case AFallible.Unfailing(vals) => assertResult(expected2)(vals)
//        case AFallible.MaybeFailing(vals, _) => assertResult(expected2)(vals)
//        case AFallible.Failing(fails) => assert(false, s"Expected $expected2 but execution failed: $fails")
//        case AFallible.Diverging(recur) => assert(false, s"Expected $expected2 but execution diverged: $recur")
//    }

  def testFailingFunction(path: Path, funcName: String, args: List[Value], failureKind: FailureKind): Unit =
    it must s"execute $funcName with args $args throwing exception $failureKind with stacked states" in {
      val res = runConstantAnalysisCFG(path, funcName, args, StackConfig.StackedStates())
      res match
        case AFallible.Unfailing(vals) => assert(false, s"Expected $failureKind but execution succeeded: $vals")
        case AFallible.MaybeFailing(_, fails) => assert(fails.set.exists(_._1 == failureKind))
        case AFallible.Failing(fails) => assert(fails.set.exists(_._1 == failureKind))
        case AFallible.Diverging(recur) => assert(false, s"Expected $failureKind but execution diverged: $recur")
    }
//    it must s"execute $funcName with args $args throwing exception $failureKind with stacked frames" in {
//      val res = runConstantAnalysisCFG(path, funcName, args, StackConfig.StackedCfgNodes())
//      res match
//        case AFallible.Unfailing(vals) => assert(false, s"Expected $failureKind but execution succeeded: $vals")
//        case AFallible.MaybeFailing(_, fails) => assert(fails.set.exists(_._1 == failureKind))
//        case AFallible.Failing(fails) => assert(fails.set.exists(_._1 == failureKind))
//        case AFallible.Diverging(recur) => assert(false, s"Expected $failureKind but execution diverged: $recur")
//        case AFallible.Diverging(recur) => assert(false, s"Expected $failureKind but execution diverged: $recur")
//    }


def runConstantAnalysisCFG(path: Path, funName: String, args: List[Value], stackConfig: StackConfig, mostGeneralClient: Boolean = false): AFallible[List[Value]] =
  val module = wasm.Parsing.fromText(path)

  val interp = new ConstantAnalysis.Instance(FrameData.empty, Iterable.empty,
    WasmConfig(FixpointConfig(fix.iter.Config.Innermost(stackConfig))))
  val cfg = ConstantAnalysis.controlFlow(CfgConfig.AllNodes(true), interp)
  val constants = ConstantAnalysis.constantInstructions(interp)

//  interp.addControlObserver(new PrintingControlObserver("  ", "\n")(println))
  interp.addControlObserver(new ControlEventChecker)
  val parser = interp.addControlObserver(new ControlEventParser)
  val graphBuilder = interp.addControlObserver(new ControlEventGraphBuilder)
//  interp.addControlObserver(new PrintingControlObserver()(println))

  val modInst = interp.initializeModule(module)
  val result = interp.failure.fallible(
    if (!mostGeneralClient)
      interp.invokeExported(modInst, funName, args)
    else {
      interp.runMostGeneralClient(modInst, ConstantAnalysis.typedTop)
      List()
    }
  )
//  println(cfg.toGraphViz)
//  println(interp.controlRecorder)


  val tree = parser.getFinalTree
  val treeSequence = tree.print
  val tree2 = ControlEventParser.parse(treeSequence)
  val treeSequence2 = tree2.print

  assert(treeSequence == treeSequence2)
  assert(tree == tree2)

  val graphFromTree = tree.toGraph
  val graphFromEvents = graphBuilder.get

  val edgesMissing = graphFromTree.edges.diff(graphFromEvents.edges)
  val edgesUnexpected = graphFromEvents.edges.diff(graphFromTree.edges)
  assertResult(Set(), "Edges missing in graph from events")(edgesMissing)
  assertResult(Set(), "Edges superfluous in graph from events")(edgesUnexpected)

//  println(cfg.toGraphViz)
  println(graphFromEvents.toGraphViz)


//  println(tree.toGraphViz)


  val deadInstructions = ControlFlow.deadInstruction(cfg, List(modInst))
  val deadLabels = ControlFlow.deadLabels(cfg)
  val constantInstructions = constants.get
  println(s"Found ${deadInstructions.size} dead instructions")
  println(s"Found ${deadLabels.size} dead labels")
  println(s"Found ${constantInstructions.size} constant instructions")
//  println(cfg.withBlocks(shortLabels = false).toGraphViz)

  LinearStateOperationCounter.addToListAndReset()
  println(s"${LinearStateOperationCounter.toString} in the last tests")
  println(s"#linear state operations in the last tests: ${LinearStateOperationCounter.getSummedOperationsPerTest}")
  Profiler.printLastMeasured()
//  println(recorder)
  result
