package sturdy.values.config

import sturdy.effect.failure.FailureKind


case class UnsupportedConfiguration[Config](conf: Config, msg: String) extends Exception(conf.toString + ": " + msg)
