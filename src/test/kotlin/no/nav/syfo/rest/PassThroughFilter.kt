package no.nav.syfo.rest

import java.io.IOException
import javax.servlet.*

class PassThroughFilter : Filter {
    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {

    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        filterChain.doFilter(servletRequest, servletResponse)
    }

    override fun destroy() {

    }
}
