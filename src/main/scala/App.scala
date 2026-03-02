import cask.model.Response

import scala.annotation.unused
import views.Home
object App extends cask.MainRoutes:
  override def port: Int = Env.get("PORT", "8080").toInt
  override def host: String = "0.0.0.0"

  @cask.get("/")
  def home() = Home.render(Authentik.host)

  @cask.get("/login")
  def login(): Response[String] =
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
  def callback(codeVerifier: cask.Cookie, code: String, @unused state: String): Response[String] =
    val response = Lichess.obtainAccessToken(code, codeVerifier.value)

    response.body match
      case Right(tokenResponse) =>
        val accountResponse = Lichess.me(tokenResponse.access_token, Some(Map("wiki" -> "true")))
        accountResponse.body match
          case Right(account) if account.groups.exists(_.contains("Lichess team")) =>
            val inviteResponse =
              Authentik.inviteLink(s"lichess-${account.username}", Map("lichess" -> account.username))
            inviteResponse match
              case Right(inviteLink) =>
                cask.Response(
                  s"Redirecting to: $inviteLink",
                  headers = Seq("Location" -> inviteLink.toString()),
                  statusCode = 302
                )
              case Left(error) =>
                cask.Response(s"Failed to create invitation: ${error.getMessage}", statusCode = 500)
          case Right(account) =>
            cask.Response(s"Error: ${account.username} is not in the `Lichess team`` group", statusCode = 403)
          case Left(error) =>
            cask.Response(s"Failed to fetch account details: ${error.getMessage}", statusCode = 500)
      case Left(error) =>
        cask.Response(s"Failed to obtain access token: ${error.getMessage}", statusCode = 500)

  initialize()
