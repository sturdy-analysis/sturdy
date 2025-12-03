package sturdy.language.tip_xdai.record

import sturdy.values.{Finite, Structural}


case class Field(name: String)
given Finite[Field] with {}
given Structural[Field] with {}