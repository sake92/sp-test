package ba.sake.snowplowtechtest.db.repositories

import cats.effect.IO
import fs2.Stream
//import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import ba.sake.snowplowtechtest.db.models._

class JsonSchemaRepository(xa: Transactor[IO]) {

  def create(jsonSchema: JsonSchema): IO[Int] = {
    sql"INSERT INTO json_schema (schema_id, content) VALUES (${jsonSchema.schemaId}, ${jsonSchema.content})".update.run
      .transact(xa)
  }

  def getById(schemaId: String): IO[JsonSchema] = {
    sql"""SELECT schema_id, content FROM json_schema
          WHERE schema_id = $schemaId""".query[JsonSchema].unique.transact(xa)
  }

  def getAll: Stream[IO, JsonSchema] = {
    sql"SELECT schema_id, content FROM json_schema".query[JsonSchema].stream.transact(xa)
  }

}
