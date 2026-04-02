class PkceTest extends munit.FunSuite:

  test("generateCodeVerifier produces unique values"):
    assertNotEquals(Pkce.generateCodeVerifier(), Pkce.generateCodeVerifier())

  test("generateCodeChallenge matches RFC 7636 Appendix B test vector"):
    // https://datatracker.ietf.org/doc/html/rfc7636#appendix-B
    val verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
    val expected = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"
    assertEquals(Pkce.generateCodeChallenge(verifier), expected)
