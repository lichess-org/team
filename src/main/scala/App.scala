import cask.model.Response
import views.Home

import scala.annotation.unused
object App extends cask.MainRoutes:
  override def port: Int = Env.get("PORT", "8080").toInt
  override def host: String = "0.0.0.0"

  @cask.staticResources("/static")
  def staticEndpoint(): String = "."

  @cask.get("/")
  def home() = Home.render(Env.get("VERSION", ""))

  @cask.get("/login")
  def login(): Response[String] =
    val codeVerifier = PKCEUtil.generateCodeVerifier()

    cask.Response(
      "",
      headers = Seq("Location" -> Lichess.requestAuthorizationCode(codeVerifier).toString),
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
  def callback(
      codeVerifier: cask.Cookie,
      code: Option[String] = None,
      error_description: Option[String] = None,
      @unused state: Option[String] = None,
      @unused error: Option[String] = None
  ): Response[String] =
    (error_description, code) match
      case (Some(desc), _) =>
        cask.Response(s"Error during authentication: $desc", statusCode = 400)
      case (_, None) =>
        cask.Response("Missing authorization code.", statusCode = 400)
      case (_, Some(code)) =>
        Lichess.obtainAccessToken(code, codeVerifier.value).body match
          case Right(tokenResponse) =>
            Lichess.me(tokenResponse.access_token, Map("wiki" -> "true")).body match
              case Right(account) if account.groups.exists(_.contains("Lichess team")) =>
                Authentik.inviteLink(s"lichess-${account.username}", Map("lichess" -> account.username)) match
                  case Right(inviteLink) =>
                    cask.Response(
                      "",
                      headers = Seq("Location" -> inviteLink.toString),
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
      case Right(_) => cask.Response("OK")
      case Left(error) =>
        cask.Response(s"Failed to connect to Authentik API: ${error.getMessage}", statusCode = 500)

  initialize()
