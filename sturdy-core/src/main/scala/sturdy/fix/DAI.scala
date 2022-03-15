package sturdy.fix

import sturdy.effect.AnalysisState
import sturdy.values.Finite
import sturdy.effect.EffectStack
import sturdy.effect.RecurrentCall
import sturdy.values.Join
import sturdy.data.JoinTuple2


trait DAIFixpoint[Dom, Codom, In, Out, All]
  (using Join[Codom], Join[Out])
  (using Finite[Dom]) extends FixpointInterface[Dom, Codom]:

  val state: AnalysisState[Dom, In, Out, All]

  type Conf = (Dom, In)
  type InCache = Map[Conf, (Codom, Out)]
  type OutCache = Map[Conf, (Codom, Out)]

  var incache: InCache = Map()
  var outcache: OutCache = Map()

  //   (mrun ((fix-cache (fix (ev-cache f))) e)))
  override def fixpoint(f: (Dom => Codom) ?=> (Dom => Codom)): Dom => Codom =
    FixCache(Fixpoint.computeFixpoint(fixed => EvCache(f(using fixed))))


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
                v×σ  ≔ (if (∈ ς $in) ($in ς) ∅)
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
        outcache.get(conf) match
          case Some(null) => throw RecurrentCall(dom)
          case Some((codom, out)) =>
            state.setOutState(out)
            codom
          case None =>
            val oldResult = incache.getOrElse(conf, null)
            outcache += conf -> oldResult
            val codom = f(dom)
            val out = state.getOutState(dom)
            val (codomOld, outOld) = outcache(conf)
            val joined = Join((codomOld, outOld), (codom, out)).get
            outcache += conf -> joined
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
  */
  object FixCache extends Combinator[Dom, Codom]:
    override def apply(f: Dom => Codom): Dom => Codom =
      def apply_(dom: Dom): Codom =
        ???
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



