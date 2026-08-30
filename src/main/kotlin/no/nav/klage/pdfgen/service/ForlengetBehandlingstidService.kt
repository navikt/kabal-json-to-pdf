package no.nav.klage.pdfgen.service

import kotlinx.html.BODY
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe
import no.nav.klage.pdfgen.api.view.ForlengetBehandlingstidRequest
import no.nav.klage.pdfgen.transformers.getCss
import no.nav.klage.pdfgen.util.createPDFA
import no.nav.klage.pdfgen.util.getBehandlingstidText
import no.nav.klage.pdfgen.util.getFormattedDate
import no.nav.klage.pdfgen.util.getYtelseDisplayText
import no.nav.klage.pdfgen.util.toFnrView
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import java.time.LocalDate

@Service
class ForlengetBehandlingstidService {
    fun getForlengetBehandlingstidAsByteArray(
        forlengetBehandlingstidRequest: ForlengetBehandlingstidRequest,
        currentDate: LocalDate = LocalDate.now(),
    ): ByteArray {
        val doc =
            getHTMLDocument(
                forlengetBehandlingstidRequest = forlengetBehandlingstidRequest,
                currentDate = currentDate,
            )
        return createPDFA(doc)
    }

    private fun getHTMLDocument(
        forlengetBehandlingstidRequest: ForlengetBehandlingstidRequest,
        currentDate: LocalDate,
    ): Document =
        createHTMLDocument()
            .html {
                head {
                    style {
                        unsafe {
                            raw(
                                getCss(),
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
                            id = "logo"
                            img { src = "nav_logo.svg" }
                        }
                    }
                    div {
                        classes = setOf("saksinfo")
                        p {
                            classes = setOf("label-content")
                            span {
                                classes = setOf("label")
                                +"Saken gjelder: "
                            }
                            span { +forlengetBehandlingstidRequest.sakenGjelder.name }
                        }
                        p {
                            classes = setOf("label-content")
                            span {
                                classes = setOf("label")
                                +"Fødselsnummer: "
                            }
                            span { +forlengetBehandlingstidRequest.sakenGjelder.fnr.toFnrView() }
                        }
                        if (forlengetBehandlingstidRequest.klager != null &&
                            forlengetBehandlingstidRequest.klager.fnr != forlengetBehandlingstidRequest.sakenGjelder.fnr
                        ) {
                            p {
                                classes = setOf("label-content")
                                span {
                                    classes = setOf("label")
                                    +"${forlengetBehandlingstidRequest.type.getKlagerDisplay()}: "
                                }
                                span { +forlengetBehandlingstidRequest.klager.name }
                            }
                        }
                        if (!forlengetBehandlingstidRequest.fullmektigFritekst.isNullOrBlank()) {
                            p {
                                classes = setOf("label-content")
                                span {
                                    classes = setOf("label")
                                    +"Fullmektig: "
                                }
                                span { +forlengetBehandlingstidRequest.fullmektigFritekst }
                            }
                        }
                        div {
                            id = "current-date"
                            classes = setOf("current-date")
                            +getFormattedDate(currentDate)
                        }
                    }
                    h1 {
                        val sakstype = forlengetBehandlingstidRequest.type.getSakstypeDisplayName()
                        val sakstypePossessive = forlengetBehandlingstidRequest.type.getSakstypePossessive()
                        val ytelse = getYtelseDisplayText(ytelseId = forlengetBehandlingstidRequest.ytelseId)
                        +"Varsel om lengre saksbehandlingstid enn forventet i $sakstype $sakstypePossessive som gjelder $ytelse"
                    }

                    p {
                        val sakstype = forlengetBehandlingstidRequest.type.getSakstypeDisplayName()
                        val sakstypePossessive = forlengetBehandlingstidRequest.type.getSakstypePossessive()
                        val mottattDato = getFormattedDate(forlengetBehandlingstidRequest.mottattKlageinstans)
                        +"Klageinstans mottok $sakstype $sakstypePossessive $mottattDato."
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
                                behandlingstidDate = forlengetBehandlingstidRequest.behandlingstidDate,
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
                        +(
                            "Vi ber deg holde oss orientert om forhold som kan ha betydning for avgjørelsen av saken din. Det vil si " +
                                "endringer i for eksempel i medisinske forhold, arbeid, inntekt, sivilstand og liknende."
                        )
                    }
                    p {
                        +(
                            "Hvis du ønsker å ettersende dokumentasjon kan du logge deg inn på mine-klager.nav.no, gå inn på saken " +
                                "og velge \"Ettersend dokumentasjon\". Du kan også gå inn på nav.no/kontakt og sende skriftlig melding " +
                                "der."
                        )
                    }
                    p {
                        +(
                            "Om du ikke ønsker å logge deg inn på nav.no kan du gå til nav.no/klage og trykke på \"Ettersend " +
                                "dokumentasjon\" for det saken gjelder."
                        )
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

    private fun BODY.cleanupInputNewParagraph(inputString: String?) {
        if (!inputString.isNullOrBlank()) {
            p {
                +inputString.trim()
                if (inputString.trim().last() != '.') {
                    +"."
                }
            }
        }
    }

    private fun BODY.cleanupInput(inputString: String?) {
        if (!inputString.isNullOrBlank()) {
            +inputString.trim()
            if (inputString.trim().last() != '.') {
                +"."
            }
        }
    }
}
