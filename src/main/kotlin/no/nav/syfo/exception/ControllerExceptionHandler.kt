package no.nav.syfo.exception

import no.nav.security.spring.oidc.validation.interceptor.OIDCUnauthorizedException
import no.nav.syfo.metric.Metric
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.util.WebUtils
import javax.inject.Inject
import javax.validation.ConstraintViolationException
import javax.ws.rs.ForbiddenException
import javax.ws.rs.NotFoundException

@ControllerAdvice
class ControllerExceptionHandler @Inject constructor(private val metric: Metric) {

    private val log = LoggerFactory.getLogger(ControllerExceptionHandler::class.java)

    private val BAD_REQUEST_MSG = "Vi kunne ikke tolke inndataene"
    private val FORBIDDEN_MSG = "Handling er forbudt"
    private val INTERNAL_MSG = "Det skjedde en uventet feil"
    private val UNAUTHORIZED_MSG = "Autorisasjonsfeil"
    private val NOT_FOUND_MSG = "Fant ikke ressurs"

    @ExceptionHandler(
            Exception::class,
            ConstraintViolationException::class,
            ForbiddenException::class,
            IllegalArgumentException::class,
            OIDCUnauthorizedException::class,
            NotFoundException::class
    )
    fun handleException(ex: Exception, request: WebRequest): ResponseEntity<ApiError> {
        val headers = HttpHeaders()

        return when (ex) {
            is OIDCUnauthorizedException -> handleOIDCUnauthorizedException(ex, headers, request)
            is ForbiddenException -> handleForbiddenException(ex, headers, request)
            is IllegalArgumentException -> handleIllegalArgumentException(ex, headers, request)
            is ConstraintViolationException -> handleConstraintViolationException(ex, headers, request)
            is NotFoundException -> handleNotFoundException(ex, headers, request)
            else -> {
                val status = HttpStatus.INTERNAL_SERVER_ERROR

                handleExceptionInternal(ex, ApiError(status.value(), INTERNAL_MSG), headers, status, request)
            }
        }
    }

    private fun handleConstraintViolationException(
            ex: ConstraintViolationException,
            headers: HttpHeaders,
            request: WebRequest
    ): ResponseEntity<ApiError> {
        return handleExceptionInternal(
                ex,
                ApiError(HttpStatus.BAD_REQUEST.value(), BAD_REQUEST_MSG),
                headers,
                HttpStatus.BAD_REQUEST,
                request
        )
    }

    private fun handleForbiddenException(
            ex: ForbiddenException,
            headers: HttpHeaders,
            request: WebRequest
    ): ResponseEntity<ApiError> {
        val status = HttpStatus.FORBIDDEN
        return handleExceptionInternal(ex, ApiError(status.value(), FORBIDDEN_MSG), headers, status, request)
    }

    private fun handleIllegalArgumentException(
            ex: IllegalArgumentException,
            headers: HttpHeaders,
            request: WebRequest
    ): ResponseEntity<ApiError> {
        val status = HttpStatus.BAD_REQUEST
        return handleExceptionInternal(ex, ApiError(status.value(), BAD_REQUEST_MSG), headers, status, request)
    }

    private fun handleOIDCUnauthorizedException(
            ex: OIDCUnauthorizedException,
            headers: HttpHeaders,
            request: WebRequest
    ): ResponseEntity<ApiError> {
        val status = HttpStatus.UNAUTHORIZED
        return handleExceptionInternal(ex, ApiError(status.value(), UNAUTHORIZED_MSG), headers, status, request)
    }

    private fun handleNotFoundException(
            ex: NotFoundException,
            headers: HttpHeaders,
            request: WebRequest
    ): ResponseEntity<ApiError> {
        val status = HttpStatus.NOT_FOUND
        return handleExceptionInternal(ex, ApiError(status.value(), NOT_FOUND_MSG), headers, status, request)
    }

    private fun handleExceptionInternal(
            ex: Exception,
            body: ApiError,
            headers: HttpHeaders,
            status: HttpStatus,
            request: WebRequest
    ): ResponseEntity<ApiError> {
        metric.tellHttpKall(status.value())

        if (HttpStatus.INTERNAL_SERVER_ERROR == status) {
            log.error("Uventet feil: {} : {}", ex.javaClass.toString(), ex.message, ex)
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST)
        }

        return ResponseEntity(body, headers, status)
    }
}
