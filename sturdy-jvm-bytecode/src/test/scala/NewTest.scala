import org.opalj.br.{ClassFile, DoubleType, FloatType, IntegerType, LongType, Method}
import org.opalj.br.analyses.Project
import org.opalj.io.process
import org.scalatest.funsuite.AnyFunSuite

import java.io.{DataInputStream, FileInputStream}
import java.nio.file.Paths
import sturdy.language.bytecode.ConcreteInterpreter

import scala.language.postfixOps

class NewTest extends AnyFunSuite:
  test("new test"):
    val projectUri = this.getClass.getResource("/sturdy/language/bytecode/simple").toURI
    val methodsPlainUri = this.getClass.getResource("/sturdy/language/bytecode/simple/MethodsPlain.class").toURI
    val projectPath = Paths.get(projectUri).toString
    val methodsPlainName = Paths.get(methodsPlainUri).toString


    val pWithLibrary = Project(
      new java.io.File(projectPath), // path to the JAR files/directories containing the project
      org.opalj.bytecode.RTJar
    )

    val methodsPlain: ClassFile =
      process(new DataInputStream(new FileInputStream(methodsPlainName))) { in =>
        org.opalj.br.reader.Java17Framework.ClassFile(in)
      }.head

    val testMths = methodsPlain.methodsWithBody.filter(mth => mth.name != "<clinit>")

    println("ConstantAnalysis Test Results")
    println("- - - - - - - - - - - - - - -")
    for (mth <- testMths) {
      try {
        val interp = new ConcreteInterpreter.Instance(pWithLibrary, projectPath, Map(), Map(), Map())
        //val constInterp = new ConstantAnalysis.Instance(pWithLibrary, projectPath, Map(), Map(), Map())
        //val intervalInterp = new IntervalAnalysis.Instance(pWithLibrary, projectPath, Map(), Map(), Map())
        if (mth.parameterTypes.nonEmpty)
          val args = InitParams(mth, interp)
          interp.stack.pushN(args.toList)
          println("Executing Method: " ++ mth.name)
          println("Concrete Interpretation: " ++ interp.invokeExternal(mth, true).toString)
        else
          println("Executing Method: " ++ mth.name)
          println("Concrete Interpretation: " ++ interp.invokeExternal(mth, true).toString)
        //println("Abstract Interpretation Constant Analysis: " ++ constInterp.invokeExternal(mth, true).toString)
        //println("Abstract Interpretation Interval Analysis: " ++ intervalInterp.invokeExternal(mth, true).toString)
        println("- - - - - - - - - - - - - - -")
      } catch {
        case e: Exception => println(e.toString)
      }

    }

def InitParams(mth: Method, concreteInterpreter: ConcreteInterpreter.Instance): Seq[ConcreteInterpreter.Value] =
  val args = mth.descriptor.parameterTypes
  val initArgs = args.map:
    case IntegerType =>
      concreteInterpreter.bytecodeOps.i32ops.randomInteger()
    case LongType =>
      concreteInterpreter.bytecodeOps.i64ops.randomInteger()
    case FloatType =>
      concreteInterpreter.bytecodeOps.f32ops.randomFloat()
    case DoubleType =>
      concreteInterpreter.bytecodeOps.f64ops.randomFloat()
    case _ =>
      ???
  initArgs
