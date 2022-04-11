package sturdy.language.wasm.wasmbench

import java.net.URI

trait Store[K,V]:
  var wbbs: Map[K, V]

//  def load(): List[V]
  def retrieve(predicate: V => Boolean): List[V]
  def retrieve(keys: List[K]): List[V]
  def retrieve(key: K): Option[V]

  def store(data: List[V]): Unit
  def store(data: V): Unit
