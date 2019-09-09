package no.nav.syfo.api.system.authorization

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import java.io.IOException
import java.util.Base64

import java.lang.String.format
import java.lang.System.getProperty
import java.util.Optional.ofNullable


class AuthorizationFilterFeed : Filter {
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    internal inner class AutoriseringsFilterException private constructor(message: String) : RuntimeException(message)

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpServletRequest = request as HttpServletRequest
        if (!erRequestAutorisert(httpServletRequest, BASIC_CREDENTIALS)) {
            throw AutoriseringsFilterException("Access denied")
        }
        chain.doFilter(request, response)
    }

    override fun destroy() {}

    companion object {

        private val BASIC_CREDENTIALS = basicCredentials("syfoveilederoppgaver.systemapi")

        fun erRequestAutorisert(httpServletRequest: HttpServletRequest, credential: String): Boolean {
            return ofNullable(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).map<Boolean>(Function<String, Boolean> { credential == it })
                .orElse(false)
        }

        fun basicCredentials(credential: String): String {
            return "Basic " + Base64.getEncoder().encodeToString(
                format(
                    "%s:%s",
                    getProperty("$credential.username"),
                    getProperty("$credential.password")
                ).toByteArray()
            )
        }
    }
}
