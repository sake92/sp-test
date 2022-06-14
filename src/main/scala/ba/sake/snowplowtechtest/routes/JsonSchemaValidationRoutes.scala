package ba.sake.snowplowtechtest.routes

import cats.effect.IO
import io.circe._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.Request
import org.http4s.Response
import org.http4s.circe._

import ba.sake.snowplowtechtest.services.JsonSchemaService
import ba.sake.snowplowtechtest.db.models.JsonSchema
import org.http4s.MediaType

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
          jsonSchemaService.create(jsonSchema).flatMap {
            case Right(_)  => Created(ApiResult.success(actionName, schemaId).asJson)
            case Left(msg) => BadRequest(ApiResult.failure(actionName, schemaId, msg).asJson)
          }
        }

      case GET -> Root / "schema" / schemaId =>
        jsonSchemaService.getById(schemaId).flatMap {
          case Some(schema) => Ok(schema.content).map(_.withContentType(`Content-Type`(MediaType.application.json)))
          case None         => NotFound(ApiResult.failure("getSchema", schemaId, "Schema does not exist").asJson)
        }
    }

  def validateRoutes: HttpRoutes[IO] =
    HttpRoutes.of { case req @ POST -> Root / "validate" / schemaId =>
      val actionName = "validateDocument"
      withJsonBody(req, actionName, schemaId) { json =>
        val jsonNoNulls = json.deepDropNullValues.asJson.noSpaces
        jsonSchemaService
          .validate(schemaId, jsonNoNulls)
          .flatMap {
            case Right(_)  => Ok(ApiResult.success(actionName, schemaId).asJson)
            case Left(msg) => BadRequest(ApiResult.failure(actionName, schemaId, msg).asJson)
          }
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
