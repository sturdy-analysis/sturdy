import org.opalj.br.{ClassFile, DoubleType, FieldType, FloatType, IntegerType, LongType}
import org.opalj.br.analyses.Project
import org.opalj.br.instructions.*
import org.opalj.io.process
import sturdy.language.bytecode.ConcreteInterpreter
import sturdy.language.bytecode.generic.ValType
import sturdy.values.integer.IntegerOps

import java.io.{DataInputStream, FileInputStream}
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

  val classFileName = "D:\\DesktopStuff\\Schule_Uni_Bewerbung_Stuff\\Studium\\Master\\Masterarbeit\\Seminar\\Example Java\\Password-Generator-master\\Password-Generator-master\\out\\production\\Password-Generator\\SimpleMath.class"
  val cfs: List[ClassFile] =
    process(new DataInputStream(new FileInputStream(classFileName))) { in =>
      org.opalj.br.reader.Java8Framework.ClassFile(in)
    }
  /*val locals = cfs.head.findMethod("return10").head.body.get.localVariableTable.get
  println(locals.map(_.fieldType))
  val localtypes = locals.map(_.fieldType)
  val convertedTypes = localtypes.map(convertTypes(_))
  println(convertedTypes)*/
  val testMethod = cfs.head.findMethod("sub2").head



  val interp = new ConcreteInterpreter.Instance

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


  val instList = List(
    BIPUSH(5),
    ISTORE_0,
    BIPUSH(6),
    ISTORE_1,
    BIPUSH(29),
    ILOAD_0,
    ILOAD_1,
    IADD
  )
  val localList = List(ValType.I32, ValType.I32, ValType.I32, ValType.I32)

  interp.invokeStatic(localList, instList, List())

  println(interp.stack.pop())
  println(interp.stack.pop())

  interp.eval(BIPUSH(5))
  interp.eval(BIPUSH(7))
  interp.invokeStatic(testMethod)

  println(interp.stack.pop())

/*

*/

}

