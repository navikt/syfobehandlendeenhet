package no.nav.syfo.config

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import java.util.Arrays.asList

@Configuration
@EnableCaching
class CacheConfig {


    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = SimpleCacheManager()
        cacheManager.setCaches(
            asList(
                ConcurrentMapCache(CACHENAME_AKTOR_ID),
                ConcurrentMapCache(CACHENAME_AKTOR_FNR),
                ConcurrentMapCache(CACHENAME_DKIF_AKTORID),
                ConcurrentMapCache(CACHENAME_DKIF_FNR),
                ConcurrentMapCache(CACHENAME_EGENANSATT),
                ConcurrentMapCache(CACHENAME_EREG_NAVN),
                ConcurrentMapCache(CACHENAME_LDAP_VEILEDER),
                ConcurrentMapCache(CACHENAME_NORG_ENHETER),
                ConcurrentMapCache(CACHENAME_ORGN_KONTORGEOGRAFISK),
                ConcurrentMapCache(CACHENAME_PERSON_GEOGRAFISK),
                ConcurrentMapCache(CACHENAME_PERSON_PERSON),
                ConcurrentMapCache(CACHENAME_TPS_BRUKER),
                ConcurrentMapCache(CACHENAME_TPS_NAVN)
            )
        )
        return cacheManager
    }

    companion object {

        const val CACHENAME_AKTOR_ID = "aktoerid"
        const val CACHENAME_AKTOR_FNR = "aktoerfnr"
        const val CACHENAME_DKIF_AKTORID = "dkifaktorid"
        const val CACHENAME_DKIF_FNR = "dkiffnr"
        const val CACHENAME_EGENANSATT = "egenansatt"
        const val CACHENAME_EREG_NAVN = "eregnavn"
        const val CACHENAME_LDAP_VEILEDER = "ldapveileder"
        const val CACHENAME_NORG_ENHETER = "norgenheter"
        const val CACHENAME_ORGN_KONTORGEOGRAFISK = "orgnkontorgeografisk"
        const val CACHENAME_PERSON_GEOGRAFISK = "persongeografisk"
        const val CACHENAME_PERSON_PERSON = "person"
        const val CACHENAME_TPS_BRUKER = "tpsbruker"
        const val CACHENAME_TPS_NAVN = "tpsnavn"
    }
}
