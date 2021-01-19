package pl.datart.arbitup.input

import monix.eval.Task
import monix.execution.Scheduler
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import pl.datart.arbitup.model._
import sttp.client3._
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.testing.SttpBackendStub

class RatesFetcherImplSpec extends AsyncWordSpec with Matchers {

  private implicit val scheduler: Scheduler = Scheduler.Implicits.global

  private val usdToJpy = "88.0778605"
  private val usdToUsd = "1.0000000"
  private val url      = "https://fx.priceonomics.com/v1/rates/"

  private val inputMap = Map[String, String](
    elems =
      "USD_JPY" -> usdToJpy,
    "USD_USD" -> usdToUsd
  )

  private implicit val testingBackend: SttpBackendStub[Task, Any] = AsyncHttpClientCatsBackend
    .stub[Task]
    .whenAnyRequest
    .thenRespond {
      Right[ResponseException[String, Error], Map[String, String]](inputMap)
    }

  private val expectedRates = Set(
    Rate(from = Currency("USD"), to = Currency("JPY"), value = 88.0778605f),
    Rate(from = Currency("USD"), to = Currency("USD"), value = 1.0000000f)
  )

  private val testingRatesParser = new RatesParser[Task] {
    def parse(body: Map[String, String]): Task[Set[Rate]] = {
      body match {
        case b if b === inputMap =>
          Task.pure(expectedRates)
        case other               =>
          Task.raiseError[Set[Rate]] {
            new Throwable(s"Output map ${other.toString()} doesn't mach expected map ${inputMap.toString()}.")
          }
      }
    }
  }

  private val testedImplementatTaskn = new RatesFetcherImpl[Task](testingRatesParser)

  "A RatesFetcherImpl" can {

    "fetch exchange rate" should {

      "should fetch them correctly when no error happens" in {

        testedImplementatTaskn
          .getRates(url)
          .map(_ shouldEqual expectedRates)
          .runToFuture
      }
    }
  }
}
