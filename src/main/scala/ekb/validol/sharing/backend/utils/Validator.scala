package ekb.validol.sharing.backend.utils

object Validator {

  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  def isValidEmail(email: String): Either[String, String] = {
    if (emailRegex.findFirstMatchIn(email).isDefined) {
      Right(email)
    } else Left(s"Invalid email $email was received")
  }

}
