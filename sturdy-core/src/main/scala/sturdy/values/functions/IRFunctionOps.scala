package sturdy.values.functions

import sturdy.ir.{IR, IROperator}

enum IRFunctionOperator extends IROperator:
  case CALL[F,A,R](invoke: (F, A) => R)

  override def toString: String = this match
    case IRFunctionOperator.CALL(invoke) => "CALL"


class IRFunctionOps[F, A](irArgs: A => Seq[IR]) extends FunctionOps[F, A, IR, IR]:
  import IRFunctionOperator.*
  override def funValue(fun: F): IR = IR.Const(fun)
  override def invokeFun(fun: IR, a: A)(invoke: (F, A) => IR): IR = fun match
    case IR.Const(f: F) => invoke(f, a)
    case _ => IR.Op(CALL(invoke), fun +: irArgs(a))

given IRFunctionOpsIRArgs[F] : IRFunctionOps[F, Seq[IR]](identity) with {}