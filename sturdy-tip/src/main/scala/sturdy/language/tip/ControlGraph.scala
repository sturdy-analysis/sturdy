package sturdy.language.tip

import sturdy.language.tip.Stm.Block

import scala.collection.mutable
import scala.util.Random

enum Node:
  case Skip(label: Any)
  case Atom(label: Any)
  case Seq(n1: Node, n2: Node)
  case Branch(decider: Node, alternatives: Map[String, Node])
  case Call(target: Node)
  case Jump(target: Node)
  case Label(label: String)

  def id: String = this.toString.replaceAll("[^a-zA-Z0-9]", "_")

  def nodeToGraphViz(sb: StringBuilder): Unit = this match
    case Skip(i) =>
      sb ++= s"\t$id [label=\"skip\"];\n"
    case Atom(label) =>
      sb ++= s"\t$id [label=\"$label\"];\n"
    case Seq(n1, n2) =>
      sb ++= s"\t$id [label=\"Seq\"];\n"
    case Branch(dec, alts) =>
      sb ++= s"\t$id [label=\"branch\"];\n"
    case Call(target) =>
      sb ++= s"\t$id [label=\"Call\"];\n"
    case Jump(target) =>
      sb ++= s"\t$id [label=\"Jump\"];\n"
    case Label(lab) =>
      sb ++= s"\t$id [label=\"Label $lab\"];\n"

  def toGraphViz(sb: StringBuilder)(using edgeColor: String): Unit =
    this.nodeToGraphViz(sb)
    this match
      case Skip(i) =>
      case Atom(label) =>
      case Seq(n1, n2) =>
        n1.toGraphViz(sb)
        n2.toGraphViz(sb)
        sb ++= s"\t$id -> ${n1.id} [color=\"$edgeColor\"];\n"
        sb ++= s"\t$id -> ${n2.id} [color=\"$edgeColor\"];\n"
      case Branch(dec, alts) =>
        dec.toGraphViz(sb)
        sb ++= s"\t$id -> ${dec.id} [label=\"decider\", color=\"$edgeColor\"];\n"
        alts.foreach(_._2.toGraphViz(sb))
        alts.foreach { case (name, node) =>
          sb ++= s"\t$id -> ${node.id} [label=\"$name\", color=\"$edgeColor\"];\n"
        }
      case Call(target) =>
        target.toGraphViz(sb)
        sb ++= s"\t$id -> ${target.id} [color=\"$edgeColor\"];\n"
      case Jump(target) =>
        target.toGraphViz(sb)
        sb ++= s"\t$id -> ${target.id} [color=\"$edgeColor\"];\n"
      case Label(lab) =>

  def toGraphViz: String =
    val sb = new StringBuilder
    this.toGraphViz(sb)(using "black")
    s"""strict digraph {
       |  ${sb.toString()}
       |}
       |""".stripMargin

class Transformer(funs: Seq[Function]):
  def transFun(f: String): Node =
    val fun = funs.find(_.name == f).get
    val funEnter = Node.Atom(fun -> "enter")
    val funExit = Node.Atom(fun -> "exit")
    val bodyNode = transStm(fun.body)
    transExp(fun.ret)  match
      case None => Node.Seq(funEnter, Node.Seq(bodyNode, funExit))
      case Some(retNode) => Node.Seq(funEnter, Node.Seq(bodyNode, Node.Seq(retNode, funExit)))

  def transExp(e: Exp): Option[Node] = e match
    case Exp.Call(Exp.Var(f), args) =>
      Some(Node.Call(transFun(f)))
    case Exp.Call(fun, args) => Some(???)
    case Exp.NumLit(n) => None
    case Exp.Input() => None
    case Exp.Var(name) => None
    case Exp.Add(e1, e2) => transExp(e1) match
      case None => transExp(e2)
      case Some(n1) => transExp(e2) match
        case None => Some(n1)
        case Some(n2) => Some(Node.Seq(n1, n2))
    case Exp.Sub(e1, e2) => transExp(e1) match
      case None => transExp(e2)
      case Some(n1) => transExp(e2) match
        case None => Some(n1)
        case Some(n2) => Some(Node.Seq(n1, n2))
    case Exp.Mul(e1, e2) => transExp(e1) match
      case None => transExp(e2)
      case Some(n1) => transExp(e2) match
        case None => Some(n1)
        case Some(n2) => Some(Node.Seq(n1, n2))
    case Exp.Div(e1, e2) => transExp(e1) match
      case None => transExp(e2)
      case Some(n1) => transExp(e2) match
        case None => Some(n1)
        case Some(n2) => Some(Node.Seq(n1, n2))
    case Exp.Gt(e1, e2) => transExp(e1) match
      case None => transExp(e2)
      case Some(n1) => transExp(e2) match
        case None => Some(n1)
        case Some(n2) => Some(Node.Seq(n1, n2))
    case Exp.Eq(e1, e2) => transExp(e1) match
      case None => transExp(e2)
      case Some(n1) => transExp(e2) match
        case None => Some(n1)
        case Some(n2) => Some(Node.Seq(n1, n2))
    case Exp.Alloc(e) => transExp(e)
    case Exp.VarRef(name) => None
    case Exp.Deref(e) => transExp(e)
    case Exp.NullRef() => None
    case Exp.Record(fields) => ???
    case Exp.FieldAccess(rec, field) => ???

  def transStm(s: Stm): Node = s match
    case Stm.Assign(lhs, e) =>
      transExp(e) match
        case None => Node.Atom(s)
        case Some(n) => Node.Seq(n, Node.Atom(s))
    case Stm.If(cond, thn, els) =>
      Node.Branch(transExp(cond).getOrElse(Node.Atom(cond)),
        Map("true" -> transStm(thn)) ++ els.map(transStm).map("false" -> _)
      )
    case Stm.While(cond, body) =>
      val L1 = Node.Label(Random().nextString(10))
      Node.Seq(
        L1,
        Node.Branch(
          transExp(cond).getOrElse(Node.Atom(cond)),
          Map(
            "true" -> Node.Seq(transStm(body), Node.Jump(L1)),
            "false" -> Node.Skip(Random.nextInt())
          )
        )
      )
    case Stm.Block(body) =>
      val skip: Node = Node.Skip(Random.nextInt())
      body.foldRight(skip)((s, n) => Node.Seq(transStm(s), n))
    case Stm.Output(e) => Node.Atom(s)
    case Stm.Error(e) => Node.Atom(s)
    case Stm.Assert(e) => ??? // TODO


