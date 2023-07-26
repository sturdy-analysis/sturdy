package sturdy.language.wasm.simple

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.AFallible.MaybeFailing
import sturdy.effect.failure.{AFallible, FailureKind}
import sturdy.fix
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.abstractions.{CfgConfig, ControlFlow}
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.analyses.IntervalAnalysis.{I32, Value}
import sturdy.language.wasm.analyses.{CallSites, FixpointConfig, IntervalAnalysis, WasmConfig}
import sturdy.language.wasm.generic.{FixIn, FixOut, FrameData}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.{Abstractly, Join, Powerset, Topped}
import sturdy.values.integer.{IntegerDivisionByZero, NumericInterval, given}
import swam.syntax.Module
import swam.text.*

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.reflect.{ClassTag, TypeTest}


class IntervalOperationsTest extends AnyFlatSpec, Matchers:
  behavior of "interval operations"

  val test = Paths.get(this.getClass.getResource("/sturdy/language/wasm/intervalsTest.wast").toURI)

  def b(l: Int, h: Int): NumericInterval[Int] = NumericInterval(l, h)
  def c(constant: Int): NumericInterval[Int] = NumericInterval.constant(constant)
  def maybeFailing(res: NumericInterval[Int]): AFallible[List[Value]] = AFallible.MaybeFailing(List(Value.Num(IntervalAnalysis.NumValue.Int32(res))), Powerset())
  def unfailing(res: NumericInterval[Int]): AFallible[List[Value]] = AFallible.Unfailing(List(Value.Num(IntervalAnalysis.NumValue.Int32(res))))
  def failing(): AFallible[List[Value]] = AFallible.Failing(Powerset())


  def isSpecialWasmOpCaseAndComputedCorrectly(operationName: String, args: List[NumericInterval[Int]], res: AFallible[List[Value]], expected: AFallible[List[Value]]): Boolean =
    import AFallible.*
    import NumericInterval.*
    if (operationName == "div_s") {
      // minValue / -1 creates an overflow exception
      (args.head, args.tail.head, res, expected) match
        case (first, second, MaybeFailing(resVals, _), Unfailing(expVals)) =>
          val firstHasMinValue = first.low == Integer.MIN_VALUE
          val secondHasMinusOne = second.containsNum(-1)
//          println(s"$firstHasMinValue $secondHasMinusOne ${resVals.head} ${expVals.head}")
          firstHasMinValue && secondHasMinusOne && resVals.head == expVals.head
        case (first, second, Failing(_), Unfailing(expVals)) =>
          val firstIsMinValue = first.low == Integer.MIN_VALUE && first.low == first.high
          val secondIsMinusOne = first.low == -1 && first.low == first.high
          firstIsMinValue && secondIsMinusOne
        case _ => false
    } else {
      false
    }

  def testOperation(operationName: String, args: List[NumericInterval[Int]], expected: AFallible[List[Value]], createExtraTest: Boolean, forcePrecise: Boolean): Unit =
    def assertCorrect(expected: Value, result: Value): Unit = {
      val (e, r) = (expected, result) match {
        case (Value.Num(IntervalAnalysis.NumValue.Int32(exp)), Value.Num(IntervalAnalysis.NumValue.Int32(res))) => (exp, res)
      }
      if (forcePrecise)
        assertResult(e, s"for $operationName of $args")(r)
      else
        assert(NumericIntervalOrdering[Int].lteq(e, r), s"$operationName with args $args = $r. This is not sound, since the real result is $e")
        if (e != r)
          println(s"$operationName with args $args = $r. This is imprecise, since the real result is $e")
    }

    def intervalAnalysisI32(a: I32): Value.Num = {
      Value.Num(IntervalAnalysis.NumValue.Int32.apply(a))
    }

    import AFallible.*
    def doTheTest() = {
      //val res = runIntervalAnalysis(test, operationName, args.map(Value.Num(IntervalAnalysis.NumValue.Int32).apply), StackConfig.StackedStates())
      val res = runIntervalAnalysis(test, operationName, args.map(intervalAnalysisI32), StackConfig.StackedStates())
      (res, expected) match
        case (Unfailing(vals), Unfailing(expectedVals)) => assertCorrect(expectedVals.head, vals.head)
        case (MaybeFailing(vals, _), MaybeFailing(expectedVals, _)) => assertCorrect(expectedVals.head, vals.head)
        case (Failing(_), Failing(_)) => // ok
        case _ if isSpecialWasmOpCaseAndComputedCorrectly(operationName, args, res, expected) => // ok
        case _ => sys.error(s"$res != $expected")
    }
    if (createExtraTest) {
      it must s"execute $operationName with args $args with result $expected with stacked states" in {
        doTheTest()
      }
    }
    doTheTest()


  def bruteForceTestBinary(intervalOperation: String, intOperation: (Int, Int) => Int, lowerBound1: Int, upperBound1: Int, lowerBound2: Int, upperBound2: Int, forcePrecise: Boolean): Unit = {
    def findResultByBruteForce(x1: Int, x2: Int, y1: Int, y2: Int): AFallible[List[Value]] = {
      var lower: Int = Integer.MAX_VALUE
      var upper: Int = Integer.MIN_VALUE

      var hasFailed = false
      var hasSucceeded = false
      (x1 to x2).foreach(x =>
        (y1 to y2).foreach(y =>
          try {
            val result = intOperation(x, y)
            hasSucceeded = true
            lower = Integer.min(result, lower)
            upper = Integer.max(result, upper)
          } catch {
            case _: ArithmeticException =>
              hasFailed = true
          }

        )
      )

      val result = NumericInterval(lower, upper)
      if (!hasFailed)
        unfailing(result)
      else if (!hasSucceeded)
        failing()
      else
        maybeFailing(result)
    }

    it must s"execute $intervalOperation for all intervals in [$lowerBound1, $upperBound1] x [$lowerBound2, $upperBound2]" in {

      (lowerBound1 to upperBound1).foreach(x1 =>
        (x1 to upperBound1).foreach(x2 =>
          val x = b(x1, x2)
          (lowerBound2 to upperBound2).foreach(y1 =>
            (y1 to upperBound2).foreach(y2 =>
              val y = b(y1, y2)
              val expected = findResultByBruteForce(x1, x2, y1, y2)
              testOperation(intervalOperation, List(x, y), expected, false, forcePrecise)
            )
          )
        )
      )
    }
  }


  def bruteForceTestUnary(intervalOperation: String, intOperation: Int => Int, lowerBound1: Int, upperBound1: Int, forcePrecise: Boolean): Unit = {
    def findResultByBruteForce(x1: Int, x2: Int): AFallible[List[Value]] = {
      var lower: Int = Integer.MAX_VALUE
      var upper: Int = Integer.MIN_VALUE

      var hasFailed = false
      var hasSucceeded = false
      (x1 to x2).foreach(x =>
          try {
            val result = intOperation(x)
            hasSucceeded = true
            lower = Integer.min(result, lower)
            upper = Integer.max(result, upper)
          } catch {
            case _: ArithmeticException =>
              hasFailed = true
          }
      )

      val result = NumericInterval(lower, upper)
      if (!hasFailed)
        unfailing(result)
      else if (!hasSucceeded)
        failing()
      else
        maybeFailing(result)
    }

    it must s"execute $intervalOperation for all intervals in [$lowerBound1, $upperBound1]" in {

      (lowerBound1 to upperBound1).foreach(x1 =>
        (x1 to upperBound1).foreach(x2 =>
          val x = b(x1, x2)
          val expected = findResultByBruteForce(x1, x2)
            //              println(s"$x | $y = $expected")
            testOperation(intervalOperation, List(x), expected, false, forcePrecise)
        )
      )
    }
  }

  {
//    testOperation("shl", List(b(-2147483642,-2147483640), b(-3, -2)), unfailing(b(-2147483648,0)), true, true)
    bruteForceTestIntervalsOnBinaryOps(List(
      TestConfigBinary("rem_s", _%_, false),
      TestConfigBinary("rem_u", Integer.remainderUnsigned, false),
      TestConfigBinary("shl", _<<_, true),
      TestConfigBinary("shr_u", _>>>_, true),
      TestConfigBinary("shr_s", _>>_, true),
      TestConfigBinary("div_u", Integer.divideUnsigned, true),
      TestConfigBinary("div_s", _/_, true),
      TestConfigBinary("xor",   _^_, true),
      TestConfigBinary("add",   _+_, true),
      TestConfigBinary("sub",   _-_, true),
      TestConfigBinary("or",    _|_, true),
      TestConfigBinary("and",   _&_, true)
      ), 10
    )

    bruteForceTestIntervalsOnUnaryOps(List(
//      TestConfigUnary("popcnt", Integer.bitCount, true),
//      TestConfigUnary("ctz", Integer.numberOfTrailingZeros, true),
//      TestConfigUnary("clz", Integer.numberOfLeadingZeros, true)
    ), 50
    )
  }

  case class TestConfigBinary(nameOfOp: String, opOnNums: (Int, Int) => Int, forcePrecise: Boolean)
  case class TestConfigUnary(nameOfOp: String, opOnNums: Int => Int, forcePrecise: Boolean)

  def getTestIntervals(size: Int): List[NumericInterval[Int]] = List(
    b(Integer.MIN_VALUE, Integer.MIN_VALUE + size - 1),
    b(-size / 2, size / 2),
    b(Integer.MAX_VALUE - size + 1, Integer.MAX_VALUE),
  )

  def bruteForceTestIntervalsOnBinaryOps(testConfigs: List[TestConfigBinary], sizeOfIntervals: Int): Unit =
    val testIntervals = getTestIntervals(sizeOfIntervals)
    testConfigs.foreach( testConfig =>
      testIntervals.foreach( i1 =>
        testIntervals.foreach( i2 =>
          bruteForceTestBinary(testConfig.nameOfOp, testConfig.opOnNums, i1.low, i1.high, i2.low, i2.high, testConfig.forcePrecise)
    )))

  def bruteForceTestIntervalsOnUnaryOps(testConfigs: List[TestConfigUnary], sizeOfIntervals: Int): Unit =
    val testIntervals = getTestIntervals(sizeOfIntervals)
    testConfigs.foreach( testConfig =>
        testIntervals.foreach( i1 =>
            bruteForceTestUnary(testConfig.nameOfOp, testConfig.opOnNums, i1.low, i1.high, testConfig.forcePrecise)
    ))


  def runIntervalAnalysis(path: Path, funName: String, args: List[Value], stackConfig: StackConfig): AFallible[List[Value]] =
    Fixpoint.DEBUG = false
    val module = wasm.Parsing.fromText(path)
    val interp = new IntervalAnalysis.Instance(FrameData.empty, Iterable.empty,
      WasmConfig(FixpointConfig(fix.iter.Config.Innermost(stackConfig))))
    val modInst = interp.initializeModule(module)
    val result = interp.failure.fallible(
      interp.invokeExported(modInst, funName, args)
    )
    result
