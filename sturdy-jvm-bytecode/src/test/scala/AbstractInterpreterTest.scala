import org.opalj.br.{ClassFile, ClassType}
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.Project.JavaClassFileReader
import org.opalj.bytecode
import org.opalj.io.process
import org.scalatest.funsuite.AnyFunSuite

import java.io.{DataInputStream, FileInputStream}
import java.nio.file.Paths
import sturdy.language.bytecode.ConcreteInterpreter
import sturdy.language.bytecode.analyses.ConstantAnalysis

class AbstractInterpreterTest extends AnyFunSuite:
  test("abstract interpreter test"):
    val projectUri = this.getClass.getResource("/sturdy/language/bytecode/simple").toURI
    val simpleMathUri = this.getClass.getResource("/sturdy/language/bytecode/simple/SimpleMath.class").toURI
    val projectPath = Paths.get(projectUri).toString
    val simpleMathName = Paths.get(simpleMathUri).toString


    val pWithLibrary = Project(
      JavaClassFileReader().ClassFiles(java.io.File(projectPath)), // path to the JAR files/directories containing the project
      JavaClassFileReader().ClassFiles(bytecode.JavaBase),
      true,
      Iterable.empty,
      (_, ex) => cancel(s"project setup failed: $ex")
    )

    val simpleMath: ClassFile =
      process(new DataInputStream(new FileInputStream(simpleMathName))) { in =>
        org.opalj.br.reader.Java17Framework.ClassFile(in)
      }.head
    val complicatedMath = pWithLibrary.classFile(ClassType("sturdy/language/bytecode/simple/ComplicatedMath")).get

    val testMths = simpleMath.methodsWithBody.filter(mth => mth.actualArgumentsCount == 0).filter(mth => mth.name != "<clinit>").filter(mth => mth.name != "stringBuilderTest").concat(
      complicatedMath.methodsWithBody.filter(mth => mth.actualArgumentsCount == 0).filter(mth => mth.name != "<clinit>").filter(mth => mth.name != "stringBuilderTest")
    )
    println("Abstract Analysis Test Results")
    println("- - - - - - - - - - - - - - -")
    for(mth <- testMths){
      val interp = new ConcreteInterpreter.Instance(pWithLibrary, Map())
      val constInterp = new ConstantAnalysis.Instance(pWithLibrary, Map())
      println("Executing Method: " ++ mth.name)
      println("Concrete Interpretation: " ++ interp.invokeExternal(mth, true).toString)
      println("Abstract Interpretation Constant Analysis: " ++ constInterp.invokeExternal(mth, true).toString)
      println("- - - - - - - - - - - - - - -")
    }
//
//  val interp = new ConcreteInterpreter.Instance(pWithLibrary, projectPath, Map(), Map(), Map())
//  println(interp.invokeExternal(testMths.find(mth => mth.name == "throwTest0").get, true))
//  val classClassFile = pWithLibrary.allClassFiles.find(cls => cls.thisType == ClassType.Class)
//  println(classClassFile.get.methods.size)
//  println(classClassFile.get.methodsWithBody.size)
//  val nativeSource = org.opalj.bytecode.JavaBase
//  val source = javaLibClassFileWrapper(ClassType.Class)
//  val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
//  println(cfs.methods.size)
//  println(cfs.methodsWithBody.size)
//  println(cfs.methods.filter(elem => !cfs.methodsWithBody.contains(elem)).map(elem => elem.name))
//  println(cfs.methods.filter(elem => cfs.methodsWithBody.contains(elem)).map(elem => elem.name))
//  println(cfs.methods.filter(elem => elem.isNative).map(elem => elem.name))
//  println(cfs.methodsWithBody.find(elem => elem.name == "<clinit>").get.body.get.foreach(println))
//  println(cfs.methodsWithBody.find(elem => elem.name == "<init>").get.body.get.foreach(println))
//  println(cfs.methodsWithBody.find(elem => elem.name == "<init>").get.parameterTypes)
//  println(cfs.methods.filter(elem => elem.name == "registerNatives"))

  def javaLibClassFileWrapper(obj: ClassType): String =
    val source = "classes/" ++ obj.packageName ++ "/" ++ obj.simpleName ++ ".class"
    source
