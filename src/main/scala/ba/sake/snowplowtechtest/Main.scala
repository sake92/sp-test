package ba.sake.snowplowtechtest

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    SnowplowtechtestServer.stream[IO].compile.drain.as(ExitCode.Success)
}
