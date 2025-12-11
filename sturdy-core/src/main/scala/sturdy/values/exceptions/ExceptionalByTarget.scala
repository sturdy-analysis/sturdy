package sturdy.values.exceptions

import sturdy.data.{WithJoin, mapJoin}
import sturdy.effect.EffectStack
import sturdy.util.Lazy
import sturdy.values.{Combine, Join, MaybeChanged, Widen}

class ExceptionalByTarget[Exc, Trg, V](lazyEffectStack: Lazy[EffectStack])(byTarget: Exc => (Trg, V), asExc: (Trg, V) => Exc)
  extends Exceptional[Exc, Map[Trg, (V,Any)], WithJoin]:

  override def exception(exc: Exc): Map[Trg, (V,Any)] = {
    val (target,value) = byTarget(exc)
    Map(target -> (value, lazyEffectStack.value.getState))
  }

  override def handle[A](e: Map[Trg, (V,Any)])(f: Exc => A): WithJoin[A] ?=> A = {
    val effectStack = lazyEffectStack.value
    mapJoin[(Exc,Any), A](e.map{ case (target,(value,state)) => (asExc(target,value),state)}, (exc,state) =>
      effectStack.setState(state)
      f(exc)
    )
  }
