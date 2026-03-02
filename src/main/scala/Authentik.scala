import sttp.client4.*
import sttp.client4.circe.*
import sttp.model.Uri
import io.circe.syntax.*

object Authentik:
  val backend = DefaultSyncBackend()

  def host = Env.get("AUTHENTIK_HOST", "https://auth.lichess.app")
  def token = Env.get("AUTHENTIK_TOKEN")

  def newUser(
    username: String,
    email: String,
    groups: Option[List[String]],
  ): Response[Either[ResponseException[String], AuthentikNewUserResponse]] =
    basicRequest
      .auth.bearer(token)
      .post(uri"$host/api/v3/core/users/")
      .body(AuthentikNewUserRequest(username, email, groups).asJson.noSpaces)
      .contentType("application/json")
      .response(asJson[AuthentikNewUserResponse])
      .send(backend)
  
  def recoveryLink(userId: String) =
    val url = uri"$host/api/v3/core/users/$userId/recovery/"
    basicRequest
      .auth.bearer(token)
      .post(url)
      .response(asJson[AuthentikRecoveryResponse])
      .send(backend)

case class AuthentikNewUserRequest(
  username: String,
  email: String,
  groups: Option[List[String]],
) derives io.circe.Encoder

case class AuthentikNewUserResponse(
  access_token: String,
  expires_in: Int,
  token_type: String,
) derives io.circe.Decoder

case class AuthentikRecoveryResponse(
  id: String,
  username: String,
  email: Option[String],
  groups: Option[List[String]]
) derives io.circe.Decoder
