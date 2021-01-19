package pl.datart.arbitup

import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.matchers.should.Matchers

class AppIntegrationTest extends AsyncWordSpec with Matchers {

  "A MainCatsEffectIOImpl" can {

    "execute for given args" should {

      "run correctly IO implementation" in {

        MainCatsEffectIOImpl.main(Array()) shouldBe (())
      }
    }
  }
}
