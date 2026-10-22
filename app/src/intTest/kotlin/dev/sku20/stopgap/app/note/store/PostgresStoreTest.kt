package dev.sku20.stopgap.app.note.store

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.sku20.stopgap.app.note.Note
import dev.sku20.stopgap.app.note.NoteReq
import dev.sku20.stopgap.app.note.UpdateReq
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.testcontainers.postgresql.PostgreSQLContainer
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@TestInstance(PER_CLASS)
class PostgresStoreTest {

    private val postgres = PostgreSQLContainer("postgres:18-alpine")
        .withDatabaseName("stopgap")

    private lateinit var dataSource: HikariDataSource

    private lateinit var store: PostgresStore

    @BeforeAll
    fun startPostgres() {
        postgres.start()
        val config = HikariConfig()
        config.jdbcUrl = postgres.jdbcUrl
        config.username = postgres.username
        config.password = postgres.password
        dataSource = HikariDataSource(config)
        store = PostgresStore(dataSource)
    }

    @BeforeEach
    fun truncateNotes() {
        dataSource.connection.use { conn ->
            conn.createStatement().use { it.execute("TRUNCATE notes") }
        }
    }

    @AfterAll
    fun stopPostgres() {
        dataSource.close()
        postgres.stop()
    }

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

    @Test
    fun create() {
        val note = store.create(NoteReq("foo"))

        assertThat(note.text).isEqualTo("foo")
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT text FROM notes WHERE note_id = ?").use {
                it.setObject(1, note.noteId.toJavaUuid())
                val rs = it.executeQuery()
                assertThat(rs.next()).isTrue()
                assertThat(rs.getString("text")).isEqualTo("foo")
            }
        }
    }

    @Test
    fun get() {
        val created = store.create(NoteReq("foo"))
        assertThat(store.get(created.noteId)).isEqualTo(created)
    }

    @Test
    fun list() {
        val first = store.create(NoteReq("foo"))
        val second = store.create(NoteReq("bar"))
        assertThat(store.list())
            .containsExactlyInAnyOrder(first, second)
            .isSortedAccordingTo(compareBy { it.noteId })
    }

    @Test
    fun update() {
        val created = store.create(NoteReq("foo"))

        val updated = store.update(UpdateReq(created.noteId, "bar"))

        assertThat(updated).isEqualTo(Note(created.noteId, "bar"))
        assertThat(store.get(created.noteId)).isEqualTo(updated)
    }

    @Test
    fun getMissing() {
        assertThatThrownBy { store.get(Uuid.random()) }.isInstanceOf(NoteNotFoundException::class.java)
    }

}
