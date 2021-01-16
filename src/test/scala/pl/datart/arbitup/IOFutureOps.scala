package pl.datart.arbitup

import cats.effect.IO

import scala.concurrent.Future

trait IOFutureOps {
  implicit def toFuture[A]: (IO[A] => Future[A]) = _.unsafeToFuture()
}

object IOFutureOps extends IOFutureOps
