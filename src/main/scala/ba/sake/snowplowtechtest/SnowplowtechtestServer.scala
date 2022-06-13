package ba.sake.snowplowtechtest

import cats.effect.{IO, Resource}
import cats.syntax.all._
import fs2.Stream
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import ba.sake.snowplowtechtest.routes.JsonSchemaValidationRoutes

object SnowplowtechtestServer {

  def stream: Stream[IO, Nothing] = {
    val httpApp = (
        JsonSchemaValidationRoutes.schemaRoutes <+>
        JsonSchemaValidationRoutes.validateRoutes
      ).orNotFound

      // With Middlewares in place
    val  finalHttpApp = Logger.httpApp(true, true)(httpApp)
    val bla = for {
      _ <- Stream(())
      

      exitCode <- Stream.resource(
        EmberServerBuilder.default[IO]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build >>
        Resource.eval(IO.never)
      )
    } yield exitCode
    bla
  }.drain
}
