package sturdy.language.jimple


import sturdy.util.Labeled
import com.sun.source.tree.IdentifierTree

import scala.collection.Seq


type Label = Identifier
type Identifier = String
type SizedDims = Seq[Immediate]
type EmptyDims = Seq[Nothing] //TODO geht das?  (empty_dims = "[]" empty_dims | ;)
type Immediate = Constant | Local

enum RVal:
  case ArrRef(i1: Immediate, i2: Immediate)
  case Const(c: Constant) //TODO: Ist das sinnvoll?
  case Expression(e: Exp)
  case InstFieldRef(i: Immediate, f: FieldSignature)
  case Loc(l: Local)
  case StaticFieldRef(f: FieldSignature)
//TODO: case NextNextStmtAddr

enum Var:
  case ArrRef(i1: Immediate, i2: Immediate)
  case InstFieldRef(i: Immediate, f: FieldSignature)
  case StaticFieldRef(f: FieldSignature)
  case Loc(l: Local)



enum Stmt extends Labeled:
  case BreakpointS()
  case AssignS(v: Var, newVal: RVal)//variable = rvalue
  case IdentityS(l: Local, i: IdentityVal) //identity_value
  case EnterMonitorS(i: Immediate)//"entermonitor" immediate
  case ExitMonitorS(i: Immediate)//"exitmonitor" immediate
  case GotoS(l: Label)//"goto" label
  case IfS(c: Cond, l: Label)//"if" condition "then" label
  case InvokeS(i: Exp.InvokeE)
  case LookupSwitchS(i: Immediate, cs: Seq[Case], l: Label)//"lookupswitch(" immediate "){" cases "default: goto" label "}"
  case NopS()
  case RetS(l: Local)//"ret" local
  case ReturnS(i: Immediate)//"ret" Immediate
  case ReturnVoidS()
  case TableSwitchS(i: Immediate, c: Seq[Case], l: Label)//"tableswitch(" immediate "){" cases "default: goto" label "}"
  case ThrowS(i: Immediate)//"throw" immediate

enum Exp extends Labeled:
  case BinopE(i1: Immediate, i2: Immediate, op: BinOps | Cond)
  case CastE(t: Type, i: Immediate)
  case InstOfE(i: Immediate, ref: Type.RefT)
  case StaticInvokeE(s: MethodSignature, l: Seq[Immediate])
  case InvokeE(t: InvokeType, i: Immediate, s: MethodSignature, l: Seq[Immediate])
  case newArrE(t: Type, i: Immediate)
  case NewE(r: Type.RefT)
  case NewMultArrE(t: Type, dim: SizedDims, edim: EmptyDims )//"new multiarray" type sized_dims empty_dims
  case UnopE(i: Immediate, op: Unops)

enum BinOps:
  case Add
  case And
  case Cmp //TODO: What are these?
  case Cmpg//TODO: What are these?
  case Cmpl//TODO: What are these?
  case Div
  case Mul
  case Or
  case Rem
  case Shl
  case Shr
  case Sub
  case Ushr //TODO: What are these?
  case Xor

enum Cond:
  case Eq
  case Ge
  case Le
  case Ne
  case Gt
  case Lt

enum Unops:
  case Length(i: Immediate)
  case Neg(i: Immediate)

enum InvokeType:
  case InterfaceI
  case SpecialI
  case VirtualI

enum Constant:
  case DoubleCon(v:Double)
  case FloatC(v: Float)
  case IntC(v: Int)
  case LongC(v: Long)
  case StringC(v: String)
  case NullC()

enum IdentityVal:
  case CaughtExcRef()
  case ParamRef(c: Constant.IntC)
  case ThisRef()

enum Type:
  case IntT()
  case LongT()
  case FloatT()
  case DoubleT()
  case RefT()
  case StmtAddT()
  case VoidT()


case class LocalDec(t: Type, name: String)

case class Case(i: Constant.IntC, l: Label)

case class MethodSignature(id: Identifier, params: Seq[Type], ret: Type)

case class FieldSignature(id: Identifier, t: Type)

case class ExceptionRange(ref: Type.RefT, start: Label, end: Label, cBlock: Label)

case class Method(locals: Seq[LocalDec], idStmts: Seq[Stmt.IdentityS], stmts: Seq[Stmt], excRanges: Seq[ExceptionRange])

case class Program(funs: Seq[String]):
  def fold[A]: Seq[Char => ((Char, Char) => Char) => Char] =
    funs.map(_.fold)


case class Local(id: Identifier)

/*case class ArrRef(i1: Immediate, i2: Immediate)
case class InstFieldRef(i: Immediate, f: FieldSignature)
case class StaticFieldRef(f: FieldSignature)*/