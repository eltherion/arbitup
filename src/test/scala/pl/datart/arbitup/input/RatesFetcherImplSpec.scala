package pl.datart.arbitup.input

import cats.effect._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import pl.datart.arbitup.IOFutureOps
import pl.datart.arbitup.model._
import sttp.client3._
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.testing.SttpBackendStub

class RatesFetcherImplSpec extends AsyncWordSpec with Matchers with IOFutureOps {

  private implicit val cs: ContextShift[IO] = IO.contextShift(this.executionContext)
  private val usdToJpy                      = "88.0778605"
  private val usdToUsd                      = "1.0000000"
  private val url                           = "https://fx.priceonomics.com/v1/rates/"

  private val inputMap = Map[String, String](
    elems =
      "USD_JPY" -> usdToJpy,
    "USD_USD" -> usdToUsd
  )

  private implicit val testingBackend: SttpBackendStub[IO, Any] = AsyncHttpClientCatsBackend
    .stub[IO]
    .whenAnyRequest
    .thenRespond {
      Right[ResponseException[String, Error], Map[String, String]](inputMap)
    }

  private val expectedRates = Set(
    Rate(from = Currency("USD"), to = Currency("JPY"), value = 88.0778605f),
    Rate(from = Currency("USD"), to = Currency("USD"), value = 1.0000000f)
  )

  private val testingRatesParser = new RatesParser[IO] {
    def parse(body: Map[String, String]): IO[Set[Rate]] = {
      body match {
        case b if b === inputMap =>
          IO.pure(expectedRates)
        case other               =>
          IO.raiseError[Set[Rate]] {
            new Throwable(s"Output map ${other.toString()} doesn't mach expected map ${inputMap.toString()}.")
          }
      }
    }
  }

  private val testedImplementation = new RatesFetcherImpl[IO](testingRatesParser)

  "A RatesFetcherImpl" can {

    "fetch exchange rate" should {

      "should fetch them correctly when no error happens" in {

        testedImplementation
          .getRates(url)
          .map(_ shouldEqual expectedRates)
      }
    }
  }
}
