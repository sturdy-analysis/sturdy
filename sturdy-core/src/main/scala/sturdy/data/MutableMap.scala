package sturdy.data

import org.eclipse.collections.api.map.MutableMap

object MutableMap {
  extension[K, V] (m: MutableMap[K, V])
    inline def updateWith(k: K, default: => V, f: V => V): Unit =
      m.compute(k, (_, value) => if (value == null) f(default) else f(value))
}
