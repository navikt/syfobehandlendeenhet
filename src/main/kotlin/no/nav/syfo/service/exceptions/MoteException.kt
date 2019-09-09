package no.nav.syfo.service.exceptions

class MoteException : RuntimeException {

    constructor() {}

    constructor(message: String) : super(message) {}
}
