package no.nav.syfo.provider.rs.filter

import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import java.io.IOException

class FakeLoginFilter : Filter {

    private var filterConfig: FilterConfig? = null

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        this.filterConfig = filterConfig
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val req = servletRequest as HttpServletRequest
        if (req.requestURI.matches("^(.*internal/selftest.*)|(.*index.html)|(.*feil.*)|((.*)\\.(js|css|jpg))".toRegex())) {
            filterChain.doFilter(servletRequest, servletResponse)
            return
        }

        filterChain.doFilter(servletRequest, servletResponse)
    }

    override fun destroy() {
        this.filterConfig = null
    }

}
