package dev.sku20.stopgap.app.note.store

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.postgresql.PostgreSQLContainer

class PostgresStoreTest {

    private val store = PostgresStore(dataSource)

    @Test
    fun createsTableOnInit() {
        val sql = "SELECT count(*) FROM information_schema.tables WHERE table_name = 'notes'"
        dataSource.connection.use { conn ->
            conn.createStatement().use {
                val rs = it.executeQuery(sql)
                rs.next()
                assertThat(rs.getLong(1)).isEqualTo(1)
            }
        }
    }

    companion object {

        private val postgres = PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("stopgap")

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
