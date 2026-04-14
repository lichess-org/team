import io.circe.syntax.*
import io.circe.{ Decoder, Encoder }
import sttp.client4.*
import sttp.client4.circe.*

object Grafana:
  lazy val backend = DefaultSyncBackend()

  lazy val host = Env.get("GRAFANA_HOST")
  lazy val user = Env.get("GRAFANA_USER")
  lazy val password = Env.get("GRAFANA_PASSWORD")
  lazy val teamId = Env.get("GRAFANA_LICHESS_TEAM_ID")

  private lazy val baseRequest = basicRequest.auth.basic(user, password)

  def lookupUser(loginOrEmail: String) =
    baseRequest
      .get(uri"$host/api/users/lookup?loginOrEmail=$loginOrEmail")
      .response(asJson[GrafanaUserResponse])
      .send(backend)
      .body

  def org() =
    baseRequest
      .get(uri"$host/api/org")
      .response(asJson[GrafanaOrgResponse])
      .send(backend)
      .body

  def addTeamMember(userId: Int) =
    baseRequest
      .post(uri"$host/api/teams/$teamId/members")
      .body(GrafanaAddMemberRequest(userId).asJson.noSpaces)
      .contentType("application/json")
      .response(asJson[GrafanaMessageResponse])
      .send(backend)
      .body

case class GrafanaOrgResponse(id: Int, name: String) derives Decoder

case class GrafanaUserResponse(id: Int) derives Decoder

case class GrafanaAddMemberRequest(userId: Int) derives Encoder

case class GrafanaMessageResponse(message: String) derives Decoder
