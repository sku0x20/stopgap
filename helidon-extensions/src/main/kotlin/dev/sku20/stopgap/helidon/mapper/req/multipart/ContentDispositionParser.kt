package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.exception.MalformedBodyException

// Parses the parameters of a part's Content-Disposition, e.g. form-data; name="a"; filename="b.txt".
// Own parser because Helidon's ContentDisposition rejects filename="", which browsers send for an empty file input.
// Only \" and \\ are unescaped, so a Windows path like C:\dir\a.txt keeps its backslashes.
class ContentDispositionParser(private val value: String) {

    private var position = 0

    // Parameter names are lowercase; the first occurrence of a name wins.
    fun parse(): Map<String, String> {
        val parameters = HashMap<String, String>()
        skipPast(';')
        while (position < value.length) {
            skipWhitespace()
            if (position == value.length) break
            val equals = value.indexOf('=', position)
            if (equals < 0) throw MalformedBodyException(null)
            val name = value.substring(position, equals).trim().lowercase()
            position = equals + 1
            skipWhitespace()
            val parameterValue = if (position < value.length && value[position] == '"') readQuoted() else readToken()
            parameters.putIfAbsent(name, parameterValue)
            skipWhitespace()
            if (position < value.length && value[position] != ';') throw MalformedBodyException(null)
            position++
        }
        return parameters
    }

    private fun readQuoted(): String {
        val result = StringBuilder()
        position++
        while (position < value.length) {
            val char = value[position]
            if (char == '"') {
                position++
                return result.toString()
            }
            val next = if (position + 1 < value.length) value[position + 1] else null
            if (char == '\\' && (next == '"' || next == '\\')) {
                result.append(next)
                position += 2
                continue
            }
            result.append(char)
            position++
        }
        throw MalformedBodyException(null)
    }

    private fun readToken(): String {
        val end = value.indexOf(';', position)
        val token = if (end < 0) value.substring(position) else value.substring(position, end)
        position += token.length
        return token.trim()
    }

    private fun skipPast(char: Char) {
        val index = value.indexOf(char)
        position = if (index < 0) value.length else index + 1
    }

    private fun skipWhitespace() {
        while (position < value.length && value[position].isWhitespace()) position++
    }
}
