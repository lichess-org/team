import sttp.model.StatusCode
object App extends cask.MainRoutes {
  override def port: Int = Env.get("PORT", "8080").toInt
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

    response.body match
      case Right(tokenResponse) =>
        val accountResponse = Lichess.me(tokenResponse.access_token, Some(Map("wiki" -> "true")))
        accountResponse.body match
          case Right(account) =>
            s"Hello, ${account.username}! Your email is ${account.email.getOrElse("not available")} and your groups are ${account.groups.mkString(", ")}."
          case Left(error) =>
            accountResponse.code match
              case StatusCode.Unauthorized => "You need the `Lichess Team` grant to access this resource."
              case _ => s"Failed to fetch account info: ${error.getMessage}"
      case Left(error) =>
        s"Failed to obtain access token: ${error.getMessage}"

  initialize()
}
