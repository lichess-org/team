import io.circe.syntax.*
import sttp.client4.*
import sttp.client4.circe.*

object Authentik:
  val backend = DefaultSyncBackend()

  def host = Env.get("AUTHENTIK_HOST", "http://localhost:9000")
  def token = Env.get("AUTHENTIK_TOKEN")
  def invitationFlow = Env.get("AUTHENTIK_INVITATION_FLOW", "enrollment-invitation")

  def inviteLink(name: String, attrs: Map[String, String] = Map.empty) =
    val response = basicRequest.auth
      .bearer(token)
      .httpVersion(sttp.model.HttpVersion.HTTP_1_1)
      .post(uri"$host/api/v3/stages/invitation/invitations/")
      .body(AuthentikInvitationRequest(name, attrs).asJson.noSpaces)
      .contentType("application/json")
      .response(asJson[AuthentikInvitationResponse])
      .send(backend)

    response.body match
      case Right(invite) =>
        val inviteLink = uri"$host/if/flow/${invitationFlow}/?${Map("itoken" -> invite.pk)}"
        Right(inviteLink)
      case Left(error) =>
        println(s"Failed to create invitation: ${error.getMessage}")
        Left(error)

  /** Version check request used to verify connectivity and token permissions with the Authentik API.
    */
  def version() =
    basicRequest.auth
      .bearer(token)
      .httpVersion(sttp.model.HttpVersion.HTTP_1_1)
      .get(uri"$host/api/v3/admin/version/")
      .response(asJson[AuthentikVersionResponse])
      .send(backend)
      .body match
      case Right(version) => Right(version)
      case Left(error) =>
        Left(error)

case class AuthentikInvitationRequest(
    name: String,
    fixed_data: Map[String, String],
    single_use: Boolean = true
) derives io.circe.Encoder

case class AuthentikInvitationResponse(
    pk: String
) derives io.circe.Decoder

case class AuthentikVersionResponse(
    build_hash: String,
    outdated: Boolean,
    outpost_outdated: Boolean,
    version_current: String,
    version_latest: String,
    version_latest_valid: Boolean
) derives io.circe.Decoder
