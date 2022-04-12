package sturdy.language.wasm.wasmbench

import java.net.URI

trait Store[K,V]:
  var wbbs: Map[K, V]

//  def load(): List[V]
  def retrieve(predicate: V => Boolean): List[V] = wbbs.values.filter(predicate).toList
  def retrieve(keys: List[K]): List[V] =
    keys.foldLeft(List.empty[V])((acc, el) => wbbs.get(el) match
      case Some(m) => m :: acc
      case None => acc)
  def retrieve(key: K): Option[V] = wbbs.get(key)

  def store(data: List[V]): Unit
  def store(data: V): Unit
