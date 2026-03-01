import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PKCEUtil {
  def generateCodeVerifier(): String = {
    val secureRandom = new SecureRandom()
    val bytes = new Array[Byte](32)
    secureRandom.nextBytes(bytes)
    Base64.getUrlEncoder.withoutPadding().encodeToString(bytes)
  }

  def generateCodeChallenge(verifier: String): String = {
    val sha256 = MessageDigest.getInstance("SHA-256")
    val digest = sha256.digest(verifier.getBytes("UTF-8"))
    Base64.getUrlEncoder.withoutPadding().encodeToString(digest)
  }

  def verifyCode(verifier: String, challenge: String): Boolean = {
    generateCodeChallenge(verifier) == challenge
  }
}
