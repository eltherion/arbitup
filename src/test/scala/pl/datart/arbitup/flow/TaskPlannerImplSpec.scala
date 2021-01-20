package pl.datart.arbitup.flow

import monix.eval.Task
import monix.execution.Scheduler
import org.jgrapht.Graph
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import pl.datart.arbitup.alg.CycleFinder
import pl.datart.arbitup.model._

class TaskPlannerImplSpec extends AsyncWordSpec with Matchers {
  private implicit val scheduler: Scheduler = Scheduler.Implicits.global

  private val usd: Currency = Currency("USD")
  private val eur: Currency = Currency("EUR")

  private val rateUsdEur: Rate = Rate(
    from = usd,
    to = eur,
    value = 1.01f
  )
  private val rateEurUsd: Rate = Rate(
    from = eur,
    to = usd,
    value = 1.0f
  )

  private val expectedOpportunity = Option(
    Opportunity(cycle = List(usd, eur, usd), rates = List(1.01f, 1.0f), multiplier = 1.01f)
  )

  private val graph = {
    val g = new SimpleDirectedWeightedGraph[Currency, Rate](classOf[Rate])
    val _ = (
      g.addVertex(usd),
      g.addVertex(eur),
      g.addEdge(usd, eur, rateUsdEur),
      g.addEdge(eur, usd, rateEurUsd),
      g.setEdgeWeight(rateUsdEur, rateUsdEur.value.toDouble),
      g.setEdgeWeight(rateEurUsd, rateEurUsd.value.toDouble)
    )
    g
  }

  private val cycleFinder = new CycleFinder[Task] {
    def find(graph: Graph[Currency, Rate], v: Currency): Task[Option[Opportunity]] = {
      Task.pure(expectedOpportunity)
    }
  }

  private val testedImplementation = new TaskPlannerImpl[Task](cycleFinder)

  "A TaskPlannerImpl" can {

    "schedule and run tasks" should {

      "should end successfully for valid input finding an opportunity" in {
        testedImplementation
          .scheduleAndRun(graph)
          .map(_ shouldEqual expectedOpportunity)
          .runToFuture
      }
    }
  }
}
