package sturdy.language.wasm.generic

import swam.*

trait ModuleInstance:
  val functionTypes: Vector[FuncType]
  val functionAddrs: Vector[Int]
  val tableAddrs: Vector[Int]
  val memoryAddrs: Vector[Int]
  val globalAddrs: Vector[Int]
  val elementAddrs: Vector[Int]
  val dataAddrs: Vector[Int]
  val exports: Vector[(String, ExternalValue)]

enum ExternalValue:
  case Function(addr: Int)
  case Table(addr: Int)
  case Memory(addr: Int)
  case Global(addr: Int)