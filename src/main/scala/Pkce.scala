import java.security.{ MessageDigest, SecureRandom }
import java.util.Base64

object PKCEUtil:
  private val random = new SecureRandom()

  def generateCodeVerifier(): String =
    val bytes = new Array[Byte](32)
    random.nextBytes(bytes)
    Base64.getUrlEncoder.withoutPadding().encodeToString(bytes)

  def generateCodeChallenge(verifier: String): String =
    val digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes("UTF-8"))
    Base64.getUrlEncoder.withoutPadding().encodeToString(digest)
