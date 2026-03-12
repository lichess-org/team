import io.circe.syntax.*
import io.circe.{ Decoder, Encoder }
import sttp.client4.*
import sttp.client4.circe.*
import sttp.model.HttpVersion

object Authentik:
  lazy val backend = DefaultSyncBackend()

  lazy val host = Env.get("AUTHENTIK_HOST", "http://localhost:9000")
  lazy val token = Env.get("AUTHENTIK_TOKEN")
  lazy val invitationFlow = Env.get("AUTHENTIK_INVITATION_FLOW", "enrollment-invitation")

  private lazy val baseRequest = basicRequest.auth.bearer(token).httpVersion(HttpVersion.HTTP_1_1)

  def inviteLink(name: String, attrs: Map[String, String] = Map.empty) =
    baseRequest
      .post(uri"$host/api/v3/stages/invitation/invitations/")
      .body(AuthentikInvitationRequest(name, attrs).asJson.noSpaces)
      .contentType("application/json")
      .response(asJson[AuthentikInvitationResponse])
      .send(backend)
      .body
      .map(invite => uri"$host/if/flow/${invitationFlow}/?${Map("itoken" -> invite.pk)}")

  /** Version check request used to verify connectivity and token permissions with the Authentik API.
    */
  def version() =
    baseRequest
      .get(uri"$host/api/v3/admin/version/")
      .response(asJson[AuthentikVersionResponse])
      .send(backend)
      .body

case class AuthentikInvitationRequest(
    name: String,
    fixed_data: Map[String, String],
    single_use: Boolean = true
) derives Encoder

case class AuthentikInvitationResponse(
    pk: String
) derives Decoder

case class AuthentikVersionResponse(
    build_hash: String,
    outdated: Boolean,
    outpost_outdated: Boolean,
    version_current: String,
    version_latest: String,
    version_latest_valid: Boolean
) derives Decoder
