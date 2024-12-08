import org.opalj.br.analyses.Project

object FullProgramTest extends App:
  val projectURL = "D:\\DesktopStuff\\Schule_Uni_Bewerbung_Stuff\\Studium\\Master\\Masterarbeit\\Seminar\\Example Java\\Password-Generator-master\\Password-Generator-master\\out\\production\\Password-Generator\\PasswordGenerator19.jar"

  val pWithLibrary = Project(
    new java.io.File(projectURL), // path to the JAR files/directories containing the project
    org.opalj.bytecode.RTJar
  )

  val mainMethod = pWithLibrary.allClassFiles.find(elem => elem.thisType.simpleName == "Main").get.findMethod("main")
  println(mainMethod)
