import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class TwilioTest extends munit.FunSuite:

  private def computeSignature(token: String, url: String, params: Map[String, String]): String =
    val data = url + params.toSeq.sortBy(_._1).map((k, v) => k + v).mkString
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(SecretKeySpec(token.getBytes("UTF-8"), "HmacSHA1"))
    Base64.getEncoder.encodeToString(mac.doFinal(data.getBytes("UTF-8")))

  test("parseFormData decodes URL-encoded pairs"):
    val result = Twilio.parseFormData("From=%2B15551234567&Body=Hello+world")
    assertEquals(result, Map("From" -> "+15551234567", "Body" -> "Hello world"))

  test("parseFormData returns empty map for blank input"):
    assertEquals(Twilio.parseFormData(""), Map.empty[String, String])
    assertEquals(Twilio.parseFormData("  "), Map.empty[String, String])

  test("parseFormData handles special characters"):
    val result = Twilio.parseFormData("Body=%26%3D%25")
    assertEquals(result, Map("Body" -> "&=%"))

  test("parseFormData handles single key-value pair"):
    val result = Twilio.parseFormData("key=value")
    assertEquals(result, Map("key" -> "value"))

  test("validateSignature accepts valid signature"):
    val token = Twilio.authToken
    val baseUrl = Twilio.baseUrl
    assume(token.nonEmpty, "TWILIO_AUTH_TOKEN not set")
    assume(baseUrl.nonEmpty, "APP_URL not set")

    val path = "/twilio/sms"
    val params = Map("Body" -> "test", "From" -> "+15551234567")
    val signature = computeSignature(token, baseUrl + path, params)

    assert(Twilio.validateSignature(path, params, signature))

  test("validateSignature rejects invalid signature"):
    assume(Twilio.authToken.nonEmpty, "TWILIO_AUTH_TOKEN not set")
    assert(!Twilio.validateSignature("/twilio/sms", Map.empty, "invalidsignature"))

  test("validateSignature rejects tampered params"):
    val token = Twilio.authToken
    val baseUrl = Twilio.baseUrl
    assume(token.nonEmpty, "TWILIO_AUTH_TOKEN not set")
    assume(baseUrl.nonEmpty, "APP_URL not set")

    val path = "/twilio/sms"
    val signature = computeSignature(token, baseUrl + path, Map("Body" -> "test"))

    assert(!Twilio.validateSignature(path, Map("Body" -> "tampered"), signature))

  test("phoneTopicName masks all but last 4 digits"):
    assertEquals(Zulip.phoneTopicName("+15551234567"), "***4567")

  test("phoneTopicName handles short input"):
    assertEquals(Zulip.phoneTopicName("1234"), "***1234")
    assertEquals(Zulip.phoneTopicName("12"), "***12")

  test("emptyTwiml contains Response"):
    assert(Twilio.emptyTwiml.contains("<Response />"))

  test("voicemailTwiml contains Say and Record"):
    assert(Twilio.voicemailTwiml.contains("<Say"))
    assert(Twilio.voicemailTwiml.contains("<Record"))
    assert(Twilio.voicemailTwiml.contains("transcribe=\"true\""))

  test("hangupTwiml contains Hangup"):
    assert(Twilio.hangupTwiml.contains("<Hangup />"))
