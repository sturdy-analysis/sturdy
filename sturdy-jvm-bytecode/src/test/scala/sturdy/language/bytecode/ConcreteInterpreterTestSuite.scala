package sturdy.language.bytecode

import org.opalj.br.analyses.Project
import org.opalj.br.analyses.Project.JavaClassFileReader
import org.opalj.br.instructions.{InvocationInstruction, LoadClass, LoadString}
import org.opalj.br.{ArrayType, ClassType, Method, ReferenceType}
import org.opalj.bytecode
import org.scalatest.Inspectors.forEvery
import org.scalatest.{Assertions, ParallelTestExecution}
import org.scalatest.compatible.Assertion
import org.scalatest.concurrent.TimeLimits
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sturdy.effect.except.ConcreteSturdyException
import sturdy.effect.failure.CFailureException
import sturdy.language.bytecode.ConcreteRefValues.nonNullArray
import sturdy.language.bytecode.abstractions.Site
import sturdy.language.bytecode.generic.JvmExcept
import sturdy.language.bytecode.util.ClassTypeValues

import java.net.URL
import java.nio.file.Path
import scala.jdk.CollectionConverters.*

// run all tests
class FullConcreteSuite extends ConcreteInterpreterTestSuite:
  runTestCases(TestCases.fullTests)

// run only the tests defined in the included test cases
class SelectiveConcreteSuite extends ConcreteInterpreterTestSuite:
  runTestCases(TestCases.includedTests)

