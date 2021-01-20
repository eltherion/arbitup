package pl.datart.arbitup.graph

import cats.MonadError
import org.jgrapht.Graph
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import pl.datart.arbitup.model._

trait GraphBuilder[F[_]] {
  def build(rates: Set[Rate]): F[Graph[Currency, Rate]]
}

class GraphBuilderImpl[F[_]](implicit monadError: MonadError[F, Throwable]) extends GraphBuilder[F] {
  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def build(rates: Set[Rate]): F[Graph[Currency, Rate]] = {
    monadError.pure {
      val builder = SimpleDirectedWeightedGraph.createBuilder[Currency, Rate](classOf[Rate])
      val noLoops = rates.filter(r => r.from.name != r.to.name)
      val _       = (
        builder.addVertices(noLoops.flatMap(r => Set(r.from, r.to)).toSeq: _*),
        noLoops.foreach(rate => builder.addEdge(rate.from, rate.to, rate, -Math.log(rate.value.toDouble)))
      )
      builder.buildAsUnmodifiable()
    }
  }
}
