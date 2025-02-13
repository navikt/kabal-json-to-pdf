package no.nav.klage.pdfgen.service

import kotlinx.html.*
import kotlinx.html.dom.createHTMLDocument
import no.nav.klage.kodeverk.TimeUnitType
import no.nav.klage.pdfgen.api.view.SvarbrevRequest
import no.nav.klage.pdfgen.transformers.getCss
import no.nav.klage.pdfgen.util.createPDFA
import no.nav.klage.pdfgen.util.getFormattedDate
import no.nav.klage.pdfgen.util.getYtelseDisplayText
import no.nav.klage.pdfgen.util.toFnrView
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import java.io.ByteArrayOutputStream
import java.time.LocalDate

@Service
class SvarbrevService {

    val enhetHeaderAndFooterMap = mapOf(
        "4291" to ("Returadresse,\nNav klageinstans Oslo og Akershus, Postboks 7028 St. Olavs plass, 0130 Oslo" to "Postadresse: Nav klageinstans Oslo og Akershus // Postboks 7028 St. Olavs plass // 0130 Oslo\\ATelefon: 55 55 33 33\\Anav.no"),
        "4293" to ("Returadresse,\nNav klageinstans øst, Postboks 2435, 3104 Tønsberg" to "Postadresse: Nav klageinstans øst // Postboks 2435 // 3104 Tønsberg\\ATelefon: 55 55 33 33\\Anav.no"),
        "4250" to ("Returadresse,\nNav klageinstans sør, Postboks 644 Lundsiden, 4606 Kristiansand S" to "Postadresse: Nav klageinstans sør // Postboks 644 Lundsiden // 4606 Kristiansand S\\ATelefon: 55 55 33 33\\Anav.no"),
        "4294" to ("Returadresse,\nNav klageinstans vest, Postboks 6245 Bedriftssenter, 5893 Bergen" to "Postadresse: Nav klageinstans vest // Postboks 6245 Bedriftssenter // 5893 Bergen\\ATelefon: 55 55 33 33\\Anav.no"),
        "4295" to ("Returadresse,\nNav klageinstans nord, Postboks 2363, 9271 Tromsø" to "Postadresse: Nav klageinstans nord // Postboks 2363 // 9271 Tromsø\\ATelefon: 55 55 33 33\\Anav.no"),
        "4292" to ("Returadresse,\nNav klageinstans midt-Norge, Postboks 2914 Torgarden, 7438 Trondheim" to "Postadresse: Nav klageinstans midt-Norge // Postboks 2914 Torgarden // 7438 Trondheim\\ATelefon: 55 55 33 33\\Anav.no"),
    )

    fun getSvarbrevAsByteArray(svarbrevRequest: SvarbrevRequest): ByteArray {
        val doc = when (svarbrevRequest.type) {
            SvarbrevRequest.Type.KLAGE -> getHTMLDocumentKlage(svarbrevRequest)
            SvarbrevRequest.Type.ANKE -> getHTMLDocumentAnke(svarbrevRequest)
            SvarbrevRequest.Type.OMGJOERINGSKRAV -> getHTMLDocumentOmgjoeringskrav(svarbrevRequest)
            null -> getHTMLDocumentAnke(svarbrevRequest)
        }
        val os = ByteArrayOutputStream()
        createPDFA(doc, os)
        return os.toByteArray()
    }

