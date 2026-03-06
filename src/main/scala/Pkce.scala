import java.nio.charset.StandardCharsets
import java.security.{ MessageDigest, SecureRandom }
import java.util.Base64

object PKCEUtil:
  private val random = new SecureRandom()
  private val encoder = Base64.getUrlEncoder.withoutPadding()

  def generateCodeVerifier(): String =
    val bytes = new Array[Byte](32)
    random.nextBytes(bytes)
    encoder.encodeToString(bytes)

  def generateCodeChallenge(verifier: String): String =
    val digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.UTF_8))
    encoder.encodeToString(digest)
