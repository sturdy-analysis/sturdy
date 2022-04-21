package sturdy.util

import sturdy.fix.Stack
import sturdy.fix.iter.TopmostCounter

object StackManager:
  var stacks: Map[Any, Any] = Map()
  var stack1: Option[Any] = None
  var stack2: Option[Any] = None

  def reset(): Unit =
    stack1 = None
    stack2 = None
    stacks = Map()
    TopmostCounter.instanceCounter = 0

  def getStacks: Map[Any, Stack[Any, Any, Any, Any, Any, Any]] = stacks.asInstanceOf[Map[Any, Stack[Any, Any, Any, Any, Any, Any]]]

  var keidelFixpoint: Any = None
