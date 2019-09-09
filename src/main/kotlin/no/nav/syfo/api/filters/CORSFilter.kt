package no.nav.syfo.api.filters

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException
import java.util.Arrays

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class CORSFilter : Filter {

    private val whitelist = Arrays.asList(
        "https://syfooversikt.nais.adeo.no",
        "https://syfooversikt.nais.preprod.local",
        "https://syfooversikt-q1.nais.preprod.local",
        "https://modiasyfofront.nais.adeo.no",
        "https://modiasyfofront.nais.preprod.local",
        "https://modiasyfofront-q1.nais.preprod.local",
        "https://fastlegefront.nais.adeo.no",
        "https://fastlegefront.nais.preprod.local",
        "https://fastlegefront-q1.nais.preprod.local",
        "https://app.adeo.no",
        "https://app-q1.adeo.no"
    )

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpResponse = servletResponse as HttpServletResponse
        val httpRequest = servletRequest as HttpServletRequest

        val reqUri = httpRequest.requestURI
        if (requestUriErIkkeMotFeedEllerInternalEndepunkt(reqUri)) {
            val origin = httpRequest.getHeader("origin")
            if (erWhitelisted(origin)) {
                httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"))
                httpResponse.setHeader("Access-Control-Allow-Credentials", "true")
                httpResponse.setHeader(
                    "Access-Control-Allow-Headers",
                    "Origin, Content-Type, Accept, NAV_CSRF_PROTECTION, authorization"
                )
                httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
            }
        }

        filterChain.doFilter(servletRequest, httpResponse)
    }

    override fun destroy() {}

    private fun requestUriErIkkeMotFeedEllerInternalEndepunkt(reqUri: String): Boolean {
        return !(reqUri.contains("/api/system") || reqUri.contains("/internal"))
    }

    private fun erWhitelisted(origin: String?): Boolean {
        return origin != null && whitelist.contains(origin)
    }
}
