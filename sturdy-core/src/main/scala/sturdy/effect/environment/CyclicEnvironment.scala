package sturdy.effect.environment

import sturdy.data.MayJoin

import scala.compiletime.uninitialized

sealed trait Box[V]:
  def get: V
  def map[U](f : V => U): Box[U]

object Box:

  case class Eager[V](v: V) extends Box[V]:
    override def get: V = v
    override def map[U](f: V => U): Box[U] = Eager(f(v))

  def Lazy[V](comp: () => V): Lazy[V] = new Lazy(comp)

  class Lazy[V](comp: () => V) extends Box[V]:
    private var cached: V = uninitialized
    private var isCached: Boolean = false
    private var isRec: Boolean  = false

    override def get: V =
      if (isRec && isCached)
        cached
      else {
        isCached = true
        try {isRec = true; cached = comp()}
        finally isRec = false
        cached
      }

    override def map[U](f: V => U): Box[U] = Lazy(() => f(get))

    override def toString: String =
      if (!isCached)
        "Lazy(???)"
      else
      if (isRec)
        s"CircularRef"
      else {

        try {isRec = true; s"Lazy($cached)"}
        finally isRec = false
      }


trait CyclicEnvironment[Var, V, J[_] <: MayJoin[_]] extends Environment[Var, V, J]:
  def bindLazy(x: Var, v: => V): Unit