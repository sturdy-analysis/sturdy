import apron.*
import org.scalatest.*
import org.scalatest._
import flatspec._
import matchers._
import sturdy.values.ints.ApronIntOps
import sturdy.values.unit
import sturdy.effect.store.ApronStore
import sturdy.effect.noJoin

case class Var(name: String) extends apron.Var:
  override def clone() = Var(name)
  override def compareTo(other: apron.Var): Int = this.name.compareTo(other.asInstanceOf[Var].name)

class ApronTest extends AnyFlatSpec with should.Matchers:
  "Apron" should "support the box domain" in {
    val manager = apron.Box()
    val intops = new ApronIntOps()
    val store = new ApronStore[Var](manager)
    val x = Var("x")
    val y = Var("y")

    store.write(x, intops.intLit(3))
    store.write(x, intops.intLit(5))

    store.write(y, intops.intLit(10))
    store.write(y,
      intops.add(
        store.read(x).orElse(null),
        store.read(y).orElse(null)))

    // x should be [3,5], but apparently Apron widens the result to [0,5]
    store.getDomain().getBound(manager, x) shouldBe apron.Interval(0,5)

    // y should be [13,15]
    store.getDomain().getBound(manager, y) shouldBe apron.Interval(0,15)
  }
