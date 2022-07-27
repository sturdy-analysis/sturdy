package sturdy.effect.callframe

import apron.{Abstract1, Environment, Manager, StringVar, Texpr1Intern, Texpr1Node, Texpr1VarNode, Var as ApronVar}
import org.eclipse.collections.api.factory.BiMaps
import org.eclipse.collections.api.bimap.{BiMap, ImmutableBiMap, MutableBiMap}
import sturdy.data.*
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.ComputationJoiner
import sturdy.values.{Join, Widen}

class ApronCallFrame[Data, Var](apronManager: Manager, initData: Data)
  extends MutableCallFrame[Data, Var, Texpr1Node, WithJoin]:

  private var _data: Data = initData
  private var names: Map[Var, Int] = _

  private var apronEnv: Environment = new Environment()
  private var apronState: Abstract1 = new Abstract1(apronManager, apronEnv)
  /** global var count, currently unbounded */
  private var apronVarCount: Int = 0
  private var boundVars: Map[Int, ApronVar] = Map()

  def allocVars(newVars: Iterable[(Var, Texpr1Node)]): Unit = {
    names = newVars.zipWithIndex.map(t => t._1._1 -> t._2).toMap
    val newApronVars = newVars.map { case (x, _) =>
      val v = new StringVar(s"apronR_${apronVarCount}_$x")
      apronVarCount += 1
      v
    }.toArray[ApronVar]
    boundVars = newApronVars.zipWithIndex.map(_.swap).toMap
    apronEnv = apronEnv.add(Array.empty[ApronVar], newApronVars)
    // create a copy because the fixpoint may reference the old state
    apronState = apronState.changeEnvironmentCopy(apronManager, apronEnv, false)
    for (((_, v), av) <- newVars.zip(newApronVars)) {
      val vIntern = new Texpr1Intern(apronEnv, v)
      apronState.assign(apronManager, av, vIntern, null)
    }
  }

  override def withNew[A](d: Data, vars: Iterable[(Var, Texpr1Node)])(f: => A): A = {
    val snapData = this._data
    val snapNames = this.names
    val snapApronEnv = this.apronEnv
    val snapBoundVars = this.boundVars
    this._data = d
    allocVars(vars)
    try f finally {
      this._data = snapData
      this.names = snapNames
      this.apronEnv = snapApronEnv
      this.boundVars = snapBoundVars
    }
  }

  override def data: Data = _data

  override def getLocal(x: Int): JOptionA[Texpr1Node] = boundVars.get(x) match
    case None => JOptionA.none
    case Some(av) => JOptionA.some(new Texpr1VarNode(av))

  override def getLocalByName(x: Var): JOptionA[Texpr1Node] = names.get(x) match
    case None => JOptionA.none
    case Some(ix) => getLocal(ix)

  override def setLocal(x: Int, v: Texpr1Node): JOptionA[Unit] = boundVars.get(x) match
    case None => JOptionA.none
    case Some(av) =>
      val vIntern = new Texpr1Intern(apronEnv, v)
      apronState.assign(apronManager, av, vIntern, null)
      JOptionA.some(())

  override def setLocalByName(x: Var, v: Texpr1Node): JOptionA[Unit] = names.get(x) match
    case None => JOptionA.none
    case Some(ix) => setLocal(ix, v)

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = ???

  override def getState: State = ???
  override def setState(st: State): Unit = ???

  override def join: Join[State] = ???
  override def widen: Widen[State] = ???