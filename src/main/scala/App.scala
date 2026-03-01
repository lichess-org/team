import sttp.client4.quick.*
import sttp.model.Uri

object App extends cask.MainRoutes {
  override def port: Int = sys.env.getOrElse("PORT", "8080").toInt
  override def host: String = "0.0.0.0"

  def lichessHost = sys.env.getOrElse("LICHESS_HOST", "https://lichess.org")
  def clientId = "app.lichess.invites"
  def appUrl = sys.env.getOrElse("APP_URL", s"http://localhost:${port}")
  def redirectUri = s"$appUrl/callback"

  @cask.get("/")
  def home() = "Hello, World!"

  @cask.get("/login")
  def login() =
    val codeVerifier = PKCEUtil.generateCodeVerifier()

    val queryParams = Map(
      "response_type" -> "code",
      "client_id" -> clientId,
      "redirect_uri" -> redirectUri,
      "scope" -> "email:read web:mod",
      "code_challenge_method" -> "S256",
      "code_challenge" -> PKCEUtil.generateCodeChallenge(codeVerifier)
    )

    val uriWithQueryParams = uri"$lichessHost/oauth?$queryParams"

    cask.Response(
      s"Redirecting to: $uriWithQueryParams",
      headers = Seq("Location" -> uriWithQueryParams.toString()),
      statusCode = 302,
      cookies = Seq(
        cask.Cookie(
          name = "codeVerifier",
          value = codeVerifier,
          path = "/"
        )
      )
    )
  
  @cask.get("/callback")
  def callback(codeVerifier: cask.Cookie, code: String, state: Option[String]) =
    val params = Map(
      "client_id" -> clientId,
      "code" -> code,
      "code_verifier" -> codeVerifier.value,
      "grant_type" -> "authorization_code",
      "redirect_uri" -> redirectUri
    )

    val response = quickRequest
      .post(uri"$lichessHost/oauth")
      .body(params)
      .send()
    
    s"response: ${response.code}, body: ${response.body}"

  initialize()
}
