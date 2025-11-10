package sturdy.apron

import org.openjdk.jmh.annotations.*
import apron.*
import gmp.*

import java.util.concurrent.TimeUnit
import scala.annotation.tailrec

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.SampleTime))
@Warmup(iterations=1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations=3, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(value = TimeUnit.MILLISECONDS)
class ApronBench {

  @Param(Array("10", "20", "30"))
  var numVars: Int = _

  @Param(Array("Polka", "Octagon", "Box"))
  var managerName: String = _

  var env: Environment = _
  var manager: Manager = _
  var abs1: Abstract1 = _
  var abs2: Abstract1 = _
  val rand = new scala.util.Random

  @Setup(Level.Trial)
  def setup: Unit =
    val vars = 0.until(numVars).map(n => s"x$n").toArray[String]
    env = Environment(vars, Array[String]())
    managerName match
      case "Polka" =>
        manager = Polka(true)
        abs1 = randomPolyhedron(env)
        abs2 = randomPolyhedron(env)
      case "Octagon" =>
        manager = Octagon()
        abs1 = randomOctagon(env)
        abs2 = randomOctagon(env)
      case "Box" =>
        manager = Box()
        abs1 = randomBox(env)
        abs2 = randomBox(env)
      case _ => throw new IllegalStateException(s"Unknown manager $managerName")
    println(s"Setup:\nabs1 = $abs1\nabs2 = $abs2")

  @tailrec
  final def randomPolyhedron(env: Environment): Abstract1 =
    val cons = 1.to(10).map(_ =>
      Lincons1(Lincons1.SUPEQ, Linexpr1(env, env.getVars.map[Coeff](_ => DoubleScalar(rand.nextInt(100))), Interval(-rand.nextInt(100), rand.nextInt(100))))
    ).toArray[Lincons1]
    val abs1 = Abstract1(manager, cons)
    if (abs1.isBottom(manager) || abs1.isTop(manager))
      randomPolyhedron(env)
    else
      abs1

  @tailrec
  final def randomOctagon(env: Environment): Abstract1 =
    val cons =
      for (v1 <- env.getVars; v2 <- env.getVars if (v1 != v2))
        yield (
          Tcons1(env, Tcons1.SUPEQ,
            Texpr1BinNode(Texpr1BinNode.OP_ADD,
              Texpr1BinNode(Texpr1BinNode.OP_ADD,
                Texpr1VarNode(v1),
                Texpr1VarNode(v2)
              ),
              Texpr1CstNode(DoubleScalar(rand.nextInt(100)))
            )
          )
        )

    val abs1 = Abstract1(manager, cons)
    if(abs1.isBottom(manager) || abs1.isTop(manager))
      randomOctagon(env)
    else
      abs1

  final def randomBox(env: Environment): Abstract1 =
    Abstract1(manager, env, env.getVars, env.getVars.map[Interval](_ => Interval(-rand.nextInt(100), rand.nextInt(100))))

  @Benchmark
  def abstract1JoinCopy: Abstract1 =
    abs1.joinCopy(manager, abs2)
//
//  @Benchmark
//  def abstract1IsBottom: Boolean = abs1.isBottom(manager)
//
//  @Benchmark
//  def abstract1IsIncluded: Boolean = abs1.isIncluded(manager, abs2)

  @Benchmark
  def apronJoinsCombineAbstract1: Abstract1 =
    ApronJoins.combineAbstract1(manager, abs1, abs2, widen=false).get
}

object ApronBench:
  @main
  def main =
    val bench = new ApronBench
    bench.numVars = 20
    bench.managerName = "Polka"
    bench.setup
    bench.apronJoinsCombineAbstract1