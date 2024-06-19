package sturdy.values.convert

import sturdy.apron.{*,given}

class RelationalCast[From, To, Addr, Type: ApronType, Config <: ConvertConfig[_]]
  (using convertType: Convert[From, To, Type, Type, Config])
  extends Convert[From, To, ApronExpr[Addr, Type], ApronExpr[Addr, Type], Config]:
  def apply(from: ApronExpr[Addr, Type], conf: Config): ApronExpr[Addr, Type] =
    val toType = convertType(from._type, conf)
    ApronExpr.cast(from, toType.roundingType, toType.roundingDir, toType)
