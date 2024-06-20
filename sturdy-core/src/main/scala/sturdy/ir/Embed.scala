package sturdy.ir

import sturdy.values.Topped

def embed(b: Boolean): IR = IR.Const(b)
def embed(b: Topped[Boolean]): IR = b match
  case Topped.Top => IR.Unknonwn()
  case Topped.Actual(b) => IR.Const(b)
