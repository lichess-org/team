object App extends cask.MainRoutes {
  override def port: Int = sys.env.getOrElse("PORT", "8080").toInt
  override def host: String = "0.0.0.0"

  @cask.get("/")
  def home() = "Hello, World!"

  @cask.get("/login")
  def login() =
    val codeVerifier = PKCEUtil.generateCodeVerifier()
    val authUrl = Lichess.requestAuthorizationCode(codeVerifier)
    cask.Response(
      s"Redirecting to: $authUrl",
      headers = Seq("Location" -> authUrl.toString()),
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
    val response = Lichess.obtainAccessToken(code, codeVerifier.value)
    s"response: ${response.code}, body: ${response.body}"

  initialize()
}
