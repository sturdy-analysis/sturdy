import org.opalj.br.analyses.Project
import org.opalj.br.instructions.*
import org.opalj.br.*
import org.opalj.io.process
import sturdy.language.bytecode.ConcreteInterpreter
import sturdy.language.bytecode.generic.{FixIn, FixOut, ValType}
import sturdy.values.integer.IntegerOps
import sturdy.fix
import sturdy.fix.ConcreteFixpoint

import java.io.{DataInputStream, File, FileInputStream}
import java.net.URL
import scala.Float.NaN
import scala.collection.mutable

object test extends App{
  def convertTypes(opalTypes: FieldType): ValType = opalTypes match
    case IntegerType => ValType.I32
    case FloatType => ValType.F32
    case LongType => ValType.I64
    case DoubleType => ValType.F64
    case _ => ValType.I32

  //edit this
  val projectPath = "/Users/stmarx/IdeaProjects/jvm-bytecode-in-sturdy-scala/jvm-bytecode-testSources"
  val simpleMathName = "/Users/stmarx/IdeaProjects/jvm-bytecode-in-sturdy-scala/jvm-bytecode-testSources/SimpleMath.class"

  val pWithLibrary = Project(
    new java.io.File(projectPath), // path to the JAR files/directories containing the project
    org.opalj.bytecode.RTJar
  )

  val cfs1: List[ClassFile] =
    process(new DataInputStream(new FileInputStream(simpleMathName))) { in =>
      org.opalj.br.reader.Java17Framework.ClassFile(in)
    }
  /*val locals = cfs.head.findMethod("return10").head.body.get.localVariableTable.get
  println(locals.map(_.fieldType))
  val localtypes = locals.map(_.fieldType)
  val convertedTypes = localtypes.map(convertTypes(_))
  println(convertedTypes)*/

  //val cfs = pWithNatives.classFile(ObjectType("SimpleMath")).get
  val cfs = cfs1.head
  val cfs2 = pWithLibrary.classFile(ObjectType("ComplicatedMath")).get
  println(pWithLibrary.classFile(ObjectType("ComplicatedMath")).get.staticInitializer)

  val sourceFile = projectPath

  val interp = new ConcreteInterpreter.Instance(pWithLibrary, projectPath, Map(), Map(), Map())

  val fixpoint = new ConcreteFixpoint[FixIn, FixOut]
  
  ///*
  interp.evalExternal(BIPUSH(5))
  interp.evalExternal(BIPUSH(10))
  interp.evalExternal(IADD)
  interp.evalExternal(BIPUSH(3))
  interp.evalExternal(ISUB)
  interp.evalExternal(INEG)
  val result = interp.stack.pop()
  println(result)

  interp.evalExternal(LoadFloat(3.2f))
  interp.evalExternal(LoadFloat(4.2f))
  interp.evalExternal(FADD)
  val result2 = interp.stack.pop()
  println(result2)

  interp.evalExternal(BIPUSH(12))
  interp.evalExternal(BIPUSH(5))
  interp.evalExternal(IREM)
  val result3 = interp.stack.pop()
  println(result3)

  interp.evalExternal(LoadFloat(12.7f))
  interp.evalExternal(LoadFloat(5.0f))
  interp.evalExternal(FREM)
  val result4 = interp.stack.pop()
  println(result4)

  interp.evalExternal(LoadFloat(12.0f))
  interp.evalExternal(LoadFloat(NaN))
  interp.evalExternal(FCMPL)
  val result5 = interp.stack.pop()
  println(result5)


  println("--- SubTest ---")
  val testMethod = cfs.findMethod("sub2").head
  interp.evalExternal(BIPUSH(5))
  interp.evalExternal(BIPUSH(7))
  println(interp.invokeExternal(testMethod, true))


  println("--- BranchTest ---")
  val testBranch = cfs.findMethod("branching").head
  interp.evalExternal(ICONST_0)
  println(interp.invokeExternal(testBranch, true))

  interp.evalExternal(ICONST_1)
  println(interp.invokeExternal(testBranch, true))

  println("--- ReturnTest ---")
  val testReturn = cfs.findMethod("returnTest").head
  interp.evalExternal(ICONST_1)
  println(interp.invokeExternal(testReturn, true))

  println("--- ObjectTest ---")
  val testObj = cfs.findMethod("objectTest").head
  println(interp.invokeExternal(testObj, true))

  println("--- InheritanceTest ---")
  val testInherit = cfs.findMethod("inheritanceTest").head
  println(interp.invokeExternal(testInherit, true))

  val testInherit2 = cfs.findMethod("inheritanceTest2").head
  println(interp.invokeExternal(testInherit2, true))

  val testInherit3 = cfs.findMethod("inheritanceTest3").head
  println(interp.invokeExternal(testInherit3, true))

  println("--- objectCompTest ---")
  val testObjCompTrue = cfs.findMethod("objectCompTestTrue").head
  println(interp.invokeExternal(testObjCompTrue, true))

  val testObjCompFalse = cfs.findMethod("objectCompTestFalse").head
  println(interp.invokeExternal(testObjCompFalse, true))

  println("--- switchTest ---")
  val switchTest = cfs.findMethod("switchTest").head
  interp.evalExternal(BIPUSH(2))
  println(interp.invokeExternal(switchTest, true))

