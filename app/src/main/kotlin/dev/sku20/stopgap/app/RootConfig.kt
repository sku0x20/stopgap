package dev.sku20.stopgap.app

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.sku20.stopgap.app.authn.AppAuthenticationResolver
import dev.sku20.stopgap.app.serde.fastjson.FastjsonSerde
import dev.sku20.stopgap.helidon.authentication.AuthenticationResolver
import dev.sku20.stopgap.helidon.serde.MapSerdeCatalog
import dev.sku20.stopgap.helidon.serde.SerdeCatalog
import dev.sku20.stopgap.helidon.serde.SerdeExtras
import dev.sku20.stopgap.ir.annotation.Creates
import dev.sku20.stopgap.ir.annotation.Qualifier
import io.helidon.config.Config

object RootConfig {

    @Creates
    fun config(): Config {
        val config = Config.create()
        return config
    }

    @Creates
    fun dataSource(config: Config): HikariDataSource {
        val postgres = config.get("db.postgres")
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = postgres.get("url").asString().get()
        hikariConfig.username = postgres.get("username").asString().get()
        hikariConfig.password = postgres.get("password").asString().get()
        return HikariDataSource(hikariConfig)
    }

    @Creates
    @Qualifier(SerdeExtras.DEFAULT_CATALOG_QUALIFIER)
    fun defaultSerdeCatalog(
        fastjsonSerde: FastjsonSerde
    ): SerdeCatalog {
        val mapSerdeCatalog = MapSerdeCatalog()
        mapSerdeCatalog.add(fastjsonSerde)
        return mapSerdeCatalog
    }

    @Creates
    fun rootEndpoint(): RootEndpoint {
        return RootEndpoint()
    }

    @Creates
    fun httpMethodsEndpoint(): HttpMethodsEndpoint {
        return HttpMethodsEndpoint()
    }

    @Creates
    fun authenticationResolver(): AuthenticationResolver {
        return AppAuthenticationResolver()
    }
}
