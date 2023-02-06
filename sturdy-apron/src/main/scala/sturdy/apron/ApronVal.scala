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

    def isEqual(that: Val, scope: ApronScope): Boolean = (this,that) match
      case (null, null) => true
      case (Val.Int(v1), Val.Int(v2)) => v1.isEqual(v2, scope)
      case (Val.Double(v1), Val.Double(v2)) => v1.isEqual(v2, scope)
      case (Val.Other(v1), Val.Other(v2)) => v1 == v2
      case _ => false

    def hashCode(scope: ApronScope): scala.Int = this match
      case Val.Int(v) => v.hashCode(scope)
      case Val.Double(v) => v.hashCode(scope)
      case Val.Other(v) => v.hashCode
      case null => -1

  object Val:
    def getVar(v: Val): Option[ApronVar] = v match
      case Val.Int(av) => Some(av)
      case Val.Double(av) => Some(av)
      case _ => None
