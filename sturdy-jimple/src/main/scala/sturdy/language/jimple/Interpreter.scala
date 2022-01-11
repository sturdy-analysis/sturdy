package sturdy.language.jimple

trait Interpreter:
  //type MayJoin[A]
  type VIntC
  type VDoubleC
  type VFloatC
  type VLongC
  type VStringC
  type VNullC
  type VIntT
  type VDoubleT
  type VFloatT
  type VLongT
  type VRefT
  type VVoidT
  type VExcRef
  type VParamRef
  type VThisRef

  enum Value:
    case TopValue
    case IntConstValue(i: VIntC)
    case DoubleConstValue(d: VDoubleC)
    case FloatConstValue(f: VFloatC)
    case LongConstValue(l: VLongC)
    case StringConstValue(s: VStringC)
    case NullConstValue(n: VNullC)
    case IntTypeValue(t: VIntT)
    case DoubleTypeValue(t: VDoubleT)
    case FloatTypeValue(t: VFloatT)
    case LongTypeValue(t: VLongT)
    case RefTypeValue(t: VRefT)
    case VoidTypeValue(t: VVoidT)
    case ExcRefValue(exc: VExcRef)
    case ParamRefValue(param: VParamRef)
    case ThisRefValue(th: VThisRef)


