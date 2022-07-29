package sturdy.effect.callframe

import apron.{Environment, StringVar, Texpr1VarNode, Texpr1Node, Texpr1Intern, Manager, Abstract1, Tcons1, Interval, Var as ApronVar}
import org.eclipse.collections.api.factory.BiMaps
import org.eclipse.collections.api.bimap.{ImmutableBiMap, MutableBiMap, BiMap}
import sturdy.apron.Apron
import sturdy.data.*
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.ComputationJoiner
import sturdy.effect.SturdyFailure
import sturdy.effect.TrySturdy
import sturdy.values.MaybeChanged
import sturdy.values.{Widen, Join}

class ApronCallFrame[Data, Var](apronManager: Manager, initData: Data, initVars: Iterable[(Var, Texpr1Node)] = Iterable.empty)
  extends MutableCallFrame[Data, Var, Texpr1Node, NoJoin] with DecidableCallFrame[Data, Var, Texpr1Node] with Apron(apronManager):

  private var _data: Data = initData
  private var names: Map[Var, Int] = Map()

  private var boundVars: Map[Int, Either[ApronVar] = Map()

  private def allocVars(newVars: Iterable[(Var, Texpr1Node)]): Unit = {
    names = newVars.zipWithIndex.map(t => t._1._1 -> t._2).toMap
    val newApronVars = newVars.map { case (x, _) =>
      val v = new StringVar(s"apronI_${apronVarCount}_$x")
      apronVarCount += 1
      v
    }.toArray[ApronVar]
    boundVars = newApronVars.zipWithIndex.map(_.swap).toMap
    apronEnv = apronEnv.add(newApronVars, Array.empty[ApronVar])
    apronState.changeEnvironment(apronManager, apronEnv, false)
    for (((_, v), av) <- newVars.zip(newApronVars)) {
      val vIntern = new Texpr1Intern(apronEnv, v)
      apronState.assign(apronManager, av, vIntern, null)
    }
  }

  allocVars(initVars)

  override def withNew[A](d: Data, vars: Iterable[(Var, Texpr1Node)])(f: => A): A = {
    val snapData = this._data
    val snapNames = this.names
    val snapBoundVars = this.boundVars
    this._data = d
    allocVars(vars)
    try f finally {
      this._data = snapData
      this.names = snapNames
      this.boundVars = snapBoundVars
    }
  }

  override def data: Data = _data

  override def getLocal(x: Int): JOptionC[Texpr1Node] = boundVars.get(x) match
    case None => JOptionC.none
    case Some(av) => JOptionC.some(new Texpr1VarNode(av))

  override def getLocalByName(x: Var): JOptionC[Texpr1Node] = names.get(x) match
    case None => JOptionC.none
    case Some(ix) => getLocal(ix)

  override def setLocal(x: Int, v: Texpr1Node): JOptionC[Unit] = boundVars.get(x) match
    case None => JOptionC.none
    case Some(av) =>
      val vIntern = new Texpr1Intern(apronEnv, v)
      apronState.assign(apronManager, av, vIntern, null)
      JOptionC.some(())

  override def setLocalByName(x: Var, v: Texpr1Node): JOptionC[Unit] = names.get(x) match
    case None => JOptionC.none
    case Some(ix) => setLocal(ix, v)

  type State = Abstract1
  /** state contains the constraints for the current frame only */
  override def getState: State = {
    val frame = new Environment(boundVars.values.toArray, Array.empty[ApronVar])
    // only retain bound vars
    apronState.changeEnvironmentCopy(apronManager, frame, true)
  }
  override def setState(st: State): Unit =
    // set bound vars to [-inf, +inf]
    apronState.forget(apronManager, boundVars.values.toArray, false)
    // meet bound vars with st
    apronState.meet(apronManager, st)

  override def join: Join[State] = (s1, s2) => {
    val joined = s1.joinCopy(apronManager, s2)
    MaybeChanged(joined, s1.isEqual(apronManager, joined))
  }
  override def widen: Widen[State] = (s1, s2) => {
    val joined = s1.widening(apronManager, s2)
    MaybeChanged(joined, s1.isEqual(apronManager, joined))
  }

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ComputationJoiner[A] {
    private val snapshot = new Abstract1(apronManager, apronState)
    private var fState: Abstract1 = _

    override def inbetween(): Unit =
      fState = apronState
      apronState = snapshot

    override def retainNone(): Unit =
      apronState = snapshot

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      apronState = fState

    override def retainSecond(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      fState.join(apronManager, apronState)
      apronState = fState
  })
