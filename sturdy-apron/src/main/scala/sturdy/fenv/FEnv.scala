package sturdy.fenv

import java.lang.foreign.{FunctionDescriptor, Linker, ValueLayout}
import java.lang.invoke.{MethodHandle, MethodHandles}

// https://man7.org/linux/man-pages/man3/fenv.3.html
enum RoundingMode extends Enum[RoundingMode]:
  case ToNearest // FE_TONEAREST
  case Upward // FE_UPWARD
  case Downward // FE_DOWNWARD
  case TowardZero // FE_TOWARDZERO

object FEnv {
  def getRoundingMode: Option[RoundingMode] = {
    val r: Int = FEnvNative.GET_ROUND_MODE.invokeExact()
    r match {
      case 0 => Some(RoundingMode.ToNearest)
      case 1 => Some(RoundingMode.Upward)
      case 2 => Some(RoundingMode.Downward)
      case 3 => Some(RoundingMode.TowardZero)
      case _ => None
    }
  }
  def setRoundingMode(mode: RoundingMode): Boolean = {
    val r: Int = mode match {
      case RoundingMode.ToNearest => 0
      case RoundingMode.Upward => 1
      case RoundingMode.Downward => 2
      case RoundingMode.TowardZero => 3
    }
    val result: Int = FEnvNative.SET_ROUND_MODE.invokeExact(r)
    result == 0
  }
}
private object FEnvNative {
  val GET_ROUND_MODE: MethodHandle = {
    val lookup = MethodHandles.lookup()
    val linker = Linker.nativeLinker()
    val handle = linker.downcallHandle(linker.defaultLookup().findOrThrow("fegetround"), FunctionDescriptor.of(ValueLayout.JAVA_INT))
    handle
  }
  val SET_ROUND_MODE: MethodHandle = {
    val lookup = MethodHandles.lookup()
    val linker = Linker.nativeLinker()
    val handle = linker.downcallHandle(linker.defaultLookup().findOrThrow("fesetround"), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT))
    handle
  }
}
