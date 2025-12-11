package sturdy.values.exceptions

import sturdy.data.MayJoin

/**
 * Private interface used by `Except` to convert between an exception type `Exc` known by the generic interpreter
 * and an analysis-specific exception type `ExcV`.
 */
trait Exceptional[Exc, ExcV, J[_] <: MayJoin[_]]:

  /**
   * Called when exception is thrown.
   * Converts exception type `Exc` known by the generic interpreter to an analysis-specific exception type `ExcV`.
   * Type `ExcV` must capture the state of the analysis, to be able to restore it on handle.
   */
  def exception(exc: Exc): ExcV

  /**
   * Called when exceptions are caught and handled.
   * Allows to pattern match on different exception cases.
   * Method `handle` must restore the analysis state contained inside `ExcV`
   */
  def handle[A](e: ExcV)(f: Exc => A): J[A] ?=> A
