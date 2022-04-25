package sturdy.language.jimple


import sturdy.util.Labeled
import com.sun.source.tree.IdentifierTree

import scala.collection.Seq
import cats.data.NonEmptyList


type Label = Identifier
type Identifier = String
type SizedDims = Seq[Option[Immediate]]
type EmptyDims = Int  //(empty_dims = "[]" empty_dims | ;)

// String.class

enum Immediate:
  case ConstI(c: Constant)
  case LocalI(l: Local)
  case ClassI(s: String)

  override def toString: String = this match
    case ConstI(c) => s"ConstI($c)"
    case LocalI(l) => s"LocalI($l)"
    case ClassI(n) => s"ClassI($n)"

enum RVal:
  case ArrayRefR(i1: Immediate, i2: Immediate)
  case ConstR(c: Constant)
  case ExpressionR(e: Exp)
  case InstanceFieldRefR(i: Immediate, f: FieldSignature)
  case StaticFieldRefR(f: FieldSignature)
  case LocalR(l: Local)
  case ClassR(s: String)
//TODO: case NextNextStmtAddr

  override def toString: String = this match
    case ArrayRefR(i1, i2) => s"ArrayRefR($i1, $i2)"
    case ConstR(c) => s"ConstR($c)"
    case ExpressionR(e) => s"ExpressionR($e)"
    case InstanceFieldRefR(i, f) => s"InstanceFieldRefR($i, $f)"
    case StaticFieldRefR(f) => s"StaticFieldRefR($f)"
    case LocalR(l) => s"LocalR($l)"
    case ClassR(n) => s"ClassR($n)"

enum Var:
  case ArrayRefV(i1: Immediate, i2: Immediate)
  case InstanceFieldRefV(i: Immediate, f: FieldSignature)
  case StaticFieldRefV(f: FieldSignature)
  case LocalV(l: Local)

  override def toString: String = this match
    case ArrayRefV(i1, i2) => s"ArrayRefV($i1, $i2)"
    case InstanceFieldRefV(i, f) => s"InstanceFieldRefV($i, $f)"
    case StaticFieldRefV(f) => s"StaticFieldRefV($f)"
    case LocalV(l) => s"LocalV($l)"

enum Stmt extends Labeled:
  case BreakpointS
  case AssignS(v: Var, newVal: RVal)//variable = rvalue
  case IdentityS(l: Local, i: IdentityVal) //identity_value
  case ExceptionIdentityS(l: Local, i: IdentityVal)
  case EnterMonitorS(i: Immediate)//"entermonitor" immediate
  case ExitMonitorS(i: Immediate)//"exitmonitor" immediate
  case GotoS(l: Label)//"goto" label
  case IfS(c: Exp.ConditionE, l: Label)//"if" condition "then" label
  case InvokeS(e: Exp.InvokeE | Exp.StaticInvokeE)
  case LookupSwitchS(i: Immediate, cs: Seq[Case], l: Label)//"lookupswitch(" immediate "){" cases "default: goto" label "}"
  case NopS
  case RetS(l: Local)//"ret" local
  case ReturnS(i: Immediate)//"ret" Immediate
  case ReturnVoidS
  case TableSwitchS(i: Immediate, cs: Seq[Case], l: Label)//"tableswitch(" immediate "){" cases "default: goto" label "}"
  case ThrowS(i: Immediate)//"throw" immediate
  case LabelS(l: Label)
  case CatchS(e: ExceptionRange)

  override def toString: String = this match
    case BreakpointS => s"BreakpointS@${this.label}"
    case AssignS(v, newVal) => s"AssignS($v, $newVal)@${this.label}"
    case IdentityS(l, i) => s"IdentityS($l, $i)@${this.label}"
    case ExceptionIdentityS(l, i) => s"ExceptionIdentityS($l, $i)@${this.label}"
    case EnterMonitorS(i) => s"EnterMonitorS($i)@${this.label}"
    case ExitMonitorS(i) => s"ExitMonitorS($i)@${this.label}"
    case GotoS(l) => s"GotoS($l)@${this.label}"
    case IfS(c, l) => s"IfS($c, $l)@${this.label}"
    case InvokeS(e) => s"InvokeS($e)@${this.label}"
    case LookupSwitchS(i, cs, l) => s"LookupSwitchS($i, $cs, $l)@${this.label}"
    case NopS => s"NopS@${this.label}"
    case RetS(l) => s"RetS($l)@${this.label}"
    case ReturnS(i) => s"ReturnS($i)@${this.label}"
    case ReturnVoidS => s"ReturnVoidS@${this.label}"
    case TableSwitchS(i, cs, l) => s"TableswitchS@${this.label}"
    case ThrowS(i) => s"ThrowS@${this.label}"
    case LabelS(l) => s"LabelS($l)@${this.label}"
    case CatchS(e) => s"CatcHS($e)@${this.label}"

