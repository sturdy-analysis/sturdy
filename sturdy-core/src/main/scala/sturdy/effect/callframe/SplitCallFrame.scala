package sturdy.effect.callframe

import sturdy.{IsSound, Soundness, data, seqIsSound}
import sturdy.data.MayJoin.NoJoin
import sturdy.data.{CombineTuple2, JOption, JOptionC, MayJoin, noJoin}
import sturdy.values.{Join, Widen}

class SplitCallFrame[Data, Var, V, Site]
    (val base: DecidableCallFrame[Data, Var, V, Site] with MutableCallFrame[Data, Var, V, Site, NoJoin],
     val special: DecidableCallFrame[Data, Var, V, Site] with MutableCallFrame[Data, Var, V, Site, NoJoin],
     useSpecial : (Var, V, Site) => Boolean
    ) extends DecidableCallFrame[Data, Var, V, Site] with MutableCallFrame[Data, Var, V, Site, NoJoin]:

  protected var delegatedToSpecial: Vector[Boolean] = Vector.empty
  protected var names: Map[Var, Int] = Map.empty
  protected var namesRev: Vector[Var] = Vector.empty
  protected var _callSite: Site = _

  override def data: Data = base.data
  def getFrameNames: Map[Var, Int] = names
  def callSite: Site = _callSite

  override def getLocal(x: Int): JOption[NoJoin, V] =
    if (delegatedToSpecial.indices.contains(x)) {
      if (delegatedToSpecial(x))
        special.getLocal(x)
      else
        base.getLocal(x)
    } else {
      JOptionC.none
    }

  override def getLocalByName(x: Var): JOption[NoJoin, V] =
    names.get(x).map(getLocal).getOrElse(JOptionC.none)

  override def setLocal(x: Int, v: V): JOption[NoJoin, Unit] =
    if (delegatedToSpecial.indices.contains(x)) {
      if (delegatedToSpecial(x)) {
        special.setLocal(x, v)
      } else if (useSpecial(namesRev(x), v, callSite)) {
        delegatedToSpecial = delegatedToSpecial.updated(x, true)
        special.setLocal(x, v)
      } else {
        base.setLocal(x, v)
      }
    } else {
      JOptionC.none
    }

  override def setLocalByName(x: Var, v: V): JOption[NoJoin, Unit] =
    names.get(x).map(setLocal(_, v)).getOrElse(JOptionC.none)

  def setVars(newVars: Iterable[(Var, Option[V])], site: Site): Unit =
    val newVarsNames = newVars.view.map(_._1)
    delegatedToSpecial = newVars.map(p => p._2.nonEmpty && useSpecial(p._1, p._2.get, site)).toVector
    names = newVarsNames.zipWithIndex.toMap
    namesRev = newVarsNames.toVector
    _callSite = site

  override def withNew[A](d: Data, vars: Iterable[(Var, Option[V])], site: Site)(f: => A): A =
    val wasDelegatedToSpecial = delegatedToSpecial
    val wasNames = names
    val wasNamesRev = namesRev
    val wasCallSite = _callSite
    setVars(vars, site)
    val baseVars = vars.map {
      case (name, None) => (name, None)
      case (name, Some(v)) if useSpecial(name, v, site) => (name, None)
      case (name, Some(v)) => (name, Some(v))
    }
    val specialVars = vars.map {
      case (name, None) => (name, None)
      case (name, Some(v)) if !useSpecial(name, v, site) => (name, None)
      case (name, Some(v)) => (name, Some(v))
    }
    try base.withNew(d, baseVars, site) {
      special.withNew(d, specialVars, site) {
        f
      }
    } finally {
      delegatedToSpecial = wasDelegatedToSpecial
      names = wasNames
      namesRev = wasNamesRev
      _callSite = wasCallSite
    }

  override type State = (base.State, special.State)
  override def getState: (base.State, special.State) = (base.getState, special.getState)
  override def setState(st: (base.State, special.State)): Unit =
    base.setState(st._1)
    special.setState(st._2)

  override def toString: String = s"SplitCallFrame(${base.getState}, ${special.getState})"

  override def join: Join[(base.State, special.State)] = CombineTuple2(using base.join, special.join)
  override def widen: Widen[(base.State, special.State)] = CombineTuple2(using base.widen, special.widen)

  def isSound[cData, cV](c: ConcreteCallFrame[cData, Var, cV, Site])(using vSoundness: Soundness[cV, V], dSoundness: Soundness[cData, Data]): IsSound =
    val dataIsSound = dSoundness.isSound(c.data, data)
    if (dataIsSound.isNotSound)
      return dataIsSound
    if (getFrameNames != c.getFrameNames)
      return IsSound.NotSound(s"Variable names in call frame differ: concrete=${c.getFrameNames}, abstract=$getFrameNames")
    val aVals = c.getVars.indices.map(getLocal.andThen(_.get))
    val cVals = c.getVars.toList
    seqIsSound.isSound(cVals, aVals)
