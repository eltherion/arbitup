package pl.datart.arbitup

import cats.effect.IO._
import cats.effect._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import pl.datart.arbitup.alg.CycleFinder
import pl.datart.arbitup.graph.GraphBuilder
import pl.datart.arbitup.input._
import pl.datart.arbitup.model._

class ArbitrageImplSpec extends AsyncWordSpec with Matchers with IOFutureOps {
  private implicit val cs: ContextShift[IO] = IO.contextShift(this.executionContext)

  private val testedImplementation =
    new ArbitrageImpl[IO](
      new RatesFetcher[IO] {
        override def getRates(url: String): IO[Set[Rate]] = IO.pure(Set.empty[Rate])
      },
      new GraphBuilder[IO] {
        override def build(rates: Set[Rate]): IO[Graph[Currency, Rate]] =
          IO.pure(new Graph[Currency, Rate] {})
      },
      new CycleFinder[IO]  {
        override def find(graph: Graph[Currency, Rate]): IO[Option[Opportunity]] =
          IO.pure(Option.empty[Opportunity])
      }
    )

  "A RatesParserImpl" can {

    "parse rates" should {

      "should return correct set of rates" in {
        testedImplementation
          .run("https://fx.priceonomics.com/v1/rates/")
          .map(_ shouldEqual (()))
      }
    }
  }
}
