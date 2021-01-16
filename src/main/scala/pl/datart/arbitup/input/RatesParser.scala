package pl.datart.arbitup.input

import cats.MonadError
import pl.datart.arbitup.model._

trait RatesParser[F[_]] {
  def parse(body: Map[String, String]): F[Set[Rate]]
}

class RatesParserImpl[F[_]](implicit monadError: MonadError[F, Throwable]) extends RatesParser[F] {
  def parse(body: Map[String, String]): F[Set[Rate]] = {
    monadError.pure {
      body.map {
        case (pairString, valueString) =>
          val Array(from, to) = pairString.split("_")
          Rate(Currency(from), Currency(to), valueString.toFloat)
      }.toSet
    }
  }
}
