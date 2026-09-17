package sturdy.values.unit

/**
 * A trait for representing operations on unit types.
 * @tparam V The value domain the unit type is part of.
 */
trait UnitOps[V]:
  /**
   * Produces a unit value, possibly with side effects.
   * @return The unit value
   */
  def value(): V
