package pl.datart.arbitup

import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import monix.eval._
import pl.datart.arbitup.alg.CycleFinderImpl
import pl.datart.arbitup.flow._
import pl.datart.arbitup.graph.GraphBuilderImpl
import pl.datart.arbitup.input._
import sttp.client3.SttpBackend

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
