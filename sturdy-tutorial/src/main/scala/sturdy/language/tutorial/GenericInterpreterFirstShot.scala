package sturdy.language.tutorial

import sturdy.data.{JOption, JOptionC, MayJoin}
import MayJoin.*
import sturdy.effect.Effectful
import sturdy.effect.{EffectStack, SturdyFailure}
import sturdy.effect.failure.FailureKind

/*
 * We now want to use sturdy to write abstract interpreters for the while language. To this end we need to
 *   - write a generic interpreter that is parametric in the representation of values (e.g., numeric operations) and
 *     effects (e.g., the store)
 *   - provide concrete and abstract instances for values and effects
 *   - instantiate the generic interpreter with the concrete instances to get a concrete interpreter
 *   - instantiate the generic interpreter with abstract instances to get an abstract interpreter
 *
 * We will use the Sign domain (Neg, Zero, Pos) as abstract domain for our abstract interpreter.
 * For illustration purpose we implement all component interfaces and instances from scratch. When using Sturdy to
 * build a real analysis we can reuse the existing components from the Sturdy library.
 */

/*
 * Interface for numeric operations of the while language. Type parameter V abstracts over the value
 * representation of the language. In our concrete interpreter V will be instantiated to Int. And in the
 * abstract interpreter we will instantiate V with values of the sign domain.
 */
trait NumericOps[V]:
  def lit(i: Int): V
  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  def div(v1: V, v2: V): V
  def lt(v1: V, v2: V): V

/*
 * We cannot in general decide the conditions of if and while statements in the generic interpreter. Therefore we need
 * an interface to abstract over decisions. Take for example the small program
 *
 * y := 1;
 * if (y < 2)
 *   x := 0;
 * else
 *   x := 1;
 *
 * In the sign domain we evaluate the condition "y < 2" in a context where y is set to "Pos". So essentially, the condition
 * becomes "Pos < Pos" in the abstract, which we cannot decide.
 * Hence, the abstract interpreter needs to evaluate both branches and join the results. Joining of results includes
 * also the joining of effect states. For example, the then branch sets x to 0 (Zero) and the else branch to 1 (Pos).
 * Hence, after evaluating the if statement the value of x needs to be (Zero join Pos) = Top in order for the analysis
 * to be sound.
 * The Branching interface abstracts over decisions.
 */
trait Branching[V,R]:
  def branch(v: V, thn: => R, els: => R): R

/*
 * The store interface abstracts over a store mapping names of type String to values of type V.
 * Since a read may fail (variable is not yet initialized) the operation returns an option.
 * The JOption type is Sturdy's special option type abstracting over the option representations.
 * For example, a concrete option always knows if a read fails or not and thus has the two possibilities None and Some.
 * In contrast, an abstract interpreter may not be sure if a read fails or not and thus additionally has a third
 * possibility NoneSome. The type parameter J configures the joining behaviour of the option type.
 */
trait Store[V, J[_] <: MayJoin[_]] extends Effectful:
  def read(name: String): JOption[J, V]
  def write(name: String, v: V): Unit

/* The interface for failures */
trait Failure extends Effectful:
  def fail(kind: FailureKind, msg: String): Nothing

case object DivisionByZero extends FailureKind
case object UninitializedVariable extends FailureKind


/*
 * With these interface at hand we can now implement the generic interpreter. This is only the first try without
 * using a fixpoint computation. We will later refine the generic interpreter.
 */
trait GenericInterpreterFirstShot[V, J[_] <: MayJoin[_]]:
  // value components - we require a NumericOps component and a Branching component
  val numericOps: NumericOps[V]
  val branching: Branching[V,Unit]

  // effect components - we require a store and a failure component
  val store: Store[V, J]
  val failure: Failure

  // joining of computations requires all effect components to participate in the join.
  // We achieve this by using an effect stack containing all effect components of the language.
  final val effectStack: EffectStack = new EffectStack(List(store, failure))
  given EffectStack = effectStack
  given Failure = failure

  // we require joining of values of type V
  implicit def jv: J[V]

  import numericOps.*
  import branching.*
  import failure.*
  import Exp.*
  import Stm.*

  // The evaluation of expressions
  // In most cases we simply use the numericOps component.
  // For evaluating variabes we look them up in the store and call "fail" in case the variable in not initialized.
  def eval(e: Exp): V = e match
    case NumLit(n) => lit(n)
    case Var(name) => store.read(name).getOrElse(fail(UninitializedVariable, s"uninitialized variable $name"))
    case Add(e1, e2) => add(eval(e1), eval(e2))
    case Sub(e1, e2) => sub(eval(e1), eval(e2))
    case Mul(e1, e2) => mul(eval(e1), eval(e2))
    case Div(e1, e2) => div(eval(e1), eval(e2))
    case Lt(e1, e2) => lt(eval(e1), eval(e2))

  // The evaluation of statements. The implementation is analog to the reference interpreter apart from
  // using the Branching component to encode the decisions for if and while statements.
  def run(s: Stm): Unit = s match
    case Assign(name, e) =>
      val eVal = eval(e)
      store.write(name, eVal)
    case If(cond, thn, els) =>
      val condVal = eval(cond)
      branch(condVal, run(thn), els.map(run).getOrElse(()))
    case w@While(cond, body) =>
      val condVal = eval(cond)
      branch(condVal, {run(body); run(w)}, ())
    case Block(body) => body.foreach(run)

  def runProg(arg: V, s: Stm): V =
    store.write("arg", arg)
    run(s)
    store.read("result").getOrElse(fail(UninitializedVariable, s"uninitialized variable result"))
    

