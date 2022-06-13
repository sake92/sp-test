package ba.sake.snowplowtechtest.db

import cats.effect.IO
import cats.effect.Resource
import org.flywaydb.core.Flyway
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import ba.sake.snowplowtechtest.AppConfig

object DB {

  def transactor(config: AppConfig): Resource[IO, HikariTransactor[IO]] = for {
    ec <- ExecutionContexts.fixedThreadPool[IO](config.db.threads)
    xa <- HikariTransactor.newHikariTransactor[IO](
      config.db.driver,
      config.db.url,
      config.db.user,
      config.db.password,
      ec
    )
  } yield xa

  def applyDbMigrations(transactor: HikariTransactor[IO]): IO[Unit] =
    transactor.configure { dataSource =>
      IO.blocking {
        println("Migrating database...")
        val flyWay = Flyway.configure().dataSource(dataSource).load()
        flyWay.migrate()
        ()
      }
    }
}
