package pl.datart.arbitup.flow

import monix.eval.Task
import monix.execution.Scheduler
import org.jgrapht.Graph
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import pl.datart.arbitup.graph.GraphBuilder
import pl.datart.arbitup.input._
import pl.datart.arbitup.model._

import java.util.function.Supplier

class ArbitrageImplSpec extends AsyncWordSpec with Matchers {
  private implicit val scheduler: Scheduler = Scheduler.Implicits.global

  private val testedImplementation =
    new ArbitrageImpl[Task](
      new RatesFetcher[Task] {
        def getRates(url: String): Task[Set[Rate]] = Task.pure(Set.empty[Rate])
      },
      new GraphBuilder[Task] {
        def build(rates: Set[Rate]): Task[Graph[Currency, Rate]] =
          Task.pure(
            new SimpleDirectedWeightedGraph[Currency, Rate](
              new Supplier[Currency] {
                def get(): Currency = List.empty[Currency].iterator.next()
              },
              new Supplier[Rate]     {
                def get(): Rate = List.empty[Rate].iterator.next()
              }
            )
          )
      },
      new TaskPlanner[Task]  {
        def scheduleAndRun(graph: Graph[Currency, Rate]): Task[Option[Opportunity]] =
          Task.pure(Option.empty[Opportunity])
      }
    )

  "An ArbitrageImpl" can {

    "run arbitrage" should {

      "should end successfully when no errors happen" in {
        testedImplementation
          .run("https://fx.priceonomics.com/v1/rates/")
          .map(_ shouldEqual (()))
          .runToFuture
      }
    }
  }
}
