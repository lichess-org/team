import io.circe.syntax.*
import sttp.client4.*
import sttp.client4.circe.*

object Authentik:
  val backend = DefaultSyncBackend()

  def host = Env.get("AUTHENTIK_HOST", "https://auth.lichess.app")
  def token = Env.get("AUTHENTIK_TOKEN")

  def newUser(
      username: String,
      name: String,
      email: String,
      groups: List[String] = List.empty
  ): Response[Either[ResponseException[String], AuthentikNewUserResponse]] =
    basicRequest.auth
      .bearer(token)
      .httpVersion(sttp.model.HttpVersion.HTTP_1_1)
      .post(uri"$host/api/v3/core/users/")
      .body(AuthentikNewUserRequest(username, name, email, groups).asJson.noSpaces)
      .contentType("application/json")
      .response(asJson[AuthentikNewUserResponse])
      .send(backend)

  def recoveryLink(userId: String) =
    val url = uri"$host/api/v3/core/users/$userId/recovery/"
    basicRequest.auth
      .bearer(token)
      .httpVersion(sttp.model.HttpVersion.HTTP_1_1)
      .post(url)
      .response(asJson[AuthentikRecoveryResponse])
      .send(backend)

case class AuthentikNewUserRequest(
    username: String,
    name: String,
    email: String,
    groups: List[String]
) derives io.circe.Encoder

case class AuthentikNewUserResponse(
    pk: Int,
    username: String,
    uid: String,
    uuid: String
) derives io.circe.Decoder

case class AuthentikRecoveryResponse(
    link: String
) derives io.circe.Decoder
