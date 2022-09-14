package sturdy.values.strings

import org.eclipse.collections.impl.block.procedure.FastListSelectProcedure
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.*
import sturdy.values.booleans.ToppedBooleanOps
import sturdy.values.integer.{IntSign, NumericInterval}
import sturdy.values.integer.IntSign.*
import sturdy.values.relational.*
import sturdy.values.integer.CombineIntSign

import java.security.KeyStore.TrustedCertificateEntry

enum StringCharacterInclusion:
  case StringSets(mustContain: Set[Char], mayContain: Topped[Set[Char]])


import sturdy.values.strings.StringCharacterInclusion.*

given Abstractly[String, StringCharacterInclusion] with
  override def apply(s: String): StringCharacterInclusion =
    StringSets(s.toCharArray.toSet[Char], Topped.Actual(s.toCharArray.toSet[Char]))


given PartialOrder[StringCharacterInclusion] with
  override def lteq(x: StringCharacterInclusion, y: StringCharacterInclusion): Boolean = (x,y) match
    case (StringSets(c1 :Set[Char], mc1 : Topped[Set[Char]]), StringSets(c2 :Set[Char], mc2 : Topped[Set[Char]])) =>
      (mc1, mc2) match
        case (Topped.Actual(amc1), Topped.Actual(amc2)) => c2.subsetOf(c1) && amc1.subsetOf(amc2)
        case (Topped.Top, Topped.Top) | (_, Topped.Top) => c2.subsetOf(c1)
        case _ => false



given CombineStringCharacterInclusion[W <: Widening]: Combine[StringCharacterInclusion, W] with
  override def apply(v1: StringCharacterInclusion, v2: StringCharacterInclusion): MaybeChanged[StringCharacterInclusion] =
    if v1 == v2 then Unchanged(v1)
    else if PartialOrder[StringCharacterInclusion].lteq(v1, v2) then Changed(v2)
    else if PartialOrder[StringCharacterInclusion].lteq(v1, v2) then Unchanged(v1)
    else (v1, v2) match
      case (StringSets(c1, mc1), StringSets(c2, mc2)) =>
        if (mc1.isTop || mc2.isTop){
          Changed(StringSets(c1.intersect(c2), Topped.Top))}
        else{
          Changed(StringSets(c1.intersect(c2), Topped.Actual(mc1.get.union(mc2.get))))
        }



