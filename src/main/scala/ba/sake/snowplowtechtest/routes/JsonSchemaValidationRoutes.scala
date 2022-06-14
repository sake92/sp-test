package ba.sake.snowplowtechtest.routes

import cats.effect.IO
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import ba.sake.snowplowtechtest.services.JsonSchemaService
import ba.sake.snowplowtechtest.db.models.JsonSchema

class JsonSchemaValidationRoutes(
    jsonSchemaService: JsonSchemaService
) extends Http4sDsl[IO] {

  import ApiResult._

  def schemaRoutes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case req @ POST -> Root / "schema" / schemaId =>
        val res = for {
          jsonSchemaContent <- req.as[Json]
          jsonSchema = JsonSchema(schemaId, jsonSchemaContent.asJson.noSpaces)
          _ <- jsonSchemaService.create(jsonSchema)
        } yield jsonSchema.asJson
        Ok(res)
      case GET -> Root / "schema" / schemaId =>
        Ok(jsonSchemaService.getById(schemaId).map(_.content))
    }

  def validateRoutes: HttpRoutes[IO] =
    HttpRoutes.of[IO] { case req @  POST -> Root / "validate" / schemaId =>
      req
        .attemptAs[Json]
        .foldF(
          failure => BadRequest(failure.message),
          json => {
            val res = jsonSchemaService.validate(schemaId, json.deepDropNullValues.asJson.noSpaces).map {
              case Right(_) => Success("validateDocument", schemaId, "success")
              case Left(msg) => Failure("validateDocument", schemaId, "error", msg)
            }.map(_.asJson)
            Ok(res)
          }
        )
    }

}
