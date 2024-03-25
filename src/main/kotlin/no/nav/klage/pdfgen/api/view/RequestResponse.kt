package no.nav.klage.pdfgen.api.view

import java.time.LocalDate

data class DocumentValidationResponse(
    val errors: List<DocumentValidationError> = emptyList()
) {
    data class DocumentValidationError(
        val type: String,
        val paths: List<List<Int>> = emptyList()
    )
}

data class InnholdsfortegnelseRequest(
    val documents: List<Document>,
) {
    data class Document(
        val tittel: String,
        val tema: String,
        val dato: LocalDate,
        val avsenderMottaker: String,
        val saksnummer: String,
        val type: Type,
    ) {
        enum class Type {
            I,
            U,
            N,
        }
    }
}

data class SvarbrevRequest(
    val sakenGjelder: SakenGjelder,
    val enhetsnavn: String,
    val fullmektigFritekst: String?,
    val ankeReceivedDate: LocalDate,
    val behandlingstidInWeeks: Int,
) {
    data class SakenGjelder(
        val name: String,
        val fnr: String,
    )
}