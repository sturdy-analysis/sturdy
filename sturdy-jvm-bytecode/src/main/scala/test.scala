import org.opalj.br.analyses.Project
import org.opalj.br.instructions.*
import sturdy.values.integer.IntegerOps

import java.net.URL
import scala.collection.mutable

object test extends App{
  /*val projectJAR = "D:\\DesktopStuff\\Schule_Uni_Bewerbung_Stuff\\Studium\\Master\\Masterarbeit\\Seminar\\Example Java\\Password-Generator-master\\Password-Generator-master\\out\\production\\Password-Generator\\PasswordGenerator19.jar"
  implicit val p: Project[URL] = Project(
    new java.io.File(projectJAR) // path to the JAR files/directories containing the project
  )

  println(p.allMethodsWithBody)*/

  val test = IADD
  val test2 = BIPUSH(5)
  val test3 = BIPUSH(10)
  println(test2.value)

  var opStack = new mutable.Stack[Int]()
  def eval(instruction: Instruction): Unit = instruction match
    case instruction: BIPUSH => opStack.push(instruction.value)
    case instruction: IADD.type =>
      val v1 = opStack.pop()
      val v2 = opStack.pop()
      println(v1+v2)

  eval(test3)
  eval(test2)
  eval(test)



}

