package ba.sake.snowplowtechtest.routes

import cats.effect.IO
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import ba.sake.snowplowtechtest.services.JsonSchemaService
import ba.sake.snowplowtechtest.db.models.JsonSchema

class JsonSchemaValidationRoutes(
    jsonSchemaService: JsonSchemaService
) extends Http4sDsl[IO] {

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
    HttpRoutes.of[IO] { case POST -> Root / "validate" / schemaId =>
      Ok(s"Validate against schema under id: $schemaId")
    }

}