given CharacterInclusionStringOps[B](using f: Failure, j: EffectStack): StringOps[StringCharacterInclusion, IntSign, Topped[Boolean]] with

  def toppedCharSetIsEmpty(toppedCharSet: Topped[Set[Char]]): Boolean ={
    if (toppedCharSet.isActual){
      toppedCharSet.get.isEmpty
    }
    false
  }

  def stringLit(s: String): StringCharacterInclusion = StringSets(s.toString.toCharArray.toSet[Char], Topped.Actual(s.toString.toCharArray.toSet[Char]))

  override def concat(s1: StringCharacterInclusion, s2: StringCharacterInclusion): StringCharacterInclusion = (s1, s2) match
    case (StringSets(c1, mc1), StringSets(c2, mc2)) =>
      var mcUnion = if mc1.isTop || mc2.isTop then Topped.Top else Topped.Actual(mc1.get.union(mc2.get))
      StringSets(c1.union(c2), mcUnion)




  //TODO: Error messages schöner machen, Code formatieren
  override def substring(s: StringCharacterInclusion, begin: IntSign, end: IntSign): StringCharacterInclusion = (begin, end) match
    case (IntSign.Zero, IntSign.Zero) => StringSets(Set[Char](), Topped.Actual(Set[Char]()))
    case (IntSign.Neg, _) | (_, IntSign.Neg) => j.joinWithFailure(f.fail(StringNegativeIndex, s"substring of $s with indices $begin to $end"))(f.fail(StringIndexOutOfBounds, s"substring of $s with indices $begin to $end"))
    case (IntSign.NegOrZero, _) | (_, IntSign.NegOrZero) => s match
      case StringSets(c, mc) =>
        var cUnionMc = if mc.isTop then Topped.Top else Topped.Actual(mc.get.union(c))
        j.joinWithFailure(StringSets(Set[Char](), cUnionMc))(j.joinWithFailure(f.fail(StringNegativeIndex, s"substring of $s with indices $begin to $end"))(f.fail(StringIndexOutOfBounds, s"substring of $s with indices $begin to $end")))
    case _ => s match
      case StringSets(c, mc) =>
        var cUnionMc = if mc.isTop then Topped.Top else Topped.Actual(mc.get.union(c))
        j.joinWithFailure(StringSets(Set[Char](), cUnionMc))(f.fail(StringIndexOutOfBounds, s"substring of $s with indices $begin to $end"))

  override def contains(s: StringCharacterInclusion, w: StringCharacterInclusion): Topped[Boolean] = (s, w) match
    case (StringSets(c1, mc1), StringSets(c2, mc2)) => if (c2.isEmpty && mc2.isEmpty) {
      Topped.Actual(true)
    }  else{
      if(!c2.subsetOf(c1) && c1 != c2)
      {
        Topped.Actual(false)
      }
      else Topped.Top
    }

  override def length(s: StringCharacterInclusion): IntSign = IntSign.Neg
    //s match
    //case (StringSets(c, mc)) =>
    //  if(c.isEmpty){
    //    if(mc.isActual){
    //      if(mc.get.isEmpty){
    //        return IntSign.Zero
    //      }
    //    }
    //    return IntSign.ZeroOrPos
    //  }
    //  IntSign.Pos



  override def isEmpty(s: StringCharacterInclusion): Topped[Boolean] = s match
    case StringSets(_, Topped.Top) => Topped.Top
    case StringSets(c, mc) => if (c.isEmpty && mc.get.isEmpty) {
      Topped.Actual(true)
    }
      else {
      if (!c.isEmpty){
        Topped.Actual(false)
      }
      else Topped.Top
    }

  override def charAt(s: StringCharacterInclusion, i: IntSign): StringCharacterInclusion = s match
    case StringSets(c, mc) =>
      if(mc.isActual){
        if(mc.get.isEmpty ){
          if (c.size == 1){
            i match
              case IntSign.Zero => return s
              case IntSign.Pos | IntSign.ZeroOrPos => j.joinWithFailure(s)(f.fail(StringIndexOutOfBounds, s"Char at $i of $s is out of bounds"))
              case IntSign.NegOrZero => j.joinWithFailure(s)(f.fail(StringNegativeIndex, s"Char at $i of $s is out of bounds"))
              case IntSign.Neg => f.fail(StringNegativeIndex, s"Char at $i of $s is out of bounds")
          }
          if (c.isEmpty){
            i match
              case IntSign.Zero | IntSign.ZeroOrPos | IntSign.Pos => f.fail(StringIndexOutOfBounds, s"Char at $i of $s is out of bounds")
              case IntSign.Neg => f.fail(StringNegativeIndex, s"Char at $i of $s is out of bounds")
              case IntSign.NegOrZero => j.joinWithFailure(f.fail(StringNegativeIndex, s"Char at $i of $s is out of bounds"))(f.fail(StringIndexOutOfBounds, s"Char at $i of $s is out of bounds"))
          }
        }
      }

      i match
        case IntSign.Zero => StringCharacterInclusion.StringSets(Set[Char](), mc)
        case IntSign.Pos | ZeroOrPos => j.joinWithFailure(StringCharacterInclusion.StringSets(Set[Char](), mc))(f.fail(StringIndexOutOfBounds, s"Char at $i of $s is out of bounds"))
        case IntSign.Neg => f.fail(StringNegativeIndex, s"Char at $i of $s is out of bounds")
        case IntSign.NegOrZero => j.joinWithFailure(StringCharacterInclusion.StringSets(Set[Char](), mc))(f.fail(StringNegativeIndex, s"Char at $i of $s is out of bounds"))




  override def equals(s1: StringCharacterInclusion, s2: StringCharacterInclusion): Topped[Boolean] = (s1, s2) match
    case (StringSets(c1, mc1), StringSets(c2, mc2)) =>
      if(mc1.isActual && mc2.isActual) {
        if(c1.isEmpty && c2.isEmpty && mc1.get.isEmpty && mc2.get.isEmpty){
          Topped.Actual(true)
        }
      }
      val c1IsSubSetOfMc2 = if mc2.isActual then c1.subsetOf(mc2.get) else true
      val c2IsSubSetOfMc1 = if mc1.isActual then c2.subsetOf(mc1.get) else true
      if (!c1IsSubSetOfMc2 || !c2IsSubSetOfMc1)
      {
        Topped.Actual(false)
      }
      else Topped.Top

  override def compareTo(s1: StringCharacterInclusion, s2: StringCharacterInclusion): IntSign = (s1, s2) match
    case (StringSets(c1, Topped.Top), StringSets(c2, Topped.Top)) => IntSign.TopSign
    case (StringSets(c1, Topped.Top), StringSets(c2, _)) => IntSign.ZeroOrPos
    case (StringSets(c1, _), StringSets(c2, Topped.Top)) => IntSign.NegOrZero
    case (StringSets(c1, Topped.Actual(mc1)), StringSets(c2, Topped.Actual(mc2))) =>
      if(mc1.isEmpty && mc2.isEmpty){
        IntSign.Zero
      }
      else{
        val mc1Array = mc1.toArray[Char];
        val mc2Array = mc2.toArray[Char];
        var firstIteration = true;
        var state = IntSign.TopSign;
        for(x <- mc1Array){
          for(y <- mc2Array){
            if(firstIteration) {
              firstIteration = false
              if(x < y){
                state = IntSign.Neg
              }
              if(x > y){
                state = IntSign.Pos
              }
              if(x == y){
                state = IntSign.Zero
              }
            }
            else{
              if(x < y){
                state = CombineIntSign(state, IntSign.Neg).get
              }
              if(x > y){
                state = CombineIntSign(state, IntSign.Pos).get
              }
              if(x == y){
                state = CombineIntSign(state, IntSign.Zero).get
              }
            }

          }
        }
        state
      }


    //case (StringSets(c1, mc1), StringSets(c2, mc2)) =>
    //  if (mc1.isActual && mc2.isActual) {
    //    if(c1.isEmpty && c2.isEmpty && mc1.get.isEmpty && mc2.get.isEmpty){
    //      return IntSign.Zero
    //    }
    //  }

      //if(mc1.isTop && toppedCharSetIsEmpty(mc2)){
      //  return IntSign.ZeroOrPos
      //}
      //if(mc2.isTop && toppedCharSetIsEmpty(mc1)){
      //  return IntSign.NegOrZero
      //}
      //if(mc1.is)







    //}
    //IntSign.TopSign

  override def startsWith(s: StringCharacterInclusion, prefix: StringCharacterInclusion, offset: IntSign): Topped[Boolean] = ???

  override def endsWith(s: StringCharacterInclusion, suffix: StringCharacterInclusion): Topped[Boolean] = (s, suffix) match
    case (StringSets(c1, mc1), StringSets(c2, mc2)) =>
      if(mc1.isActual && mc2.isActual) {
        if(c1.isEmpty && c2.isEmpty && mc1.get.isEmpty && mc2.get.isEmpty){
          Topped.Actual(true)
        }
      }
      val c2IsSubSetOfMc1 = if mc1.isActual then c2.subsetOf(mc1.get) else true
      if (!c2IsSubSetOfMc1)
      {
        Topped.Actual(false)
      }
      else Topped.Top


  override def indexOf(s: StringCharacterInclusion, word: StringCharacterInclusion, fromIndex: IntSign): IntSign = ???

  override def replace(s: StringCharacterInclusion, word: StringCharacterInclusion, newWord: StringCharacterInclusion): StringCharacterInclusion = ???

  override def toLowerCase(s: StringCharacterInclusion): StringCharacterInclusion = ???

  override def toUpperCase(s: StringCharacterInclusion): StringCharacterInclusion = ???

  override def trim(s: StringCharacterInclusion): StringCharacterInclusion = ???

