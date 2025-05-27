package sturdy.apron

import apron.Texpr1Node
import sturdy.values.types.BaseType


trait ApronType[Type]:
  extension(t: Type)
    def apronRepresentation: ApronRepresentation
    def roundingDir: RoundingDir
    def roundingType: RoundingType
    def byteSize: Int

enum ApronRepresentation:
  case Int
  case Real

enum RoundingType:
  case Real
  case Int
  case Single
  case Double
  case Extended
  case Quad

  def toApron: Int =
    this match
      case Real => Texpr1Node.RTYPE_REAL
      case Int => Texpr1Node.RTYPE_INT
      case Single => Texpr1Node.RTYPE_SINGLE
      case Double => Texpr1Node.RTYPE_DOUBLE
      case Extended => Texpr1Node.RTYPE_EXTENDED
      case Quad => Texpr1Node.RTYPE_QUAD

enum RoundingDir:
  case Down
  case Nearest
  case Rnd
  case Up
  case Zero

  def toApron: Int =
    this match
      case Down => Texpr1Node.RDIR_DOWN
      case Nearest => Texpr1Node.RDIR_NEAREST
      case Rnd => Texpr1Node.RDIR_RND
      case Up => Texpr1Node.RDIR_UP
      case Zero => Texpr1Node.RDIR_ZERO

given ByteApronType: ApronType[BaseType[Byte]] with
  extension(t: BaseType[Byte])
    override def apronRepresentation: ApronRepresentation = ApronRepresentation.Int
    override def roundingDir: RoundingDir = RoundingDir.Zero
    override def roundingType: RoundingType = RoundingType.Int
    override def byteSize: Int = java.lang.Byte.BYTES

given ShortApronType: ApronType[BaseType[Short]] with
  extension(t: BaseType[Short])
    override def apronRepresentation: ApronRepresentation = ApronRepresentation.Int
    override def roundingDir: RoundingDir = RoundingDir.Zero
    override def roundingType: RoundingType = RoundingType.Int
    override def byteSize: Int = java.lang.Short.BYTES

given IntApronType: ApronType[BaseType[Int]] with
  extension(t: BaseType[Int])
    override def apronRepresentation: ApronRepresentation = ApronRepresentation.Int
    override def roundingDir: RoundingDir = RoundingDir.Zero
    override def roundingType: RoundingType = RoundingType.Int
    override def byteSize: Int = java.lang.Integer.BYTES


given LongApronType: ApronType[BaseType[Long]] with
  extension(t: BaseType[Long])
    override def apronRepresentation: ApronRepresentation = ApronRepresentation.Int
    override def roundingDir: RoundingDir = RoundingDir.Zero
    override def roundingType: RoundingType = RoundingType.Int
    override def byteSize: Int = java.lang.Long.BYTES

given BooleanApronType: ApronType[BaseType[Boolean]] with
  extension (t: BaseType[Boolean])
    override def apronRepresentation: ApronRepresentation = ApronRepresentation.Int
    override def roundingDir: RoundingDir = RoundingDir.Zero
    override def roundingType: RoundingType = RoundingType.Int
    override def byteSize: Int = java.lang.Byte.BYTES

given FloatApronType: ApronType[BaseType[Float]] with
  extension (t: BaseType[Float])
    override def apronRepresentation: ApronRepresentation = ApronRepresentation.Real
    override def roundingDir: RoundingDir = RoundingDir.Rnd
    override def roundingType: RoundingType = RoundingType.Single
    override def byteSize: Int = java.lang.Float.BYTES

given DoubleApronType: ApronType[BaseType[Double]] with
  extension (t: BaseType[Double])
    override def apronRepresentation: ApronRepresentation = ApronRepresentation.Real
    override def roundingDir: RoundingDir = RoundingDir.Rnd
    override def roundingType: RoundingType = RoundingType.Double
    override def byteSize: Int = java.lang.Double.BYTES