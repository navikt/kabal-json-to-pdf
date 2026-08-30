package no.nav.klage.pdfgen.service

import kotlinx.html.body
import kotlinx.html.classes
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.li
import kotlinx.html.ol
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.time
import kotlinx.html.tr
import kotlinx.html.unsafe
import no.nav.klage.pdfgen.api.view.InnholdsfortegnelseRequest
import no.nav.klage.pdfgen.api.view.InnholdsfortegnelseRequest.Document.JournalpostMetadata.Type
import no.nav.klage.pdfgen.transformers.getVedleggsoversiktCss
import no.nav.klage.pdfgen.util.createPDFA
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Service
class InnholdsfortegnelseService {
    fun getInnholdsfortegnelsePDFAsByteArray(input: InnholdsfortegnelseRequest): ByteArray {
        val doc = getHTMLDocument(input)
        return createPDFA(doc)
    }

    private val dateFormat =
        DateTimeFormatter.ofPattern("dd. MMM yyyy", Locale.forLanguageTag("nb-NO")).withZone(ZoneId.of("Europe/Oslo"))
    private val isoDateFormat =
        DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.of("Europe/Oslo"))

    private fun getHTMLDocument(input: InnholdsfortegnelseRequest): Document {
        val totalCount = input.documents.size

        val title = "Vedleggsoversikt til \"${input.parentTitle}\""

        return createHTMLDocument()
            .html {
                head {
                    style {
                        unsafe {
                            raw(
                                getVedleggsoversiktCss(totalCount),
                            )
                        }
                    }
                }
                body {
                    id = "body"
                    time {
                        classes = setOf("overview-date")
                        dateTime = input.parentDate.format(isoDateFormat)
                        +input.parentDate.format(dateFormat)
                    }
                    h1 { +title }

                    if (input.documents.isNotEmpty()) {
                        ol {
                            classes = setOf("document-list")
                            input.documents.forEachIndexed { index, document ->
                                li {
                                    classes = setOf("document-item")
                                    attributes["data-count"] = "${index + 1} av $totalCount"

                                    h2 {
                                        classes = setOf("document-title")
                                        +document.tittel
                                    }

                                    ol {
                                        classes = setOf("journalpost-metadata-list")
                                        document.journalpostMetadataList.forEach { journalpostMetadata ->
                                            li {
                                                classes = setOf("journalpost-metadata-item")
                                                attributes["data-date"] = journalpostMetadata.dato.format(dateFormat)

                                                table {
                                                    classes = setOf("metadata-table")

                                                    when (journalpostMetadata.type) {
                                                        Type.I -> {
                                                            tr {
                                                                td { +"Mottaker" }
                                                                td { +"Nav" }
                                                            }
                                                            if (journalpostMetadata.avsenderMottaker.isNotBlank()) {
                                                                tr {
                                                                    td { +"Avsender" }
                                                                    td { +journalpostMetadata.avsenderMottaker }
                                                                }
                                                            }
                                                        }

                                                        Type.U -> {
                                                            if (journalpostMetadata.avsenderMottaker.isNotBlank()) {
                                                                tr {
                                                                    td { +"Mottaker" }
                                                                    td { +journalpostMetadata.avsenderMottaker }
                                                                }
                                                            }
                                                            tr {
                                                                td { +"Avsender" }
                                                                td { +"Nav" }
                                                            }
                                                        }

                                                        Type.N -> {
                                                            tr {
                                                                td { +"Type" }
                                                                td { +"Internt notat i saken." }
                                                            }
                                                        }
                                                    }

                                                    if (journalpostMetadata.saksnummer.isNotBlank()) {
                                                        tr {
                                                            td { +"Saksnr." }
                                                            td { +journalpostMetadata.saksnummer }
                                                        }
                                                    }

                                                    if (journalpostMetadata.tema.isNotBlank()) {
                                                        tr {
                                                            td { +"Tema" }
                                                            td { +journalpostMetadata.tema }
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
            }
    }
}
