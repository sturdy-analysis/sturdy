import cats.parse.{Parser as P, Parser0 as P0}
import cats.syntax.all.*

import scala.util.Try
import LLVM.{Terminator, Type, Value, *}
import LLVM.Terminator.*
import LLVM.Instruction.*
import BinOp.*
import LLVM.MetaData.LOOP

object LLVMParser:

  // ---- Whitespace
  val ws: P[Unit] = P.charIn(" \t\r\n").rep.void
  val ws0: P0[Unit] = P.charIn(" \t\r\n").rep0.void

  // ---- Identifiers
  val local: P[Local] = P.char('%') *> P.charIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).rep(1).string.map(s => Local("%" + s))
  val global: P[Global] = P.char('@') *> P.charIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).rep(1).string.map(s => Global("@" + s))

  // ---- Types
  val intType: P[Type] =
    (P.string("i") *> P.charsWhile(_.isDigit).string).map(n => Type.Int(n.toInt))

  val floatType: P[Type] =
    P.string("half").as(Type.Half)
      .orElse(P.string("float").as(Type.Float))
      .orElse(P.string("double").as(Type.Double))
      .orElse(P.string("fp128").as(Type.Fp128))
      .orElse(P.string("x86_fp80").as(Type.X86Fp80))
      .orElse(P.string("ppc_fp128").as(Type.PpcFp128))

  val typ: P[Type] = intType.orElse(floatType)

  // ---- Params
  val param: P[Param] = (typ <* ws <* P.string("noundef ").?) ~ local map { case (t, n) => Param(t, n) }

  val paramList: P[Seq[Param]] =
    P.char('(') *> ws0 *> param.repSep0(P.char(',') <* ws0).map(_.toList) <* ws0 <* P.char(')')

  val pos_digits: P[String] = P.charIn('0' to '9').rep.string
  val neg_digits: P[String] = (P.char('-') *> pos_digits).map("-" ++ _)
  val digits: P[String] = neg_digits.backtrack | pos_digits

  val functionDefHead: P[(Type, Global, Seq[Param], Int)] =
    for {
      ty <- P.string("define") *> ws *> typ <* ws
      name <- global
      params <- paramList <* ws0
      attr <- P.char('#') *> digits <* ws
    } yield (ty, name, params, attr.toInt)

  val align: P[Int] = (P.string(",") *> ws0 *> P.string("align") *> ws *> digits).map(_.toInt)

  val localValue: P[Value] = local.map(Value.Ref(_))
  val int32Value: P[Value] = digits.map(s => Value.Int(s.toInt, Type.Int(32)))

  val valueParser: P[Value] = List(localValue, int32Value).reduceLeft(_.orElse(_))

  val alloca: P[Instruction] =
    for {
      l <- local <* ws0 <* P.char('=') <* ws0
      _ <- P.string("alloca") <* ws
      t <- typ
      a <- align <* ws
    } yield Alloca(t, align = a).named(l)

  val store: P[Instruction] =
    for {
      k <- (P.string("store") *> ws0 *> typ) ~ (ws0 *> valueParser)
      p <- P.string(", ptr") *> ws0 *> local
      a <- align <* ws0
    } yield Store(k._1, k._2, p, a)

  val load: P[Instruction] =
    for {
      l <- local <* ws <* P.char('=') <* ws
      ty <- P.string("load") *> ws *> typ
      p <- P.string(", ptr") *> ws *> local
      a <- align <* ws0
    } yield Load(ty, p, a).named(l)

  val call: P[Instruction] =
    for {
      l <- local <* ws <* P.char('=') <* ws
      ty <- P.string("call") *> ws *> typ <* ws
      name <- global
      params <- paramList <* ws0
    } yield Call(ty, name, params).named(l)

  def binOp(s: BinOp): P[Instruction] =
    for {
      l <- local <* ws <* P.char('=') <* ws0
      ty <- P.string(s.toString) *> ws0 *> typ <* ws0
      arg1 <- valueParser <* P.string(", ")
      arg2 <- valueParser
    } yield BinaryOperator(s, ty, arg1, arg2).named(l)


  val slt: P[Predicate] = P.string("slt").map(_ => Predicate.ICMP_SLT)
  val sgt: P[Predicate] = P.string("sgt").map(_ => Predicate.ICMP_SLT)
  val eqt: P[Predicate] = P.string("eq").map(_ => Predicate.ICMP_EQ)
  val sle: P[Predicate] = P.string("slt").map(_ => Predicate.ICMP_SLE)

  val pred: P[Predicate] = slt.backtrack | sgt.backtrack | eqt.backtrack| sle.backtrack

  val icmp: P[Instruction] =
    for {
      l <- local <* ws <* P.char('=') <* ws
      c <- P.string("icmp ") *> pred <* ws
      ty <- typ <* ws
      arg1 <- valueParser <* P.string(", ")
      arg2 <- valueParser
    } yield ICmpInst(c, ty, arg1, arg2).named(l)





  val instruction: P[Instruction] = Seq(
    store, alloca, load, binOp(AddNSW), binOp(SubNSW), binOp(MulNSW), binOp(SREM), call, icmp
  ).map(_.backtrack).reduceLeft(_.orElse(_))

  val instructions: P0[List[Instruction]] = (instruction <* ws0).rep0

  val meta: P[MetaData] = (P.string("!llvm.loop !") *> pos_digits).map(s => LOOP(s.toInt))

  val br: P[Terminator] = for {
    lb <- P.string("br label %") *> digits
    meta <- (P.string(", ") *> meta).?
  } yield Br(lb.toInt, meta)

  val brIf: P[Terminator] = for {
    ty <- P.string("br ") *> typ <* ws
    v <- valueParser <* P.string(", ")
    l1 <- P.string("label %") *> digits <* P.string(", ")
    l2 <- P.string("label %") *> digits
  } yield BrIf(ty, v, l1.toInt, l2.toInt)

  val ret: P[Terminator] =
    for {
      ty <- P.string("ret ") *> ws0 *> typ
      v <- ws0 *> valueParser <* ws0
    } yield Return(ty, v)

  val terminator: P[Terminator] = ret.backtrack | br.backtrack | brIf.backtrack

  val label: P[Label] =
    for {
      nm <- digits <* P.char(':') <* ws <* P.string("; preds = ")
      labels <- (P.char('%') *> pos_digits).repSep0(P.string(", ")) <* ws0
    } yield Label(nm, labels)


  val bblock: P[BasicBlock] =
    for {
      label <- label.?.with1
      insts <- instructions.with1
      term <- terminator <* ws0
    } yield BasicBlock(label, insts, term)

  val functionDef: P[FunctionDef] =
    for {
      head <- functionDefHead <* ws0
      bb <- P.string("{") *> ws0 *> bblock.rep0 <* ws0 <* P.string("}") <* ws0
    } yield FunctionDef(head._1, head._2, head._3, head._4, bb)


  val program: P[Prog] =
    for {
      fdefs <- (functionDef <* ws0).rep
    } yield Prog(fdefs.toList)