import org.opalj.br.analyses.Project
import org.scalatest.funsuite.AnyFunSuite
import sturdy.language.bytecode.ConcreteInterpreter
import sturdy.language.bytecode.analyses.ConstantAnalysis

import java.nio.file.Paths

class FullProgramTest extends AnyFunSuite:
  test("full progam test"):
    val projectURI = this.getClass.getResource("/sturdy/language/bytecode/simpleAlgorithms").toURI
    val projectPath = Paths.get(projectURI).toString

    val pWithLibrary = Project(
      new java.io.File(projectPath), // path to the JAR files/directories containing the project
      org.opalj.bytecode.RTJar
    )

    val mainMethods = pWithLibrary.allClassFiles.flatMap(elem => elem.findMethod("main")).filter(elem => !pWithLibrary.isLibraryType(elem.classFile.thisType))
    println(mainMethods)


    for(mth <- mainMethods.filter(meth => meth.classFile.thisType.simpleName == "QuickSort")){
      val interp = new ConcreteInterpreter.Instance(pWithLibrary, projectPath, Map(), Map(), Map())
      val absInterp = new ConstantAnalysis.Instance(pWithLibrary, projectPath, Map(), Map(), Map())
      println("- - - - - - - - - - - - - - - - -")
      println("Executing Method: " ++ mth.name ++ " from " ++ mth.classFile.thisType.simpleName)
  //    println("Concrete Interpretation: " ++ interp.invokeExternal(mth, true).toString)
      println("Abstract Interpretation: " ++ absInterp.invokeExternal(mth, true).toString)
      //println(interp.arrayValStore.entries.toSeq.sortBy(_._1))
      //println(interp.objFieldStore.entries)
    }
