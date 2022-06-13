package ba.sake.snowplowtechtest

import cats.effect.{IO, Resource}
import cats.syntax.all._
import fs2.Stream
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import ba.sake.snowplowtechtest.routes.JsonSchemaValidationRoutes
import pureconfig._
import pureconfig.generic.auto._
import AppConfig._

object SnowplowtechtestServer {


  def stream: Stream[IO, Nothing] = {
    val httpApp = (
      JsonSchemaValidationRoutes.schemaRoutes <+>
        JsonSchemaValidationRoutes.validateRoutes
    ).orNotFound

    val finalHttpApp = Logger.httpApp(true, true)(httpApp)

    for {
      config <- Stream.fromEither[IO](
        ConfigSource.default.load[AppConfig].leftMap(failures => new RuntimeException(failures.toString))
      )

      xa <- Stream.resource(DB.transactor(config))

      _ <- Stream(DB.applyDbMigrations(xa))

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
