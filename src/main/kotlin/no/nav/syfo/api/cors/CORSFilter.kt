package no.nav.syfo.api.cors

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class CORSFilter : Filter {

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpResponse = servletResponse as HttpServletResponse
        val httpRequest = servletRequest as HttpServletRequest

        val reqUri = httpRequest.requestURI
        if (requestUriErIkkeMotInternalEndepunkt(reqUri)) {
            httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"))
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true")
            httpResponse.setHeader(
                "Access-Control-Allow-Headers",
                "Origin, Content-Type, Accept, NAV_CSRF_PROTECTION, authorization"
            )
            httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
        }
        filterChain.doFilter(servletRequest, httpResponse)
    }

    override fun destroy() {}

    private fun requestUriErIkkeMotInternalEndepunkt(reqUri: String): Boolean {
        return !reqUri.contains("/internal")
    }
}
