package sturdy.values.convert

import sturdy.ir.{IR, IROperator}
import sturdy.values.config
import sturdy.values.config.{Bits, BytesSize}

import java.nio.ByteOrder
import scala.annotation.unused

trait TypeTag[T](val repr: String):
  override def toString: String = repr
  override def equals(obj: Any): Boolean = obj match
    case that: TypeTag[T] => this.repr == that.repr
  override def hashCode(): Int = repr.hashCode

given TypeTag[Int] = new TypeTag[Int]("Int") {}
given TypeTag[Long] = new TypeTag[Long]("Long") {}
given TypeTag[Float] = new TypeTag[Float]("Float") {}
given TypeTag[Double] = new TypeTag[Double]("Double") {}
given TypeTag[Seq[Byte]] = new TypeTag[Seq[Byte]]("Seq[Byte]") {}

class IRConvertOp[From, To, Config <: ConvertConfig[_]](val conf: Config)(using val from: TypeTag[From], val to: TypeTag[To]) extends IROperator:
  override def toString: String = s"Convert($from, $to, $conf)"
  override def equals(obj: Any): Boolean = obj match
    case that: IRConvertOp[From, To, Config] =>
      (this.from, this.to, this.conf) == (that.from, that.to, that.conf)
    case _ => false
  override def hashCode(): Int = (this.from, this.to, this,conf).hashCode()

def IRConvertBytesInt(conf: config.BytesSize && SomeCC[ByteOrder] && config.Bits): IRConvertOp[Seq[Byte], Int, BytesSize && SomeCC[ByteOrder] && Bits] =
  new IRConvertOp[Seq[Byte], Int, config.BytesSize && SomeCC[ByteOrder] && config.Bits](conf)

given IRConvert[From, To, Config <: ConvertConfig[_]](using ft: TypeTag[From], tt: TypeTag[To]): Convert[From, To, IR, IR, Config] with
  override def apply(from: IR, conf: Config): IR = IR.Op(new IRConvertOp(conf)(using ft, tt), from)
