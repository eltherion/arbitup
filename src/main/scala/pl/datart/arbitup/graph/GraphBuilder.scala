package pl.datart.arbitup.graph

import pl.datart.arbitup.model._

trait GraphBuilder[F[_]] {
  def build(rates: Set[Rate]): F[Graph[Currency, Rate]]
}
