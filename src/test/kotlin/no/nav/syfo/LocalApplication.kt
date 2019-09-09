package no.nav.syfo

import no.nav.security.spring.oidc.validation.api.EnableOIDCTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@EnableOIDCTokenValidation
object LocalApplication {
    @JvmStatic
    fun main(args: Array<String>) {
        SpringApplication.run(LocalApplication::class.java, *args)
    }
}

