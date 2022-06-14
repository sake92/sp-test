package ba.sake.snowplowtechtest.services

import scala.jdk.CollectionConverters._
import cats.effect.IO
import fs2.Stream
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.github.fge.jackson.JsonLoader

import ba.sake.snowplowtechtest.db.models._
import ba.sake.snowplowtechtest.db.repositories._

class JsonSchemaService(jsonSchemaRepository: JsonSchemaRepository) {

  private val validator = JsonSchemaFactory.byDefault.getValidator

  def create(jsonSchema: JsonSchema): IO[Either[String, Int]] =
    jsonSchemaRepository.getById(jsonSchema.schemaId).flatMap {
      case None    => jsonSchemaRepository.create(jsonSchema).map(Right(_))
      case Some(_) => IO.pure(Left(s"Schema with id '${jsonSchema.schemaId}' already exists"))
    }

  def getById(schemaId: String): IO[Option[JsonSchema]] =
    jsonSchemaRepository.getById(schemaId)

  def getAll: Stream[IO, JsonSchema] =
    jsonSchemaRepository.getAll

  def validate(schemaId: String, json: String): IO[Either[String, String]] = for {
    schemaOpt <- getById(schemaId)
    res = validateJson(schemaId, schemaOpt, json)
  } yield res

  private def validateJson(schemaId: String, schemaOpt: Option[JsonSchema], json: String): Either[String, String] = {
    schemaOpt.toRight(s"Schema with id $schemaId does not exist").flatMap { schema =>
      val schemaNode = JsonLoader.fromString(schema.content)
      val jsonNode = JsonLoader.fromString(json)
      val res = validator.validate(schemaNode, jsonNode, true)
      if (res.isSuccess) Right("success")
      else {
        val errors = res.iterator.asScala.map { processingMessage =>
          val messageNode = processingMessage.asJson
          val path = messageNode.at("/instance/pointer").asText
          val message = messageNode.at("/message").asText
          s"""Property '$path' error: $message"""
        }
        Left(errors.mkString(";"))
      }
    }

  }
}
