object LLVM:
  case class Prog(fdef: Seq[FunctionDef]):
    override def toString: String =
      fdef.mkString("\n")

  case class FunctionDef(rt: Type, name: Global, params: Seq[Param], attr: Int, body: Seq[BasicBlock]):
    override def toString: String =
      s"""define $rt $name(${params.mkString(", ")}) #$attr {
         |${body.mkString("\n\n")}
         |}
         |""".stripMargin

  case class Param(t: Type, name: Local):
    override def toString: String = s"$t $name"

  case class Label(name: String, pred: Seq[String]):
    override def toString: String =
      s"$name:                  ; preds = ${pred.map("%"++_).mkString(", ")}"

  case class BasicBlock(label: Option[Label], insts: Seq[Instruction], term: Terminator):
    override def toString: String = {
      val labS = if (label.isEmpty) "" else s"${label.get}\n"
      labS + insts.map(i => s"  $i\n").mkString + "  " + term
    }

  sealed trait Name:
    def name: String
    override def toString: String = name
  case class Local(name: String) extends Name:
    if (!name.startsWith("%"))
      throw new IllegalArgumentException(s"Local name must start with '%'")
  case class Global(name: String) extends Name:
    if (!name.startsWith("@"))
      throw new IllegalArgumentException(s"Global name must start with '@'")

  /**
   * https://llvm.org/doxygen/classllvm_1_1Value.html#details
   * This is a very important LLVM class. It is the base class of all values computed by a program that may be used as
   * operands to other values.
   */
  enum Value:
    case Void
    case Ref(name: Name)
    case Int(v: scala.Int, ty: Type)
    case Float(v: scala.Double, ty: Type)

    override def toString: String = this match
      case Void => "void"
      case Ref(name) => name.toString
      case Int(v, _) => v.toString
      case Float(v, _) => v.toString

  enum Type:
    case Int(bits: scala.Int)
    case Half
    case Float
    case Double
    case Fp128
    case X86Fp80
    case PpcFp128

    override def toString: String = this match
      case Int(bits) => s"i$bits"
      case Half => "half"
      case Float => "float"
      case Double => "double"
      case Fp128 => "fp128"
      case X86Fp80 => "x86_fp80"
      case PpcFp128 => "ppc_fp128"

  val i1: Type = Type.Int(1)
  val i32: Type = Type.Int(32)

  trait Targetable {
    var targetRegister: Option[Name] = None
    def named(name: Name): this.type = {
      this.targetRegister = Some(name)
      this
    }
    def prefixed(s: Any): String = targetRegister match {
      case None => s.toString
      case Some(name) => s"$name = $s"
    }
    def yieldsVoid: Boolean = false
  }

  /**
   * https://llvm.org/doxygen/classllvm_1_1Instruction.html
   */
  enum Instruction extends Targetable:
    case Alloca(ty: Type, numElements: Int = 1, align: Int = 4)
    case Store(ty: Type, v: Value, name: Name, align: Int = 4)
    case Load(ty:Type, name: Name, align: Int = 4)
    case BinaryOperator(op: BinOp, ty: Type, v1: Value, v2: Value)
    case ICmpInst(pred: Predicate, ty: Type, v1: Value, v2: Value)
    case FCmpInst(pred: Predicate, v1: Value, v2: Value)
    case Terminating(term: Terminator)
    case Call(ty: Type, nm: Name, params: Seq[Param])

    override def yieldsVoid: Boolean = this match
      case Store(_, _, _, _) | Terminating(_) => true
      case _ => false

    override def toString: String = this match
      case Alloca(ty, numElements, align) =>
        if (numElements == 1)
          prefixed(s"alloca $ty, align $align")
        else
          prefixed(s"alloca [$numElements x $ty], align $align")
      case Store(ty, v, name, align) => prefixed(s"store $ty $v, ptr $name, align $align")
      case Load(ty, name, align) => prefixed(s"load $ty, ptr $name, align $align")
      case BinaryOperator(op, ty, v1, v2) => prefixed(s"$op $ty $v1, $v2")
      case ICmpInst(pred, ty, v1, v2) => prefixed(s"icmp $pred $ty $v1, $v2")
      case FCmpInst(pred, v1, v2) => prefixed(s"fcmp $pred $v1, $v2")
      case Call(ty, nm, params) => prefixed(s"call $ty $nm(${params.mkString(", ")})")
      case Terminating(term) => prefixed(term.toString)

  /**
   * Instructions that terminate a basic block
   */
  enum MetaData:
    case LOOP(n:Int)

    override def toString: String = this match
      case LOOP(n) => s"!llvm.loop !$n"
  enum Terminator:
    case Br(label: Int, meta: Option[MetaData]=None)
    case BrIf(ty: Type, v: Value, thnLabel: Int, elsLabel: Int)
    case Return(ty: Type, v: Value)
    case ReturnVoid
    case Dummy

    override def toString: String = this match
      case Br(label, None) => s"br label %$label"
      case Br(label, Some(m)) => s"br label %$label, $m"
      case BrIf(ty, v, thnLabel, elsLabel) => s"br $ty $v, label %$thnLabel, label %$elsLabel"
      case Return(ty, v) => s"ret $ty $v"
      case ReturnVoid => "ret void"
      case Dummy => "dummy"

  enum BinOp:
    case AddNSW
    case SubNSW
    case MulNSW
    case SREM

    override def toString: String = this match
      case AddNSW => "add nsw"
      case SubNSW => "sub nsw"
      case MulNSW => "mul nsw"
      case SREM => "srem"


  enum Predicate:
    case ICMP_EQ
    case ICMP_SLT
    case ICMP_SGT
    case ICMP_SLE
    case FCMP_OLT
    case FCMP_OLE
    // ...

    override def toString: String = this match
      case ICMP_EQ => "eq"
      case ICMP_SLT => "slt"
      case ICMP_SGT => "sgt"
      case ICMP_SLE => "sle"
      case FCMP_OLT => "olt"
      case FCMP_OLE => "ole"
