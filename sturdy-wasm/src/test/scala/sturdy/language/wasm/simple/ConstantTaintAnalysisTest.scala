package sturdy.language.wasm.simple

import cats.effect.{IO, Blocker}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.{AFallible, FailureKind}
import sturdy.language.wasm
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.abstractions.CfgConfig
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.language.wasm.analyses.ConstantTaintAnalysis
import sturdy.language.wasm.analyses.ConstantTaintAnalysis.{untaint, Value}
import sturdy.language.wasm.analyses.CallSites
import sturdy.language.wasm.analyses.WasmConfig
import sturdy.language.wasm.generic.{WasmFailure, FrameData}
import sturdy.values.Abstractly
import sturdy.values.Topped
import sturdy.values.taint.Taint
import sturdy.values.taint.Taint.{Untainted, Tainted, TopTaint}
import sturdy.values.integer.IntegerDivisionByZero
import sturdy.values.taint.TaintProduct
import swam.syntax.Module
import swam.text.*

import java.nio.file.{Path, Paths, Files}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.reflect.{TypeTest, ClassTag}


class ConstantTaintAnalysisTest extends AnyFlatSpec, Matchers:
  behavior of "Wasm constant taint analysis"

  val uriSimple = this.getClass.getResource("/sturdy/language/wasm/simple.wast").toURI;
  val uriFact = this.getClass.getResource("/sturdy/language/wasm/fact.wast").toURI;
  val uriTaintTest = this.getClass.getResource("/sturdy/language/wasm/taint_test.wast").toURI;
  val simple = Paths.get(uriSimple)
  val fact = Paths.get(uriFact)
  val taintTest = Paths.get(uriTaintTest)

  type CVal = ConcreteInterpreter.Value
  type ConstVal = ConstantAnalysis.Value
  
  {
    import sturdy.language.wasm.ConcreteInterpreter.Value
    /*
    testFunctionConstantArgs(simple, "noop", List.empty, List((Value.Int32(0), Untainted)))
    testFunctionConstantArgs(simple, "const", List(Value.Int32(5)), List((Value.Int32(5), Tainted)))
    testFunctionConstantArgs(simple, "first", List(Value.Int32(1), Value.Int32(2)), List((Value.Int32(1), Tainted)))
    testFunctionConstantArgs(simple, "second", List(Value.Int32(1), Value.Int32(2)), List((Value.Int32(2), Tainted)))
    testFunctionConstantArgs(simple, "test-mem", List(Value.Int32(42)), List((Value.Int32(43), TopTaint)))
    testFunctionConstantArgs(simple, "test-size", List.empty, List((Value.Int32(1), Untainted)))
    testFunctionConstantArgs(simple, "test-memgrow", List.empty, List((Value.Int32(1), Untainted), (Value.Int32(2), Untainted)))
    testFunctionConstantArgs(simple, "test-call-indirect", List.empty, List((Value.Int32(0), Untainted)))
    testFunctionConstantArgs(simple, "call-first", List.empty, List((Value.Int32(0), Untainted)))

    testFunctionConstantArgs(taintTest, "taint_memory_write", List(Value.Int32(0), Value.Int32(1)), List((Value.Int32(1), Tainted)))
    testFunctionConstantArgsConst(taintTest, "taint_loop", List(Value.Int32(0)), List((ConstantAnalysis.Value.Int32(Topped.Top), TopTaint)))
    testFunctionConstantArgs(taintTest, "write_mem_mixed", List(Value.Int32(0)), List((Value.Int32(0), TopTaint)))
    testFunctionConstantArgs(taintTest, "write_mem_mixed", List(Value.Int32(10)), List((Value.Int32(43), Untainted)))
    */
  }

  def testFunctionConstantArgsConst(path: Path, funcName: String, args: List[CVal], expectedResult: List[(ConstVal, Taint)]) =
    testFunction(path, funcName, args.map(Abstractly.apply),
      expectedResult.map((x: ConstVal, taint: Taint) => ConstantTaintAnalysis.liftConstantValue(x,taint)))

  def testFunctionConstantArgs(path: Path, funcName: String, args: List[CVal], expectedResult: List[(CVal, Taint)]) =
    testFunction(path, funcName, args.map(Abstractly.apply),
      expectedResult.map((x: CVal, taint: Taint) => ConstantTaintAnalysis.liftConcreteValue(x,taint)))

  def testFunction(path: Path, funcName: String, args: List[ConstantAnalysis.Value], expected: List[Value]) =
    it must s"execute $funcName withs args $args with result $expected" in {
      val res = runConstantTaintAnalysis(path, funcName, args.map(ConstantTaintAnalysis.liftConstantValue(_,Taint.Tainted)))
      res match
        case AFallible.Unfailing(vals) => assertResult(expected)(vals)
        case AFallible.MaybeFailing(vals, _) => assertResult(expected)(vals)
        case AFallible.Failing(fails) => assert(false, s"Expected $expected but execution failed: $fails")
    }

  def testFailingFunction(path: Path, funcName: String, args: List[ConstantAnalysis.Value], failureKind: FailureKind) =
    it must s"execute $funcName with args $args throwing exception $failureKind" in {
      val res = runConstantTaintAnalysis(path, funcName, args.map(ConstantTaintAnalysis.liftConstantValue(_,Taint.Tainted)))
      res match
        case AFallible.Unfailing(vals) => assert(false, s"Expected $failureKind but execution succeeded: $vals")
        case AFallible.MaybeFailing(_, fails) => assert(fails.set.exists(_._1 == failureKind))
        case AFallible.Failing(fails) => assert(fails.set.exists(_._1 == failureKind))
    }


def runConstantTaintAnalysis(path: Path, funName: String, args: List[Value]): AFallible[List[Value]] =
  val module = wasm.Parsing.fromText(path)

  val interp = new ConstantTaintAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig(ctx = CallSites(3)))
//  val cfg = ConstantTaintAnalysis.controlFlow(CfgConfig.AllNodes(false), interp)
  //    val constants = ConstantTaintAnalysis.constantInstructions(interp)
  val memory = ConstantTaintAnalysis.taintedMemoryAccessLogger(interp)
  val modInst = interp.initializeModule(module)
  val result = interp.failure.fallible(
    interp.invokeExported(modInst, funName, args)
  )
  println(memory.instructions)
  println(memory.instructionInfo)
  result