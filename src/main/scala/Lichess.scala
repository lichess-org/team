import sttp.client4.*
import sttp.client4.circe.*
import sttp.model.Uri

case class LichessTokenResponse(
  access_token: String,
  expires_in: Int,
  token_type: String,
) derives io.circe.Decoder

object Lichess:
  val backend = DefaultSyncBackend()

  def lichessHost = sys.env.getOrElse("LICHESS_HOST", "https://lichess.org")
  def clientId = "app.lichess.invites"
  def redirectUri =
    val appUrl = sys.env.getOrElse("APP_URL", s"http://localhost:${sys.env.getOrElse("PORT", "8080")}")
    s"$appUrl/callback"

  def requestAuthorizationCode(codeVerifier: String): Uri =
    uri"$lichessHost/oauth?${Map(
      "response_type" -> "code",
      "client_id" -> clientId,
      "redirect_uri" -> redirectUri,
      "scope" -> "email:read web:mod",
      "code_challenge_method" -> "S256",
      "code_challenge" -> PKCEUtil.generateCodeChallenge(codeVerifier)
    )}"

  def obtainAccessToken(
    code: String,
    codeVerifier: String,
  ): Response[Either[ResponseException[String], LichessTokenResponse]] =
    basicRequest
      .post(uri"$lichessHost/api/token")
      .body(Map(
        "client_id" -> clientId,
        "code" -> code,
        "code_verifier" -> codeVerifier,
        "grant_type" -> "authorization_code",
        "redirect_uri" -> redirectUri
      ))
      .response(asJson[LichessTokenResponse])
      .send(backend)
