package dev.sku20.stopgap.app.health

import com.zaxxer.hikari.HikariDataSource
import dev.sku20.stopgap.ir.annotation.Creates
import io.helidon.health.checks.DeadlockHealthCheck
import io.helidon.health.checks.HeapMemoryHealthCheck
import io.helidon.webserver.observe.health.HealthObserver

object HealthConfig {

    @Creates
    fun healthObserver(dataSource: HikariDataSource): HealthObserver {
        return HealthObserver.builder()
            .useSystemServices(false)
            .addCheck(DeadlockHealthCheck.create())
            .addCheck(HeapMemoryHealthCheck.create())
            .addCheck(PostgresHealthCheck(dataSource))
            .build()
    }
}
