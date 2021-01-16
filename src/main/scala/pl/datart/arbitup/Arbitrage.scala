package pl.datart.arbitup

import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.scalalogging.StrictLogging
import pl.datart.arbitup.alg.CycleFinder
import pl.datart.arbitup.graph.GraphBuilder
import pl.datart.arbitup.input.RatesFetcher

trait Arbitrage[F[_]] {
  def run(url: String): F[Unit]
}

class ArbitrageImpl[F[_]](
    ratesFetcher: RatesFetcher[F],
    graphBuilder: GraphBuilder[F],
    cycleFinder: CycleFinder[F]
)(implicit monadError: MonadError[F, Throwable])
    extends Arbitrage[F]
    with StrictLogging {
  def run(url: String): F[Unit] = {
    for {
      rates       <- ratesFetcher.getRates(url)
      graph       <- graphBuilder.build(rates)
      opportunity <- cycleFinder.find(graph)
      _           <- monadError.pure(logger.info(s"${opportunity.fold("No opportunity found.")(_.asString)}"))
    } yield (())
  }
}
