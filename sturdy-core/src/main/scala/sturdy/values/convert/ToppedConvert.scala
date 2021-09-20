package sturdy.values.convert

import sturdy.values.Topped

given ToppedConvert[From, To, VFrom, VTo, Config](using c: Convert[From, To, VFrom, VTo, Config]): Convert[From, To, Topped[VFrom], Topped[VTo], Config] with
  def apply(from: Topped[VFrom], conf: Config): Topped[VTo] =
    from.map(v => c(v, conf))
