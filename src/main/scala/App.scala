import cask.model.Response
import views.Home

import scala.annotation.unused
object App extends cask.MainRoutes:
  override def port: Int = Env.get("PORT", "8080").toInt
  override def host: String = "0.0.0.0"

  @cask.staticResources("/static")
  def staticEndpoint(): String = "."

  @cask.get("/")
  def home() = Home.render(Authentik.host, Env.get("VERSION", ""))

  @cask.get("/login")
  def login(): Response[String] =
    val codeVerifier = PKCEUtil.generateCodeVerifier()
    val authUrl = Lichess.requestAuthorizationCode(codeVerifier)

    cask.Response(
      "",
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
  def callback(codeVerifier: cask.Cookie, code: String, @unused state: String): Response[String] =
    val response = Lichess.obtainAccessToken(code, codeVerifier.value)

    response.body match
      case Right(tokenResponse) =>
        val accountResponse = Lichess.me(tokenResponse.access_token, Map("wiki" -> "true"))
        accountResponse.body match
          case Right(account) if account.groups.exists(_.contains("Lichess team")) =>
            val inviteResponse =
              Authentik.inviteLink(s"lichess-${account.username}", Map("lichess" -> account.username))
            inviteResponse match
              case Right(inviteLink) =>
                cask.Response(
                  "",
                  headers = Seq("Location" -> inviteLink.toString()),
                  statusCode = 302
                )
              case Left(error) =>
                cask.Response(s"Failed to create invitation: ${error.getMessage}", statusCode = 500)
          case _ =>
            cask.Response(
              "Unauthorized: You must be a member of the Lichess team to access this service.",
              statusCode = 401
            )
      case Left(error) =>
        cask.Response(s"Failed to obtain access token: ${error.getMessage}", statusCode = 500)

  @cask.get("/healthcheck")
  def healthcheck(): Response[String] =
    Authentik.version() match
      case Right(_) => cask.Response("OK", statusCode = 200)
      case Left(error) =>
        cask.Response(s"Failed to connect to Authentik API: ${error.getMessage}", statusCode = 500)

  initialize()
