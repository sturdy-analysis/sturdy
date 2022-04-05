package sturdy.language.wasm.wasmbench

import java.net.URI

case class WASMBenchBinary(md: Metadata, ex: List[FuncDef])

trait Store:
  var wbbs: Map[String, WASMBenchBinary]

//  def load(): List[WASMBenchBinary]
  def retrieve(predicate: WASMBenchBinary => Boolean): List[WASMBenchBinary]
  def retrieve(keys: List[String]): List[WASMBenchBinary]
  def retrieve(key: String): Option[WASMBenchBinary]

  def store(data: List[WASMBenchBinary]): Unit
  def store(data: WASMBenchBinary): Unit
