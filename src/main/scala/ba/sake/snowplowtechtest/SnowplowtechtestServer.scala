package ba.sake.snowplowtechtest

import cats.effect.{IO, Resource}
import cats.syntax.all._
import fs2.Stream
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

import ba.sake.snowplowtechtest.db.DB
import ba.sake.snowplowtechtest.db.repositories._
import ba.sake.snowplowtechtest.services._
import ba.sake.snowplowtechtest.routes._

object SnowplowtechtestServer {

  def stream: Stream[IO, Nothing] = {
    for {
      config <- Stream.fromEither[IO](AppConfig.load)

      xa <- Stream.resource(DB.transactor(config))
      _ <- Stream.eval(DB.applyDbMigrations(xa))

      jsonSchemaRepository = new JsonSchemaRepository(xa)
      jsonSchemaService = new JsonSchemaService(jsonSchemaRepository)
      jsonSchemaValidationRoutes = new JsonSchemaValidationRoutes(jsonSchemaService)

      httpApp = (
        jsonSchemaValidationRoutes.schemaRoutes <+>
          jsonSchemaValidationRoutes.validateRoutes
      ).orNotFound

      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- Stream.resource(
        EmberServerBuilder
          .default[IO]
          .withHost(ipv4"0.0.0.0")
          .withPort(config.server.port)
          .withHttpApp(finalHttpApp)
          .build >>
          Resource.eval(IO.never)
      )
    } yield exitCode
  }.drain

}
