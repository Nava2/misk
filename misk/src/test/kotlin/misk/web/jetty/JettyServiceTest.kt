package misk.web.jetty

import java.nio.file.InvalidPathException
import misk.security.ssl.SslLoader
import misk.web.WebConfig
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.StatisticsHandler
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.util.thread.ThreadPool
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class JettyServiceTest {

  @Test
  fun `only file paths are accepted for unix domain sockets`() {
    requireFileUnixDomainSocketPath("/tmp/socket.sock")
    requireFileUnixDomainSocketPath("socket.sock")
    assertThatThrownBy { requireFileUnixDomainSocketPath("") }.isInstanceOf(IllegalArgumentException::class.java)
    assertThatThrownBy { requireFileUnixDomainSocketPath("@socket.sock") }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Abstract")
    assertThatThrownBy { requireFileUnixDomainSocketPath("\u0000socket.sock") }
      .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `stop suppresses exception when all nested exceptions are InvalidPathException`() {
    val server = mock(Server::class.java)
    val exception = InvalidPathException("http-ingress.sock", "Nul character not allowed")
    exception.addSuppressed(InvalidPathException("istio-proxy.sock", "Nul character not allowed"))
    exception.addSuppressed(InvalidPathException("grpc-ingress.sock", "Nul character not allowed"))
    `when`(server.isRunning).thenReturn(true)
    doThrow(exception).`when`(server).stop()

    val jettyService = jettyService(server)

    assertThatCode { jettyService.stop() }.doesNotThrowAnyException()
  }

  @Test
  fun `stop rethrows when an InvalidPathException has a different suppressed failure`() {
    val server = mock(Server::class.java)
    val exception = InvalidPathException("http-ingress.sock", "Nul character not allowed")
    exception.addSuppressed(RuntimeException("unexpected shutdown failure"))
    `when`(server.isRunning).thenReturn(true)
    doThrow(exception).`when`(server).stop()

    val jettyService = jettyService(server)

    assertThatThrownBy { jettyService.stop() }.isSameAs(exception)
  }

  @Test
  fun `stop rethrows exception when the primary failure is not an InvalidPathException`() {
    val server = mock(Server::class.java)
    val exception = RuntimeException("unexpected shutdown failure")
    exception.addSuppressed(InvalidPathException("http-ingress.sock", "Nul character not allowed"))
    `when`(server.isRunning).thenReturn(true)
    doThrow(exception).`when`(server).stop()

    val jettyService = jettyService(server)

    assertThatThrownBy { jettyService.stop() }.isSameAs(exception)
  }

  private fun jettyService(server: Server): JettyService {
    val jettyService =
      JettyService(
        sslLoader = mock(SslLoader::class.java),
        webActionsServlet = mock(WebActionsServlet::class.java),
        webConfig = WebConfig(port = 0),
        threadPool = mock(ThreadPool::class.java),
        connectionMetricsCollector = mock(JettyConnectionMetricsCollector::class.java),
        statisticsHandler = mock(StatisticsHandler::class.java),
        gzipHandler = mock(GzipHandler::class.java),
        http2RateControlFactory = mock(MeasuredWindowRateControl.Factory::class.java),
        servletFilters = emptySet(),
      )

    JettyService::class.java.getDeclaredField("server").apply {
      isAccessible = true
      set(jettyService, server)
    }

    return jettyService
  }
}
