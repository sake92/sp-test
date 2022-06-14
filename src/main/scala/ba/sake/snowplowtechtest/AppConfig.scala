package ba.sake.snowplowtechtest

import cats.syntax.all._
import com.comcast.ip4s._
import pureconfig._
import pureconfig.error._
import pureconfig.generic.auto._

case class ServerConfig(port: Port)

case class DbConfig(
    driver: String,
    url: String,
    user: String,
    password: String,
    threads: Int
)

case class AppConfig(
    server: ServerConfig,
    db: DbConfig
)

object AppConfig {
  implicit val ip4sPortReader: ConfigReader[Port] =
    ConfigReader[Int].emap { n =>
      Port.fromInt(n).toRight(CannotConvert(n.toString, "Port", "Invalid port number"))
    }

  def load = ConfigSource.default
    .load[AppConfig]
    .leftMap(failures => new RuntimeException(failures.toString))
}
