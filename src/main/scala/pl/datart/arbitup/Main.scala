package pl.datart.arbitup

import cats.MonadError
import cats.effect.IO._
import cats.effect._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.scalalogging.StrictLogging
import monix.eval._
import monix.execution.Scheduler.global
import pl.datart.arbitup.alg.CycleFinderImpl
import pl.datart.arbitup.flow._
import pl.datart.arbitup.graph.GraphBuilderImpl
import pl.datart.arbitup.input._
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

import scala.concurrent.ExecutionContext

@SuppressWarnings(Array("org.wartremover.warts.Any"))
class Main[F[_]](implicit
    monadError: MonadError[F, Throwable],
    sttpBackend: F[SttpBackend[F, Any]],
    taskLift: TaskLift[F],
    taskLike: TaskLike[F]
) {

  private val ratesParser  = new RatesParserImpl[F]()
  private val ratesFetcher = sttpBackend.map { backend =>
    implicit val sttpBackend: SttpBackend[F, Any] = backend
    new RatesFetcherImpl[F](ratesParser)
  }
  private val graphBuilder = new GraphBuilderImpl[F]()
  private val cycleFinder  = new CycleFinderImpl[F]()
  private val taskPlanner  = new TaskPlannerImpl[F](cycleFinder)
  private val arbitrage    = ratesFetcher.map { fetcher =>
    new ArbitrageImpl[F](
      ratesFetcher = fetcher,
      graphBuilder = graphBuilder,
      taskPlanner = taskPlanner
    )
  }

  def run: F[Unit] = {
    arbitrage
      .flatMap(_.run("https://fx.priceonomics.com/v1/rates/"))
  }
}

object MainCatsEffectIOImpl extends App with StrictLogging {

  private implicit val cs: ContextShift[IO]                  = IO.contextShift {
    ExecutionContext.fromExecutor(
      new java.util.concurrent.ForkJoinPool(Runtime.getRuntime.availableProcessors)
    )
  }
  private implicit val sttpBackend: IO[SttpBackend[IO, Any]] = AsyncHttpClientCatsBackend[IO]()
  private implicit val taskLift: TaskLift[IO]                = TaskLift.toIO(Task.catsEffect(global))

  new Main[IO]().run.unsafeRunAsync {
    case Right(_) =>
      System.exit(0)
    case Left(ex) =>
      logger.error(ex.getMessage)
      System.exit(1)
  }

}
