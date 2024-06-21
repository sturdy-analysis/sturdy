package sturdy.values.convert

import sturdy.apron.{*,given}

//given RelationalCast[From, To, Addr, Type: ApronType, Config <: ConvertConfig[_]]
//  (using convertType: Convert[From, To, Type, Type, Config]):
//  Convert[From, To, ApronExpr[Addr, Type], ApronExpr[Addr, Type], Config] with
//  def apply(from: ApronExpr[Addr, Type], conf: Config): ApronExpr[Addr, Type] =
//    val toType = convertType(from._type, conf)
//    ApronExpr.cast(from, toType.roundingType, toType.roundingDir, toType)
