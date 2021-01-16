package pl.datart.arbitup.alg

import pl.datart.arbitup.model._

trait CycleFinder[F[_]] {
  def find(graph: Graph[Currency, Rate]): F[Option[Opportunity]]
}
