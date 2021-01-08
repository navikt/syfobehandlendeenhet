package no.nav.syfo.config

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = SimpleCacheManager()
        cacheManager.setCaches(
            listOf(
                ConcurrentMapCache(CACHENAME_ARBEIDSFORDELING),
                ConcurrentMapCache(CACHENAME_EGENANSATT),
                ConcurrentMapCache(CACHENAME_ORGANISASJONENHET),
            )
        )
        return cacheManager
    }

    companion object {
        const val CACHENAME_ARBEIDSFORDELING = "arbeidsfordeling"
        const val CACHENAME_EGENANSATT = "egenansatt"
        const val CACHENAME_ORGANISASJONENHET = "organisasjonenhet"
    }
}
