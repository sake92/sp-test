package ba.sake.snowplowtechtest

import cats.effect.{ExitCode, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    SnowplowtechtestServer.stream.compile.drain.as(ExitCode.Success)
}
