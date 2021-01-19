package pl.datart.arbitup.graph

import monix.eval.Task
import monix.execution.Scheduler
import org.scalatest.Succeeded
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import pl.datart.arbitup.model._

class GraphBuilderImplSpec extends AsyncWordSpec with Matchers {
  private implicit val scheduler: Scheduler = Scheduler.Implicits.global

  private val testedImplementation = new GraphBuilderImpl[Task]()

  "A GraphBuilderImpl" can {

    "build graph" should {

      "should return proper graph for given rates" in {
        val usd = Currency("USD")
        val eur = Currency("EUR")

        val rates = Set[Rate](
          Rate(
            from = usd,
            to = eur,
            value = 0.8f
          ),
          Rate(
            from = eur,
            to = usd,
            value = 1.2f
          )
        )

        testedImplementation
          .build(
            rates
          )
          .map { graph =>
            assert {
              List(
                graph.vertexSet().size() shouldEqual 2,
                graph.vertexSet() should contain allElementsOf Set(usd, eur),
                graph.edgeSet().size() shouldEqual 2,
                graph.edgeSet() should contain allElementsOf rates
              ).forall(_ === Succeeded)
            }
          }
          .runToFuture
      }
    }
  }
}
