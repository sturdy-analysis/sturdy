package sturdy.language.bytecode

import org.opalj.br.analyses.Project
import org.opalj.br.reader.Java17Framework
import org.opalj.br.{ArrayType, ClassType, ReferenceType}
import org.opalj.bytecode
import org.opalj.io.process
import org.scalatest.Inspectors.forEvery
import org.scalatest.compatible.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableFor1
import org.scalatest.prop.Tables.Table
import sturdy.effect.failure.CFailureException
import sturdy.language.bytecode.ConcreteRefValues.nonNullArray
import sturdy.language.bytecode.abstractions.Site

import java.io.{DataInputStream, FileInputStream}
import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters.*

class ConcreteInterpreterTestSuite extends AnyFunSuite with Matchers:
  // path to the bytecode files
  val resourcePath = "./sturdy-jvm-bytecode/src/test/resources"
  val cfTable: TableFor1[Path] = Table("path") ++ Files.walk(Paths.get(resourcePath)).iterator().asScala.filter(Files.isRegularFile(_)).toSeq

  def testClassFile(path: Path): Assertion =
    // logic taken from the existing tests
    val project = Project(path.toFile, bytecode.RTJar)
    val classFiles = process(DataInputStream(FileInputStream(path.toString)))(Java17Framework.ClassFile)

    // we can safely call head and get here as there should be exactly one class file; it should contain a main method
    val main = classFiles.head.methods.find(_.name == "main").get
    val allowField = (ClassType("java/lang/System"), "allowSecurityManager")
    val allowAddr = (Site.StaticInitialization(allowField._1, allowField._2), 1)
    val concreteInterpreter = new ConcreteInterpreter.Instance(project, path.toString, Map(), Map(), Map(
      // removed due to the entry then still missing from staticAddrMap, would need to be set separately
      allowAddr -> ConcreteInterpreter.Value.Int32(1)
    ))
    concreteInterpreter.staticAddrMap += (allowField -> allowAddr)
    // args for invocation of main
    concreteInterpreter.stack.push(ConcreteInterpreter.Value.ReferenceValue(nonNullArray(1, Vector(), ArrayType(ReferenceType("String")), 0)))

    val v: ConcreteInterpreter.Value = try
      concreteInterpreter.invokeExternal(main, true)
    catch
      case CFailureException(concreteInterpreter.AbortEval.Exit(v), _) => v
    // alternative invocation
    // val v = concreteInterpreter.external(concreteInterpreter.invoke(main, Seq(ConcreteInterpreter.Value.ReferenceValue(nonNullArray(1,Vector(), ArrayType(ReferenceType("String")), 0)))))
    assert(v.asInt32(using concreteInterpreter.failure) === 95)

  forEvery(cfTable):
    path =>
      test(path.subpath(path.getNameCount - 4, path.getNameCount).toString.dropRight(6)):
        testClassFile(path)
