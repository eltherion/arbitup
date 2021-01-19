package pl.datart.arbitup.alg

import cats.MonadError
import cats.syntax.functor._
import com.typesafe.scalalogging.StrictLogging
import org.jgrapht._
import org.jgrapht.alg.shortestpath._
import pl.datart.arbitup.model._

import scala.jdk.CollectionConverters._
import scala.util.Try

trait CycleFinder[F[_]] {
  def find(graph: Graph[Currency, Rate], v: Currency): F[Option[Opportunity]]
}

class CycleFinderImpl[F[_]](implicit monadError: MonadError[F, Throwable]) extends CycleFinder[F] with StrictLogging {
  def find(graph: Graph[Currency, Rate], v: Currency): F[Option[Opportunity]] = {
    monadError.handleError {
      monadError
        .fromTry {
          Try(new BellmanFordShortestPath(graph).getPath(v, v))
        }
        .map(_ => Option.empty[Opportunity])
    } {
      case exception: NegativeCycleDetectedException =>
        val cycle       = exception.getCycle.getVertexList.asScala.toList.collect {
          case c: Currency => c
        }
        val multiplier  = exception.getCycle.getEdgeList.asScala.toList
          .collect {
            case r: Rate => r
          }
          .map(_.value)
          .product
        val opportunity = Opportunity(cycle, multiplier)
        logger.info(s"Found opportunity ${opportunity.asString}")
        Option(Opportunity(cycle, multiplier))
    }
  }
}
