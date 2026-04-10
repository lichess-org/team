import cask.model.Response
import sttp.client4.ResponseException.UnexpectedStatusCode
import sttp.model.HeaderNames.{ ContentType, Location }
import views.Home

import scala.annotation.unused

object App extends cask.MainRoutes:
  override def port: Int = Env.get("PORT", "8080").toInt
  override def host: String = "0.0.0.0"

  private val lichessTeam =
    "Lichess team" // must match https://github.com/lichess-org/lila/blob/93488dc9894d455162a3278a66bf693631686c20/modules/core/src/main/perm.scala#L109
  private val requiredScopes = Seq("web:mod")
  private val homePage = Home.render(Env.get("VERSION", ""), lichessTeam, requiredScopes)

  @cask.staticResources("/static")
  def staticEndpoint(): String = "."

  @cask.get("/")
  def home() = homePage

  @cask.get("/login")
  def login(): Response[String] =
    val codeVerifier = Pkce.generateCodeVerifier()

    cask.Response(
      "",
      headers = Seq(Location -> Lichess.requestAuthorizationCode(codeVerifier, requiredScopes).toString),
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
      case (Some(desc), _) =>
        scribe.error(s"Error during authentication: $desc")
        cask.Response(s"Error during authentication: $desc", statusCode = 400)
      case (_, None) =>
        scribe.error("Missing authorization code.")
        cask.Response("Missing authorization code.", statusCode = 400)
      case (_, Some(code)) =>
        val res = for
          tokenRes = Lichess.obtainAccessToken(code, codeVerifier.value).body.map(_.access_token)
          token <- tokenRes.left.map(error => s"Failed to obtain access token: ${error.getMessage}")
          me <- Lichess.me(token).body.left.map(error => s"Failed to fetch account: ${error.getMessage}")
          meWithMyGroups = Lichess.me(token, Map("wiki" -> "true")).body
          account <- meWithMyGroups.left.map {
            case error: UnexpectedStatusCode[?] if error.body.toString.contains("Wiki access not granted") =>
              s"Unauthorized: ${me.username} is not a member of `$lichessTeam`"
            case error => s"Failed to fetch account ${me.username}: ${error.getMessage}"
          }
          isMember = account.groups.exists(_.contains(lichessTeam))
          _ <- Either.cond(
            isMember,
            (),
            s"Unauthorized: ${me.username} is not a member of `$lichessTeam`"
          )
          inviteRes = Authentik.inviteLink(s"lichess-${me.username}", Map("lichess" -> me.username))
          inviteLink <- inviteRes.left.map(error =>
            s"Failed to create invitation for ${me.username}: ${error.getMessage}"
          )
          _ = scribe.info(s"Success: Invite link generated for ${me.username}")
        yield cask.Response(
          "",
          headers = Seq(Location -> inviteLink.toStringSafe()),
          statusCode = 302
        )
        res.fold(
          errorMsg =>
            scribe.error(errorMsg); cask.Response(errorMsg, statusCode = 500)
          ,
          identity
        )

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
      cask.Response(
        """{"pk":"dev-invitation-token"}""",
        headers = Seq(ContentType -> "application/json")
      )
    else cask.Response("", statusCode = 404)

  @cask.get("/api/v3/admin/version/")
  def devAuthentikVersion() =
    if devMode then cask.Response("{}", headers = Seq(ContentType -> "application/json"))
    else cask.Response("", statusCode = 404)

  @cask.get("/if/flow/:flowSlug")
  def devAuthentikFlow(flowSlug: String, itoken: Option[String] = None) =
    if devMode then
      cask.Response(s"Success: Emulated enrollment for $flowSlug with token ${itoken.getOrElse("none")}")
    else cask.Response("", statusCode = 404)

  initialize()
