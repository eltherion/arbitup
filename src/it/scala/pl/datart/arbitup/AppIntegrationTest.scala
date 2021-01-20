package pl.datart.arbitup

import cats.effect.{ContextShift, IO}
import monix.eval.{Task, TaskLift}
import monix.execution.Scheduler.global
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.matchers.should.Matchers
import pl.datart.arbitup.Main
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

import scala.concurrent.ExecutionContext

class AppIntegrationTest extends AsyncWordSpec with Matchers {

  "A MainCatsEffectIOImpl" can {

    "execute for given args" should {

      "run correctly IO implementation" in {
        implicit val cs: ContextShift[IO]                  = IO.contextShift {
          ExecutionContext.fromExecutor(
            new java.util.concurrent.ForkJoinPool(Runtime.getRuntime.availableProcessors)
          )
        }
        implicit val sttpBackend: IO[SttpBackend[IO, Any]] = AsyncHttpClientCatsBackend[IO]()
        implicit val taskLift: TaskLift[IO]                = TaskLift.toIO(Task.catsEffect(global))

        new Main[IO].run.unsafeRunSync() shouldBe (())
      }
    }
  }
}
