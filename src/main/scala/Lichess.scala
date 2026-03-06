import io.circe.Decoder
import sttp.client4.*
import sttp.client4.circe.*
import sttp.model.{ HeaderNames, Uri }

object Lichess:
  val backend = DefaultSyncBackend()

  lazy val host        = Env.get("LICHESS_HOST", "https://lichess.org")
  val clientId         = "app.lichess.invites"
  lazy val appUrl      = Env.get("APP_URL", s"http://localhost:${Env.get("PORT", "8080")}")
  lazy val redirectUri = s"$appUrl/callback"
  lazy val userAgent   = s"$clientId ($appUrl)"

  def requestAuthorizationCode(codeVerifier: String): Uri =
    uri"$host/oauth?${Map(
        "response_type" -> "code",
        "client_id" -> clientId,
        "redirect_uri" -> redirectUri,
        "scope" -> "web:mod",
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

  def me(accessToken: String, queryParams: Map[String, String] = Map.empty) =
    basicRequest
      .get(uri"$host/api/account?$queryParams")
      .header(HeaderNames.UserAgent, userAgent)
      .header(HeaderNames.Authorization, s"Bearer $accessToken")
      .response(asJson[LichessAccountResponse])
      .send(backend)

case class LichessTokenResponse(access_token: String) derives Decoder

case class LichessAccountResponse(
    username: String,
    groups: Option[List[String]]
) derives Decoder
