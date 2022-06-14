package ba.sake.snowplowtechtest.routes

import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax._

sealed trait ApiResult extends Product with Serializable

object ApiResult {

  case class Success(
      action: String,
      id: String,
      status: String
  ) extends ApiResult

  case class Failure(
      action: String,
      id: String,
      status: String,
      message: String
  ) extends ApiResult

  implicit val encodeEvent: Encoder[ApiResult] = Encoder.instance {
    case foo: Success => foo.asJson
    case bar: Failure => bar.asJson
  }

  implicit val decodeEvent: Decoder[ApiResult] =
    List[Decoder[ApiResult]](
      Decoder[Success].widen,
      Decoder[Failure].widen
    ).reduceLeft(_ or _)

}
