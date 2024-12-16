import org.opalj.br.{ClassFile, ObjectType}
import org.opalj.br.analyses.Project
import org.opalj.io.process

import java.io.{DataInputStream, FileInputStream}
import java.nio.file.Paths
import sturdy.language.bytecode.{ConcreteInterpreter, test}
import sturdy.language.bytecode.analyses.{ConstantAnalysis, IntervalAnalysis}
import sturdy.language.bytecode.test.{pWithLibrary, projectPath}

object AbstractInterpreterTest extends App:
  val projectUri = this.getClass.getResource("/sturdy/language/bytecode/simple").toURI
  val simpleMathUri = this.getClass.getResource("/sturdy/language/bytecode/simple/SimpleMath.class").toURI
  val projectPath = Paths.get(projectUri).toString
  val simpleMathName = Paths.get(simpleMathUri).toString


  val pWithLibrary = Project(
    new java.io.File(projectPath), // path to the JAR files/directories containing the project
    org.opalj.bytecode.RTJar
  )

  val simpleMath: ClassFile =
    process(new DataInputStream(new FileInputStream(simpleMathName))) { in =>
      org.opalj.br.reader.Java17Framework.ClassFile(in)
    }.head
  val complicatedMath = pWithLibrary.classFile(ObjectType("sturdy/language/bytecode/simple/ComplicatedMath")).get

  val testMths = simpleMath.methodsWithBody.filter(mth => mth.actualArgumentsCount == 0).filter(mth => mth.name != "<clinit>").filter(mth => mth.name != "stringBuilderTest").concat(
    complicatedMath.methodsWithBody.filter(mth => mth.actualArgumentsCount == 0).filter(mth => mth.name != "<clinit>").filter(mth => mth.name != "stringBuilderTest")
  )
  println("Abstract Analysis Test Results")
  println("- - - - - - - - - - - - - - -")
  for(mth <- testMths){
    val interp = new ConcreteInterpreter.Instance(pWithLibrary, projectPath, Map(), Map(), Map())
    val constInterp = new ConstantAnalysis.Instance(pWithLibrary, projectPath, Map(), Map(), Map())
    val intervalInterp = new IntervalAnalysis.Instance(pWithLibrary, projectPath, Map(), Map(), Map())
    println("Executing Method: " ++ mth.name)
    println("Concrete Interpretation: " ++ interp.invokeExternal(mth, true).toString)
    println("Abstract Interpretation Constant Analysis: " ++ constInterp.invokeExternal(mth, true).toString)
    println("Abstract Interpretation Interval Analysis: " ++ intervalInterp.invokeExternal(mth, true).toString)
    println("- - - - - - - - - - - - - - -")
  }
//
//  val interp = new ConcreteInterpreter.Instance(pWithLibrary, projectPath, Map(), Map(), Map())
//  println(interp.invokeExternal(testMths.find(mth => mth.name == "throwTest0").get, true))
//  val classClassFile = pWithLibrary.allClassFiles.find(cls => cls.thisType == ObjectType("java/lang/Class"))
//  println(classClassFile.get.methods.size)
//  println(classClassFile.get.methodsWithBody.size)
//  val nativeSource = org.opalj.bytecode.RTJar
//  val source = javaLibClassFileWrapper(ObjectType("java/lang/Class"))
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

  def javaLibClassFileWrapper(obj: ObjectType): String =
    val source = "classes/" ++ obj.packageName ++ "/" ++ obj.simpleName ++ ".class"
    source