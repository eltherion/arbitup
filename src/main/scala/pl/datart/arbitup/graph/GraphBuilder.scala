package pl.datart.arbitup.graph

import cats.MonadError
import org.jgrapht.Graph
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import pl.datart.arbitup.model._

import scala.tools.nsc.tasty.SafeEq

trait GraphBuilder[F[_]] {
  def build(rates: Set[Rate]): F[Graph[Currency, Rate]]
}

class GraphBuilderImpl[F[_]](implicit monadError: MonadError[F, Throwable]) extends GraphBuilder[F] {
  def build(rates: Set[Rate]): F[Graph[Currency, Rate]] = {
    monadError.pure {
      val builder = SimpleDirectedWeightedGraph.createBuilder[Currency, Rate](classOf[Rate])
      val noLoops = rates.filter(r => !(r.from === r.to))
      builder.addVertices(noLoops.flatMap(r => Set(r.from, r.to)).toSeq: _*)
      noLoops.foreach(rate => builder.addEdge(rate.from, rate.to, rate, -Math.log(rate.value.toDouble)))
      builder.buildAsUnmodifiable()
    }
  }
}
