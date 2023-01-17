package sturdy.apron

class ApronVal[V](val apron: Apron,
                  makeIntVal: ApronExpr => V,
                  makeDoubleVal: ApronExpr => V):
  import apron.alloc
  enum Val:
    case Int(v: ApronVar)
    case Double(v: ApronVar)
    case Other(v: V)

    def asV: V = this match
      case Int(v) => makeIntVal(ApronExpr.Var(v))
      case Double(v) => makeDoubleVal(ApronExpr.Var(v))
      case Other(v) => v
      case null => null.asInstanceOf[V]

  object Val:
    def getVar(v: Val): Option[ApronVar] = v match
      case Val.Int(av) => Some(av)
      case Val.Double(av) => Some(av)
      case _ => None
