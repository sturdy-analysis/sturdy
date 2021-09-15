package sturdy.language.wasm.abstractions

import sturdy.language.wasm.Interpreter
import sturdy.values.Singleton
import sturdy.values.Topped

trait SingletonValues extends Interpreter:
  type I32 = Singleton[Int]
  type I64 = Singleton[Long]
  type F32 = Singleton[Float]
  type F64 = Singleton[Double]
  type Bool = Topped[Boolean]

  def topI32: I32 = Singleton.NoSingleton
  def topI64: I64 = Singleton.NoSingleton
  def topF32: F32 = Singleton.NoSingleton
  def topF64: F64 = Singleton.NoSingleton
  def asBoolean(v: Value): Bool = v.asInt32 match
    case Singleton.NoSingleton => Topped.Top
    case Singleton.Single(i) => Topped.Actual(i != 0)
  def boolean(b: Bool): Value = b match
    case Topped.Top => Value.Int32(topI32)
    case Topped.Actual(true) => Value.Int32(Singleton.Single(1))
    case Topped.Actual(false) => Value.Int32(Singleton.Single(0))

  