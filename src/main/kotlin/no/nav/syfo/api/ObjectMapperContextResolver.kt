package no.nav.syfo.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

import javax.ws.rs.ext.ContextResolver
import javax.ws.rs.ext.Provider

@Provider
class ObjectMapperContextResolver : ContextResolver<ObjectMapper> {
    private val mapper: ObjectMapper

    init {
        mapper = ObjectMapper()
        mapper.registerModule(JavaTimeModule())
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    }

    override fun getContext(aClass: Class<*>): ObjectMapper {
        return mapper
    }
}
