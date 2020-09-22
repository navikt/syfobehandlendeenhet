package no.nav.syfo.testhelper

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.util.UriComponentsBuilder

fun mockAndExpectSkjermedPersonerEgenAnsatt(
    mockRestServiceServer: MockRestServiceServer,
    url: String,
    isEgenAnsatt: Boolean
) {
    val uriString = UriComponentsBuilder.fromHttpUrl(url)
        .toUriString()

    try {
        val json = ObjectMapper().writeValueAsString(isEgenAnsatt)

        mockRestServiceServer.expect(ExpectedCount.manyTimes(), MockRestRequestMatchers.requestTo(uriString))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(json, MediaType.APPLICATION_JSON))
    } catch (e: JsonProcessingException) {
        e.printStackTrace()
    }
}
