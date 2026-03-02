case class LichessTokenResponse(
  access_token: String,
  expires_in: Int,
  token_type: String,
) derives io.circe.Decoder
