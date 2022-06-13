package ba.sake.snowplowtechtest

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import munit.CatsEffectSuite
import ba.sake.snowplowtechtest.routes.JsonSchemaValidationRoutes

class SchemaRoutesSpec extends CatsEffectSuite {

  test("HelloWorld returns status code 200") {
    assertIO(retHelloWorld.map(_.status) ,Status.Ok)
  }

  test("HelloWorld returns hello world message") {
    assertIO(retHelloWorld.flatMap(_.as[String]), "Schema with id: example123")
  }

  private[this] val retHelloWorld: IO[Response[IO]] = {
    val getHW = Request[IO](Method.GET, uri"/schema/example123")
    JsonSchemaValidationRoutes.schemaRoutes.orNotFound(getHW)
  }
}