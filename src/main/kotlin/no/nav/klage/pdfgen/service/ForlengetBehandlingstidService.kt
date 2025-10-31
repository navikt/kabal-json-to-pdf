package no.nav.klage.pdfgen.service

import kotlinx.html.*
import kotlinx.html.dom.createHTMLDocument
import no.nav.klage.pdfgen.api.view.ForlengetBehandlingstidRequest
import no.nav.klage.pdfgen.transformers.getCss
import no.nav.klage.pdfgen.util.*
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import java.time.LocalDate

@Service
class ForlengetBehandlingstidService {

    val enhetHeaderAndFooterMap = mapOf(
        "4291" to ("Returadresse,\nKlageinstans Oslo, Postboks 7028 St. Olavs plass, 0130 Oslo" to "Postadresse: Klageinstans Oslo // Postboks 7028 St. Olavs plass // 0130 Oslo\\ATelefon: 55 55 33 33\\Anav.no"),
        "4293" to ("Returadresse,\nKlageinstans Tønsberg, Postboks 7028 St. Olavs plass, 0130 Oslo" to "Postadresse: Klageinstans Tønsberg // Postboks 7028 St. Olavs plass // 0130 Oslo\\ATelefon: 55 55 33 33\\Anav.no"),
        "4250" to ("Returadresse,\nKlageinstans Kristiansand, Postboks 7028 St. Olavs plass, 0130 Oslo" to "Postadresse: Klageinstans Kristiansand // Postboks 7028 St. Olavs plass // 0130 Oslo\\ATelefon: 55 55 33 33\\Anav.no"),
        "4294" to ("Returadresse,\nKlageinstans Bergen, Postboks 7028 St. Olavs plass, 0130 Oslo" to "Postadresse: Klageinstans Bergen // Postboks 7028 St. Olavs plass // 0130 Oslo\\ATelefon: 55 55 33 33\\Anav.no"),
        "4295" to ("Returadresse,\nKlageinstans Tromsø, Postboks 7028 St. Olavs plass, 0130 Oslo" to "Postadresse: Klageinstans Tromsø // Postboks 7028 St. Olavs plass // 0130 Oslo\\ATelefon: 55 55 33 33\\Anav.no"),
        "4292" to ("Returadresse,\nKlageinstans Trondheim, Postboks 7028 St. Olavs plass, 0130 Oslo" to "Postadresse: Klageinstans Trondheim // Postboks 7028 St. Olavs plass // 0130 Oslo\\ATelefon: 55 55 33 33\\Anav.no"),
    )

    fun getForlengetBehandlingstidAsByteArray(
        forlengetBehandlingstidRequest: ForlengetBehandlingstidRequest,
        currentDate: LocalDate = LocalDate.now(),
    ): ByteArray {
        val doc = getHTMLDocument(
            forlengetBehandlingstidRequest = forlengetBehandlingstidRequest,
            currentDate = currentDate,
        )
        return createPDFA(doc)
    }

