package dev.sku20.stopgap.app.note.store

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.postgresql.PostgreSQLContainer

class PostgresStoreTest {

    companion object {

        private val postgres = PostgreSQLContainer("postgres:18-alpine")

        @JvmStatic
        @BeforeAll
        fun startPostgres() {
            postgres.start()
        }

        @JvmStatic
        @AfterAll
        fun stopPostgres() {
            postgres.stop()
        }

    }

}
