package sturdy.language.bytecode

import org.opalj.br.analyses.Project
import org.opalj.br.reader.Java17Framework
import org.opalj.br.{ArrayType, ClassType, ReferenceType}
import org.opalj.bytecode
import org.opalj.io.process
import org.scalatest.ParallelTestExecution
import org.scalatest.compatible.Assertion
import org.scalatest.concurrent.TimeLimits
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}
import sturdy.effect.failure.CFailureException
import sturdy.language.bytecode.ConcreteRefValues.{NullValue, nonNullArray}
import sturdy.language.bytecode.abstractions.Site

import java.io.{DataInputStream, FileInputStream}
import java.nio.file.{Files, Path, Paths}
import scala.collection.immutable.ArraySeq
import scala.jdk.CollectionConverters.*
import scala.util.matching.Regex

enum TestedMethodType:
  case Main
  case Run

object FileTable:
  // path to the bytecode files
  val resourcePath = "./sturdy-jvm-bytecode/src/test/resources/"
  // newline-separated regexes of file names to ignore
  // basic comment lines using "//" are supported
  val ignoreFileName = "ignored-files.txt"

  val ignoreRegexes: Seq[Regex] = Files.lines(Paths.get(resourcePath + ignoreFileName)).iterator().asScala.filterNot(_.startsWith("//")).map(_.r).toSeq
  val cfTable: ArraySeq[Path] = ArraySeq.from(Files.walk(Paths.get(resourcePath)).iterator().asScala.filter(f => Files.isRegularFile(f) && !f.equals(Paths.get(resourcePath + ignoreFileName)) && !ignoreRegexes.exists(_.matches(f.toString))))

class ConcreteInterpreterTestSuite extends AnyFunSuite with Matchers with TimeLimits with ParallelTestExecution:
  def testClassFile(path: Path): Assertion =
    // logic taken from the existing tests
    val project = Project(path.toFile, bytecode.RTJar)
    val classFiles = process(DataInputStream(FileInputStream(path.toString)))(Java17Framework.ClassFile)
    println(s"testing $path")

    // we can safely call head and get here as there should be exactly one class file
    val method = classFiles.head.methods.find(m => m.name == "main" || m.name == "run").getOrElse:
      fail(s"no suitable method found.\navailable methods: ${classFiles.head.methods.map(_.name)}")

    val mType = method.name match
      case "main" => TestedMethodType.Main
      case "run" => TestedMethodType.Run

    val allowField = (ClassType("java/lang/System"), "allowSecurityManager")
    val allowAddr = (Site.StaticInitialization(allowField._1, allowField._2), 1)
    val concreteInterpreter = new ConcreteInterpreter.Instance(project, path.toString, Map(
      // removed due to the entry then still missing from staticAddrMap, would need to be set separately
      allowAddr -> ConcreteInterpreter.Value.Int32(1)
    ))
    concreteInterpreter.staticAddrMap += (allowField -> allowAddr)
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
    // alternative invocation
    // val v = concreteInterpreter.external(concreteInterpreter.invoke(main, Seq(ConcreteInterpreter.Value.ReferenceValue(nonNullArray(1,Vector(), ArrayType(ReferenceType("String")), 0)))))
    assert(v.asInt32(using concreteInterpreter.failure) === getExpectedValue(mType))

  FileTable.cfTable.foreach: path =>
    test(path.subpath(path.getNameCount - 4, path.getNameCount).toString.dropRight(6)):
      // TODO: fix cancelAfter
      cancelAfter(Span(10, Seconds)):
        testClassFile(path)

def getExpectedValue(mType: TestedMethodType): Int = mType match
  case TestedMethodType.Main => 95
  case TestedMethodType.Run => 0
