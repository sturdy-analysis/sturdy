package sturdy.language.bytecode

import org.opalj.br.analyses.Project
import org.opalj.br.analyses.Project.JavaClassFileReader
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
import java.nio.file.{Files, NoSuchFileException, Path, Paths}
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
  val fullTests: ArraySeq[ArraySeq[Path]] = allTestCases.filterNot: paths =>
    ignoreRegexes.exists(_.matches(paths.head.toString))

  // all explicitly included tests that are not ignored
  val includedTests: ArraySeq[ArraySeq[Path]] = fullTests.filter: paths =>
    includeRegexes.exists(_.matches(paths.head.toString))

  // all test cases
  def allTestCases: ArraySeq[ArraySeq[Path]] =
    // flatten nested files
    val files = Files.list(testRootPath).flatMap:
      Files.list(_).flatMap:
        Files.list
    // sorting here is really important for the grouping
    // the test case is the path without suffixes, so it will always be first of a group after sorting
    .iterator().asScala.toSeq.sorted
    // group testcases that belong together
    import scala.collection.mutable
    val map = mutable.Map[Path, mutable.ListBuffer[Path]]()
    files.foreach: path =>
      // check whether a test case this path belongs to already exists
      map.keys.find: key =>
        val p = path.getFileName.toString
        val k = key.getFileName.toString
        p.startsWith(k)
      match
        case Some(key) => map(key).append(path)
        // add a new test case
        case None => map += path -> mutable.ListBuffer(path)

    // the first element is the test case, all others are auxiliary
    // every case must contain at least one path, using head is therefore safe and will return the test case
    ArraySeq.from:
      map.values.map: list =>
        ArraySeq.from(list.sorted)
    .sortBy(_.head)

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
  private def readRegexesFromFile(filename: String): Seq[Regex] =
    try Files.lines(Paths.get(resourcePath + filename)).iterator().asScala.filterNot(_.startsWith("//")).map(_.r).toSeq
    catch case e: NoSuchFileException =>
      Console.err.println(s"exception while reading $filename, continuing without regexes:\n${e.getMessage}")
      Seq()

// run all tests
class FullSuite extends ConcreteInterpreterTestSuite:
  runTestCases(TestCases.fullTests)

// run only the tests defined in the included test cases
class SelectiveSuite extends ConcreteInterpreterTestSuite:
  runTestCases(TestCases.includedTests)

val delegatedMethodNames = Seq("runPositive", "runNegative", "loadPositive", "loadNegative", "instantiatePositive", "instantiateNegative")

class ConcreteInterpreterTestSuite extends AnyFunSuite with Matchers with TimeLimits with ParallelTestExecution:
  // if the harness fails, the test is canceled
  def assertCase(paths: Seq[Path]): Assertion =
    val testCase = paths.head
    val caseName = testCase.getFileName.toString
    val project = Project(
      JavaClassFileReader().AllClassFiles(paths.map(_.toFile)),
      JavaClassFileReader().ClassFiles(bytecode.RTJar),
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
      import scala.collection.mutable
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

      // forEvery is not good for stack traces, so only use it if needed
      if posCases.size != 1 then
        runPositive(project, testCase, caseName)(posCases.head)
      else
        forEvery(posCases):
          runPositive(project, testCase, caseName)

  def runTestCases(testCases: Seq[Seq[Path]]): Unit =
    testCases.foreach: paths =>
      val path = paths.head
      test(path.subpath(path.getNameCount - 3, path.getNameCount).toString):
        // TODO: fix cancelAfter
        cancelAfter(Span(1, Minutes)):
          assertCase(paths)

  def runPositive(project: Project[URL], testCase: Path, caseName: String)(method: Method): Assertion =
    val mType = method.name match
      case "main" => TestedMethodType.Main
      case "run" => TestedMethodType.Run
      case s => cancel(s"[$caseName] invalid method name: $s")

    val concreteInterpreter = new ConcreteInterpreter.Instance(project, testCase.toString, Map())
    // args for invocation of main
    concreteInterpreter.stack.push(ConcreteInterpreter.Value.ReferenceValue(nonNullArray((Site.StaticInitialization(ClassType.String, "ignored"), 1), Vector(), ArrayType(ReferenceType("java/lang/String")), ConcreteInterpreter.Value.Int32(0))))
    if mType == TestedMethodType.Run then
      // push System.out (null as a replacement)
      concreteInterpreter.stack.push(ConcreteInterpreter.Value.ReferenceValue(NullValue()))

    val v = try
      concreteInterpreter.invokeExternal(method, true)
    catch
      // all other exceptions fail the test
      case CFailureException(concreteInterpreter.AbortEval.Exit(v), _) => v
      case CFailureException(concreteInterpreter.AbortEval.Native(m), _) => cancel(s"[$caseName] native method encountered: $m")
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
