package sturdy.symboltable
import org.scalatest.funsuite.AnyFunSuite
import sturdy.data.JOptionC
import sturdy.effect.symboltable.ConcreteSymbolTable
import sturdy.symboltable
import sturdy.values.*

class SymbolTableTest extends AnyFunSuite {

  test("testFalse") {
    val tab = new ConcreteSymbolTable[Int, String, String]
    tab.putNew(1)
    tab.set(1, "a", "abc")
    assertResult(JOptionC.some("abc"))(tab.get(1, "a"))
  }
}
