package dev.sku20.stopgap.app.note.store

import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource

internal inline fun <T> DataSource.withConnection(block: Connection.() -> T): T = connection.use(block)

// Untainted: callers pass only const SQL and bind values through ? placeholders.
@Suppress("SqlSourceToSinkFlow")
internal inline fun <T> Connection.withStatement(
    @Language("SQL")
    sql: String,
    block: (PreparedStatement) -> T
): T = prepareStatement(sql).use(block)

internal inline fun <T> ResultSet.map(transform: (ResultSet) -> T): List<T> {
    val result = mutableListOf<T>()
    while (next()) {
        result.add(transform(this))
    }
    return result
}
