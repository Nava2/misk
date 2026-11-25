package misk.web.jetty

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.junit.jupiter.api.Test

class ShutdownIdleTimeoutTest {
  private fun connector(idleTimeout: Long, shutdownIdleTimeout: Long = 1000L) =
    ServerConnector(Server()).apply {
      this.idleTimeout = idleTimeout
      // Jetty 12.1's setIdleTimeout() no longer touches this, so set it after to model both Jetty versions.
      this.shutdownIdleTimeout = shutdownIdleTimeout
    }

  @Test
  fun `zero idle timeout means a zero shutdown idle timeout`() {
    assertThat(connector(idleTimeout = 0).also { it.applyShutdownIdleTimeout() }.shutdownIdleTimeout).isEqualTo(0)
  }

  @Test
  fun `idle timeout below one second bounds the shutdown idle timeout`() {
    for (idle in listOf(1L, 10L, 500L, 999L)) {
      val connector = connector(idleTimeout = idle).also { it.applyShutdownIdleTimeout() }
      assertThat(connector.shutdownIdleTimeout).describedAs("idleTimeout=$idle").isEqualTo(idle)
    }
  }

  @Test
  fun `idle timeout of one second or more keeps the one second default`() {
    for (idle in listOf(1000L, 1001L, 30_000L)) {
      val connector = connector(idleTimeout = idle).also { it.applyShutdownIdleTimeout() }
      assertThat(connector.shutdownIdleTimeout).describedAs("idleTimeout=$idle").isEqualTo(1000)
    }
  }

  @Test
  fun `an explicit override always wins`() {
    val connector = connector(idleTimeout = 5).also { it.applyShutdownIdleTimeout(override = 1234) }
    assertThat(connector.shutdownIdleTimeout).isEqualTo(1234)
  }

  @Test
  fun `the result matches what Jetty's own setIdleTimeout does today`() {
    // Documents equivalence on the Jetty versions that still clamp; remove this test when upgrading past 12.0.
    for (idle in listOf(0L, 1L, 500L, 999L, 1000L, 1001L, 30_000L)) {
      val jetty = ServerConnector(Server()).apply { idleTimeout = idle }
      val derived = connector(idleTimeout = idle).also { it.applyShutdownIdleTimeout() }
      assertThat(derived.shutdownIdleTimeout).describedAs("idleTimeout=$idle").isEqualTo(jetty.shutdownIdleTimeout)
    }
  }
}