enum Exp extends Labeled:
  case BinopE(i1: Immediate, i2: Immediate, op: BinOp)
  case ConditionE(i1: Immediate, i2: Immediate, op: CondOp)
  case CastE(t: Type, i: Immediate)
  case InstanceOfE(i: Immediate, ref: Type.RefT)
  case StaticInvokeE(s: MethodSignature, l: Seq[Immediate])
  case InvokeE(t: InvokeType, i: Immediate, s: MethodSignature, l: Seq[Immediate])
  case NewArrayE(t: Type, i: Immediate)
  case NewE(t: Type.RefT)
  case NewMultArrE(t: Type, dims: SizedDims)//"new multiarray" type sized_dims empty_dims
  case UnopE(i: Immediate, op: UnOp)

  override def toString: String = this match
    case BinopE(i1, i2, op) => s"BinOpE($i1, $i2, $op)@${this.label}"
    case ConditionE(i1, i2, op) => s"ConditionE($i1, $i2, $op)@${this.label}"
    case CastE(t, i) => s"CastE($t, $i)@${this.label}"
    case InstanceOfE(i, ref) => s"InstanceOfE($i, $ref)@${this.label}"
    case StaticInvokeE(s, l) => s"StaticInvokeE($s, $l)@${this.label}"
    case InvokeE(t, i, s, l) => s"InvokeE($t, $i, $s, $l)@${this.label}"
    case NewArrayE(t, i) => s"NewArrayE($t, $i)@${this.label}"
    case NewE(t) => s"NewE($t)@${this.label}"
    case NewMultArrE(t, dim) => s"NewMultArrayE($t, $dim)@${this.label}"
    case UnopE(i, op) => s"UnOpE($i, $op)@${this.label}"


enum BinOp:
  case Add
  case And
  case Cmp //turns long comparison into int result, so it can be compared
  case Cmpg //turns floating point comparison into int result, so it can be compared
  case Cmpl //turns floating point comparison into int result, so it can be compared
  case Div
  case Mul
  case Or
  case Rem
  case Shl
  case Shr
  case Sub
  case Ushr
  case Xor

  override def toString: String = this match
    case Add => s"Add"
    case And => s"And"
    case Cmp => s"Cmp" 
    case Cmpg => s"Cmpg" 
    case Cmpl => s"Cmpl"
    case Div => s"Div"
    case Mul => s"Mul"
    case Or => s"Or"
    case Rem => s"Rem"
    case Shl => s"Shl"
    case Shr => s"Shr"
    case Sub => s"Sub"
    case Ushr => s"Ushr"
    case Xor => s"Xor"

enum CondOp: //does not compare floating point or long values
  case Eq
  case Ge
  case Le
  case Ne
  case Gt
  case Lt

  override def toString: String = this match
    case Eq => s"Eq"
    case Ge => s"Ge"
    case Le => s"Le"
    case Ne => s"Ne"
    case Gt => s"Gt"
    case Lt => s"Lt"

enum UnOp:
  case Length
  case Neg
  case NegWord

  override def toString: String = this match
    case Length => s"Length"
    case Neg => s"Neg"
    case NegWord => s"NegWord"

enum InvokeType:
  case InterfaceI
  case SpecialI //TODO: only invokes <init>
  case VirtualI

  override def toString: String = this match
    case InterfaceI => s"InterfaceI"
    case SpecialI => s"SpecialI"
    case VirtualI => s"VirtualI"

enum Constant:
  case DoubleC(v: Double)
  case FloatC(v: Float)
  case IntC(v: Int)
  case LongC(v: Long)
  case StringC(v: String)
  case NullC
  case InfinityC
  case NegInfinityC
  case NanC
  case FloatInfinityC
  case FloatNegInfinityC
  case FloatNanC

  override def toString: String = this match
    case DoubleC(v) => s"DoubleC($v)"
    case FloatC(v) => s"FloatC($v)"
    case IntC(v) => s"IntC($v)"
    case LongC(v) => s"LongC($v)"
    case StringC(v) => s"StringC($v)"
    case NullC => s"NullC"
    case InfinityC => s"InfinityC"
    case NegInfinityC => s"NegInfinityC("
    case NanC => s"NanC"
    case FloatInfinityC => s"FloatInfinityC"
    case FloatNegInfinityC => s"FloatNegInfinityC"
    case FloatNanC => s"FloatNanC"


enum IdentityVal:
  case CaughtExcRef()
  case ParamRef(c: Constant.IntC, t: Type)
  case ThisRef(t: Type)

  override def toString: String = this match
    case CaughtExcRef() => s"CaughtExcRef()"
    case ParamRef(c, t) => s"ParamRef($c, $t)"
    case ThisRef(t) => s"ThisRef($t)"

