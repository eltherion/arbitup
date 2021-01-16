package pl.datart.arbitup.input

import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import pl.datart.arbitup.model.Rate
import sttp.client3._
import sttp.client3.circe._

trait RatesFetcher[F[_]] {
  def getRates(url: String): F[Set[Rate]]
}

class RatesFetcherImpl[F[_]](ratesParser: RatesParser[F])(implicit
    backend: SttpBackend[F, Any],
    monadError: MonadError[F, Throwable]
) extends RatesFetcher[F] {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def getRates(url: String): F[Set[Rate]] = {
    for {
      request  <- monadError.pure(quickRequest.get(uri"$url").response(asJson[Map[String, String]]))
      response <- request.send[F, Any](backend)
      body     <- monadError.fromTry(response.body.toTry)
      rates    <- ratesParser.parse(body)
    } yield rates
  }
}
