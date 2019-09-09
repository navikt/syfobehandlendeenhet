package no.nav.syfo.service.ws

import org.apache.cxf.binding.soap.SoapHeader
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.interceptor.Fault
import org.apache.cxf.jaxb.JAXBDataBinding
import org.apache.cxf.message.Message
import org.apache.cxf.phase.AbstractPhaseInterceptor
import org.apache.cxf.phase.Phase
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.xml.bind.JAXBException
import javax.xml.namespace.QName

class CallIdHeader : AbstractPhaseInterceptor<Message>(Phase.PRE_STREAM) {

    @Throws(Fault::class)
    override fun handleMessage(message: Message) {
        try {
            val qName = QName("uri:no.nav.applikasjonsrammeverk", "callId")
            val header = SoapHeader(qName, randomValue(), JAXBDataBinding(String::class.java))
            (message as SoapMessage).headers.add(header)
        } catch (ex: JAXBException) {
            logger.warn("Error while setting CallId header", ex)
        }

    }

    private fun randomValue(): String {
        return "syfomottoakoppslag-" + (Math.random() * 10000).toInt()
    }

    companion object {

        private val logger = LoggerFactory.getLogger(CallIdHeader::class.java)
    }

}
