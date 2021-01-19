package pl.datart.arbitup.alg

import monix.eval.Task
import monix.execution.Scheduler
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import pl.datart.arbitup.graph.GraphBuilderImpl
import pl.datart.arbitup.model._

class CycleFinderImplSpec extends AsyncWordSpec with Matchers {
  private implicit val scheduler: Scheduler = Scheduler.Implicits.global

  private val usd: Currency = Currency("USD")
  private val eur: Currency = Currency("EUR")
  private val gbp: Currency = Currency("GBP")
  private val jpy: Currency = Currency("JPY")
  private val btc: Currency = Currency("BTC")
  private val eth: Currency = Currency("ETH")

  private val testedImplementation = new CycleFinderImpl[Task]

  "A CycleFinderImpl" can {

    "finds arbitrage cycles in graph" should {

      "find an existing cycle" in {
        val rateUsdEur: Rate = Rate(
          from = usd,
          to = eur,
          value = 1.01f
        )
        val rateEurUsd: Rate = Rate(
          from = eur,
          to = usd,
          value = 1.0f
        )

        val expectedOpportunity = Option(Opportunity(cycle = List(usd, eur, usd), multiplier = 1.01f))

        val graph = new GraphBuilderImpl[Task]().build(Set(rateUsdEur, rateEurUsd))

        graph.flatMap { g =>
          testedImplementation
            .find(g, usd)
            .map(_ shouldEqual expectedOpportunity)
        }.runToFuture
      }

      "not find any negative cycles if they don't exists" in {
        val rateUsdEur: Rate = Rate(
          from = usd,
          to = eur,
          value = 1.0f
        )
        val rateEurUsd: Rate = Rate(
          from = eur,
          to = usd,
          value = 1.0f
        )

        val expectedOpportunity = Option.empty[Opportunity]

        val graph = new GraphBuilderImpl[Task]().build(Set(rateUsdEur, rateEurUsd))

        graph.flatMap { g =>
          testedImplementation
            .find(g, usd)
            .map(_ shouldEqual expectedOpportunity)
        }.runToFuture
      }

      "find the best cycle if more than one exist" in {
        val allCurrencies = List(usd, eur, gbp, jpy, btc, eth)

        val rates = allCurrencies
          .flatMap { c1 =>
            allCurrencies.map(c2 => (c1, c2))
          }
          .filterNot {
            case (c1, c2) => c1 === c2
          }
          .map {
            case (c1, c2) => Rate(c1, c2, 1.0f)
          }
          .collect {
            case Rate(Currency("USD"), Currency("EUR"), _) => Rate(usd, eur, 1.01f)
            case Rate(Currency("JPY"), Currency("ETH"), _) => Rate(jpy, eth, 1.01f)
            case Rate(Currency("ETH"), Currency("BTC"), _) => Rate(eth, btc, 1.02f)
            case Rate(Currency("BTC"), Currency("JPY"), _) => Rate(btc, jpy, 1.03f)
            case r                                         => r
          }
          .toSet

        val expectedOpportunity = Option(Opportunity(cycle = List(jpy, eth, btc, jpy), multiplier = 1.061106f))

        val graph = new GraphBuilderImpl[Task]().build(rates)

        graph.flatMap { g =>
          testedImplementation
            .find(g, usd)
            .map(_ shouldEqual expectedOpportunity)
        }.runToFuture
      }
    }
  }
}
