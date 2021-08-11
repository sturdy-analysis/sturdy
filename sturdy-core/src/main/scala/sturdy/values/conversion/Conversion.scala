package sturdy.values.conversion

import sturdy.effect.failure.FailureKind

/*
 * Most conversion rules in this package have been copied from:
 *   https://github.com/satabin/swam/tree/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime
 */

case object ConversionFailure extends FailureKind
