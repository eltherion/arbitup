package pl.datart.arbitup.alg

import cats.MonadError
import cats.syntax.functor._
import org.jgrapht._
import org.jgrapht.alg.shortestpath._
import pl.datart.arbitup.model._

import scala.jdk.CollectionConverters._
import scala.util.Try

trait CycleFinder[F[_]] {
  def find(graph: Graph[Currency, Rate], v: Currency): F[Option[Opportunity]]
}

class CycleFinderImpl[F[_]](implicit monadError: MonadError[F, Throwable]) extends CycleFinder[F] {
  def find(graph: Graph[Currency, Rate], v: Currency): F[Option[Opportunity]] = {
    monadError.handleError {
      monadError
        .fromTry {
          Try(new BellmanFordShortestPath(graph).getPath(v, v))
        }
        .map(_ => Option.empty[Opportunity])
    } {
      case exception: NegativeCycleDetectedException =>
        val cycle        = exception.getCycle.getVertexList.asScala.toList.collect {
          case c: Currency => c
        }
        val edgesInCycle = exception.getCycle.getEdgeList.asScala.toList
        val rates        = edgesInCycle
          .collect {
            case r: Rate => r
          }
          .map(_.value)
        Option(Opportunity(cycle, rates, rates.product))
    }
  }
}