  interp.evalExternal(SIPUSH(200))
  println(interp.invokeExternal(switchTest, true))

  println("--- nonCompactSwitchTest ---")
  val nonCompactSwitchTest = cfs.findMethod("nonCompactSwitchTest").head
  interp.evalExternal(SIPUSH(200))
  println(interp.invokeExternal(nonCompactSwitchTest, true))

  interp.evalExternal(SIPUSH(201))
  println(interp.invokeExternal(nonCompactSwitchTest, true))

  println("--- staticVarTest ---")
  val staticVarTest = cfs.findMethod("staticVarTest").head
  println(interp.invokeExternal(staticVarTest, true))
  println(interp.invokeExternal(staticVarTest, true))

  println("--- arrayTest ---")
  val arrayTest = cfs.findMethod("arrayTest").head
  println(interp.invokeExternal(arrayTest, true))

  val arrayTest2 = cfs.findMethod("arrayTest2").head
  println(interp.invokeExternal(arrayTest2, true))

  val arrayTest3 = cfs.findMethod("arrayTest3").head
  println(interp.invokeExternal(arrayTest3, true))

  val arrayCompTest = cfs.findMethod("arrayCompTest").head
  println(interp.invokeExternal(arrayCompTest, true))

  val arrayLengthTest = cfs.findMethod("arrayLengthTest").head
  println(interp.invokeExternal(arrayLengthTest, true))

  println("--- objectArrayTest ---")
  val objectArrayTest = cfs.findMethod("objectArrayTest").head
  println(interp.invokeExternal(objectArrayTest, true))

  val objectArrayTypeTest = cfs.findMethod("objectArrayTypeTest").head
  println(interp.invokeExternal(objectArrayTypeTest, true))

  println("--- multiDArrayTest ---")
  val multiDArrayTest = cfs.findMethod("multiDArrayTest").head
  println(interp.invokeExternal(multiDArrayTest, true))

  println("--- d3ArrayTest ---")
  val d3ArrayTest = cfs.findMethod("d3ArrayTest").head
  println(interp.invokeExternal(d3ArrayTest, true))

  println("--- d4ArrayTest ---")
  val d4ArrayTest = cfs.findMethod("d4ArrayTest").head
  println(interp.invokeExternal(d4ArrayTest, true))

  println("--- interfaceTest ---")
  val interfaceTest = cfs2.findMethod("interfaceTest").head
  println(interp.invokeExternal(interfaceTest, true))
  val defaultInterfaceTest = cfs2.findMethod("defaultInterfaceTest").head
  println(interp.invokeExternal(defaultInterfaceTest, true))

  println("--- lambdaTest ---")
  val lambdaTest = cfs2.findMethod("lambdaTest").head
  println(interp.invokeExternal(lambdaTest, true))

  println("--- exceptionTest ---")
  val exceptionTest = cfs.findMethod("exceptionTest").head
  println(interp.invokeExternal(exceptionTest, true))
  val nullPointerTest = cfs.findMethod("nullPointerTest").head
  println(interp.invokeExternal(nullPointerTest, true))
  val throwTest = cfs.findMethod("throwTest").head
  interp.evalExternal(BIPUSH(1))
  println(interp.invokeExternal(throwTest, true))
  interp.evalExternal(BIPUSH(0))
  println(interp.invokeExternal(throwTest, true))

  println("--- nullTest ---")
  val nullTest = cfs.findMethod("nullTest").head
  interp.evalExternal(ACONST_NULL)
  println(interp.invokeExternal(nullTest, true))
  interp.evalExternal(NEW(ObjectType("SimpleMath")))
  println(interp.invokeExternal(nullTest, true))

  println("--- typeTest ---")
  val typeTest = cfs.findMethod("typeTest").head
  println(interp.invokeExternal(typeTest, true))
  val typeTestInterface = cfs2.findMethod("typeTestInterface").head
  println(interp.invokeExternal(typeTestInterface, true))
  val typeTestArray = cfs2.findMethod("typeTestArray").head
  println(interp.invokeExternal(typeTestArray, true))
  val typeTest2 = cfs.findMethod("typeTest2").head
  interp.evalExternal(NEW(ObjectType("SimpleMath")))
  println(interp.invokeExternal(typeTest2, true))
  interp.evalExternal(ACONST_NULL)
  println(interp.invokeExternal(typeTest2, true))


  println("--- stringTest ---")
  val stringTest = cfs.findMethod("stringTest").head
  println(interp.invokeExternal(stringTest, true))
  val stringTest2 = cfs.findMethod("stringTest2").head
  println(interp.invokeExternal(stringTest2, true))
  val stringBuilderTest = cfs.findMethod("stringBuilderTest").head
  //interp.invoke(stringBuilderTest, true)
  //println(interp.stack.pop())

  println("--- POP2Test ---")
  interp.evalExternal(LoadLong(500))
  println(interp.stack.size)
  interp.evalExternal(POP2)
  println(interp.stack.size)
  interp.evalExternal(BIPUSH(5))
  interp.evalExternal(BIPUSH(5))
  println(interp.stack.size)
  interp.evalExternal(POP2)
  println(interp.stack.size)

  /*

  */

}

