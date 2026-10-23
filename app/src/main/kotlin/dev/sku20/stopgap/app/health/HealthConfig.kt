package dev.sku20.stopgap.app.health

import com.zaxxer.hikari.HikariDataSource
import dev.sku20.stopgap.ir.annotation.Creates
import io.helidon.health.checks.DeadlockHealthCheck
import io.helidon.health.checks.HeapMemoryHealthCheck
import io.helidon.webserver.observe.ObserveFeature
import io.helidon.webserver.observe.health.HealthObserver

object HealthConfig {

    @Creates
    fun observeFeature(dataSource: HikariDataSource): ObserveFeature {
        val health = HealthObserver.builder()
            .useSystemServices(false)
            .addCheck(DeadlockHealthCheck.create())
            .addCheck(HeapMemoryHealthCheck.create())
            .addCheck(PostgresHealthCheck(dataSource))
            .build()
        return ObserveFeature.builder()
            .observersDiscoverServices(false)
            .addObserver(health)
            .build()
    }
}
