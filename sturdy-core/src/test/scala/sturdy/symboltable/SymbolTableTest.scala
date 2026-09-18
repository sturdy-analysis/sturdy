package sturdy.symboltable
import org.scalatest.funsuite.AnyFunSuite
import sturdy.data.{JOptionA, JOptionC}
import sturdy.effect.symboltable.{ConcreteSymbolTable, UpperBoundSymbolTable}
import sturdy.symboltable
import sturdy.values.{JoinToppedFlat, *}
import sturdy.values.given

class SymbolTableTest extends AnyFunSuite {

  test("testFalse") {
    val tab = new ConcreteSymbolTable[Int, String, String]
    tab.putNew(1)
    tab.set(1, "a", "abc")
    assertResult(JOptionC.some("abc"))(tab.get(1, "a"))
  }


  test("testUpperBoundSymbolTable") {
    summon[Join[Topped[Int]]]
    given Finite[Int] with {}
    val tab = new UpperBoundSymbolTable[Int, Int, Topped[Int]]
    tab.putNew(1)
    assertResult(JOptionA.none)(tab.get(1, 0))
    tab.set(1, 0, Topped.Actual(1))
    assertResult(JOptionA.noneSome(Topped.Actual(1)))(tab.get(1, 0))
    tab.set(1, 1, Topped.Actual(1))
    assertResult(JOptionA.noneSome(Topped.Actual(1)))(tab.get(1, 0))
    tab.set(1, 2, Topped.Actual(2))
    assertResult(JOptionA.noneSome(Topped.Top))(tab.get(1, 0))

  }
}


