import io.circe.Decoder
import sttp.client4.*
import sttp.client4.circe.*

enum Channel(val name: String):
  case AdminPhone extends Channel("admin-phone")

object Zulip:
  lazy val backend = DefaultSyncBackend()
  lazy val host = Env.get("ZULIP_HOST")
  lazy val email = Env.get("ZULIP_EMAIL")
  lazy val apiKey = Env.get("ZULIP_API_KEY")

  private lazy val baseRequest = basicRequest.auth.basic(email, apiKey)

  def phoneTopicName(phone: String): String =
    val last4 = phone.takeRight(4)
    s"***$last4"

  def healthcheck() =
    baseRequest
      .get(uri"$host/api/v1/users/me")
      .response(asJson[ZulipUserResponse])
      .send(backend)
      .body

  def send(channel: Channel, topic: String, message: String): Unit =
    baseRequest
      .post(uri"$host/api/v1/messages")
      .body(ChannelMessage(channel.name, topic, message).toForm*)
      .send(backend)

case class ZulipUserResponse(user_id: Int) derives Decoder

case class ChannelMessage(to: String, topic: String, content: String):
  def toForm: Seq[(String, String)] = Seq(
    "type" -> "stream",
    "to" -> to,
    "topic" -> topic,
    "content" -> content
  )
