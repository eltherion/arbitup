import coursierapi.MavenRepository

interp.repositories.update(
  interp.repositories() ::: List(MavenRepository.of("~/.m2/local"))
)

import $ivy.`org.typelevel::cats-effect:2.3.1`, cats._, cats.effect.IO._, cats.effect._, cats.syntax.flatMap._, cats.syntax.functor._
import $ivy.`org.codehaus.janino:janino:3.1.2`
import $ivy.`org.jgrapht:jgrapht-core:1.5.0`
import $ivy.`ch.qos.logback:logback-classic:1.2.3`
import $ivy.`io.monix::monix:3.3.0`, monix.eval._, monix.execution.Scheduler.global
import $ivy.`com.typesafe.scala-logging::scala-logging:3.9.2`, com.typesafe.scalalogging._
import $ivy.`org.scalatest::scalatest:3.2.3`
import $ivy.`com.softwaremill.sttp.client3::core:3.0.0`, sttp.client3.SttpBackend
import $ivy.`com.softwaremill.sttp.client3::async-http-client-backend-cats:3.0.0`, sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import $ivy.`com.softwaremill.sttp.client3::circe:3.0.0`
import $ivy.`pl.datart::arbitup:1.0.0`, pl.datart.arbitup.alg._, pl.datart.arbitup.graph._, pl.datart.arbitup.input._

import scala.concurrent.ExecutionContext

implicit val cs: ContextShift[IO]                  = IO.contextShift {
  ExecutionContext.fromExecutor(
    new java.util.concurrent.ForkJoinPool(Runtime.getRuntime.availableProcessors)
  )
}
implicit val sttpBackend: IO[SttpBackend[IO, Any]] = AsyncHttpClientCatsBackend[IO]()
implicit val taskLift: TaskLift[IO]                = TaskLift.toIO(Task.catsEffect(global))

def run(): Unit = {
  import pl.datart.arbitup.Main

  new Main[IO]().run.unsafeRunSync()
}

run()
