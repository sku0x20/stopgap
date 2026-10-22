package dev.sku20.stopgap.app.health

import io.helidon.health.HealthCheck
import io.helidon.health.HealthCheckResponse
import io.helidon.health.HealthCheckType
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Readiness, not liveness: restarting the app doesn't fix a down database, it only stops traffic until it's back.
 */
class PostgresHealthCheck(
    private val dataSource: DataSource,
) : HealthCheck {

    override fun type(): HealthCheckType = HealthCheckType.READINESS

    override fun name(): String = "postgres"

    // An outage is expected, so report DOWN (503) instead of throwing, which Helidon logs as an ERROR on every probe.
    override fun call(): HealthCheckResponse {
        try {
            val isValid = dataSource.connection.use { it.isValid(VALIDATION_TIMEOUT_SECONDS) }
            return HealthCheckResponse.builder().status(isValid).build()
        } catch (e: SQLException) {
            return HealthCheckResponse.builder()
                .status(false)
                .detail("error", e.message ?: e.javaClass.name)
                .build()
        }
    }

    private companion object {
        const val VALIDATION_TIMEOUT_SECONDS = 1
    }
}
