package sturdy.values.convert

class TransitiveConvert[From, Via, To, VFrom, VVia, VTo, Config1 <: ConvertConfig[_], Config2 <: ConvertConfig[_]]
  (using convert1: Convert[From, Via, VFrom, VVia, Config1], convert2: Convert[Via, To, VVia, VTo, Config2])
  extends Convert[From, To, VFrom, VTo, Config1 && Config2]:

  override def apply(from: VFrom, conf: Config1 && Config2): VTo =
    convert2(convert1(from, conf.c1), conf.c2)

