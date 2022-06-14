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

  def create(jsonSchema: JsonSchema): IO[Int] =
    jsonSchemaRepository.create(jsonSchema)

  def getById(schemaId: String): IO[JsonSchema] =
    jsonSchemaRepository.getById(schemaId)

  def getAll: Stream[IO, JsonSchema] =
    jsonSchemaRepository.getAll

  def validate(schemaId: String, json: String): IO[Either[String, String]] = for {
    schema <- getById(schemaId)
    res = validateJson(schema.content, json)
  } yield res

  private def validateJson(schemaContent: String, json: String): Either[String, String] = {
    val schemaNode = JsonLoader.fromString(schemaContent)
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
