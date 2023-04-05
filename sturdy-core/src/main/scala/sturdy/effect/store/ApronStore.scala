//package sturdy.effect.store
//
//import apron.{Abstract1, Environment, Texpr1Intern, Var}
//import sturdy.data.{JOption, JOptionA, MayJoin, WithJoin}
//import sturdy.effect.{ComputationJoiner, TrySturdy}
//import sturdy.values.{Combine, Join, MaybeChanged, Widen, Widening}
//import sturdy.values.integer.ApronValue
//
//import scala.collection.mutable.ListBuffer
//import scala.reflect.ClassTag
//
//class ApronStore(manager: apron.Manager) extends Store[apron.Var, ApronValue, MayJoin.WithJoin]:
//
//  protected var environment: apron.Environment = apron.Environment()
//  protected var domain: apron.Abstract1 = apron.Abstract1(manager, environment)
//  def getDomain(): Abstract1 = domain
//
//  override def read(x: Var): JOption[MayJoin.WithJoin, ApronValue] =
//    if(environment.hasVar(x))
//      JOptionA.some(apron.Texpr1VarNode(x))
//    else
//      JOptionA.none
//
//  override def write(x: apron.Var, v: ApronValue): Unit =
//    if(!environment.hasVar(x)) {
//      environment = environment.add(Array(x), Array.empty[Var])
//      domain.changeEnvironment(manager, environment, true)
//    }
//    val newDomain = domain.assignCopy(manager, x, new Texpr1Intern(environment, v),null)
//    domain.join(manager, newDomain)
//
//  override def free(x: Var): Unit =
//    environment.remove(Array(x))
//    domain.changeEnvironment(manager, environment, false)
//
//  override type State = (Environment,Abstract1)
//  override def getState: (Environment,Abstract1) = (environment.clone(),domain)
//
//  override def setState(st: (Environment,Abstract1)): Unit =
//    environment = st._1
//    domain = st._2
//
//  override def join: Join[(Environment,Abstract1)] = sturdy.data.JoinTuple2(using ApronEnvCombine, ApronAbstractJoin(manager))
//
//  override def widen: Widen[(Environment,Abstract1)] = sturdy.data.JoinTuple2(using ApronEnvCombine, ApronAbstractWiden(manager))
//
//  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ApronStoreJoiner)
//
//  private class ApronStoreJoiner[A] extends ComputationJoiner[A] {
//    private val snapshotEnvironment = environment.clone()
//    private val snapshotDomain = domain
//    private var firstEnvironment: Environment = _
//    private var firstDomain: Abstract1 = _
//
//    override def inbetween(): Unit =
//      firstEnvironment = environment
//      firstDomain = domain
//      environment = snapshotEnvironment
//      domain = snapshotDomain
//
//    override def retainNone(): Unit =
//      environment = snapshotEnvironment
//      domain = snapshotDomain
//
//    override def retainFirst(fRes: TrySturdy[A]): Unit =
//      environment = firstEnvironment
//      domain = firstDomain
//
//    override def retainSecond(gRes: TrySturdy[A]): Unit = {}
//
//    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
//      val (newEnv,newDomain) = join((firstEnvironment,firstDomain),(environment,domain)).get
//      environment = newEnv
//      domain = newDomain
//  }
//
//given ApronEnvCombine[W <: Widening]: Combine[Environment, W] with
//  override def apply(v1: Environment, v2: Environment): MaybeChanged[Environment] =
//    val newEnv = new Environment(v1.getIntVars, v1.getRealVars)
//    newEnv.add(v2.getIntVars.filter(! newEnv.hasVar(_)), v2.getRealVars.filter(! newEnv.hasVar(_)))
//    MaybeChanged(newEnv, newEnv.getSize > v1.getSize || newEnv.getSize > v2.getSize)
//
//final class ApronAbstractJoin(manager: apron.Manager) extends Join[apron.Abstract1]:
//  override def apply(v1: Abstract1, v2: Abstract1): MaybeChanged[Abstract1] =
//    val joined = v1.joinCopy(manager, v2)
//    MaybeChanged(joined, joined.isEqual(manager, v1) && joined.isEqual(manager, v2))
//
//final class ApronAbstractWiden(manager: apron.Manager) extends Widen[Abstract1]:
//  override def apply(v1: Abstract1, v2: Abstract1): MaybeChanged[Abstract1] =
//    val joined = v1.joinCopy(manager, v2)
//    MaybeChanged(joined, joined.isEqual(manager, v1) && joined.isEqual(manager, v2))
