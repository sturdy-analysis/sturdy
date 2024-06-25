package sturdy.fix

import sturdy.effect.TrySturdy

trait Logger[-Dom, -Codom]:
  def enter(dom: Dom): Unit
  def exit(dom: Dom, codom: TrySturdy[Codom]): Unit
  final def &&[DDom <: Dom, CCodom <: Codom](other: Logger[DDom, CCodom]): Logger[DDom, CCodom] = new ProductLogger(this, other)

def manyLogger[Dom, Codom](loggers: Iterable[Logger[Dom, Codom]]) = loggers.size match
  case 0 => throw new UnsupportedOperationException
  case 1 => loggers.head
  case 2 => loggers.head && loggers.tail.head
  case _ => new ManyLogger(loggers)

class ManyLogger[-Dom, -Codom](loggers: Iterable[Logger[Dom, Codom]]) extends Logger[Dom, Codom]:
  inline override def enter(dom: Dom): Unit =
    for (logger <- loggers)
      logger.enter(dom)

  inline override def exit(dom: Dom, codom: TrySturdy[Codom]): Unit =
    for (logger <- loggers)
      logger.exit(dom, codom)

class ProductLogger[-Dom, -Codom](l1: Logger[Dom, Codom], l2: Logger[Dom, Codom]) extends Logger[Dom, Codom]:
  inline override def enter(dom: Dom): Unit =
    l1.enter(dom)
    l2.enter(dom)

  inline override def exit(dom: Dom, codom: TrySturdy[Codom]): Unit =
    l1.exit(dom, codom)
    l2.exit(dom, codom)


def log[Dom, Codom](logger: Logger[Dom, Codom], phi: Combinator[Dom, Codom]): Log[Dom, Codom] = new Log(logger, phi)
final class Log[Dom, Codom](logger: Logger[Dom, Codom], val phi: Combinator[Dom, Codom]) extends Combinator[Dom, Codom] {
  override def apply(f: Dom => Codom): Dom => Codom = dom =>
    logger.enter(dom)
    val codom = TrySturdy(phi(f)(dom))
    logger.exit(dom, codom)
    codom.getOrThrow
}

final class DomLogger[Dom, Codom] extends Logger[Dom, Codom]:
  private var doms: List[Dom] = List()

  override def enter(dom: Dom): Unit = doms = dom :: doms

  override def exit(dom: Dom, codom: TrySturdy[Codom]): Unit =
    doms = doms.drop(1)

  def currentDom: Option[Dom] =
    doms.headOption