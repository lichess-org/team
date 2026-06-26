import java.net.URLDecoder
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Twilio:
  lazy val authToken = Env.get("TWILIO_AUTH_TOKEN")
  lazy val baseUrl = Env.get("APP_URL")

  def parseFormData(body: String): Map[String, String] =
    if body.isBlank then Map.empty
    else
      body
        .split("&")
        .flatMap { param =>
          param.split("=", 2) match
            case Array(key, value) =>
              Some(URLDecoder.decode(key, "UTF-8") -> URLDecoder.decode(value, "UTF-8"))
            case _ => None
        }
        .toMap

  def validateSignature(path: String, params: Map[String, String], signature: String): Boolean =
    val url = baseUrl + path
    val data = url + params.toSeq.sortBy(_._1).map((k, v) => k + v).mkString
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(SecretKeySpec(authToken.getBytes("UTF-8"), "HmacSHA1"))
    val computed = Base64.getEncoder.encodeToString(mac.doFinal(data.getBytes("UTF-8")))
    MessageDigest.isEqual(computed.getBytes("UTF-8"), signature.getBytes("UTF-8"))

  val emptyTwiml: String =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<Response />""".stripMargin

  val voicemailTwiml: String =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<Response>
      |  <Say voice="alice">Thank you for calling Lichess dot org. Please leave a message after the tone.</Say>
      |  <Record maxLength="120" transcribe="true" transcribeCallback="/twilio/call-complete" />
      |</Response>""".stripMargin

  val hangupTwiml: String =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<Response>
      |  <Hangup />
      |</Response>""".stripMargin
