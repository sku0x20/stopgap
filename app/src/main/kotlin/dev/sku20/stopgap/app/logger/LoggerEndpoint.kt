package dev.sku20.stopgap.app.logger

import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.endpoint.Post
import dev.sku20.stopgap.helidon.param.PathParam
import dev.sku20.stopgap.helidon.param.QueryParam
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

@Endpoint("/logger")
class LoggerEndpoint {

    // levels below logback's configured level (INFO) are dropped, e.g. POST /logger/debug logs nothing
    @Post("/{level}")
    fun log(
        @PathParam("level")
        level: String,
        @QueryParam("message")
        message: String
    ) {
        logger.atLevel(parseLevel(level)).log(message)
    }

    private fun parseLevel(level: String): Level =
        Level.entries.firstOrNull { it.name.equals(level, ignoreCase = true) } ?: throw InvalidLogLevelException(level)

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(LoggerEndpoint::class.java)
    }
}
