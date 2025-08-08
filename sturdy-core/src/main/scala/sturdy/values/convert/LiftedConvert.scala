package sturdy.values.convert

final class LiftedConvert[From, To, VFrom, VTo, UFrom, UTo, Config <: ConvertConfig[_]]
  (extract: VFrom => UFrom, inject: UTo => VTo)
  (using c: Convert[From, To, UFrom, UTo, Config])
  extends Convert[From, To, VFrom, VTo, Config]:

  inline override def apply(from: VFrom, conf: Config): VTo = inject(c(extract(from), conf))
