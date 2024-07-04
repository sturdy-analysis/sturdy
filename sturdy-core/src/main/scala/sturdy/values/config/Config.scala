package sturdy.values.config

import sturdy.effect.failure.{Failure, FailureKind}

case object UnsupportedConfiguration extends FailureKind

inline def unsupportedConfiguration[Config](using failure: Failure)(conf: Config, klass: Any) =
  failure(UnsupportedConfiguration, s"$conf, ${klass.getClass.getSimpleName}")
