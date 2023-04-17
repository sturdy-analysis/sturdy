package sturdy.incremental

import org.scalatest.*
import org.scalatest.flatspec.*
import org.scalatest.matchers.*
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.immutable.ArraySeq

class DeltaTest extends AnyFunSuite with should.Matchers {

  import Call.*

  test("adding a function should not change the indices of other functions") {
    val p1 = ArraySeq(
      Function("f", List(DirectCall(1))),
      Function("g", List(DirectCall(2))),
      Function("h", List(DirectCall(0))),
    )
    val p2 = ArraySeq(
      Function("f", List(DirectCall(2))),
      Function("new", List(DirectCall(2))),
      Function("g", List(DirectCall(3))),
      Function("h", List(DirectCall(0))),
    )

    given Identifiable[Function] = IdentifiableFunction(p1, p2)

    IterableDelta.sub(p1, p2) shouldBe (new IterableDelta[Function]()(
      delta = ArraySeq(
        Change.Nil(Function("f", List(DirectCall(1))), Function("f", List(DirectCall(2)))),
        Change.Nil(Function("g", List(DirectCall(2))), Function("g", List(DirectCall(3)))),
        Change.Nil(Function("h", List(DirectCall(0))), Function("h", List(DirectCall(0)))),
      ),
      added = ArraySeq(
        Function("new", List(DirectCall(2)))
      ),
      remapping = Map(
        0 -> 0,
        1 -> 3,
        2 -> 1,
        3 -> 2
      )
    ))
  }

  test("removing a function should not change the indices of other functions") {

    val p1 = ArraySeq(
      Function("f", List(DirectCall(2))),
      Function("g", List(DirectCall(2))),
      Function("h", List(DirectCall(0))),
    )
    val p2 = ArraySeq(
      Function("f", List(DirectCall(1))),
      Function("h", List(DirectCall(0))),
    )

    given Identifiable[Function] = IdentifiableFunction(p1, p2)

    IterableDelta.sub(p1, p2) shouldBe (new IterableDelta[Function]()(
      delta = ArraySeq(
        Change.Nil(Function("f", List(DirectCall(2))), Function("f", List(DirectCall(1)))),
        Change.Remove(Function("g", List(DirectCall(2)))),
        Change.Nil(Function("h", List(DirectCall(0))), Function("h", List(DirectCall(0)))),
      ),
      added = ArraySeq.empty,
      remapping = Map(
        0 -> 0,
        1 -> 2,
      )
    ))
  }

  test("changes to a function should be detected as a 'replace'") {
    val p1 = ArraySeq(
      Function("f", List(DirectCall(1))),
      Function("g", List(DirectCall(2))),
      Function("h", List(DirectCall(0))),
    )
    val p2 = ArraySeq(
      Function("f", List(DirectCall(2))),
      Function("g", List(DirectCall(2))),
      Function("h", List(DirectCall(0))),
    )

    given Identifiable[Function] = IdentifiableFunction(p1, p2)

    IterableDelta.sub(p1, p2) shouldBe (new IterableDelta[Function]()(
      delta = ArraySeq(
        Change.Replace(Function("f", List(DirectCall(1))), Function("f", List(DirectCall(2)))),
        Change.Nil(Function("g", List(DirectCall(2))), Function("g", List(DirectCall(2)))),
        Change.Nil(Function("h", List(DirectCall(0))), Function("h", List(DirectCall(0)))),
      ),
      added = ArraySeq.empty,
      remapping = Map(
        0 -> 0,
        1 -> 1,
        2 -> 2
      )
    ))
  }


  test("Functions with indirect calls do not change, if the targets of the indirect calls did not change") {
    val p1 = ArraySeq(
      Function("f", List(IndirectCall(Set(1, 2)))),
      Function("g", List(DirectCall(2))),
      Function("h", List(DirectCall(0))),
    )
    val p2 = ArraySeq(
      Function("f", List(IndirectCall(Set.empty))),
      Function("g", List(DirectCall(2))),
      Function("h", List(DirectCall(0))),
    )

    given Identifiable[Function] = IdentifiableFunction(p1, p2)

    IterableDelta.sub(p1, p2) shouldBe (new IterableDelta[Function]()(
      delta = ArraySeq(
        Change.Nil(Function("f", List(IndirectCall(Set(1, 2)))), Function("f", List(IndirectCall(Set.empty)))),
        Change.Nil(Function("g", List(DirectCall(2))), Function("g", List(DirectCall(2)))),
        Change.Nil(Function("h", List(DirectCall(0))), Function("h", List(DirectCall(0)))),
      ),
      added = ArraySeq.empty,
      remapping = Map(
        0 -> 0,
        1 -> 1,
        2 -> 2
      )
    ))
  }

  test("Functions with indirect calls are detected as a replace, if the targets of the indirect calls change") {
    val p1 = ArraySeq(
      Function("f", List(IndirectCall(Set(1, 2)))),
      Function("g", List(DirectCall(2))),
      Function("h", List(DirectCall(0))),
    )
    val p2 = ArraySeq(
      Function("f", List(IndirectCall(Set.empty))),
      Function("new", List(DirectCall(2))),
      Function("g", List(DirectCall(3))),
      Function("h", List(DirectCall(0))),
    )

    given Identifiable[Function] = IdentifiableFunction(p1, p2)

    IterableDelta.sub(p1, p2) shouldBe (new IterableDelta[Function]()(
      delta = ArraySeq(
        Change.Replace(Function("f", List(IndirectCall(Set(1, 2)))), Function("f", List(IndirectCall(Set.empty)))),
        Change.Nil(Function("g", List(DirectCall(2))), Function("g", List(DirectCall(2)))),
        Change.Nil(Function("h", List(DirectCall(0))), Function("h", List(DirectCall(0)))),
      ),
      added = ArraySeq.empty,
      remapping = Map(
        0 -> 0,
        1 -> 1,
        2 -> 2
      )
    ))
  }
}

final class IdentifiableFunction(oldModule: ArraySeq[Function], newModule: ArraySeq[Function]) extends Identifiable[Function]:
  override type Id = String
  extension (f: Function)
    override def id: Id = f.name

  override def eqv(x: Function, y: Function): Boolean =
    x.name == y.name && x.body.zip(y.body).forall{
      case (Call.DirectCall(idx1),Call.DirectCall(idx2)) =>
        oldModule(idx1).id == newModule(idx2).id
      case (Call.IndirectCall(targets), Call.IndirectCall(_)) =>
        targets.forall( idx =>
          oldModule(idx).id == newModule(idx).id
        )
      case (_,_) =>
        false
    }

case class Function(name: String, body: List[Call])

enum Call:
  case DirectCall(idx: Int)
  case IndirectCall(targets: Set[Int])
