package sturdy.fix

import sturdy.effect.{EffectStack, RecurrentCall, TrySturdy}
import sturdy.values.Finite
import sturdy.values.{Widen, Join}
import sturdy.data.CombineTuple2
import sturdy.util.Profiler

import scala.reflect.ClassTag

//class OutCacheOwner[Dom, Codom, In, Out, All]:
//  var outCache: Map[(Dom, In), (Codom, In)] = Map()


class DAIFixpoint[Dom, Codom]
//  (filterFunc: Dom => Boolean)
  (filterFunc: Dom => Int)
//  (comp: Map[(Dom, In), (Codom, Out)])
//  (comp: OutCacheOwner[Dom, Codom, In, Out, All])
  (using val state: State)
  (using Join[Codom], Finite[Dom])    // Join[Out]
  extends Fixpoint[Dom, Codom]:

  type Conf = (Dom, state.In)
  type InCache = Map[Conf, (Codom, state.In)]
  type OutCache = Map[Conf, (Codom, state.In)]

  var inCache: InCache = Map()
  var outCache: OutCache = Map()

  def apply(f: (Dom => Codom) ?=> (Dom => Codom)): Dom => Codom =
//    FixCache(Fixpoint.computeFixpoint(fixed => EvCache(f(using fixed))))
//
    FixCache(Fixpoint.computeLeastFixpoint(
      (fixed: Dom => Codom) => dispatch(filterFunc,
        Seq((fixed: Dom => Codom) => EvCache(f(using fixed)), (fixed: Dom => Codom) => EvCache(f(using fixed)))
      )(f(using fixed))
    ))

  //   (mrun ((fix-cache (fix (ev-cache f))) e)))


  def getOutCache: OutCache = outCache

  /*
  (define (((ev-cache ev₀) ev) e)
    (do ρ ← ask-env
        σ ← get-store
        ς ≔ (list e ρ σ)
        $out ← get-cache-out
        (if (∈ ς $out)
            (for/monad+ ([let v×σ = ($out ς)])
                  (do (put-store σ)
                      (return v)))
            (do $in ← ask-cache-in
                v×σ₀  ≔ (if (∈ ς $in) ($in ς) ∅)
                (put-cache-out ($out ς v×σ₀))
                v ← ((ev₀ ev) e)
                σ′ ← get-store
                (update-cache-out
                 (λ ($out) ($out ς (set-add ($out ς) v×σ′))))
                (return v)))))
  */
  object EvCache extends Combinator[Dom, Codom]:
    override def apply(f: Dom => Codom): Dom => Codom =
      def apply_(dom: Dom): Codom =
        val conf = (dom, state.getInState(dom))
//        println(conf)
        outCache.get(conf) match
          case Some(null) => throw RecurrentCall(dom)
          case Some((codom, out)) =>
            state.setInState(dom, out)    // setOut
            codom
          case None =>
            val oldResult = inCache.getOrElse(conf, null)
            outCache += conf -> oldResult
            val codom = f(dom)
            val out = state.getInState(dom)   // getOut

            val joinedValueAndOut = outCache(conf) match
              case null => (codom, out)
              case (codomCached, outCached) =>
                val widened = (Join(codomCached, codom).get, state.joinIn(dom)(outCached, out).get)
                //                println(s"${widened._1}    $codom     $codomCached")
                widened

            outCache += conf -> joinedValueAndOut
            codom
      apply_

  /*
    (define ((fix-cache eval) e)
      (do ρ ← ask-env
          σ ← get-store
          ς ≔ (list e ρ σ)
          $⁺ ← (mlfp (λ ($) (do (put-cache-out ∅)
                                (put-store σ)
                                (local-cache-in $ (eval e))
                                get-cache-out)))
          (for/monad+ ([let v×σ = ($⁺ ς)])
                (do (put-store σ)
                  (return v)))))

    (define (mlfp f)
      (let loop ([x ∅])
        (do x′ ← (f x)
        (if (equal? x′ x) (return x) (loop x′)))))
  */
  object FixCache extends Combinator[Dom, Codom]:
    var counter = 0
    override def apply(f: Dom => Codom): Dom => Codom =
      def apply_(dom: Dom): Codom = {
        var result: Option[Codom] = None
        val stateAllFromStart = state.getAllState
        while
          inCache = outCache
          outCache = Map()
          state.setAllState(stateAllFromStart)
          println("startInCache: " + inCache)
          result = Some(f(dom))
          //          println(s"In     $incache")
          //          println(s"Out    $outcache")

          counter += 1
          Profiler.addTime("comparison")(
            inCache != outCache //&& counter < 5
          )
        do()
//        comp.outCache = outCache
        result.get
      }
      apply_


/*

ev0: Exp => Val
type Conf = (Exp, Env, Store)
type OutCache = Map[Conf, (Val, Store)]


f: Dom => Codom
f_internal: (Dom, In) => (Codom, Out)

type Conf = (Dom, In)
type InCache = Map[Conf, (Codom, Out)]
type OutCache = Map[Conf, (Codom, Out)]

state.getInState() // <- (ask-env, get-store)
state.setOutState(out) // <- put-store



(define (((ev-cache ev₀) ev) e)
  (do ρ ← ask-env
      σ ← get-store
      ς ≔ (list e ρ σ)
      $out ← get-cache-out
      (if (∈ ς $out)
          (for/monad+ ([let v×σ = ($out ς)])
                (do (put-store σ)
                    (return v)))
          (do $in ← ask-cache-in
              v×σ  ≔ (if (∈ ς $in) ($in ς) ∅)
              (put-cache-out ($out ς v×σ₀))
              v ← ((ev₀ ev) e)
              σ′ ← get-store
              (update-cache-out
               (λ ($out) ($out ς (set-add ($out ς) v×σ′))))
              (return v)))))

(define ((fix-cache eval) e)
  (do ρ ← ask-env
      σ ← get-store
      ς ≔ (list e ρ σ)
      $⁺ ← (mlfp (λ ($) (do (put-cache-out ∅)
                            (put-store σ)
                            (local-cache-in $ (eval e))
                            get-cache-out)))
      (for/monad+ ([let v×σ = ($⁺ ς)])
            (do (put-store σ)
              (return v)))))

(define (mlfp f)
  (let loop ([x ∅])
    (do x′ ← (f x)
        (if (equal? x′ x) (return x) (loop x′)))))



*/



