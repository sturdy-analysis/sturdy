package sturdy.language.bytecode

import org.opalj.br.analyses.Project
import org.opalj.br.instructions.{InvocationInstruction, LoadString}
import org.opalj.br.{ArrayType, ClassType, IntegerType, Method, MethodDescriptor, ReferenceType}
import org.opalj.bytecode
import org.scalatest.Inspectors.forEvery
import org.scalatest.ParallelTestExecution
import org.scalatest.compatible.Assertion
import org.scalatest.concurrent.TimeLimits
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Minutes, Span}
import sturdy.effect.failure.CFailureException
import sturdy.language.bytecode.ConcreteRefValues.{NullValue, nonNullArray}
import sturdy.language.bytecode.abstractions.Site

import java.net.URL
import java.nio.file.{Files, Path, Paths}
import scala.collection.immutable.ArraySeq
import scala.jdk.CollectionConverters.*
import scala.util.matching.Regex

object TestCases:
  // path to the bytecode files
  val resourcePath = "./sturdy-jvm-bytecode/src/test/resources/"
  // newline-separated regexes of file names to ignore
  // basic comment lines using "//" are supported
  val ignoreFileName = "ignored-files.txt"
  // used by selective test, comments allowed as well
  val includeFileName = "included-files.txt"

  val ignoreRegexes: Seq[Regex] = readRegexesFromFile(ignoreFileName)
  val includeRegexes: Seq[Regex] = readRegexesFromFile(includeFileName)

  // all tests that are not ignored
  val fullTests: ArraySeq[Path] = allTestCases.filterNot: f =>
    ignoreRegexes.exists(_.matches(f.toString))

  // all explicitly included tests
  val includedTests: ArraySeq[Path] = allTestCases.filter: f =>
    includeRegexes.exists(_.matches(f.toString))

  // all test cases
  def allTestCases: ArraySeq[Path] =
    ArraySeq.from:
      // flatten nested files
      Files.list(testRootPath).flatMap:
        Files.list(_).flatMap:
          Files.list
      .iterator().asScala
    .sorted

  // path where the test hierarchy is located
  def testRootPath: Path =
    var path = Paths.get(resourcePath)
    // walk directory tree until we hit the test cases
    while Files.list(path).filter(Files.isDirectory(_)).count() == 1 do
      // safe as the loop head checks that at least one element exists
      val subDir = Files.list(path).filter(Files.isDirectory(_)).findFirst().get()
      path = subDir
    path

  // parsing for the files defining the tests to include/exclude
  private def readRegexesFromFile(filename: String) =
    Files.lines(Paths.get(resourcePath + filename)).iterator().asScala.filterNot(_.startsWith("//")).map(_.r).toSeq

// run all tests
class FullSuite extends ConcreteInterpreterTestSuite:
  runTestCases(TestCases.fullTests)

// run only the tests defined in the included test cases
class SelectiveSuite extends ConcreteInterpreterTestSuite:
  runTestCases(TestCases.includedTests)

val delegatedMethodNames = Seq("runPositive", "runNegative", "loadPositive", "loadNegative", "instantiatePositive", "instantiateNegative")

