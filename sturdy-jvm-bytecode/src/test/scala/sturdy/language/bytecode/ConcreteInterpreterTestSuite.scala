package sturdy.language.bytecode

import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, ClassType, IntegerType, MethodDescriptor, ReferenceType}
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

import java.nio.file.{Files, Path, Paths}
import scala.collection.immutable.ArraySeq
import scala.jdk.CollectionConverters.*
import scala.util.matching.Regex

enum TestedMethodType:
  case Main
  case Run

  def getExpectedValue: Int = this match
    case TestedMethodType.Main => 95
    case TestedMethodType.Run => 0

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

  // all explicitly included tests that are not ignored
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

class ConcreteInterpreterTestSuite extends AnyFunSuite with Matchers with TimeLimits with ParallelTestExecution:
  def assertCase(testCase: Path): Assertion =
    val project = Project(testCase.toFile, bytecode.RTJar)
    println(s"testing $testCase")
    val name = testCase.getFileName.toString

    val rootCf = project.projectClassFilesWithSources.map(_._1).find:
      _.thisType.simpleName == name
    .getOrElse:
      cancel("invalid layout")
    val mains = rootCf.methods.filter:
      _.name == "main"
    if mains.size != 1 then cancel(s"unexpected amount of main methods: ${mains.size}")
    val runs = rootCf.methods.filter: m =>
      // this should find the correct run method, it has signature (String[] x PrintStream) -> int
      m.name == "run" && MethodDescriptor.unapply(m.descriptor).get == (Seq(ArrayType(ClassType("java/lang/String")), ClassType("java/io/PrintStream")), IntegerType)
    if runs.size != 1 then
      cancel(s"unexpected amount of run methods:\n" + runs.mkString("\n"))

    // checked above
    val run = runs.head
    // if run doesn't have a body, failing the test is fine
    val methods = if run.body.getOrElse:
      fail(s"method has no body:\n$run")
    .exists: p =>
      p.instruction.isInvocationInstruction && p.instruction.asInvocationInstruction.name == "runPositive"
    then
      // bypass reflections by collecting all run methods
      val cfs = project.projectClassFilesWithSources.filterNot: (cf, source) =>
        cf.thisType.simpleName == name || cf.thisType.simpleName.endsWith("n") || TestCases.ignoreRegexes.exists(_.matches(source.toString))
      cfs.flatMap(_._1.methods.filter(_.name == "run"))
    else if run.body.get.exists: p =>
      p.instruction.isInvocationInstruction && p.instruction.asInvocationInstruction.name == "loadNegative"
    then
      // loading uses reflections so we ignore those tests for now
      val targets = run.body.get.flatMap: p =>
        Option.when(p.instruction.isInvocationInstruction)(p.instruction.asInvocationInstruction)
      cancel("loading/instantiating tests are currently ignored. this test contains the following invocation instructions:\n" + targets.mkString("\n"))
    else
      Seq(run)

    // TODO: improve this structure
    forEvery(methods): method =>
      val mType = method.name match
        case "main" => TestedMethodType.Main
        case "run" => TestedMethodType.Run

      val concreteInterpreter = new ConcreteInterpreter.Instance(project, testCase.toString, Map())
      // args for invocation of main
      concreteInterpreter.stack.push(ConcreteInterpreter.Value.ReferenceValue(nonNullArray(1, Vector(), ArrayType(ReferenceType("String")), 0)))
      if mType == TestedMethodType.Run then
        // push System.out (null as a replacement)
        concreteInterpreter.stack.push(ConcreteInterpreter.Value.ReferenceValue(NullValue()))

      val v: ConcreteInterpreter.Value = try
        concreteInterpreter.invokeExternal(method, true)
      catch
        case CFailureException(concreteInterpreter.AbortEval.Exit(v), _) => v
        case e: UnsupportedOperationException if e.getMessage.contains("unsupported instruction") => cancel(e.getMessage)
      assert(v.asInt32(using concreteInterpreter.failure) === mType.getExpectedValue)

  def runTestCases(testCases: Seq[Path]): Unit =
    testCases.foreach: path =>
      test(path.subpath(path.getNameCount - 3, path.getNameCount).toString):
        // TODO: fix cancelAfter
        cancelAfter(Span(1, Minutes)):
          assertCase(path)
