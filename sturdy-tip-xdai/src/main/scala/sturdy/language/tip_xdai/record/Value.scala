package sturdy.language.tip_xdai.record

import sturdy.values.Finite


case class Field(name: String)
given Finite[Field] with {}