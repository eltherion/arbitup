package pl.datart.arbitup.input

import cats.effect._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import pl.datart.arbitup.IOFutureOps
import pl.datart.arbitup.model._

class RatesParserImplSpec extends AsyncWordSpec with Matchers with IOFutureOps {

  private val usdToJpy = "88.0778605"
  private val usdToUsd = "1.0000000"

  private val inputMap = Map[String, String](
    elems =
      "USD_JPY" -> usdToJpy,
    "USD_USD" -> usdToUsd
  )

  private val expectedRates = Set(
    Rate(from = Currency("USD"), to = Currency("JPY"), value = 88.0778605f),
    Rate(from = Currency("USD"), to = Currency("USD"), value = 1.0000000f)
  )

  private val testedImplementation = new RatesParserImpl[IO]()

  "A RatesParserImpl" can {

    "parse rates" should {

      "should return correct set of rates" in {

        testedImplementation
          .parse(inputMap)
          .map(_ shouldEqual expectedRates)
      }
    }
  }
}
