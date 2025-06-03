package no.nav.klage.pdfgen.service

import kotlinx.html.*
import kotlinx.html.dom.createHTMLDocument
import no.nav.klage.pdfgen.api.view.InnholdsfortegnelseRequest
import no.nav.klage.pdfgen.api.view.InnholdsfortegnelseRequest.Document.Type
import no.nav.klage.pdfgen.transformers.getVedleggsoversiktCss
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class InnholdsfortegnelseService(
    private val pdfGenService: PDFGenService,
) {

    fun getInnholdsfortegnelsePDFAsByteArray(input: InnholdsfortegnelseRequest): ByteArray {
        val doc = getHTMLDocument(input)
        return pdfGenService.createPDFA(doc)
    }

    private val DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd. MMM yyyy", Locale("nb", "NO")).withZone(ZoneId.of("Europe/Oslo"))

    private fun getHTMLDocument(input: InnholdsfortegnelseRequest): Document {
        var counter = 1
        val totalCount = input.documents.size

        return createHTMLDocument()
            .html {
                head {
                    style {
                        unsafe {
                            raw(
                                getVedleggsoversiktCss()
                            )
                        }
                    }
                }
                body {
                    id = "body"
                    h1 { +"Vedleggsoversikt" }

                    if (input.documents.isNotEmpty()) {
                        table {
                            thead {
                                tr {
                                    th {
                                        classes = setOf("white-space-no-wrap")
                                        +"Nr."
                                    }
                                    th {
                                        classes = setOf("white-space-no-wrap")
                                        +"Dato"
                                    }
                                    th { +"Tittel" }

                                }
                            }

                            tbody {
                                input.documents.forEachIndexed { index, it ->
                                    tr {
                                        classes = setOf(if (index % 2 == 0) "even" else "odd")

                                        td {
                                            classes = setOf("white-space-no-wrap")
                                            +"${counter++} av $totalCount"
                                        }
                                        td {
                                            classes = setOf("white-space-no-wrap")
                                            +it.dato.format(DATE_FORMAT)
                                        }
                                        td { +it.tittel }
                                    }

                                    tr {
                                        classes = setOf("extra-row", if (index % 2 == 0) "even" else "odd")
                                        td {
                                            colSpan = "3"

                                            div {
                                                classes = setOf("combined-row")                                    
                                                span {
                                                    classes = setOf("combined-row-item")
                                                    label { +"Saksnr.: " }
                                                    span {
                                                        +it.saksnummer
                                                    }
                                                }

                                                when (it.type) {
                                                    Type.I -> span {
                                                        span {
                                                            classes = setOf("combined-row-item")
                                                            label { +"Avsender: " }
                                                            span { +it.avsenderMottaker }
                                                        }
                                                        span {
                                                            classes = setOf("combined-row-item")
                                                            label { +"Mottaker: " }
                                                            span { +"NAV" }
                                                        }
                                                    }

                                                    Type.U -> span {
                                                        span {
                                                            classes = setOf("combined-row-item")
                                                            label { +"Avsender: " }
                                                            span { +"NAV" }
                                                        }
                                                        span {
                                                            classes = setOf("combined-row-item")
                                                            label { +"Mottaker: " }
                                                            span { +it.avsenderMottaker }
                                                        }
                                                    }

                                                    Type.N -> span {
                                                        classes = setOf("bold", "combined-row-item")
                                                        +"Notat"
                                                    }
                                                }
                                            }

                                            div {
                                                label { +"Tema: " }
                                                span { +it.tema }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

    }
}