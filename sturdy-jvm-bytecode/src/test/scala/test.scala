import org.opalj.br.analyses.Project
import org.opalj.br.instructions.*
import org.opalj.br.*
import org.opalj.io.process
import sturdy.language.bytecode.ConcreteInterpreter
import sturdy.language.bytecode.generic.ValType
import sturdy.values.integer.IntegerOps

import java.io.{DataInputStream, FileInputStream, File}
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
      org.opalj.br.reader.Java8Framework.ClassFile(in)
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
  
  /*
  interp.eval(BIPUSH(5))
  interp.eval(BIPUSH(10))
  interp.eval(IADD)
  interp.eval(BIPUSH(3))
  interp.eval(ISUB)
  interp.eval(INEG)
  val result = interp.stack.pop()
  println(result)

  interp.eval(LoadFloat(3.2f))
  interp.eval(LoadFloat(4.2f))
  interp.eval(FADD)
  val result2 = interp.stack.pop()
  println(result2)

  interp.eval(BIPUSH(12))
  interp.eval(BIPUSH(5))
  interp.eval(IREM)
  val result3 = interp.stack.pop()
  println(result3)

  interp.eval(LoadFloat(12.7f))
  interp.eval(LoadFloat(5.0f))
  interp.eval(FREM)
  val result4 = interp.stack.pop()
  println(result4)

  interp.eval(LoadFloat(12.0f))
  interp.eval(LoadFloat(NaN))
  interp.eval(FCMPL)
  val result5 = interp.stack.pop()
  println(result5)


  println("--- SubTest ---")
  val testMethod = cfs.findMethod("sub2").head
  interp.eval(BIPUSH(5))
  interp.eval(BIPUSH(7))
  interp.invoke(testMethod, true)
  println(interp.stack.pop())


  println("--- BranchTest ---")
  val testBranch = cfs.findMethod("branching").head
  interp.eval(ICONST_0)
  interp.invoke(testBranch, true)
  println(interp.stack.pop())

  interp.eval(ICONST_1)
  interp.invoke(testBranch, true)
  println(interp.stack.pop())
  println(interp.stack.size)

  println("--- ReturnTest ---")
  val testReturn = cfs.findMethod("returnTest").head
  interp.eval(ICONST_1)
  interp.invoke(testReturn, true)
  println(interp.stack.pop())
  println(interp.stack.size)

  println("--- ObjectTest ---")
  val testObj = cfs.findMethod("objectTest").head
  interp.invoke(testObj, true)
  println(interp.stack.pop())

  println("--- InheritanceTest ---")
  val testInherit = cfs.findMethod("inheritanceTest").head
  interp.invoke(testInherit, true)
  println(interp.stack.pop())

  println("--- objectCompTest ---")
  val testObjCompTrue = cfs.findMethod("objectCompTestTrue").head
  interp.invoke(testObjCompTrue, true)
  println(interp.stack.pop())

  val testObjCompFalse = cfs.findMethod("objectCompTestFalse").head
  interp.invoke(testObjCompFalse, true)
  println(interp.stack.pop())

  println("--- switchTest ---")
  val switchTest = cfs.findMethod("switchTest").head
  interp.eval(BIPUSH(2))
  interp.invoke(switchTest, true)
  println(interp.stack.pop())

  interp.eval(SIPUSH(200))
  interp.invoke(switchTest, true)
  println(interp.stack.pop())

  println("--- nonCompactSwitchTest ---")
  val nonCompactSwitchTest = cfs.findMethod("nonCompactSwitchTest").head
  interp.eval(SIPUSH(200))
  interp.invoke(nonCompactSwitchTest, true)
  println(interp.stack.pop())

  interp.eval(SIPUSH(201))
  interp.invoke(nonCompactSwitchTest, true)
  println(interp.stack.pop())

  println("--- staticVarTest ---")
  val staticVarTest = cfs.findMethod("staticVarTest").head
  interp.invoke(staticVarTest, true)
  println(interp.stack.pop())
  interp.invoke(staticVarTest, true)
  println(interp.stack.pop())

  println("--- arrayTest ---")
  val arrayTest = cfs.findMethod("arrayTest").head
  interp.invoke(arrayTest, true)
  println(interp.stack.pop())

  val arrayTest2 = cfs.findMethod("arrayTest2").head
  interp.invoke(arrayTest2, true)
  println(interp.stack.pop())

  val arrayTest3 = cfs.findMethod("arrayTest3").head
  interp.invoke(arrayTest3, true)
  println(interp.stack.pop())

  val arrayCompTest = cfs.findMethod("arrayCompTest").head
  interp.invoke(arrayCompTest, true)
  println(interp.stack.pop())

  val arrayLengthTest = cfs.findMethod("arrayLengthTest").head
  interp.invoke(arrayLengthTest, true)
  println(interp.stack.pop())

  val arrayTypeTest = cfs.findMethod("arrayTypeTest").head
  interp.invoke(arrayTypeTest, true)
  println(interp.stack.pop())

  println("--- objectArrayTest ---")
  val objectArrayTest = cfs.findMethod("objectArrayTest").head
  interp.invoke(objectArrayTest, true)
  println(interp.stack.pop())

  val objectArrayTypeTest = cfs.findMethod("objectArrayTypeTest").head
  interp.invoke(objectArrayTypeTest, true)
  println(interp.stack.pop())

  println("--- multiDArrayTest ---")
  val multiDArrayTest = cfs.findMethod("multiDArrayTest").head
  interp.invoke(multiDArrayTest, true)
  println(interp.stack.pop())

  println("--- d3ArrayTest ---")
  val d3ArrayTest = cfs.findMethod("d3ArrayTest").head
  interp.invoke(d3ArrayTest, true)
  println(interp.stack.pop())

  println("--- d4ArrayTest ---")
  val d4ArrayTest = cfs.findMethod("d4ArrayTest").head
  interp.invoke(d4ArrayTest, true)
  println(interp.stack.pop())

  println("--- interfaceTest ---")
  val interfaceTest = cfs2.findMethod("interfaceTest").head
  interp.invoke(interfaceTest, true)
  println(interp.stack.pop())
  val defaultInterfaceTest = cfs2.findMethod("defaultInterfaceTest").head
  interp.invoke(defaultInterfaceTest, true)
  println(interp.stack.pop())

  println("--- lambdaTest ---")
  val lambdaTest = cfs2.findMethod("lambdaTest").head
  interp.invoke(lambdaTest, true)
  println(interp.stack.pop())

  println("--- exceptionTest ---")
  val exceptionTest = cfs.findMethod("exceptionTest").head
  interp.invoke(exceptionTest, true)
  println(interp.stack.pop())
  val nullPointerTest = cfs.findMethod("nullPointerTest").head
  interp.invoke(nullPointerTest, true)
  println(interp.stack.pop())
  val throwTest = cfs.findMethod("throwTest").head
  interp.eval(BIPUSH(1))
  interp.invoke(throwTest, true)
  println(interp.stack.pop())
  interp.eval(BIPUSH(0))
  interp.invoke(throwTest, true)
  println(interp.stack.pop())

  println("--- nullTest ---")
  val nullTest = cfs.findMethod("nullTest").head
  interp.eval(ACONST_NULL)
  interp.invoke(nullTest, true)
  println(interp.stack.pop())
  interp.eval(NEW(ObjectType("SimpleMath")))
  interp.invoke(nullTest, true)
  println(interp.stack.pop())

  println("--- typeTest ---")
  val typeTest = cfs.findMethod("typeTest").head
  interp.invoke(typeTest, true)
  println(interp.stack.pop())
  val typeTestInterface = cfs2.findMethod("typeTestInterface").head
  interp.invoke(typeTestInterface, true)
  println(interp.stack.pop())
  val typeTestArray = cfs2.findMethod("typeTestArray").head
  interp.invoke(typeTestArray, true)
  println(interp.stack.pop())
  val typeTest2 = cfs.findMethod("typeTest2").head
  interp.eval(NEW(ObjectType("SimpleMath")))
  interp.invoke(typeTest2, true)
  println(interp.stack.pop())
  interp.eval(ACONST_NULL)
  interp.invoke(typeTest2, true)
  println(interp.stack.pop())*/


  println("--- stringTest ---")
  val stringTest = cfs.findMethod("stringTest").head
  //interp.invoke(stringTest, true)
  //println(interp.stack.pop())
  val stringBuilderTest = cfs.findMethod("stringBuilderTest").head
  interp.invoke(stringBuilderTest, true)
  println(interp.stack.pop())

  /*

  */

}

