package sturdy.language.tip

import cats.parse.{Numbers, Parser0 as P0, Parser as P}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.values.TreeList

import java.nio.file.Path
import java.nio.file.{Paths, Files}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Try, Success, Failure}

class TreeInterpreterTest extends AnyFlatSpec, Matchers:

  behavior of "Tip tree interpreter"

  private val uri = classOf[TreeInterpreterTest].getResource("/sturdy/language/tip").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith("factorial_recursive.tip")).sorted.foreach { p =>
    it must s"execute ${p.getFileName}" in {
      runFile(p)
    }
  }

  def runFile(p: Path): Unit =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)
    if (program.funs.exists(_.name == "main")) {
      val interp = TreeInterpreter(Map(), Map())
      val res = interp.failure.fallible(interp.execute(program))
      println(res)
    }
//
//object O {
//  Unfailing(
//    InvokeFun(FunValue(function main), List(),
//      List(InNewFrame((), Map(n -> IntegerLit(-1)), List(UserInputTree(), SetLocalByName(n, UserInputTree())))),
//      InvokeFun(FunValue(function ite), List(GetLocalByName(n)),
//        List(
//          InNewFrame((), Map(n -> GetLocalByName(n), f -> IntegerLit(-1)),
//            List(
//              SetLocalByName(f, IntegerLit(1)),
//              SetLocalByName(f, Mul(GetLocalByName(f), GetLocalByName(n))),
//              SetLocalByName(n, Sub(GetLocalByName(n), IntegerLit(1))),
//              TreeList(List()),
//              BoolBranch(
//                GT(GetLocalByName(n), IntegerLit(0)),
//                TreeList(List(
//                  SetLocalByName(f, Mul(GetLocalByName(f), GetLocalByName(n))),
//                  SetLocalByName(n, Sub(GetLocalByName(n), IntegerLit(1))))),
//                TreeList(List()))))),
//        GetLocalByName(f))))
//}
//object Factorial_Iterative {
//  Unfailing(
//    InvokeFun(FunValue(function main),List(),
//      List(InNewFrame((),Map(n -> IntegerLit(-1)), List(UserInputTree(), SetLocalByName(n,UserInputTree())))),
//      InvokeFun(FunValue(function ite),List(GetLocalByName(n)),
//        List(
//          InNewFrame((),Map(n -> GetLocalByName(n), f -> IntegerLit(-1)),
//            List(
//              SetLocalByName(f,IntegerLit(1)),
//              Loop(run While(Gt)),
//              BoolBranch(
//                GT(GetLocalByName(n),IntegerLit(0)),
//                TreeList(List(
//                  PrintTree(GetLocalByName(f)),
//                  SetLocalByName(f,Mul(GetLocalByName(f),GetLocalByName(n))),
//                  SetLocalByName(n,Sub(GetLocalByName(n),IntegerLit(1))),
//                  Break(run While(Gt)))),
//                TreeList(List())), PrintTree(GetLocalByName(f))))),GetLocalByName(f))))
//}
//object Factorial_Recursive {
//  Unfailing(
//    InvokeFun(FunValue(function main),List(),
//      List(InNewFrame((),Map(n -> IntegerLit(-1)),
//        List(IRFunction(enter main), SetLocalByName(n,IntegerLit(5)))),
//        InvokeFun(FunValue(function rec),List(GetLocalByName(n)),
//          List(
//            InNewFrame((),Map(n -> GetLocalByName(n), f -> IntegerLit(-1)),
//              List(
//                IRFunction(enter rec),
//                BoolBranch(
//                  Equ(GetLocalByName(n),IntegerLit(0)),
//                  TreeList(List(SetLocalByName(f,IntegerLit(1)))),
//                  TreeList(List(
//                    SetLocalByName(f,
//                      Mul(GetLocalByName(n),
//                        InvokeFun(FunValue(function rec),List(
//                          Sub(GetLocalByName(n),IntegerLit(1))),
//                          List(
//                            InNewFrame((),Map(n -> Sub(GetLocalByName(n),IntegerLit(1)), f -> IntegerLit(-1)),List()),
//                            IRCall(enter rec)))))))),
//                PrintTree(GetLocalByName(f)))),
//            GetLocalByName(f))))))
//}