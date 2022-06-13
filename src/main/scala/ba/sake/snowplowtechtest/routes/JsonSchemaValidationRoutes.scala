package ba.sake.snowplowtechtest.routes

import cats.effect.IO
//import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object JsonSchemaValidationRoutes {

  def schemaRoutes: HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] {
      case POST -> Root / "schema" / schemaId =>
        Ok(s"Created a new schema under id: $schemaId")
      case GET -> Root / "schema" / schemaId =>
        Ok(s"Schema with id: $schemaId")
    }
  }

  def validateRoutes: HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] { case POST -> Root / "validate" / schemaId =>
      Ok(s"Validate against schema under id: $schemaId")
    }
  }
}
