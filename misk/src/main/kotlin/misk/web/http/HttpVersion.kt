package misk.web.http

/** The HTTP protocol version of a call. */
enum class HttpVersion {
  HTTP_0_9,
  HTTP_1_0,
  HTTP_1_1,
  HTTP_2_0,
  HTTP_3_0;

  companion object {
    /** Maps [jakarta.servlet.ServletRequest.getProtocol], like `HTTP/1.1`, to an [HttpVersion]. */
    internal fun fromServletRequestProtocol(protocol: String): HttpVersion =
      when (protocol) {
        "HTTP/0.9" -> HTTP_0_9
        "HTTP/1.0" -> HTTP_1_0
        "HTTP/1.1" -> HTTP_1_1
        "HTTP/2", "HTTP/2.0" -> HTTP_2_0
        "HTTP/3", "HTTP/3.0" -> HTTP_3_0
        else -> throw IllegalArgumentException("Unrecognized HTTP protocol: $protocol")
      }

    internal fun fromJetty(version: org.eclipse.jetty.http.HttpVersion): HttpVersion =
      when (version) {
        org.eclipse.jetty.http.HttpVersion.HTTP_0_9 -> HTTP_0_9
        org.eclipse.jetty.http.HttpVersion.HTTP_1_0 -> HTTP_1_0
        org.eclipse.jetty.http.HttpVersion.HTTP_1_1 -> HTTP_1_1
        org.eclipse.jetty.http.HttpVersion.HTTP_2 -> HTTP_2_0
        org.eclipse.jetty.http.HttpVersion.HTTP_3 -> HTTP_3_0
      }
  }
}
