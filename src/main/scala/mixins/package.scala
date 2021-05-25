package object mixins {
  type Thunk[B] = Unit ~> B
}
