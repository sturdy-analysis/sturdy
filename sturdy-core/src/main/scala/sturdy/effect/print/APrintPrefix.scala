package sturdy.effect.print

import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.Effectful
import sturdy.values.Finite
import sturdy.values.Join

import scala.collection.mutable.ListBuffer

object APrintPrefix:
  enum PrintResult[A]:
    case Definite(as: Vector[A])
    case OneOf(rs: Set[PrintResult[A]])
    case Concat(p1: PrintResult[A], p2: PrintResult[A])

    def join(that: PrintResult[A]): PrintResult[A] = (this, that) match
      case _ if this.isEmpty => this
      case _ if that.isEmpty => that
      case (PrintResult.OneOf(rs1), PrintResult.OneOf(rs2)) => PrintResult.OneOf(rs1 ++ rs2)
      case (PrintResult.OneOf(rs1), _) => PrintResult.OneOf(rs1 + that)
      case (_, PrintResult.OneOf(rs2)) => PrintResult.OneOf(rs2 + this)
      case (r1: PrintResult.Definite[A], r2: PrintResult.Definite[A]) if r1.as == r2.as => r1
      case _ => PrintResult.OneOf(Set(this, that))

    def :+(a: A): PrintResult[A] = this match
      case Definite(as) => Definite(as :+ a)
      case Concat(p1, p2) => Concat(p1, p2 :+ a)
      case OneOf(_) => Concat(this, Definite(Vector(a)))

    def ++(that: PrintResult[A]): PrintResult[A] = (this, that) match
      case (Definite(as1), Definite(as2)) => Definite(as1 ++ as2)
      case (Definite(as1), OneOf(alts2)) => OneOf(alts2.map(this ++ _))
      case (OneOf(alts1), Definite(as2)) => OneOf(alts1.map(_ ++ that))
      case _ => Concat(this, that)

    def size: Int = this match
      case PrintResult.Definite(as) => as.size
      case PrintResult.OneOf(rs) => rs.map(_.size).sum
      case PrintResult.Concat(p1, p2) => p1.size + p2.size

    def isEmpty: Boolean = this match
      case PrintResult.Definite(as) => as.isEmpty
      case PrintResult.OneOf(rs) => rs.exists(_.isEmpty)
      case PrintResult.Concat(p1, p2) => p1.isEmpty && p2.isEmpty

    def matchOne[C](c: C, rest: PrintResult[A])(using s: Soundness[C, A]): MatchResult[C, A] = this match
      case Definite(Vector()) =>
        if (rest.isEmpty)
          MatchResult.PrefixMatched()
        else
          rest.matchOne(c, Definite(Vector()))
      case Definite(as) =>
        val a = as.head
        if (s.isSound(c, a).isSound)
          MatchResult.Partial(Definite(as.tail) ++ rest)
        else
          MatchResult.Mismatch(List(c), this ++ rest)
      case OneOf(alts) =>
        val matchedAlts = alts.flatMap(p => p.matchOne(c, rest) match
          case MatchResult.PrefixMatched() => return MatchResult.PrefixMatched()
          case MatchResult.Mismatch(_, _) => None
          case MatchResult.Partial(rest) => Some(rest)
        )
        if (matchedAlts.isEmpty)
          MatchResult.Mismatch(List(c), this ++ rest)
        else if (matchedAlts.size == 1)
          MatchResult.Partial(matchedAlts.head)
        else
          MatchResult.Partial(OneOf(matchedAlts))
      case Concat(p1, p2) =>
        p1.matchOne(c, p2 ++ rest)

    def matchAll[C](cs: List[C])(using Soundness[C, A]): MatchResult[C, A] =
      var current = this
      var toBeMatched = cs
      while (toBeMatched.nonEmpty) {
        val c = toBeMatched.head
        toBeMatched = toBeMatched.tail
        current.matchOne(c, Definite(Vector())) match
          case MatchResult.PrefixMatched() => return MatchResult.PrefixMatched()
          case MatchResult.Mismatch(csRest, rest) => return MatchResult.Mismatch(csRest ++ toBeMatched, rest)
          case MatchResult.Partial(rest) => current = rest
      }
      if (current.isEmpty)
        MatchResult.PrefixMatched()
      else
        MatchResult.Mismatch(List(), current)

  enum MatchResult[C, A]:
    case PrefixMatched()
    case Mismatch(cs: List[C], rest: PrintResult[A])
    case Partial(rest: PrintResult[A])

// TODO this is a workaround. We don't need widening for PrintResult since it's stability does not influence the fixed point
given finitePrintResult[A]: Finite[APrintPrefix.PrintResult[A]] with {}

given JoinPrintResult[A]: Join[APrintPrefix.PrintResult[A]] with
  import APrintPrefix.*
  override def apply(v1: PrintResult[A], v2: PrintResult[A]): PrintResult[A] = v1.join(v2)


trait APrintPrefix[P] extends Print[P], Effectful:
  import APrintPrefix.*

  private var printed: PrintResult[P] = PrintResult.Definite(Vector.empty)

  def getPrinted: PrintResult[P] = printed
  protected def setPrinted(p: PrintResult[P]): Unit =
    this.printed = p

  override def print(a: P): Unit =
    printed = printed :+ a

  override def joinComputations[A](f: => A)(g: => A): Joined[A] = {
    val snapshot = printed
    var printedF: PrintResult[P] = null
    var printedG: PrintResult[P] = null
    try 
      super.joinComputations(
        try f finally printedF = printed)(
        try {printed = snapshot; g} finally printedG = printed)
    finally 
      printed = printedF.join(printedG)
  }

  def printIsSound[C](c: CPrint[C])(using Soundness[C, P]): IsSound =
    IsSound.Sound
    // TODO
//    printed.matchAll(c.getPrinted) match
//      case MatchResult.PrefixMatched() | MatchResult.Partial(_) => IsSound.Sound
//      case MatchResult.Mismatch(as, p) => IsSound.NotSound(s"Abstract print $p does not describe a prefix of concrete print $as")

