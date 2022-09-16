package sturdy.language.jimple

import sturdy.language.jimple.ConcreteInterpreter

import sturdy.effect.symboltable.{ConcreteSymbolTable, JoinedSymbolTable}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.jimple.Parser.LanguageKeywords.KRETURN
import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import Parser.*

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import sturdy.data.NoJoin


class ConcreteInterpreterTest extends AnyFlatSpec, Matchers {
  behavior of "Jimple concrete interpreter"

  val jdkUri = classOf[ConcreteInterpreterTest].getResource("/sturdy/language/jimple/jdk").toURI;
  val jdk = Paths.get(jdkUri)

//  val classTable: ConcreteSymbolTable[Unit, String, Container] = new ConcreteSymbolTable[Unit, String, Container]
//  val runtimeTable: ConcreteSymbolTable[Unit, String, RuntimeUnit] = new ConcreteSymbolTable[Unit, String, RuntimeUnit]

  def testFile(p: Path): Unit =
    it must s"correctly execute ${p.getFileName}" in {
      val classTable: ConcreteSymbolTable[Unit, String, Container] = new ConcreteSymbolTable[Unit, String, Container]
      val runtimeTable: ConcreteSymbolTable[Unit, String, RuntimeUnit] = new ConcreteSymbolTable[Unit, String, RuntimeUnit]
      parseJDK(classTable, runtimeTable)
      val program = parseAndLoad(p, classTable, runtimeTable)
//      val refInterp = new ReferenceInterpreter
      val concInterp = new ConcreteInterpreter.Instance
//      val resRef = fallible(refInterp.runProg(arg, program))
//      val resConc = fallible(concInterp.runProg(arg, program))
//      assertResult(resRef)(resConc)
    }


  def parseJDK(classTable: ConcreteSymbolTable[Unit, String, Container], runtimeTable: ConcreteSymbolTable[Unit, String, RuntimeUnit]): Unit =
    Files.list(jdk).toScala(List).filter(p => p.toString.endsWith(".jimple")).sorted.foreach { p =>
      parseAndLoad(p, classTable, runtimeTable)
    }

  def splitUpBody(className: String, body: collection.Seq[ClassBodyElement]): (Map[QName, ClassBodyElement.GlobalVarCB], Map[QName, ClassBodyElement.MethodCB]) =
    var fields = Map[QName, ClassBodyElement.GlobalVarCB]()
    var methods = Map[QName, ClassBodyElement.MethodCB]()

    body.foreach{ bodyEl =>
      bodyEl match {
        case ClassBodyElement.GlobalVarCB(_, _, _, _, _, _, _, _, _, id) =>
          fields + ((className, id) -> bodyEl)
        case ClassBodyElement.MethodCB(_, _, _, _, _) =>
          methods + ((className, bodyEl.getID) -> bodyEl)
        case _ => throw new IllegalArgumentException(s"A ClassBodyElement is expected.")
      }
    }
    (fields, methods)

  def parseAndLoad(p: Path, classTable: ConcreteSymbolTable[Unit, String, Container], runtimeTable: ConcreteSymbolTable[Unit, String, RuntimeUnit]): Program =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val parsed = parse(sourceCode) //Seq[Container]
    parsed.funs.foreach{ c =>
      val ident = c.getID
      classTable.set((), ident, c)
      c match {
        case Container.ClassC(_, _, isAbstract, _, _, _, id, extend, implement, body) =>
          val bodyEls = splitUpBody(ident, body)
          val runtimeUnit = RuntimeUnit(ident, bodyEls._1, bodyEls._2, isAbstract)
          runtimeTable.set((), ident, runtimeUnit)
        case Container.InterfaceC(_, _, id, extend, implement, body) =>
          val bodyEls = splitUpBody(ident, body)
          val runtimeUnit = RuntimeUnit(ident, bodyEls._1, bodyEls._2, true) // interfaces are just like abstract classes
          runtimeTable.set((), ident, runtimeUnit)
      }
    }
    parsed

}
