import org.opalj.br.analyses.Project

import java.net.URL

object test extends App{
  val projectJAR = "D:\\DesktopStuff\\Schule_Uni_Bewerbung_Stuff\\Studium\\Master\\Masterarbeit\\Seminar\\Example Java\\Password-Generator-master\\Password-Generator-master\\out\\production\\Password-Generator\\PasswordGenerator19.jar"
  implicit val p: Project[URL] = Project(
    new java.io.File(projectJAR) // path to the JAR files/directories containing the project
  )

  println(p.allMethodsWithBody)

}

