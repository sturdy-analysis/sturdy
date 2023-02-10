package sturdy.values

import apron.Texpr1Node
import sturdy.apron.{Apron, ApronExpr, ApronScope}

given ApronPartialOrder(using scope: ApronScope): PartialOrder[ApronExpr] with
  override def lteq(x: ApronExpr, y: ApronExpr): Boolean =
//    println(s"$x (${scope.getBound(x)}) <= $y (${scope.getBound(y)})   in $scope")
    scope.getBound(x).isLeq(scope.getBound(y))
