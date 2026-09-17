import org.opalj.br.{ArrayType, ClassFile, ClassType, DoubleType, FieldType, FloatType, IntegerType, LongType, Method, ReferenceType}
import org.opalj.br.analyses.Project
import org.opalj.io.process
import org.scalatest.funsuite.AnyFunSuite
import sturdy.effect.SturdyException

import java.io.{DataInputStream, FileInputStream}
import java.nio.file.Paths
import sturdy.language.bytecode.ConcreteInterpreter
import sturdy.language.bytecode.abstractions.Site.External

import scala.language.postfixOps

class NewTest extends AnyFunSuite:
  test("new test"):
    val projectUri = this.getClass.getResource("/sturdy/language/bytecode/simple").toURI
    val methodsPlainUri = this.getClass.getResource("/sturdy/language/bytecode/simple/MethodsPlain.class").toURI
    val projectPath = Paths.get(projectUri).toString
    val methodsPlainName = Paths.get(methodsPlainUri).toString


    val pWithLibrary = Project(
      new java.io.File(projectPath), // path to the JAR files/directories containing the project
      org.opalj.bytecode.JavaBase
    )

    val methodsPlain: ClassFile =
      process(new DataInputStream(new FileInputStream(methodsPlainName))) { in =>
        org.opalj.br.reader.Java17Framework.ClassFile(in)
      }.head

    val testMths = methodsPlain.methodsWithBody.filter(mth => !mth.name.startsWith("<"))

    println("ConstantAnalysis Test Results")
    println("- - - - - - - - - - - - - - -")
    for (mth <- testMths) {
      try {
        val interp = new ConcreteInterpreter.Instance(pWithLibrary, Map())
        //val constInterp = new ConstantAnalysis.Instance(pWithLibrary, projectPath, Map())
        if (mth.parameterTypes.nonEmpty || mth.isNotStatic)
          val args = InitParams(mth, interp)
          interp.stack.pushN(args.toList)
        println("Executing Method: " ++ mth.name)
        println("Concrete Interpretation: " ++ interp.failure.fallible(interp.invokeExternal(mth)).toString)
        //println("Abstract Interpretation Constant Analysis: " ++ constInterp.invokeExternal(mth, true).toString)
        println("- - - - - - - - - - - - - - -")
      } catch {
        case e: Exception => println(e.toString)
        case e: SturdyException => println(e.getMessage)
      }

    }

def argForType(instance: ConcreteInterpreter.Instance, x: FieldType): ConcreteInterpreter.Value = {
  x match
    case IntegerType =>
      instance.bytecodeOps.i32ops.randomInteger()
    case LongType =>
      instance.bytecodeOps.i64ops.randomInteger()
    case FloatType =>
      instance.bytecodeOps.f32ops.randomFloat()
    case DoubleType =>
      instance.bytecodeOps.f64ops.randomFloat()
    case r: ClassType =>
      instance.createObject(r)(using External)
    case r: ArrayType =>
      instance.createArray(instance.bytecodeOps.i32ops.integerLit(5), r.componentType)(using External)
    case _ =>
      ???
}
def InitParams(mth: Method, concreteInterpreter: ConcreteInterpreter.Instance): Seq[ConcreteInterpreter.Value] =
  val args = mth.descriptor.parameterTypes
  val initArgs = args.map(argForType(concreteInterpreter, _))
  if (mth.isNotStatic) {
    return initArgs.prepended(argForType(concreteInterpreter, mth.classFile.thisType))
  }
  initArgs
