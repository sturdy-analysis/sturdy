import org.opalj.br.analyses.Project
import sturdy.language.bytecode.ConcreteInterpreter

import java.nio.file.Paths

object FullProgramTest extends App:
  val projectURI = this.getClass.getResource("/sturdy/language/bytecode/simpleAlgorithms").toURI
  val projectPath = Paths.get(projectURI).toString

  val pWithLibrary = Project(
    new java.io.File(projectPath), // path to the JAR files/directories containing the project
    org.opalj.bytecode.RTJar
  )

  val mainMethods = pWithLibrary.allClassFiles.flatMap(elem => elem.findMethod("main")).filter(elem => !pWithLibrary.isLibraryType(elem.classFile.thisType))
  println(mainMethods)
  val test = Seq(10, 80, 111, 115, 116, 111, 114, 100, 101, 114, 32, 84, 114, 97, 118, 101, 114, 115, 97, 108, 58)
  println(test.map(e => e.toChar))


  for(mth <- mainMethods){
    val interp = new ConcreteInterpreter.Instance(pWithLibrary, projectPath, Map(), Map(), Map())
    println("Executing Method: " ++ mth.name ++ " from " ++ mth.classFile.thisType.simpleName)
    println("Concrete Interpretation: " ++ interp.invokeExternal(mth, true).toString)
    //println(interp.arrayValStore.entries.toSeq.sortBy(_._1))
    //println(interp.objFieldStore.entries)
  }
