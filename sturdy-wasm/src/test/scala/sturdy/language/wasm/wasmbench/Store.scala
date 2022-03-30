package sturdy.language.wasm.wasmbench

import java.net.URI

trait Store:
  
  var md: Map[String, Metadata]

  def load(): List[Metadata]
  def load(keys: List[String]): List[Metadata]
  def load(key: String): Option[Metadata]

  def store(data: List[Metadata]): Unit
  def store(data: Metadata): Unit