class Resolver(root: Node):
  private var edges: Set[(Node, Node)] = Set()

  def resolve(): Set[(Node, Node)] =
    resolve(root, Set())
    edges

  /** takes a set of predecessor nodes to connect to, and produces a new set for subsequent nodes */
  def resolve(n: Node, preds: Set[Node]): Set[Node] = n match
    case Node.Skip(label) =>
      preds
    case Node.Atom(label) =>
      preds.foreach(pred => edges += pred -> n)
      Set(n)
    case Node.Seq(n1, n2) =>
      resolve(n2, resolve(n1, preds))
    case Node.Branch(decider, alternatives) =>
      preds.foreach(pred => edges += pred -> n)
      val branchPreds = resolve(decider, Set(n))
      alternatives.flatMap { case (_, alt) =>
        resolve(alt, branchPreds)
      }.toSet
    case Node.Call(target) =>
      preds.foreach(pred => edges += pred -> n)
      resolve(target, Set(n))
    case Node.Jump(target) =>
      preds.foreach(pred => edges += pred -> n)
      resolve(target, Set(n))
    case Node.Label(label) =>
      preds.foreach(pred => edges += pred -> n)
      Set(n)

  def toGraphViz: String =
    val sb = new StringBuilder
    root.toGraphViz(sb)(using "gray")
    edges.foreach { case (from,to) =>
      sb ++= s"\t${from.id} -> ${to.id} [color=\"purple\"];\n"
    }
    s"""strict digraph {
       |  ${sb.toString()}
       |}
       |""".stripMargin


def run(funs: Seq[Function], main: String): Unit =
  val trans = new Transformer(funs)
  val root = trans.transExp(Exp.Call(Exp.Var(main), Seq())).get
  val resolver = new Resolver(root)
  val resolved = resolver.resolve()

  println(root)
  println()
  println(root.toGraphViz)
  println()
  println(resolver.toGraphViz)


object RunExample1 extends App:
  val f = Function("f", Seq(), Seq("x", "y"),
    Block(Seq(
      Stm.Assign(Assignable.AVar("x"), Exp.NumLit(7)),
      Stm.Assign(Assignable.AVar("y"), Exp.Call(Exp.Var("sqr"), Seq(Exp.Var("x"))))
    )),
    Exp.Var("y")
  )
  val sqr = Function("sqr", Seq("n"), Seq(), Stm.Block(Seq()), Exp.Mul(Exp.Var("n"), Exp.Var("n")))
  run(Seq(f, sqr), "f")

object RunExample2 extends App:
  val f = Function("f", Seq(), Seq("x", "y", "z"),
    Block(Seq(
      Stm.Assign(Assignable.AVar("x"), Exp.NumLit(7)),
      Stm.Assign(Assignable.AVar("y"), Exp.Call(Exp.Var("sqr"), Seq(Exp.Var("x")))),
      Stm.If(Exp.Gt(Exp.Var("y"), Exp.NumLit(0)),
        Stm.Assign(Assignable.AVar("z"), Exp.NumLit(1)),
        Some(Stm.Assign(Assignable.AVar("z"), Exp.NumLit(0)))
      )
    )),
    Exp.Var("y")
  )
  val sqr = Function("sqr", Seq("n"), Seq(), Stm.Block(Seq()), Exp.Mul(Exp.Var("n"), Exp.Var("n")))
  run(Seq(f, sqr), "f")

object RunExample3 extends App:
  val f = Function("f", Seq(), Seq("x", "y", "z"),
    Block(Seq(
      Stm.Assign(Assignable.AVar("x"), Exp.NumLit(7)),
      Stm.Assign(Assignable.AVar("y"), Exp.Call(Exp.Var("sqr"), Seq(Exp.Var("x")))),
      Stm.If(Exp.Gt(Exp.Var("y"), Exp.NumLit(0)),
        Stm.Assign(Assignable.AVar("z"), Exp.NumLit(1)),
        Some(
          Stm.If(Exp.Gt(Exp.NumLit(0), Exp.Var("y")),
            Stm.Assign(Assignable.AVar("z"), Exp.NumLit(-1)),
            Some(Stm.Assign(Assignable.AVar("z"), Exp.NumLit(0)))
          )
        )
      ),
    )),
    Exp.Var("y")
  )
  val sqr = Function("sqr", Seq("n"), Seq(), Stm.Block(Seq()), Exp.Mul(Exp.Var("n"), Exp.Var("n")))
  run(Seq(f, sqr), "f")