enum Type:
  case IntT
  case LongT
  case FloatT
  case DoubleT
  case RefT(s: String)
  //case StmtAddressT
  case VoidT

  override def toString: String = this match
    case IntT => s"IntT"
    case LongT => s"LongT"
    case FloatT => s"FloatT"
    case DoubleT => s"DoubleT"
    case RefT(s) => s"RefT($s)"
   // case StmtAddressT => s"StmtAddressT"
    case VoidT => s"VoidT"



case class LocalDec(t: Type, name: Identifier)

case class Case(i: Constant.IntC, l: Label)

case class MethodSignature(id: Identifier, params: Seq[Type], ret: Type, classOrigin: String)

case class FieldSignature(id: Identifier, t: Type, classOrigin: String)

case class ExceptionRange(ref: Type.RefT, start: Label, end: Label, catchBlock: Label)

case class Method(header: MethodHeader, locals: Seq[LocalDec], idStmts: Seq[Stmt.IdentityS], stmts: Seq[Stmt], excRanges: Seq[ExceptionRange])

case class MethodHeader(isPublic: Boolean, isPrivate: Boolean, isProtected: Boolean, isStatic: Boolean, isFinal: Boolean, isStrict: Boolean, isTransient: Boolean, isSynchronized: Boolean, isVolatile: Boolean, isAbstract: Boolean, ret: Type, id: Identifier, params: Seq[Type], throws: Seq[Type.RefT]):
  def getID: Identifier =
    this match
      case MethodHeader(_, _, _, _, _, _, _, _, _, _, _, id, _, _) => id

case class Program(funs: Seq[Container])//:
//  def fold[A]: Seq[Char => ((Char, Char) => Char) => Char] =
//    funs.map(_.fold)

case class Local(id: Identifier)

enum Container:
  case ClassC(isPublic: Boolean, isPrivate: Boolean, isAbstract: Boolean, isStatic: Boolean, isFinal: Boolean, isEnum: Boolean, id: Identifier, extend: Option[Type.RefT], implement: Seq[Type.RefT], body: Seq[ClassBodyElement])
  case InterfaceC(isPublic: Boolean, isPrivate: Boolean, id: Identifier, extend: Option[Type.RefT], implement: Seq[Type.RefT], body: Seq[ClassBodyElement])

  override def toString: String = this match
    case ClassC(isPublic,isPrivate,isAbstract,isStatic,isFinal,isEnum,id,extend,implement,body) => s"ClassC($isPublic,$isPrivate,$isAbstract,$isStatic,$isFinal,$isEnum,$id,$extend,$implement,$body)"
    case InterfaceC(isPublic, isPrivate, id, extend, implement, body) => s"InterfaceC($isPublic,$isPrivate,$id,$extend,$implement,$body)"

enum ClassBodyElement:
  case GlobalVarCB(isPublic: Boolean, isPrivate: Boolean, isProtected: Boolean, isStatic: Boolean, isFinal: Boolean, isEnum: Boolean, isTransient: Boolean, isVolatile: Boolean, t: Type, id: Identifier)
  case NativeCallCB(isPublic: Boolean, isPrivate: Boolean, isProtected: Boolean, isStatic: Boolean, isFinal: Boolean, isSynchronized: Boolean, isNative: Boolean, t: Type, id: Identifier, params: Seq[Type], except: Option[Type.RefT])
  case MethodCB(header: MethodHeader, locals: Seq[LocalDec], idStmts: Seq[Stmt.IdentityS], stmts: Seq[Stmt], excRanges: Seq[ExceptionRange])
  case MethodHeaderCB(header: MethodHeader)

  def getID: Identifier =
    this match
      case ClassBodyElement.GlobalVarCB(_, _, _, _, _, _, _, _, _, id) => id
      case ClassBodyElement.NativeCallCB(_, _, _, _, _, _, _, _, id, _, _) => id
      case ClassBodyElement.MethodCB(header, _, _, _, _) => header.getID
      case ClassBodyElement.MethodHeaderCB(header) => header.getID
  
  override def toString: String = this match
    case GlobalVarCB(isPublic, isPrivate, isProtected, isStatic, isFinal, isEnum, isTransient, isVolatile, t, id) => s"GlobalVarCB($isPublic,$isPrivate,$isProtected,$isStatic,$isFinal,$isEnum,$isTransient,$isVolatile,$t,$id)"
    case NativeCallCB(isPublic, isPrivate, isProtected, isStatic, isFinal, isSynchronized, isNative, t, id, params, except) => s"NativeCallCB($isPublic,$isPrivate,$isProtected,$isStatic,$isFinal,$isSynchronized,$isNative,$t,$id,$params,$except)"
    case MethodCB(header, locals, idStmts, stmts, excRanges) => s"MethodCB($header,$locals,$idStmts,$stmts,$excRanges)"
    case MethodHeaderCB(header) => s"MethodHeaderCB($header)"