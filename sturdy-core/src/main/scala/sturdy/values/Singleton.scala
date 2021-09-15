package sturdy.values

enum Singleton[+A]:
  case NoSingleton extends Singleton[Nothing]
  case Single(a: A)

  inline def flatMap[B](f: A => Singleton[B]): Singleton[B] = this match
    case NoSingleton => NoSingleton
    case Single(a) => f(a)

  inline def map[B](f: A => B): Singleton[B] = this match
    case NoSingleton => NoSingleton
    case Single(a) => Single(f(a))

  inline def binary[B, AA >: A](f: (A, AA) => B, other: Singleton[AA]): Singleton[B] =
    for (i1 <- this; i2 <- other) yield f(i1, i2)

  inline def unary[B](f: A => B): Singleton[B] = map(f)
