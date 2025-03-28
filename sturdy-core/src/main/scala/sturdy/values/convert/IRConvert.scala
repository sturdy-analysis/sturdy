package sturdy.values.convert

import sturdy.ir.{IR, IROperator}

case class IRConvertOp[From, To, Config <: ConvertConfig[_]](conf: Config) extends IROperator

given IRConvert[From, To, Config <: ConvertConfig[_]]: Convert[From, To, IR, IR, Config] with
  override def apply(from: IR, conf: Config): IR = IR.Op(IRConvertOp(conf), from)
