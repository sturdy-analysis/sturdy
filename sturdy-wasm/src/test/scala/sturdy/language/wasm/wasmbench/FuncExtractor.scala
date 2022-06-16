package sturdy.language.wasm.wasmbench

import cats.parse.{Numbers, Parser as P, Parser0 as P0}

import java.nio.file.{Files, Path, Paths}
import java.util.regex.Pattern
import scala.util.matching.Regex
import sys.process.*
import scala.util.matching.Regex.Match
import scala.jdk.CollectionConverters.*

object FuncExtractor:


  def extractFuncDefs(bin: Path, binMd: Metadata, wasm2wat: Path): List[FuncDef] =
    val typeDefRegex = "\\(type \\(;[0-9]+;\\).*".r
    val funcExportRegex = "\\(export \".*\" \\(func .*\\)\\)".r
//    def funcDefRegex = "\\(func (\\(;[0-9]+\\)|\\$.+) \\(type [0-9]+\\).*".r
    def funcDefsRegex(lab: Set[Label]) = {
      val labStr = lab.mkString("|").flatMap(c => {
        if List('(', ')', '$').contains(c) then s"\\${c}" else s"$c"
      })
      val r = s"\\(func (${labStr}) \\(type [0-9]+\\).*"
      val p = Pattern.compile(r)
      r.r}

    var typeDefStr = ""
    var funcExStr = ""
    var funcDefStr = ""

    {
      s"$wasm2wat $bin -o /tmp/tr.wat".!
      val tmpFilePath = Paths.get("/tmp/tr.wat")
      val in = Files.newBufferedReader(tmpFilePath).lines().iterator().asScala

      in.foreach(line => {
        val l = line.strip()
        if l.startsWith("(func") then
          funcDefStr = funcDefStr + "\n" + line
        else if l.strip().startsWith("(type") then
          typeDefStr = typeDefStr + "\n" + line
        else if l.startsWith("(export") then
          funcExStr = funcExStr + "\n" + line
      })
      if Files.isSymbolicLink(tmpFilePath) then
        Files.delete(Files.readSymbolicLink(tmpFilePath))
      else
        Files.delete(tmpFilePath)
    }

    val typeDefs = typeDefRegex
      .findAllMatchIn(typeDefStr)
      .map{
        case Match(str) =>
          val td = parseTypeDef(str)
          (td.label, td)}
      .toMap

    val funcExports = funcExportRegex
      .findAllMatchIn(funcExStr)
      .map{
        case Match(str) =>
          val fe = parseFuncExport(str)
          (fe.label, fe)}
      .toMap

    val funcDefs = funcDefsRegex(funcExports.keySet)
      .findAllMatchIn(funcDefStr)
      .map{
        case Match(str) =>
          funcDef.parseAll(str) match {
            case Right((l, i)) => FuncDef(l, typeDefs(Label(i)), Some(funcExports(l).name))
            case Left(err) => ???}
      }.toList
    funcDefs

  def parseFuncExport(source: String): FuncExport =
    funcExport.parseAll(source) match
      case Right(p) => p
      case Left(err) => throw new IllegalArgumentException(s"Parse error at ${source.slice(err.failedAtOffset, err.failedAtOffset+10)}: $err")

  def parseTypeDef(source: String): TypeDef =
    typeDef.parseAll(source) match
      case Right(p) => p
      case Left(err) => throw new IllegalArgumentException(s"Parse error at ${source.slice(err.failedAtOffset, err.failedAtOffset+10)}: $err")

  val whitespace: P[Unit] = (P.charIn(" \t\r\n").void)
  val whitespaces0: P0[Unit] = whitespace.rep0.void

  def spaced[A](p: P[A]): P[A] =
    p <* whitespaces0

  val letter: P[Unit] = P.ignoreCaseCharIn('a' to 'z').void
  val digit: P[Unit] = P.charIn('0' to '9').void
  val letterDigit: P[Unit] = P.charIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).void


  val id: P[String] =
    val exclude = List(' ',',', ';', '"', '\'', '(', ')', '[', ']', '{', '}')
    P.char('$') *> P.charWhere(c => ! exclude.contains(c)).rep0
      .string

  val identifier: P[String] =
    spaced(id)

  def inParens[A](p: P0[A]): P[A] =
    op('(') *> p <* op(')')

  def inParens[A](p: P[A]): P[A] =
    op('(') *> p <* op(')')

  def op(c: Char): P[Unit] =
    spaced(P.char(c))

  def op(s: String): P[Unit] =
    spaced(P.string(s))

  def comment[A](p: P[A]): P[A] =
    op("(;") *> p <* op(";)")

  val digitComment: P[Int] =
    comment(Numbers.signedIntString).map(_.toInt)

  val label: P[Label] =
    identifier.backtrack.map(Label.Symbolic(_))
      | digitComment.map(Label.Numeric(_))

  val wasmType: P[WASMType] =
    val types = List("i32", "i64", "f32", "f64")
    spaced(
      P.stringIn(types)
    ).map(WASMType(_))

  val param: P[List[WASMType]] =
    inParens(
      op("param") *> wasmType.rep
    ).map(_.toList)

  val result: P[List[WASMType]] =
    inParens(
      op("result") *> wasmType.rep
    ).map(_.toList)

  val typeDef: P[TypeDef] =
    (inParens(
      op("type")
        *> label ~ inParens(op("func") *> (param.backtrack.? ~ result.?))
    )).map{
      case (lab, (param, result)) =>
        TypeDef(lab, param.getOrElse(List.empty), result.getOrElse(List.empty))
    }

  val funcDef: P[(Label, Int)] =
    (op('(') ~ op("func"))
      *> (label ~ inParens(op("type") *> Numbers.signedIntString.map(_.toInt)))
      <* P.charsWhile0(_ => true)

  val funcExport: P[FuncExport] =
    inParens(
      op("export")
      *> (op('"') *> P.charsWhile(c => c != '"') <* op('"'))
      ~ inParens(
        op("func") *> (Numbers.signedIntString.map(_.toInt) | identifier))
    ).map{
      case (name, label: Int) => FuncExport(name, Label.Numeric(label))
      case (name, label: String) => FuncExport(name, Label.Symbolic(label))
    } <* op(')').?
    
object test extends App:
  import FuncExtractor.*
  val str = "(export \"_start\" (func $_start))"
  val str2 = "(export \"main\" (func $main))"
  val str3 = "(type (;1;) (func (param i32 i64 i32) (result i64)))"
  val comment_str = "(;1;)"

  println(parseFuncExport(str2))
  println(label.parse(comment_str))
  println(parseTypeDef(str3))


// (type (;1;) (func (param i32 i64 i32) (result i64)))
// (type (;0;) (func (param i32)))
// (type (;1;) (func (result i32)))

// (func (;10;) (type 1)
// (func $xlsx_get_sheets (type 4) (param i32 i32)

// (export "_start" (func $_start))
// (export "resume" (func 22))
