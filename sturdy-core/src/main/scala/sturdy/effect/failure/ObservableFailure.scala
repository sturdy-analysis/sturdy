package sturdy.effect.failure

import sturdy.control.{ControlEvent, ControlObservable, ControlObserver}

trait ObservableFailure(obs: ControlObservable[_,_,_]) extends Failure {
  abstract override def fail(kind: FailureKind, msg: String): Nothing =
    obs.triggerControlEvent(ControlEvent.Failed())
    super.fail(kind, msg)
}
