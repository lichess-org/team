import sttp.client4.*
import sttp.client4.circe.*
import sttp.model.Uri
import sttp.model.HeaderNames
import io.circe.Decoder

object Lichess:
  val backend = DefaultSyncBackend()

  def host = Env.get("LICHESS_HOST", "https://lichess.org")
  def clientId = "app.lichess.invites"
  def appUrl = Env.get("APP_URL", s"http://localhost:${Env.get("PORT", "8080")}")
  def redirectUri = s"$appUrl/callback"
  def userAgent = s"$clientId ($appUrl)"

  def requestAuthorizationCode(codeVerifier: String): Uri =
    uri"$host/oauth?${Map(
        "response_type" -> "code",
        "client_id" -> clientId,
        "redirect_uri" -> redirectUri,
        "scope" -> "email:read web:mod",
        "code_challenge_method" -> "S256",
        "code_challenge" -> PKCEUtil.generateCodeChallenge(codeVerifier)
      )}"

  def obtainAccessToken(
      code: String,
      codeVerifier: String
  ): Response[Either[ResponseException[String], LichessTokenResponse]] =
    basicRequest
      .post(uri"$host/api/token")
      .header(HeaderNames.UserAgent, userAgent)
      .body(
        Map(
          "client_id" -> clientId,
          "code" -> code,
          "code_verifier" -> codeVerifier,
          "grant_type" -> "authorization_code",
          "redirect_uri" -> redirectUri
        )
      )
      .response(asJson[LichessTokenResponse])
      .send(backend)

  def me(accessToken: String, queryParams: Option[Map[String, String]] = None) =
    val url = uri"$host/api/account?${queryParams.getOrElse(Map.empty)}"
    basicRequest
      .get(url)
      .header(HeaderNames.UserAgent, userAgent)
      .header(HeaderNames.Authorization, s"Bearer $accessToken")
      .response(asJson[LichessAccountResponse])
      .send(backend)

case class LichessTokenResponse(
    access_token: String,
    expires_in: Int,
    token_type: String
) derives Decoder

case class LichessAccountResponse(
    id: String,
    username: String,
    email: Option[String],
    groups: Option[List[String]]
) derives Decoder
