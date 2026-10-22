package dev.sku20.stopgap.app.note.store

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.postgresql.PostgreSQLContainer

class PostgresStoreTest {

    private val store = PostgresStore(dataSource)

    companion object {

        private val postgres = PostgreSQLContainer("postgres:18-alpine").withDatabaseName("stopgap")

        private lateinit var dataSource: HikariDataSource

        @JvmStatic
        @BeforeAll
        fun startPostgres() {
            postgres.start()
            val config = HikariConfig()
            config.jdbcUrl = postgres.jdbcUrl
            config.username = postgres.username
            config.password = postgres.password
            dataSource = HikariDataSource(config)
        }

        @JvmStatic
        @AfterAll
        fun stopPostgres() {
            dataSource.close()
            postgres.stop()
        }

    }

}
