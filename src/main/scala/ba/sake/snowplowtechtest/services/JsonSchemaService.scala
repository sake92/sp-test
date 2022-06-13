package ba.sake.snowplowtechtest.services

import cats.effect.IO
import fs2.Stream
import ba.sake.snowplowtechtest.db.models._
import ba.sake.snowplowtechtest.db.repositories._

class JsonSchemaService(jsonSchemaRepository: JsonSchemaRepository) {

  def create(jsonSchema: JsonSchema): IO[Int] =
    jsonSchemaRepository.create(jsonSchema)

  def getById(schemaId: String): IO[JsonSchema] =
    jsonSchemaRepository.getById(schemaId)

  def getAll: Stream[IO, JsonSchema] =
    jsonSchemaRepository.getAll
}
