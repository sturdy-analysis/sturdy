import apron.*
import org.scalatest.*
import org.scalatest.*
import flatspec.*
import matchers.*
import sturdy.effect.EffectStack
import sturdy.values.integer.{ApronIntegerOps, given}
import sturdy.data.given
import sturdy.values.ApronValue

class ApronTest extends AnyFlatSpec with should.Matchers:

  tests(using apron.Box())
  tests(using apron.Octagon())
  tests(using apron.Polka(true))
  tests(using apron.PolkaEq())

  def tests(using manager: Manager) =
    behavior of manager.toString

    val intops = new ApronIntegerOps

    val three = intops.integerLit(3)
    val five = intops.integerLit(5)
    val minusThree = intops.integerLit(-3)

    it should "approximate number literals with a precise interval" in {
      three.getBound should overapproximate (Interval(3,3))
      five.getBound should overapproximate (Interval(5,5))
      minusThree.getBound should overapproximate (Interval(-3,-3))
    }

    it should "add two numeric values by adding their interval bounds" in {
      intops.add(three, five).getBound should overapproximate (Interval(8,8))
      intops.add(three, minusThree).getBound should overapproximate (Interval(0,0))
      intops.add(minusThree.join(five), three).getBound should overapproximate (Interval(0, 8))
    }

    it should "join numeric values returning their smallest enclosing interval" in {
      three.join(five).getBound should overapproximate (Interval(3, 5))
      minusThree.join(five).getBound should overapproximate (Interval(-3, 5))
      minusThree.join(five).join(three) should overapproximate (minusThree.join(five))
    }

    it should "compute a positive absolute value of a numeric value" in {
      intops.absolute(three).getBound should overapproximate (Interval(3, 3))
      intops.absolute(minusThree.join(five)).getBound should overapproximate (Interval(0, 5))
      intops.absolute(minusThree).getBound should overapproximate (Interval(3, 3))
    }

  def overapproximate(expected: Interval): Matcher[Interval] =
    (actual: Interval) =>
      MatchResult(
        expected.isLeq(actual),
        s"$actual does not overappxomiate $expected",
        s"$actual overappxomiates $expected")

  def overapproximate(expected: ApronValue): Matcher[ApronValue] =
    (actual: ApronValue) =>
       overapproximate(expected.getBound)(actual.getBound)