abstract class ConcreteInterpreterTestSuite extends AnyFunSuite with Matchers with TimeLimits with ParallelTestExecution:
  // if the harness fails, the test is canceled
  def assertCase(paths: Seq[Path]): Assertion =
    val testCase = paths.head
    val caseName = testCase.getFileName.toString
    val project = Project(
      JavaClassFileReader().AllClassFiles(paths.map(_.toFile)),
      JavaClassFileReader().ClassFiles(bytecode.JavaBase),
      true,
      Iterable.empty,
      (_, ex) => cancel(s"[$caseName] project setup failed: $ex")
    )
    println(s"testing $testCase")

    // structure validation
    val rootCf = project.projectClassFilesWithSources.map(_._1).find:
      _.thisType.simpleName == caseName
    .getOrElse:
      cancel(s"[$caseName] invalid layout: no root class file in case")
    val rootMains = rootCf.methods.filter:
      _.name == "main"
    if rootMains.size != 1 then cancel(s"[$caseName] unexpected amount of main methods:\n" + rootMains.mkString("\n"))
    val rootRuns = rootCf.methods.filter(runSignaturePredicate)
    if rootRuns.size != 1 then cancel(s"[$caseName] unexpected amount of run methods:\n" + rootRuns.mkString("\n"))

    // checked above
    val rootRun = rootRuns.head
    val runBody = rootRun.body.getOrElse:
      cancel(s"[$caseName] root run method with no body:\n$rootRun")
    // every native test checks for this flag and therefore needs to load it on the stack
    if runBody.exists: i =>
      try
        val instr = i.instruction
        instr.isLoadConstantInstruction && instr.asInstanceOf[LoadString].value == "-platform.nativeCodeSupported"
      catch case _: ClassCastException => false
    then
      cancel(s"[$caseName] TODO: native tests are currently ignored")

    // determine which delegated methods are called, if any
    val calledDelegatedMethods = runBody.flatMap: p =>
      val instr = p.instruction
      Option.when(instr.isInvocationInstruction && delegatedMethodNames.contains(instr.asInvocationInstruction.name)):
        instr.asInvocationInstruction.name
    .toSeq

    if calledDelegatedMethods.isEmpty then
      // tests that don't call delegate methods are expected to be positive tests
      return runPositive(project, testCase, caseName)(rootRun)
    // no delegated methods that we currently support
    if calledDelegatedMethods.intersect(supportedDelegatedMethods).isEmpty then
      // TODO: run these as well
      val targets = runBody.flatMap: p =>
        Option.when(p.instruction.isInvocationInstruction)(p.instruction.asInvocationInstruction)
      cancel(s"[$caseName] loading/instantiating tests are currently ignored. this test contains the following invocation instructions:\n" + targets.mkString("\n"))

    // attempt to parse the information for each delegated method call
    import scala.collection.mutable
    // map of class names invoked by each delegated method
    val invocationMap = mutable.Map.from:
      delegatedMethodNames.map:
        _ -> mutable.ListBuffer[String]()
    // map of loaded classes
    val classMap = mutable.Map.from:
      delegatedMethodNames.map:
        _ -> mutable.ListBuffer[ReferenceType]()
    var nextTestClass: Option[String] = None
    var nextExpectedClass: Option[ReferenceType] = None
    // each invocation of a delegated method must be preceded by loading its class name as a string
    // each invocation of a delegated negative method must be preceded by loading the expected exception class
    runBody.foreachInstruction:
      case LoadString(s) =>
        nextTestClass = Some(s)
      case LoadClass(r) =>
        if nextExpectedClass.isDefined then fail(s"[$caseName] unexpectedly full: $nextExpectedClass")
        nextExpectedClass = Some(r)
      case i: InvocationInstruction =>
        val name = i.asInvocationInstruction.name
        invocationMap(name) += nextTestClass.getOrElse:
          cancel(s"[$caseName] error when constructing delegate call map: option empty")
        nextTestClass = None
        // only negative tests load classes
        if name.contains("Negative") then
          classMap(name) += nextExpectedClass.getOrElse:
            fail(s"[$caseName] error when constructing expected class map: option empty")
          nextExpectedClass = None
      // do nothing if instruction is irrelevant
      case _ => ()
    // assertions after run body has been parsed
    if nextTestClass.isDefined then cancel(s"[$caseName] test class not empty after loop: $nextTestClass")
    if nextExpectedClass.isDefined then fail(s"[$caseName] expected class not empty after loop: $nextExpectedClass")
    if invocationMap.size != delegatedMethodNames.size then cancel(s"[$caseName] unexpected invocation map size: ${invocationMap.size}")
    if classMap.size != delegatedMethodNames.size then cancel(s"[$caseName] unexpected class map size: ${invocationMap.size}")

    val runNegInvocations = invocationMap("runNegative")
    val runNegExpectedClasses = classMap("runNegative")
    if runNegInvocations.size != runNegExpectedClasses.size then
      fail(s"${runNegInvocations.size} runNegative calls detected, ${runNegExpectedClasses.size} classes loaded")

    val posCases = invocationMap("runPositive").filterNot: className =>
      TestCases.ignoreRegexes.exists:
        _.matches(className)
    .map:
      getRunMethodFromName(project, caseName)

    // zip them first as the order of both should be the same, so filtering needs to happen to both
    val negCases = runNegInvocations.zip(runNegExpectedClasses).filterNot: tuple =>
      TestCases.ignoreRegexes.exists:
        _.matches(tuple._1)
    .map: tuple =>
      (getRunMethodFromName(project, caseName)(tuple._1), tuple._2)

    // forEvery is not good for stack traces, so only use it if needed
    if posCases.size == 1 then
      runPositive(project, testCase, caseName)(posCases.head)
    else
      forEvery(posCases):
        runPositive(project, testCase, caseName)
    if negCases.size == 1 then
      runNegative(project, testCase, caseName)(negCases.head._1, negCases.head._2)
    else
      forEvery(negCases):
        runNegative(project, testCase, caseName)

  def getRunMethodFromName(project: Project[URL], caseName: String)(className: String): Method =
    val cfs = project.projectClassFilesWithSources.filter: (cf, _) =>
      cf.thisType.simpleName == className
    if cfs.size != 1 then cancel(s"[$caseName] unexpected amount of class file candidates: ${cfs.size}")
    // head access checked above
    val runs = cfs.head._1.methods.filter(runSignaturePredicate)
    if runs.size != 1 then cancel(s"[$caseName] unexpected amount of run candidates for $className: ${runs.size}")
    // checked above
    runs.head

  def runTestCases(testCases: Seq[Seq[Path]]): Unit =
    testCases.foreach: paths =>
      val path = paths.head
      test(path.subpath(path.getNameCount - 3, path.getNameCount).toString):
        // TODO: fix cancelAfter
        // cancelAfter(Span(1, Minutes)):
        assertCase(paths)

  // returns all that is required to run the tested method
  def runSetup(project: Project[URL], testCase: Path, caseName: String)(method: Method): (ConcreteInterpreter.Instance, TestedMethodType) =
    val mType = method.name match
      case "main" => TestedMethodType.Main
      case "run" => TestedMethodType.Run
      case s => fail(s"[$caseName] invalid method name: $s")

    val concreteInterpreter = ConcreteInterpreter.Instance(project, testCase.toString, Map())
    // args for invocation of main
    concreteInterpreter.stack.push(ConcreteInterpreter.Value.ReferenceValue(nonNullArray((Site.External, 1), Seq(), ArrayType(ReferenceType("java/lang/String")), 0)))
    if mType == TestedMethodType.Run then
      // push System.out
      concreteInterpreter.stack.push(concreteInterpreter.createObject(ClassTypeValues.PrintStream, Site.External))
    (concreteInterpreter, mType)

  def runPositive(project: Project[URL], testCase: Path, caseName: String)(method: Method): Assertion =
    val (concreteInterpreter, mType) = runSetup(project, testCase, caseName)(method)
    val v = try
      concreteInterpreter.invokeExternal(method, true)
    catch
      // all other exceptions fail the test
      case CFailureException(concreteInterpreter.AbortEval.Exit(v), _) => v
      case CFailureException(concreteInterpreter.AbortEval.Native(m), _) => cancel(s"[$caseName] native method encountered: $m")
      case e: UnsupportedOperationException if e.getMessage.contains("unsupported instruction") => cancel(s"[$caseName] " + e.getMessage)

    assert(v.asInt32(using concreteInterpreter.failure) === mType.getExpectedValue)

  def runNegative(project: Project[URL], testCase: Path, caseName: String)(method: Method, expectedException: ReferenceType): Assertion =
    val (concreteInterpreter, _) = runSetup(project, testCase, caseName)(method)
    try
      concreteInterpreter.invokeExternal(method, true)
      fail(s"[$caseName] no exception thrown")
    catch
      // all other exceptions fail the test
      case CFailureException(concreteInterpreter.AbortEval.Native(m), _) => fail(s"[$caseName] native method encountered: $m")
      case e: UnsupportedOperationException if e.getMessage.contains("unsupported instruction") => fail(s"[$caseName] " + e.getMessage)
      case ConcreteSturdyException(e) => e match
        case JvmExcept.ThrowObject(exception: ConcreteInterpreter.Value) =>
          exception.asRef(using concreteInterpreter.failure) match
            case ConcreteRefValues.Object(_, cls, _) => assert(cls.thisType == expectedException)
            case refValue => fail(s"unexpected throw object: $refValue")
        case x =>
          fail(s"unexpected throw: $x")
