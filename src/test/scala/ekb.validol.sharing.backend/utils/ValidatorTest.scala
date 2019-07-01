package ekb.validol.sharing.backend.utils

import org.scalatest.{FreeSpec, Matchers}

class ValidatorTest extends FreeSpec with Matchers {

  "Validator" - {
    "should properly validate correct email" in {
      val validEmails = Seq("foo@bar.com", "foo-bar@bar.com", "foo_bar@bar.com", "foo111@bar.com", "foo@bar111-24.com")
      val res = validEmails.map(Validator.isValidEmail)
      assert(!res.exists(_.isLeft))
    }

    "should properly validate incorrect email" in {
      val invalidEmails = Seq("foo", "12434g", "12@", "3435@113232^")
      val res = invalidEmails.map(Validator.isValidEmail)
      assert(!res.exists(_.isRight))
    }
  }

}
