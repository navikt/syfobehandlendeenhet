package no.nav.syfo.exception

import lombok.Getter

@Getter
class ApiError(val status: Int, val message: String)
