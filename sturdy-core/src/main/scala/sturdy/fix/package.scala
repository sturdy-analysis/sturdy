package sturdy

/** Computes the fixpoint of a big-step abstract interpreter implemented with Sturdy.
 *
 * The design of the fixpoint algorithms is described in more detail in
 * ''Combinator-Based Fixpoint Algorithms for Big-Step Abstract Interpreters'' 
 * (https://dl.acm.org/doi/10.1145/3607863).
 * 
 * In short, fixpoint algorithms are modularly composed of fixpoint combinators.
 * Each combinator handles a specific aspect of the fixpoint algorithm, 
 * such as limiting the recursion depth or iterating on results.
 */
package object fix {}
