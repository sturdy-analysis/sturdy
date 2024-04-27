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

  val classFileName = "C:\\Users\\Stefan Marx\\IdeaProjects\\CompileProject\\out\\production\\CompileProject"
  val simpleMathName = "C:\\Users\\Stefan Marx\\IdeaProjects\\CompileProject\\out\\production\\CompileProject\\SimpleMath.class"

  val pWithNatives = Project(
    new java.io.File(classFileName), // path to the JAR files/directories containing the project
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
  val cfs2 = pWithNatives.classFile(ObjectType("ComplicatedMath")).get
  println(pWithNatives.classFile(ObjectType("ComplicatedMath")).get.staticInitializer)

  val sourceFile = classFileName

  val interp = new ConcreteInterpreter.Instance(pWithNatives, sourceFile, Map(), Map(), Map())

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
  interp.invokeExternal(testMethod, true)
  println(interp.stack.pop())


  println("--- BranchTest ---")
  val testBranch = cfs.findMethod("branching").head
  interp.evalExternal(ICONST_0)
  interp.invokeExternal(testBranch, true)
  println(interp.stack.pop())

  interp.evalExternal(ICONST_1)
  interp.invokeExternal(testBranch, true)
  println(interp.stack.pop())
  println(interp.stack.size)

  println("--- ReturnTest ---")
  val testReturn = cfs.findMethod("returnTest").head
  interp.evalExternal(ICONST_1)
  interp.invokeExternal(testReturn, true)
  println(interp.stack.pop())
  println(interp.stack.size)

  println("--- ObjectTest ---")
  val testObj = cfs.findMethod("objectTest").head
  interp.invokeExternal(testObj, true)
  println(interp.stack.pop())

  println("--- InheritanceTest ---")
  val testInherit = cfs.findMethod("inheritanceTest").head
  interp.invokeExternal(testInherit, true)
  println(interp.stack.pop())

  println("--- objectCompTest ---")
  val testObjCompTrue = cfs.findMethod("objectCompTestTrue").head
  interp.invokeExternal(testObjCompTrue, true)
  println(interp.stack.pop())

  val testObjCompFalse = cfs.findMethod("objectCompTestFalse").head
  interp.invokeExternal(testObjCompFalse, true)
  println(interp.stack.pop())

  println("--- switchTest ---")
  val switchTest = cfs.findMethod("switchTest").head
  interp.evalExternal(BIPUSH(2))
  interp.invokeExternal(switchTest, true)
  println(interp.stack.pop())

  interp.evalExternal(SIPUSH(200))
  interp.invokeExternal(switchTest, true)
  println(interp.stack.pop())

  println("--- nonCompactSwitchTest ---")
  val nonCompactSwitchTest = cfs.findMethod("nonCompactSwitchTest").head
  interp.evalExternal(SIPUSH(200))
  interp.invokeExternal(nonCompactSwitchTest, true)
  println(interp.stack.pop())

  interp.evalExternal(SIPUSH(201))
  interp.invokeExternal(nonCompactSwitchTest, true)
  println(interp.stack.pop())

  println("--- staticVarTest ---")
  val staticVarTest = cfs.findMethod("staticVarTest").head
  interp.invokeExternal(staticVarTest, true)
  println(interp.stack.pop())
  interp.invokeExternal(staticVarTest, true)
  println(interp.stack.pop())

  println("--- arrayTest ---")
  val arrayTest = cfs.findMethod("arrayTest").head
  interp.invokeExternal(arrayTest, true)
  println(interp.stack.pop())

  val arrayTest2 = cfs.findMethod("arrayTest2").head
  interp.invokeExternal(arrayTest2, true)
  println(interp.stack.pop())

  val arrayTest3 = cfs.findMethod("arrayTest3").head
  interp.invokeExternal(arrayTest3, true)
  println(interp.stack.pop())

  val arrayCompTest = cfs.findMethod("arrayCompTest").head
  interp.invokeExternal(arrayCompTest, true)
  println(interp.stack.pop())

  val arrayLengthTest = cfs.findMethod("arrayLengthTest").head
  interp.invokeExternal(arrayLengthTest, true)
  println(interp.stack.pop())

  val arrayTypeTest = cfs.findMethod("arrayTypeTest").head
  interp.invokeExternal(arrayTypeTest, true)
  println(interp.stack.pop())

  println("--- objectArrayTest ---")
  val objectArrayTest = cfs.findMethod("objectArrayTest").head
  interp.invokeExternal(objectArrayTest, true)
  println(interp.stack.pop())

  val objectArrayTypeTest = cfs.findMethod("objectArrayTypeTest").head
  interp.invokeExternal(objectArrayTypeTest, true)
  println(interp.stack.pop())

  println("--- multiDArrayTest ---")
  val multiDArrayTest = cfs.findMethod("multiDArrayTest").head
  interp.invokeExternal(multiDArrayTest, true)
  println(interp.stack.pop())

  println("--- d3ArrayTest ---")
  val d3ArrayTest = cfs.findMethod("d3ArrayTest").head
  interp.invokeExternal(d3ArrayTest, true)
  println(interp.stack.pop())

  println("--- d4ArrayTest ---")
  val d4ArrayTest = cfs.findMethod("d4ArrayTest").head
  interp.invokeExternal(d4ArrayTest, true)
  println(interp.stack.pop())

  println("--- interfaceTest ---")
  val interfaceTest = cfs2.findMethod("interfaceTest").head
  interp.invokeExternal(interfaceTest, true)
  println(interp.stack.pop())
  val defaultInterfaceTest = cfs2.findMethod("defaultInterfaceTest").head
  interp.invokeExternal(defaultInterfaceTest, true)
  println(interp.stack.pop())

  println("--- lambdaTest ---")
  val lambdaTest = cfs2.findMethod("lambdaTest").head
  interp.invokeExternal(lambdaTest, true)
  println(interp.stack.pop())

  println("--- exceptionTest ---")
  val exceptionTest = cfs.findMethod("exceptionTest").head
  interp.invokeExternal(exceptionTest, true)
  println(interp.stack.pop())
  val nullPointerTest = cfs.findMethod("nullPointerTest").head
  interp.invokeExternal(nullPointerTest, true)
  println(interp.stack.pop())
  val throwTest = cfs.findMethod("throwTest").head
  interp.evalExternal(BIPUSH(1))
  interp.invokeExternal(throwTest, true)
  println(interp.stack.pop())
  interp.evalExternal(BIPUSH(0))
  interp.invokeExternal(throwTest, true)
  println(interp.stack.pop())

  println("--- nullTest ---")
  val nullTest = cfs.findMethod("nullTest").head
  interp.evalExternal(ACONST_NULL)
  interp.invokeExternal(nullTest, true)
  println(interp.stack.pop())
  interp.evalExternal(NEW(ObjectType("SimpleMath")))
  interp.invokeExternal(nullTest, true)
  println(interp.stack.pop())

  println("--- typeTest ---")
  val typeTest = cfs.findMethod("typeTest").head
  interp.invokeExternal(typeTest, true)
  println(interp.stack.pop())
  val typeTestInterface = cfs2.findMethod("typeTestInterface").head
  interp.invokeExternal(typeTestInterface, true)
  println(interp.stack.pop())
  val typeTestArray = cfs2.findMethod("typeTestArray").head
  interp.invokeExternal(typeTestArray, true)
  println(interp.stack.pop())
  val typeTest2 = cfs.findMethod("typeTest2").head
  interp.evalExternal(NEW(ObjectType("SimpleMath")))
  interp.invokeExternal(typeTest2, true)
  println(interp.stack.pop())
  interp.evalExternal(ACONST_NULL)
  interp.invokeExternal(typeTest2, true)
  println(interp.stack.pop())


  println("--- stringTest ---")
  val stringTest = cfs.findMethod("stringTest").head
  interp.invokeExternal(stringTest, true)
  println(interp.stack.pop())
  val stringTest2 = cfs.findMethod("stringTest2").head
  interp.invokeExternal(stringTest2, true)
  println(interp.stack.pop())
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

