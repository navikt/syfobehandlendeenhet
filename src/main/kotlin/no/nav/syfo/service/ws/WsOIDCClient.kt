package no.nav.syfo.service.ws

import org.apache.cxf.endpoint.Client
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.message.Message
import org.apache.cxf.phase.PhaseInterceptor
import org.apache.cxf.ws.addressing.WSAddressingFeature

import javax.xml.ws.BindingProvider
import javax.xml.ws.handler.Handler
import java.util.Arrays
import java.util.Objects

class WsOIDCClient<T> {

    fun createPort(
        serviceUrl: String,
        portType: Class<*>,
        handlers: List<Handler<*>>,
        vararg interceptors: PhaseInterceptor<out Message>
    ): T {
        val jaxWsProxyFactoryBean = JaxWsProxyFactoryBean()
        jaxWsProxyFactoryBean.serviceClass = portType
        jaxWsProxyFactoryBean.address = Objects.requireNonNull(serviceUrl)
        jaxWsProxyFactoryBean.features.add(WSAddressingFeature())
        val port = jaxWsProxyFactoryBean.create() as T
        (port as BindingProvider).binding.handlerChain = handlers
        val client = ClientProxy.getClient(port)
        Arrays.stream(interceptors).forEach(Consumer<PhaseInterceptor<out Message>> { client.outInterceptors.add(it) })
        STSClientConfig.configureRequestSamlTokenOnBehalfOfOidc<T>(port)
        return port
    }

}
