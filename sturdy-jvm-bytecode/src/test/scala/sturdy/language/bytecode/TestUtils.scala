package sturdy.language.bytecode

import org.opalj.br.{ArrayType, ClassType, IntegerType, Method, MethodDescriptor}
import org.scalatest.Assertions

import java.nio.file.{Files, NoSuchFileException, Path, Paths}
import scala.jdk.CollectionConverters.*
import scala.collection.immutable.ArraySeq
import scala.util.matching.Regex

// definitions for use in tests
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
    var path = try
      Paths.get(resourcePath)
    catch case e: NoSuchFileException =>
      Assertions.cancel(s"exception while reading resource path $resourcePath:\n${e.getMessage}")

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

final val delegatedMethodNames = Seq("runPositive", "runNegative", "loadPositive", "loadNegative", "instantiatePositive", "instantiateNegative")
final val supportedDelegatedMethods = Seq("runPositive", "runNegative")

// predicate for finding the run method with signature (String[] x PrintStream) -> int
def runSignaturePredicate(m: Method) =
  // get is safe since unapply always returns Some
  m.name == "run" && MethodDescriptor.unapply(m.descriptor).get == (Seq(ArrayType(ClassType.String), ClassType("java/io/PrintStream")), IntegerType)

enum TestedMethodType:
  case Main
  case Run

  def getExpectedValue: Int = this match
    case TestedMethodType.Main => 95
    case TestedMethodType.Run => 0