class ConcreteInterpreterTestSuite extends AnyFunSuite with Matchers with TimeLimits with ParallelTestExecution:
  // if the harness fails, the test is canceled
  def assertCase(testCase: Path): Assertion =
    val project = Project(testCase.toFile, bytecode.RTJar)
    println(s"testing $testCase")
    val caseName = testCase.getFileName.toString

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

    // determine which delegated methods are called, if any
    val calledDelegatedMethods = runBody.flatMap: p =>
      val instr = p.instruction
      Option.when(instr.isInvocationInstruction && delegatedMethodNames.contains(instr.asInvocationInstruction.name)):
        instr.asInvocationInstruction.name
    .toSeq

    if calledDelegatedMethods.isEmpty then
      // tests that don't call delegate methods are expected to be positive tests
      runPositive(project, testCase, caseName)(rootRun)
    else if !calledDelegatedMethods.contains("runPositive") then
      // TODO: better handling for other delegated methods
      if calledDelegatedMethods.contains("runNegative") then
        cancel(s"[$caseName] TODO: negative tests are currently ignored")
      else
        val targets = runBody.flatMap: p =>
          Option.when(p.instruction.isInvocationInstruction)(p.instruction.asInvocationInstruction)
        cancel(s"[$caseName] loading/instantiating tests are currently ignored. this test contains the following invocation instructions:\n" + targets.mkString("\n"))
    else
      // attempt to parse the classes called by each delegated method in the given method body
      import scala.collection.mutable;
      val map = mutable.Map.from:
        delegatedMethodNames.map:
          _ -> mutable.ListBuffer[String]()
      var nextClass: Option[String] = None
      // each invocation of a delegated method must be preceded by loading its class name as a string
      runBody.foreachInstruction:
        case LoadString(s) =>
          nextClass = Some(s)
        case i: InvocationInstruction =>
          map(i.asInvocationInstruction.name) += nextClass.getOrElse:
            cancel(s"[$caseName] error when constructing delegate call map: option empty")
          nextClass = None
        // do nothing if instruction is irrelevant
        case _ => ()
      if nextClass.isDefined then cancel(s"[$caseName] class not empty after loop")
      if map.size != delegatedMethodNames.size then cancel(s"[$caseName] unexpected map size: ${map.size}")

      val posCases = map("runPositive").filterNot: className =>
        TestCases.ignoreRegexes.exists:
          _.matches(className)
      .map: className =>
        val cfs = project.projectClassFilesWithSources.filter: (cf, _) =>
          cf.thisType.simpleName == className
        if cfs.size != 1 then cancel(s"[$caseName] unexpected amount of class file candidates: ${cfs.size}")
        // head access checked above
        val runs = cfs.head._1.methods.filter(runSignaturePredicate)
        if runs.size != 1 then cancel(s"[$caseName] unexpected amount of run candidates for $className: ${runs.size}")
        // checked above
        runs.head

      forEvery(posCases):
        runPositive(project, testCase, caseName)

  def runTestCases(testCases: Seq[Path]): Unit =
    testCases.foreach: path =>
      test(path.subpath(path.getNameCount - 3, path.getNameCount).toString):
        // TODO: fix cancelAfter
        cancelAfter(Span(1, Minutes)):
          assertCase(path)

  def runPositive(project: Project[URL], testCase: Path, caseName: String)(method: Method): Assertion =
    val mType = method.name match
      case "main" => TestedMethodType.Main
      case "run" => TestedMethodType.Run
      case s => cancel(s"[$caseName] invalid method name: $s")

    val concreteInterpreter = new ConcreteInterpreter.Instance(project, testCase.toString, Map())
    // args for invocation of main
    concreteInterpreter.stack.push(ConcreteInterpreter.Value.ReferenceValue(nonNullArray(1, Vector(), ArrayType(ReferenceType("String")), 0)))
    if mType == TestedMethodType.Run then
      // push System.out (null as a replacement)
      concreteInterpreter.stack.push(ConcreteInterpreter.Value.ReferenceValue(NullValue()))

    val v = try
      concreteInterpreter.invokeExternal(method, true)
    catch
      // all other exceptions fail the test
      case CFailureException(concreteInterpreter.AbortEval.Exit(v), _) => v
      case e: UnsupportedOperationException if e.getMessage.contains("unsupported instruction") => cancel(s"[$caseName] " + e.getMessage)

    assert(v.asInt32(using concreteInterpreter.failure) === mType.getExpectedValue)

// predicate for finding the run method with signature (String[] x PrintStream) -> int
def runSignaturePredicate(m: Method) =
  // get is safe since unapply always returns Some
  m.name == "run" && MethodDescriptor.unapply(m.descriptor).get == (Seq(ArrayType(ClassType("java/lang/String")), ClassType("java/io/PrintStream")), IntegerType)


enum TestedMethodType:
  case Main
  case Run

  def getExpectedValue: Int = this match
    case TestedMethodType.Main => 95
    case TestedMethodType.Run => 0