    private fun getHTMLDocument(forlengetBehandlingstidRequest: ForlengetBehandlingstidRequest, currentDate: LocalDate): Document {
        return createHTMLDocument()
            .html {
                head {
                    style {
                        unsafe {
                            raw(
                                getCss(footer = enhetHeaderAndFooterMap[forlengetBehandlingstidRequest.avsenderEnhetId]!!.second)
                            )
                        }
                    }
                    title(forlengetBehandlingstidRequest.title)
                }
                body {
                    id = "body"
                    classes = setOf("svarbrev")
                    header {
                        div {
                            id = "header_text"
                            +enhetHeaderAndFooterMap[forlengetBehandlingstidRequest.avsenderEnhetId]!!.first
                        }
                        div {
                            id = "logo"
                            img { src = "nav_logo.png" }
                        }
                    }
                    div {
                        classes = setOf("current-date")
                        +"Dato: ${getFormattedDate(currentDate)}"
                    }
                    h1 {
                        +"Varsel om lengre saksbehandlingstid enn forventet i ${forlengetBehandlingstidRequest.type.getSakstypeDisplayName()} ${forlengetBehandlingstidRequest.type.getSakstypePossessive()} som gjelder ${
                            getYtelseDisplayText(
                                ytelseId = forlengetBehandlingstidRequest.ytelseId
                            )
                        }"
                    }

                    br {}
                    p {
                        div {
                            span {
                                classes = setOf("bold")
                                +"Saken gjelder: "
                            }
                            +forlengetBehandlingstidRequest.sakenGjelder.name
                        }
                        div {
                            span {
                                classes = setOf("bold")
                                +"Fødselsnummer: "
                            }
                            +forlengetBehandlingstidRequest.sakenGjelder.fnr.toFnrView()
                        }
                        if (forlengetBehandlingstidRequest.klager != null && forlengetBehandlingstidRequest.klager.fnr != forlengetBehandlingstidRequest.sakenGjelder.fnr) {
                            div {
                                span {
                                    classes = setOf("bold")
                                    +"${forlengetBehandlingstidRequest.type.getKlagerDisplay()}: "
                                }
                                +forlengetBehandlingstidRequest.klager.name
                            }
                        }
                        if (!forlengetBehandlingstidRequest.fullmektigFritekst.isNullOrBlank()) {
                            div {
                                span {
                                    classes = setOf("bold")
                                    +"Fullmektig: "
                                }
                                +forlengetBehandlingstidRequest.fullmektigFritekst
                            }
                        }
                    }

                    p {
                        +"Klageinstans mottok ${forlengetBehandlingstidRequest.type.getSakstypeDisplayName()} ${forlengetBehandlingstidRequest.type.getSakstypePossessive()} ${
                            getFormattedDate(
                                forlengetBehandlingstidRequest.mottattKlageinstans
                            )
                        }."
                    }

                    cleanupInputNewParagraph(forlengetBehandlingstidRequest.previousBehandlingstidInfo)

                    p {
                        +"Vi beklager at saksbehandlingstiden vil bli lengre i din sak. "
                        this@body.cleanupInput(forlengetBehandlingstidRequest.reason)
                    }
                    p {
                        +"Vi forventer at saken din vil bli behandlet innen ${
                            getBehandlingstidText(
                                behandlingstidUnitTypeId = forlengetBehandlingstidRequest.behandlingstidUnitTypeId,
                                behandlingstidUnits = forlengetBehandlingstidRequest.behandlingstidUnits,
                                behandlingstidDate = forlengetBehandlingstidRequest.behandlingstidDate
                            )
                        }"
                        if (forlengetBehandlingstidRequest.behandlingstidDate != null) {
                            +". "
                        } else {
                            +" fra vi sendte dette brevet. "
                        }
                        +"Du finner en oppdatert oversikt over saksbehandlingstiden vår på "
                        +"www.nav.no/saksbehandlingstid."
                    }

                    cleanupInputNewParagraph(forlengetBehandlingstidRequest.customText)

                    h2 { +"Du må melde fra om endringer" }
                    p {
                        +"Vi ber deg holde oss orientert om forhold som kan ha betydning for avgjørelsen av saken din. Det vil si endringer i for eksempel i medisinske forhold, arbeid, inntekt, sivilstand og liknende."
                    }
                    p {
                        +"Hvis du ønsker å ettersende dokumentasjon kan du logge deg inn på mine-klager.nav.no, gå inn på saken og velge \"Ettersend dokumentasjon\". Du kan også gå inn på nav.no/kontakt og sende skriftlig melding der."
                    }
                    p {
                        +"Om du ikke ønsker å logge deg inn på nav.no kan du gå til nav.no/klage og trykke på \"Ettersend dokumentasjon\" for det saken gjelder."
                    }
                    h2 { +"Du har rett til innsyn" }
                    p {
                        +"Du har rett til å se dokumentene i saken din."
                    }
                    h2 { +"Informasjon om fri rettshjelp" }
                    p {
                        +"Dette får du vite mer om hos Statsforvalteren eller advokat."
                    }
                    div {
                        classes = setOf("signature")
                        +"Med hilsen"
                        br { }
                        +"Arbeids- og velferdsdirektoratet Klageinstans"
                    }
                }
            }
    }

    private fun BODY.cleanupInputNewParagraph(
        inputString: String?,
    ) {
        if (!inputString.isNullOrBlank()) {
            p {
                +inputString.trim()
                if (inputString.trim().last() != '.') {
                    +"."
                }
            }
        }
    }

    private fun BODY.cleanupInput(
        inputString: String?,
    ) {
        if (!inputString.isNullOrBlank()) {
            +inputString.trim()
            if (inputString.trim().last() != '.') {
                +"."
            }
        }
    }
}