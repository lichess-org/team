import sttp.model.StatusCode
import Authentik.newUser
import cask.model.Response
object App extends cask.MainRoutes:
  override def port: Int = Env.get("PORT", "8080").toInt
  override def host: String = "0.0.0.0"

  @cask.get("/")
  def home() = "Hello, World!"

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
  def callback(codeVerifier: cask.Cookie, code: String, state: Option[String]): Response[String] =
    val response = Lichess.obtainAccessToken(code, codeVerifier.value)

    response.body match
      case Right(tokenResponse) =>
        val accountResponse = Lichess.me(tokenResponse.access_token, Some(Map("wiki" -> "true")))
        accountResponse.body match
          case Right(account) =>
            account.email match
              case Some(email) =>
                val response = newUser(
                  username = account.username,
                  name = account.username,
                  email = email
                )
                response.body match
                  case Right(newUserResponse) =>
                    val recoveryResponse = Authentik.recoveryLink(newUserResponse.uid)
                    recoveryResponse.body match
                      case Right(recovery) =>
                        cask.Response(
                          s"Redirecting to ${recovery.link}",
                          headers = Seq("Location" -> recovery.link),
                          statusCode = 302
                        )
                      case Left(error) =>
                        cask.Response(
                          s"User created but failed to generate recovery link: ${error.getMessage}",
                          statusCode = 500
                        )
                  case Left(error) =>
                    cask.Response(s"Failed to create user: ${error.getMessage}", statusCode = 500)
              case None =>
                cask.Response(
                  s"Hello, ${account.username}! Your email is ${account.email.getOrElse("not available")} and your groups are ${account.groups.mkString(", ")}.",
                  statusCode = 200
                )
          case Left(error) =>
            accountResponse.code match
              case StatusCode.Unauthorized =>
                cask.Response("You need the `Lichess Team` grant to access this resource.", statusCode = 403)
              case _ => cask.Response(s"Failed to fetch account info: ${error.getMessage}", statusCode = 500)
      case Left(error) =>
        cask.Response(s"Failed to obtain access token: ${error.getMessage}", statusCode = 500)

  initialize()
