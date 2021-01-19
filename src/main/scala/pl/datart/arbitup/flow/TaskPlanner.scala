package pl.datart.arbitup.flow

import monix.eval._
import monix.reactive.Observable
import org.jgrapht.Graph
import pl.datart.arbitup.alg.CycleFinder
import pl.datart.arbitup.model._

import scala.jdk.CollectionConverters._

trait TaskPlanner[F[_]] {
  def scheduleAndRun(graph: Graph[Currency, Rate]): F[Option[Opportunity]]
}

@SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
class TaskPlannerImpl[F[_]](cycleFinder: CycleFinder[F])(implicit taskLike: TaskLike[F], taskLift: TaskLift[F])
    extends TaskPlanner[F] {
  def scheduleAndRun(graph: Graph[Currency, Rate]): F[Option[Opportunity]] = {
    Observable
      .from {
        graph
          .vertexSet()
          .asScala
          .toSet
      }
      .mapParallelUnordered(
        parallelism = Runtime.getRuntime.availableProcessors
      )(c => Task.from(cycleFinder.find(graph, c)))
      .collect {
        case Some(op) => op
      }
      .maxByL(_.multiplier)
      .to[F]
  }
}