    private fun getHTMLDocumentKlage(svarbrevRequest: SvarbrevRequest): Document {
        return createHTMLDocument()
            .html {
                head {
                    style {
                        unsafe {
                            raw(
                                getCss(footer = enhetHeaderAndFooterMap[svarbrevRequest.avsenderEnhetId]!!.second)
                            )
                        }
                    }
                    title(svarbrevRequest.title)
                }
                body {
                    id = "body"
                    classes = setOf("svarbrev")
                    header {
                        div {
                            id = "header_text"
                            +enhetHeaderAndFooterMap[svarbrevRequest.avsenderEnhetId]!!.first
                        }
                        div {
                            id = "logo"
                            img { src = "nav_logo.png" }
                        }
                    }
                    div {
                        classes = setOf("current-date")
                        +"Dato: ${getFormattedDate(LocalDate.now())}"
                    }
                    h1 { +"Klageinstansen orienterer om saksbehandlingen av klagen din" }
                    br {}
                    p {
                        div {
                            span {
                                classes = setOf("bold")
                                +"Saken gjelder: "
                            }
                            +svarbrevRequest.sakenGjelder.name
                        }
                        div {
                            span {
                                classes = setOf("bold")
                                +"Fødselsnummer: "
                            }
                            +svarbrevRequest.sakenGjelder.fnr.toFnrView()
                        }
                        if (svarbrevRequest.fullmektigFritekst != null) {
                            div {
                                span {
                                    classes = setOf("bold")
                                    +"Fullmektig: "
                                }
                                +svarbrevRequest.fullmektigFritekst
                            }
                        }
                    }
                    br {}
                    p {
                        +"Vi skal behandle klagen din som gjelder ${
                            getYtelseDisplayText(
                                ytelseId = svarbrevRequest.ytelseId,
                                ytelsenavn = svarbrevRequest.ytelsenavn
                            )
                        }, som vi har fått oversendt ${
                            getFormattedDate(
                                svarbrevRequest.receivedDate!!
                            )
                        }."
                    }

                    h2 { +"Klageinstansens saksbehandlingstid" }
                    p {
                        +"Saksbehandlingstiden vår er vanligvis "
                        span { +getBehandlingstidText(svarbrevRequest) }
                        +", men dette kan variere avhengig av hvor mange klagesaker vi har til behandling. ${svarbrevRequest.customText ?: ""}"
                    }
                    p {
                        div {
                            +"Du finner en oppdatert oversikt over saksbehandlingstiden vår på"
                        }
                        div {
                            +"www.nav.no/saksbehandlingstid."
                        }
                    }
                    h2 { +"Klageinstansens behandling av klagen" }
                    p {
                        +"Vi vil vurdere alle dokumentene i saken din."
                    }
                    p {
                        +"Mangler vi opplysninger, vil vi innhente disse. Får vi informasjon du ikke er kjent med, vil vi sende deg en kopi slik at du kan uttale deg. Dette gjelder også hvis vi får uttalelser fra rådgivende lege. Du får beskjed fra oss dersom dette påvirker saksbehandlingstiden."
                    }
                    p {
                        +"Du får avgjørelsen tilsendt på den måten du ønsker, og som du har allerede har valgt. "
                    }
                    h2 { +"Du må melde fra om endringer" }
                    p {
                        +"Skjer det endringer du mener er viktig for saken din, må du orientere oss. Dette kan for eksempel være medisinske forhold, arbeid, inntekt og sivilstand. "
                    }
                    p {
                        +"Du kan logge deg inn på nav.no/kontakt og sende skriftlig melding der. Hvis du ønsker å ettersende dokumentasjon, kan du gå til nav.no/klage og trykke på \"Ettersend dokumentasjon\" for det saken gjelder."
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
                        +"Nav klageinstans"
                    }
                }
            }
    }

    private fun getBehandlingstidText(svarbrevRequest: SvarbrevRequest): String {
        return svarbrevRequest.behandlingstidUnits.toString() + when (TimeUnitType.of(svarbrevRequest.behandlingstidUnitTypeId)) {
            TimeUnitType.WEEKS -> {
                if (svarbrevRequest.behandlingstidUnits == 1) {
                    " uke"
                } else {
                    " uker"
                }
            }

            TimeUnitType.MONTHS -> {
                if (svarbrevRequest.behandlingstidUnits == 1) {
                    " måned"
                } else {
                    " måneder"
                }
            }
        }
    }

    private fun getHTMLDocumentAnke(svarbrevRequest: SvarbrevRequest): Document {
        return createHTMLDocument()
            .html {
                head {
                    style {
                        unsafe {
                            raw(
                                getCss(footer = enhetHeaderAndFooterMap[svarbrevRequest.avsenderEnhetId]!!.second)
                            )
                        }
                    }
                    title(svarbrevRequest.title)
                }
                body {
                    id = "body"
                    classes = setOf("svarbrev")
                    header {
                        div {
                            id = "header_text"
                            +enhetHeaderAndFooterMap[svarbrevRequest.avsenderEnhetId]!!.first
                        }
                        div {
                            id = "logo"
                            img { src = "nav_logo.png" }
                        }
                    }
                    div {
                        classes = setOf("current-date")
                        +"Dato: ${getFormattedDate(LocalDate.now())}"
                    }
                    h1 {
                        +"Nav orienterer om saksbehandlingen av anken din som gjelder ${
                            getYtelseDisplayText(
                                ytelseId = svarbrevRequest.ytelseId,
                                ytelsenavn = svarbrevRequest.ytelsenavn
                            )
                        }"
                    }
                    br { }
                    p {
                        div {
                            span {
                                classes = setOf("bold")
                                +"Saken gjelder: "
                            }
                            +svarbrevRequest.sakenGjelder.name
                        }
                        div {
                            span {
                                classes = setOf("bold")
                                +"Fødselsnummer: "
                            }
                            +svarbrevRequest.sakenGjelder.fnr.toFnrView()
                        }


                        if (svarbrevRequest.klager != null && svarbrevRequest.klager.fnr != svarbrevRequest.sakenGjelder.fnr) {
                            div {
                                span {
                                    classes = setOf("bold")
                                    +"Den ankende part: "
                                }
                                +svarbrevRequest.klager.name
                            }
                        }
                        if (svarbrevRequest.fullmektigFritekst != null) {
                            div {
                                span {
                                    classes = setOf("bold")
                                    +"Fullmektig: "
                                }
                                +svarbrevRequest.fullmektigFritekst
                            }
                        }
                    }

                    br { }

                    p {
                        +"Vi viser til anken din, som vi mottok ${getFormattedDate(svarbrevRequest.ankeReceivedDate ?: svarbrevRequest.receivedDate!!)}."
                    }

                    h2 { +"Behandlingen av ankesaken" }
                    p {
                        +"Saksbehandlingstiden vår er nå "
                        span { +getBehandlingstidText(svarbrevRequest) }
                        +". Du finner oversikt over saksbehandlingstidene våre på www.nav.no/saksbehandlingstid."
                    }
                    if (svarbrevRequest.customText != null) {
                        p {
                            +svarbrevRequest.customText
                        }
                    }
                    p {
                        +"Dersom vi ikke endrer vedtaket du har fått, sender vi saken din til Trygderetten."
                    }
                    h2 { +"Dersom saken går til Trygderetten" }
                    p {
                        +"Hvis saken din går videre til Trygderetten, vil du få kopi av oversendelsesbrevet, der vi forklarer saken og begrunnelsen for vedtaket vårt."
                    }
                    p {
                        +"Du får da mulighet til å komme med merknader, som vil følge saken til Trygderetten."
                    }
                    h2 { +"Du må melde fra om endringer" }
                    p {
                        +"Vi ber deg holde oss orientert om forhold som kan ha betydning for avgjørelsen av saken din. Det vil si endringer i for eksempel i medisinske forhold, arbeid, inntekt, sivilstand og lignende."
                    }
                    p {
                        +"Du kan ettersende dokumentasjon på nav.no/klage ved å trykke på \"Ettersend dokumentasjon\" for det saken gjelder."
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
                        +"Nav klageinstans"
                    }
                }
            }
    }

    private fun getHTMLDocumentOmgjoeringskrav(svarbrevRequest: SvarbrevRequest): Document {
        return createHTMLDocument()
            .html {
                head {
                    style {
                        unsafe {
                            raw(
                                getCss(footer = enhetHeaderAndFooterMap[svarbrevRequest.avsenderEnhetId]!!.second)
                            )
                        }
                    }
                    title(svarbrevRequest.title)
                }
                body {
                    id = "body"
                    classes = setOf("svarbrev")
                    header {
                        div {
                            id = "header_text"
                            +enhetHeaderAndFooterMap[svarbrevRequest.avsenderEnhetId]!!.first
                        }
                        div {
                            id = "logo"
                            img { src = "nav_logo.png" }
                        }
                    }
                    div {
                        classes = setOf("current-date")
                        +"Dato: ${getFormattedDate(LocalDate.now())}"
                    }
                    h1 { +"Nav klageinstans har mottatt kravet ditt om omgjøring" }
                    br {}
                    p {
                        div {
                            span {
                                classes = setOf("bold")
                                +"Saken gjelder: "
                            }
                            +svarbrevRequest.sakenGjelder.name
                        }
                        div {
                            span {
                                classes = setOf("bold")
                                +"Fødselsnummer: "
                            }
                            +svarbrevRequest.sakenGjelder.fnr.toFnrView()
                        }
                        if (svarbrevRequest.fullmektigFritekst != null) {
                            div {
                                span {
                                    classes = setOf("bold")
                                    +"Fullmektig: "
                                }
                                +svarbrevRequest.fullmektigFritekst
                            }
                        }
                    }
                    br {}
                    p {
                        +"Vi viser til kravet ditt om omgjøring av vedtak som gjelder ${
                            getYtelseDisplayText(
                                ytelseId = svarbrevRequest.ytelseId,
                                ytelsenavn = svarbrevRequest.ytelsenavn
                            )
                        }, som vi mottok ${
                            getFormattedDate(
                                svarbrevRequest.receivedDate!!
                            )
                        }."
                    }

                    h2 { +"Behandling av kravet om omgjøring" }
                    p {
                        +"Saksbehandlingstiden vår er vanligvis "
                        span { +getBehandlingstidText(svarbrevRequest) }
                        +", men dette kan variere avhengig av hvor mange klagesaker vi har til behandling. ${svarbrevRequest.customText ?: ""}"
                    }
                    p {
                        div {
                            +"Du finner en oppdatert oversikt over saksbehandlingstiden vår på"
                        }
                        div {
                            +"www.nav.no/saksbehandlingstid."
                        }
                    }
                    h2 { +"Du må melde fra om endringer" }
                    p {
                        +"Skjer det endringer du mener er viktig for saken din, må du orientere oss. Dette kan for eksempel være medisinske forhold, arbeid, inntekt og sivilstand. "
                    }
                    p {
                        +"Du kan logge deg inn på nav.no/kontakt og sende skriftlig melding der. Hvis du ønsker å ettersende dokumentasjon, kan du gå til nav.no/klage og trykke på \"Ettersend dokumentasjon\" for det saken gjelder."
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
                        +"Nav klageinstans"
                    }
                }
            }
    }
}