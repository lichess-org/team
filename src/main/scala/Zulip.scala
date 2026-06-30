import io.circe.Decoder
import sttp.client4.*
import sttp.client4.circe.*

enum EventType(val name: String):
  case Call extends EventType("call")
  case Sms extends EventType("sms")

enum Channel(val name: String):
  case AdminPhone extends Channel("admin-phone")

object Zulip:
  lazy val backend = DefaultSyncBackend()
  lazy val host = Env.get("ZULIP_HOST")
  lazy val email = Env.get("ZULIP_EMAIL")
  lazy val apiKey = Env.get("ZULIP_API_KEY")

  private lazy val baseRequest = basicRequest.auth.basic(email, apiKey)

  def phoneTopicName(typ: EventType, params: Map[String, String]): String =
    val phone = params.get("From")
    val city = params.get("FromCity")
    val state = params.get("FromState")
    val country = params.get("FromCountry")

    val location = List(city, state, country).flatten match
      case Nil => ""
      case parts => s" (${parts.mkString(", ")})"

    val maskedPhone = phone.fold("")(_.takeRight(4))
    s"${typ.name} ***$maskedPhone$location"

  def uploadFile(filename: String, bytes: Array[Byte]): Either[Exception, String] =
    baseRequest
      .post(uri"$host/api/v1/user_uploads")
      .multipartBody(multipart("filename", bytes).fileName(filename).contentType("audio/mpeg"))
      .response(asJson[ZulipUploadResponse])
      .send(backend)
      .body
      .map(_.uri)

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

case class ZulipUploadResponse(uri: String) derives Decoder

case class ZulipUserResponse(user_id: Int) derives Decoder

case class ChannelMessage(to: String, topic: String, content: String):
  def toForm: Seq[(String, String)] = Seq(
    "type" -> "stream",
    "to" -> to,
    "topic" -> topic,
    "content" -> content
  )
