package misk.web.http

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class HttpVersionTest {
  @Test
  fun `servlet protocol strings map to versions`() {
    assertThat(HttpVersion.fromServletRequestProtocol("HTTP/0.9")).isEqualTo(HttpVersion.HTTP_0_9)
    assertThat(HttpVersion.fromServletRequestProtocol("HTTP/1.0")).isEqualTo(HttpVersion.HTTP_1_0)
    assertThat(HttpVersion.fromServletRequestProtocol("HTTP/1.1")).isEqualTo(HttpVersion.HTTP_1_1)
    assertThat(HttpVersion.fromServletRequestProtocol("HTTP/2")).isEqualTo(HttpVersion.HTTP_2_0)
    assertThat(HttpVersion.fromServletRequestProtocol("HTTP/2.0")).isEqualTo(HttpVersion.HTTP_2_0)
    assertThat(HttpVersion.fromServletRequestProtocol("HTTP/3")).isEqualTo(HttpVersion.HTTP_3_0)
  }

  @Test
  fun `unknown protocol strings are rejected`() {
    assertThatThrownBy { HttpVersion.fromServletRequestProtocol("SPDY/3") }
      .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `every jetty version maps to a version`() {
    for (jetty in org.eclipse.jetty.http.HttpVersion.values()) {
      HttpVersion.fromJetty(jetty)
    }
  }
}
