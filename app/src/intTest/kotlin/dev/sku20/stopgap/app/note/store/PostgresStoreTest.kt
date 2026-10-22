package dev.sku20.stopgap.app.note.store

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.postgresql.PostgreSQLContainer
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

class PostgresStoreTest {

    @Test
    fun learnJdbc() {
        dataSource(postgres.jdbcUrl).use { ds ->
            ds.connection.use { conn ->
                // CREATE DATABASE can't run inside a transaction; fine here since Hikari defaults to auto-commit.
                conn.createStatement().use { it.execute("CREATE DATABASE learn") }
            }
        }

        val learnUrl = "jdbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/learn"
        dataSource(learnUrl).use { ds ->
            learnCrud(ds)
        }
    }

    private fun learnCrud(ds: HikariDataSource) {
        ds.connection.use { conn ->
            conn.createStatement().use { it.execute("CREATE TABLE notes (note_id uuid PRIMARY KEY, text text NOT NULL)") }

            val noteId = Uuid.random().toJavaUuid()
            conn.prepareStatement("INSERT INTO notes (note_id, text) VALUES (?, ?)").use {
                it.setObject(1, noteId)
                it.setString(2, "foo")
                assertThat(it.executeUpdate()).isEqualTo(1)
            }

            conn.prepareStatement("SELECT text FROM notes WHERE note_id = ?").use {
                it.setObject(1, noteId)
                it.executeQuery().use { rs ->
                    assertThat(rs.next()).isTrue()
                    assertThat(rs.getString("text")).isEqualTo("foo")
                    assertThat(rs.next()).isFalse()
                }
            }

            conn.prepareStatement("DELETE FROM notes WHERE note_id = ?").use {
                it.setObject(1, noteId)
                assertThat(it.executeUpdate()).isEqualTo(1)
            }

            conn.prepareStatement("SELECT count(*) FROM notes").use {
                it.executeQuery().use { rs ->
                    rs.next()
                    assertThat(rs.getLong(1)).isZero()
                }
            }
        }
    }

    private fun dataSource(jdbcUrl: String): HikariDataSource {
        val config = HikariConfig()
        config.jdbcUrl = jdbcUrl
        config.username = postgres.username
        config.password = postgres.password
        return HikariDataSource(config)
    }

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
