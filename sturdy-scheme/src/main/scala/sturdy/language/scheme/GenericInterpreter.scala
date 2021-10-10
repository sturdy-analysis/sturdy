//package sturdy.language.scheme
//
//import sturdy.data.unit
//import sturdy.effect.allocation.Allocation
//import sturdy.effect.branching.BoolBranching
//import sturdy.effect.environment.CEnvironment
//import sturdy.effect.failure.{Failure, FailureKind}
//import sturdy.effect.store.Store
//import sturdy.fix
//import Op1Kinds.*
//import Op2Kinds.*
//import OpVarKinds.*
//import sturdy.util.Label
//import sturdy.values.config
//import sturdy.values.ints.*
//import sturdy.values.rationals.*
//import sturdy.values.doubles.*
//import sturdy.values.booleans.BooleanOps
//import sturdy.values.closures.ClosureOps
//import sturdy.values.relational.{EqOps, CompareOps}
//
//object GenericInterpreter:
//  type GenericEffects[V, Addr] =
//    BoolBranching[V] with
//    CEnvironment[String, Addr] with
//    Store[Addr, V] with
//    Allocation[Addr, AllocationSite] with
//    Failure
//
//  enum AllocationSite:
//    case LetBinding(let: Exp, x: String)
//    case Toplevel(d: Define)
//    case ClosureParam(body: Body, x: String)
//
//  case object UnboundVariable extends FailureKind
//  case object UnboundAddr extends FailureKind
//  case object UserError extends FailureKind
//  case object TypeError extends FailureKind
//  case object NullDeconstruct extends FailureKind
//  case object ClosureComparison extends FailureKind
//
//  type GenericPhi[V] = fix.Combinator[Exp, V]
//
//trait TypeOps[V]:
//  def isNumber(v: V): V
//  def isInteger(v: V): V
//  def isDouble(v: V): V
//  def isRational(v: V): V
//  def isNull(v: V): V
//  def isCons(v: V): V
//  def isBoolean(v: V): V
//
//trait ListOps[V]:
//  def cons(v1: V, v2: V): V
//  def nil: V
//  def cdr(v: V): V
//  def car(v: V): V
//
//trait VoidOps[V]:
//  def void: V
//
//trait SymbolOps[V]:
//  def symbolLit(s: String): V
//
//trait QuoteOps[V]:
//  def quoteLit(v: V): V
//
//trait StringOps[V]:
//  def stringLit(s: String): V
//  def numberToString(v: V): V
//  def stringToSymbol(v: V): V
//  def symbolToString(v: V): V
//  def stringRef(v1: V, v2: V): V
//  def stringAppend(v1: V, v2: V): V
//
//trait CharOps[V]:
//  def charLit(c: Char): V
//
//import GenericInterpreter.*
//
//trait GenericInterpreter[V, Addr, Effects <: GenericEffects[V, Addr]]
//  (using val effectOps: Effects)
//  (using val intOps: IntOps[V], intDouble: ConvertIntDouble[V, V],
//             rationalOps: RationalOps[V], rationalDouble: ConvertRationalDouble[V, V],
//             doubleOps: DoubleOps[V],
//             boolOps: BooleanOps[V], charOps: CharOps[V], stringOps: StringOps[V],
//             listOps: ListOps[V], symbolOps: SymbolOps[V], quoteOps: QuoteOps[V], voidOps: VoidOps[V],
//             typeOps: TypeOps[V],
//             eqOps: EqOps[V, V], compareOps: CompareOps[V, V],
//             closureOps: ClosureOps[String, V, Body, effectOps.Env, V, V])
//  (using effectOps.StoreJoin[V], effectOps.StoreJoin[Unit], effectOps.BoolBranchJoin[V]):
//
//  import intOps.intLit
//  import rationalOps.rationalLit
//  import doubleOps.doubleLit
//  import boolOps._
//  import eqOps._
//  import charOps._
//  import stringOps._
//  import symbolOps._
//  import quoteOps._
//  import effectOps._
//  import voidOps._
//  import closureOps._
//  import compareOps._
//  import typeOps._
//
//  val phi: GenericPhi[V]
//
//  private lazy val fixed = fix.Fixpoint { (rec: Exp => V) =>
//    def eval(e: Exp): V = rec(e)
//
//    def eval_open(e: Exp): V = { e match
//      case Exp.Lit(l) => lit(l)
//      case Exp.Nil_ => listOps.nil
//      case Exp.Cons_(e1, e2) => listOps.cons(eval(e1), eval(e2))
//      case Exp.Begin(es) => es match
//        case Nil => void
//        case _ =>
//          es.init.foreach(eval)
//          eval(es.last)
//      case Exp.Apply(e1, args) =>
//        val clsVal = eval(e1)
//        val argVals = args.map(arg => eval(arg))
//        invokeClosure(clsVal, argVals) { applyClosure }
//      case Exp.Var(x) =>
//        val addr = lookup(x).getOrElse(fail(UnboundVariable, x))
//        read(addr).getOrElse(fail(UnboundAddr, s"$addr for variable $x"))
//      case Exp.Lam(names, body) =>
//        val env = closeEnvironment
//        closureValue(names, body, env)
//      case let@Exp.Let(bnds, body) =>
//        scoped {
//          val addrs = bnds.map(xe => alloc(AllocationSite.LetBinding(let, xe._1)))
//          bnds.zip(addrs).foreach { case ((x,e), addr) =>
//            val v = eval(e)
//            bind(x, addr)
//            write(addr, v)
//          }
//          try runBody(body)
//          finally addrs.foreach(free)
//        }
//      case let@Exp.LetRec(bnds, body, star) =>
//        val (envbnds, storebnds) = bnds.map { case (x,e) =>
//          val addr = alloc(AllocationSite.LetBinding(let, x))
//          ((x,addr),(addr,e))
//        }.unzip
//        scoped {
//          envbnds.foreach(bind)
//          if (star)
//            storebnds.foreach { case (addr, e) =>
//              val v = eval(e)
//              write(addr, v)
//            }
//          else {
//            val vals = storebnds.map(ae => eval(ae._2))
//            storebnds.zip(vals).foreach { case ((addr, _), v) => write(addr, v) }
//          }
//          try runBody(body)
//          finally storebnds.foreach(b => free(b._1))
//        }
//      case s@Exp.Set_(x, e) =>
//        val addr = lookup(x).getOrElse(fail(UnboundVariable, x))
//        write(addr, eval(e))
//        void
//      case Exp.If(e1, e2, e3) => boolBranch(eval(e1), eval(e2), eval(e3))
//      case Exp.Op1(op, e) => op1(op, eval(e))
//      case Exp.Op2(op, e1, e2) => op2(op, eval(e1), eval(e2))
//      case Exp.OpVar(op, es) =>
//        val vs = es.map(e => eval(e))
//        opVar(op, vs)
//      case Exp.Error(err) => fail(UserError, err)
//    }
//
//    def runBody(body: Body): V =
//      if (body.defs.isEmpty) {
//        body.exps.init.foreach(eval)
//        eval(body.exps.last)
//      } else {
//        val letrec = Exp.LetRec(
//          body.defs.map(d => d.name -> d.e),
//          Body(List(), body.exps),
//          star = true
//        )
//        eval(letrec)
//      }
//
//    def lit(literal: Literal): V = { literal match
//      case Literal.IntLit(i) => intLit(i)
//      case Literal.DoubleLit(d) => doubleLit(d)
//      case Literal.RationalLit(i1, i2) => rationalLit(i1,i2)
//      case Literal.BoolLit(b) => boolLit(b)
//      case Literal.CharLit(c) => charLit(c)
//      case Literal.StringLit(str) => stringLit(str)
//      case Literal.SymbolLit(sym) => symbolLit(sym)
//      case Literal.QuoteLit(qot) => quoteLit(eval(Exp.Lit(qot)))
//    }
//
//    def op1(op: Op1Kinds, v: V): V = { op match
//      case IsNumber => isNumber(v)
//      case IsInteger => isInteger(v)
//      case IsDouble => isDouble(v)
//      case IsRational => isRational(v)
//      case IsZero => numTypeDispatch(v)(equ(v, intLit(0)), equ(v, rationalLit(0, 1)), equ(v, doubleLit(0)), fail(TypeError, s"Cannot compute zero? on $v"))
//      case IsPositive => numTypeDispatch(v)(gt(v, intLit(0)), gt(v, rationalLit(0, 1)), gt(v, doubleLit(0)), fail(TypeError, s"Cannot compute positive? on $v"))
//      case IsNegative => numTypeDispatch(v)(lt(v, intLit(0)), lt(v, rationalLit(0, 1)), lt(v, doubleLit(0)), fail(TypeError, s"Cannot compute negative? on $v"))
//      case IsOdd => equ(intOps.remainder(v, intLit(2)), intLit(1))
//      case IsEven => equ(intOps.remainder(v, intLit(2)), intLit(0))
//      case IsNull => isNull(v)
//      case IsCons => isCons(v)
//      case IsBoolean => isBoolean(v)
//
//      case Abs => numTypeDispatch(v)(intOps.absolute(v), rationalOps.absolute(v), doubleOps.absolute(v), fail(TypeError, s"Cannot compute abs on $v"))
//      case Floor => numTypeDispatch(v)(v, rationalOps.floor(v), doubleOps.floor(v), fail(TypeError, s"Cannot compute floor on $v"))
//      case Ceiling => numTypeDispatch(v)(v, rationalOps.ceil(v), doubleOps.ceil(v), fail(TypeError, s"Cannot compute ceiling on $v"))
//      case Log =>
//        val doubleV = numTypeDispatch(v)(intDouble(v, config.Bits.Signed), rationalDouble(v, ()), v, fail(TypeError, s"Cannot compute log on $v"))
//        doubleOps.logNatural(doubleV)
//
//      case Not => not(v)
//
//      case Car => listOps.car(v)
//      case Cdr => listOps.cdr(v)
//      case Caar => listOps.car(listOps.car(v))
//      case Cadr => listOps.car(listOps.cdr(v))
//      case Cddr => listOps.cdr(listOps.cdr(v))
//      case Caddr => listOps.car(listOps.cdr(listOps.cdr(v)))
//      case Cadddr => listOps.car(listOps.cdr(listOps.cdr(listOps.cdr(v))))
//
//      case NumberToString => numberToString(v)
//      case StringToSymbol => stringToSymbol(v)
//      case SymbolToString => symbolToString(v)
//
//      case Random => ???
//    }
//
//    def op2(op: Op2Kinds, v1: V, v2: V): V = { op match
//      case Eqv => eqOps.equ(v1,v2)
//      case Quotient => intOps.div(v1,v2)
//      case Remainder => intOps.remainder(v1,v2)
//      case Modulo => intOps.modulo(v1,v2)
//      case StringRef => stringRef(v1,v2)
//    }
//
//    def opVar(op: OpVarKinds, vs: List[V]): V = { op match
//      case Equal => vs.init.zip(vs.tail).map(equ).reduce(and)
//      case Smaller => vs.init.zip(vs.tail).map(lt).reduce(and)
//      case Greater => vs.init.zip(vs.tail).map(gt).reduce(and)
//      case SmallerEqual => vs.init.zip(vs.tail).map(ge).reduce(and)
//      case GreaterEqual => vs.init.zip(vs.tail).map(le).reduce(and)
//      case Max => vs.reduce { case (x1, x2) => withNum2(x1,x2)(intOps.max)(rationalOps.max)(doubleOps.max) }
//      case Min => vs.reduce { case (x1, x2) => withNum2(x1,x2)(intOps.min)(rationalOps.min)(doubleOps.min) }
//      case Add => vs.reduce { case (x1, x2) => withNum2(x1,x2)(intOps.add)(rationalOps.add)(doubleOps.add) }
//      case Mul => vs.reduce { case (x1, x2) => withNum2(x1,x2)(intOps.mul)(rationalOps.mul)(doubleOps.mul) }
//      case Sub =>
//        if (vs.size == 1) {
//          val v = vs.head
//          numTypeDispatch(v)(intOps.sub(intLit(0), v), rationalOps.sub(rationalLit(0, 1), v), doubleOps.sub(doubleLit(0), v), fail(TypeError, s"Cannot compute (- $v)"))
//        } else {
//          vs.reduce { case (x1, x2) => withNum2(x1,x2)(intOps.sub)(rationalOps.sub)(doubleOps.sub) }
//        }
//      case Div =>
//        if (vs.size == 1) {
//          val v = vs.head
//          numTypeDispatch(v)(intOps.div(intLit(1), v), rationalOps.div(rationalLit(1, 1), v), doubleOps.div(doubleLit(1), v), fail(TypeError, s"Cannot compute (/ $v)"))
//        } else {
//          vs.reduce { case (x1, x2) => withNum2(x1, x2)(intOps.div)(rationalOps.div)(doubleOps.div) }
//        }
//      case Gcd => vs.reduce(intOps.gcd)
//      case Lcm => vs.reduce(intOps.lcm)
//      case StringAppend => vs.reduce(stringAppend)
//    }
//
//    def applyClosure(vars: List[String], body: Body, args: List[V], env: Env): V = {
//      scoped {
//        loadClosedEnvironment(env)
//        // TODO compare size of vars and args\
//        val addrs = vars.map(x => alloc(AllocationSite.ClosureParam(body, x)))
//        vars.zip(addrs).zip(args).foreach { case ((x, addr), arg) =>
//          bind(x, addr)
//          write(addr, arg)
//        }
//        try runBody(body)
//        finally addrs.foreach(free)
//      }
//    }
//
//    phi { eval_open }
//  }
//
//  def numTypeDispatch(v: V)(int: => V, rational: => V, double: => V, other: => V): V =
//    boolBranch(isInteger(v), int,
//      boolBranch(isRational(v), rational,
//        boolBranch(isDouble(v), double,
//          other
//        )
//      )
//    )
//
//  def withNum2(v1: V, v2: V)(opInt: (V, V) => V)(opRatio: (V, V) => V)(opDouble: (V, V) => V): V =
//    numTypeDispatch(v1)(
//      numTypeDispatch(v2)(
//        opInt(v1, v2),
//        opRatio(v1, v2),
//        opDouble(v1, v2),
//        fail(TypeError, s"(withNum2):expected num as second argument but got $v2")
//      ),
//      numTypeDispatch(v2)(
//        opRatio(v1, v2),
//        opRatio(v1, v2),
//        opDouble(v1, v2),
//        fail(TypeError, s"(withNum2):expected num as second argument but got $v2")
//      ),
//      numTypeDispatch(v2)(
//        opDouble(v1, v2),
//        opDouble(v1, v2),
//        opDouble(v1, v2),
//        fail(TypeError, s"(withNum2):expected num as second argument but got $v2")
//      ),
//      fail(TypeError, s"(withNum2):expected num as first argument but got $v1")
//    )
//
//  def eval(e: Exp): V = fixed(e)
//
//  def runForm(f: Form): V = f match
//    case Form.Expression(e) => eval(e)
//    case Form.Definition(d) =>
//      val addr = alloc(AllocationSite.Toplevel(d))
//      bind(d.name, addr)
//      write(addr, eval(d.e))
//      void
//    case Form.Begin(fs) =>
//      fs.init.foreach(runForm)
//      fs.lastOption match {
//        case None => void
//        case Some(f) => runForm(f)
//      }
//
//  def execute(p: Program): V = runForm(Form.Begin(p.forms))
//
