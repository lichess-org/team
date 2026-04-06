import cask.model.Response
import views.Home

import scala.annotation.unused

object App extends cask.MainRoutes:
  override def port: Int = Env.get("PORT", "8080").toInt
  override def host: String = "0.0.0.0"

  private val lichessTeam =
    "Lichess team" // must match https://github.com/lichess-org/lila/blob/93488dc9894d455162a3278a66bf693631686c20/modules/core/src/main/perm.scala#L109
  private val homePage = Home.render(Env.get("VERSION", ""), lichessTeam)

  @cask.staticResources("/static")
  def staticEndpoint(): String = "."

  @cask.get("/")
  def home() = homePage

  @cask.get("/login")
  def login(): Response[String] =
    val codeVerifier = Pkce.generateCodeVerifier()

    cask.Response(
      "",
      headers = Seq("Location" -> Lichess.requestAuthorizationCode(codeVerifier).toString),
      statusCode = 302,
      cookies = Seq(
        cask.Cookie(
          name = "codeVerifier",
          value = codeVerifier,
          path = "/",
          httpOnly = true
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
      case (Some(desc), _) => cask.Response(s"Error during authentication: $desc", statusCode = 400)
      case (_, None) => cask.Response("Missing authorization code.", statusCode = 400)
      case (_, Some(code)) =>
        val res = for
          tokenRes = Lichess.obtainAccessToken(code, codeVerifier.value).body.map(_.access_token)
          token <- tokenRes.left.map(error => s"Failed to obtain access token: ${error.getMessage}")
          accountRes = Lichess.me(token, Map("wiki" -> "true")).body
          account <- accountRes.left.map(error => s"Failed to fetch account: ${error.getMessage}")
          isMember = account.groups.exists(_.contains(lichessTeam))
          _ <- Either.cond(isMember, (), s"Unauthorized: You must be a member of $lichessTeam.")
          inviteRes = Authentik.inviteLink(s"lichess-${account.username}", Map("lichess" -> account.username))
          inviteLink <- inviteRes.left.map(error => s"Failed to create invitation: ${error.getMessage}")
        yield cask.Response("", headers = Seq("Location" -> inviteLink.toStringSafe()), statusCode = 302)
        res.fold(errorMsg => cask.Response(errorMsg, statusCode = 500), identity)

  @cask.get("/healthcheck")
  def healthcheck(): Response[String] =
    Authentik.version() match
      case Right(_) => cask.Response("OK")
      case Left(error) =>
        cask.Response(s"Failed to connect to Authentik API: ${error.getMessage}", statusCode = 500)

  private val devMode = sys.env.get("AUTHENTIK_HOST").isEmpty

  @cask.post("/api/v3/stages/invitation/invitations/")
  def devAuthentikInvitations() =
    if devMode then
      cask.Response("""{"pk":"dev-invitation-token"}""", headers = Seq("Content-Type" -> "application/json"))
    else cask.Response("", statusCode = 404)

  @cask.get("/api/v3/admin/version/")
  def devAuthentikVersion() =
    if devMode then cask.Response("{}", headers = Seq("Content-Type" -> "application/json"))
    else cask.Response("", statusCode = 404)

  @cask.get("/if/flow/:flowSlug")
  def devAuthentikFlow(flowSlug: String, itoken: Option[String] = None) =
    if devMode then
      cask.Response(s"Success: Emulated enrollment for $flowSlug with token ${itoken.getOrElse("none")}")
    else cask.Response("", statusCode = 404)

  initialize()
