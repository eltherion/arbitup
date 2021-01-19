package pl.datart.arbitup.flow

import cats.MonadError
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import pl.datart.arbitup.graph.GraphBuilder
import pl.datart.arbitup.input.RatesFetcher

trait Arbitrage[F[_]] {
  def run(url: String): F[Unit]
}

class ArbitrageImpl[F[_]](
    ratesFetcher: RatesFetcher[F],
    graphBuilder: GraphBuilder[F],
    taskPlanner: TaskPlanner[F]
)(implicit monadError: MonadError[F, Throwable])
    extends Arbitrage[F]
    with StrictLogging {
  def run(url: String): F[Unit] = {
    for {
      rates       <- ratesFetcher.getRates(url)
      graph       <- graphBuilder.build(rates)
      opportunity <- taskPlanner.scheduleAndRun(graph)
      _           <- monadError.pure {
                       logger.info(opportunity.fold("No opportunity found.")(op => s"Best found opportunity ${op.asString}"))
                     }
    } yield (())
  }
}
