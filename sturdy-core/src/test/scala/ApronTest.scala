import apron.*
import org.scalatest.*
import org.scalatest.*
import flatspec.*
import matchers.*
import sturdy.effect.{EffectStack}
import sturdy.values.integer.{ApronIntegerOps, given}
import sturdy.data.given

class ApronTest extends AnyFlatSpec with should.Matchers:
  given manager: Manager = apron.Box()
  val intops = new ApronIntegerOps

  val three = intops.integerLit(3)
  val five = intops.integerLit(5)
  val minusThree = intops.integerLit(-3)

  "Number Literals" should "be approximated with a precise interval" in {
    three.getBound shouldBe Interval(3,3)
    five.getBound shouldBe Interval(5,5)
    minusThree.getBound shouldBe Interval(-3,-3)
  }

  it should "be added without loosing precision" in {
    intops.add(three, five).getBound shouldBe Interval(8,8)
    intops.add(three, minusThree).getBound shouldBe Interval(0,0)
  }

  "The join of two numeric value" should "return the nearest enclosing interval" in {
    three.join(five).getBound shouldBe Interval(3, 5)
    minusThree.join(five).getBound shouldBe Interval(-3, 5)
    minusThree.join(five).join(three) shouldBe minusThree.join(five)
  }

  "The absolute value of a numeric value" should "be positive" in {
    intops.absolute(three).getBound shouldBe Interval(3, 3)
    intops.absolute(minusThree.join(five)).getBound shouldBe Interval(0, 5)
    intops.absolute(minusThree).getBound shouldBe Interval(3, 3)
  }
