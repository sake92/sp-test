package ba.sake.snowplowtechtest.routes

import cats.effect.IO
import io.circe._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.Request
import org.http4s.Response
import org.http4s.circe._

import ba.sake.snowplowtechtest.services.JsonSchemaService
import ba.sake.snowplowtechtest.db.models.JsonSchema

class JsonSchemaValidationRoutes(
    jsonSchemaService: JsonSchemaService
) extends Http4sDsl[IO] {

  import ApiResult._

  def schemaRoutes: HttpRoutes[IO] =
    HttpRoutes.of {
      case req @ POST -> Root / "schema" / schemaId =>
        val actionName = "uploadSchema"
        withJsonBody(req, actionName, schemaId) { json =>
          val jsonSchema = JsonSchema(schemaId, json.asJson.noSpaces)
          val res = for {
            _ <- jsonSchemaService.create(jsonSchema)
          } yield ApiResult.success(actionName, schemaId).asJson
          Created(res)
        }

      case GET -> Root / "schema" / schemaId =>
        Ok(jsonSchemaService.getById(schemaId).map(_.content))
    }

  def validateRoutes: HttpRoutes[IO] =
    HttpRoutes.of { case req @ POST -> Root / "validate" / schemaId =>
      val actionName = "validateDocument"
      withJsonBody(req, actionName, schemaId) { json =>
        val jsonNoNulls = json.deepDropNullValues.asJson.noSpaces
        val res = jsonSchemaService
          .validate(schemaId, jsonNoNulls)
          .map {
            case Right(_)  => ApiResult.success(actionName, schemaId)
            case Left(msg) => ApiResult.failure(actionName, schemaId, msg)
          }
          .map(_.asJson)
        Ok(res)
      }
    }

  private def withJsonBody[T](req: Request[IO], actionName: String, schemaId: String)(
      action: Json => IO[Response[IO]]
  ): IO[Response[IO]] =
    req
      .attemptAs[Json]
      .foldF(
        failure => {
          BadRequest(ApiResult.failure(actionName, schemaId, failure.message).asJson)
        },
        validJson => action(validJson)
      )

}
