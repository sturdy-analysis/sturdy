package sturdy.effect.failure

import sturdy.control.{BasicControlEvent, ControlEvent, ControlObservable, ControlObserver}

trait ObservableFailure(obs: ControlObservable[_,_,_,_]) extends Failure {
  abstract override def fail(kind: FailureKind, msg: String): Nothing =
    obs.triggerControlEvent(BasicControlEvent.Failed())
    super.fail(kind, msg)
}
