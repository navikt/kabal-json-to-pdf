package no.nav.klage.pdfgen.service

import kotlinx.html.*
import kotlinx.html.dom.createHTMLDocument
import no.nav.klage.pdfgen.api.view.SvarbrevRequest
import no.nav.klage.pdfgen.transformers.getCss
import no.nav.klage.pdfgen.util.*
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import java.time.LocalDate

@Service
class SvarbrevService {
        fun getSvarbrevAsByteArray(
        svarbrevRequest: SvarbrevRequest,
        currentDate: LocalDate = LocalDate.now(),
    ): ByteArray {
        val doc = when (svarbrevRequest.type) {
            SvarbrevRequest.Type.KLAGE -> getHTMLDocumentKlage(
                svarbrevRequest = svarbrevRequest,
                currentDate = currentDate,
            )

            SvarbrevRequest.Type.ANKE -> getHTMLDocumentAnke(
                svarbrevRequest = svarbrevRequest,
                currentDate = currentDate,
            )

            SvarbrevRequest.Type.OMGJOERINGSKRAV -> getHTMLDocumentOmgjoeringskrav(
                svarbrevRequest = svarbrevRequest,
                currentDate = currentDate,
            )

            SvarbrevRequest.Type.BEGJAERING_OM_GJENOPPTAK -> getHTMLDocumentBegjaeringOmGjenopptak(
                svarbrevRequest = svarbrevRequest,
                currentDate = currentDate,
            )

            null -> getHTMLDocumentAnke(
                svarbrevRequest = svarbrevRequest,
                currentDate = currentDate,
            )
        }
        return createPDFA(doc)
    }

    private fun getHTMLDocumentKlage(
        svarbrevRequest: SvarbrevRequest,
        currentDate: LocalDate,
    ): Document {
        return createHTMLDocument()
            .html {
                head {
                    style {
                        unsafe {
                            raw(
                                getCss()
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
                            span { +svarbrevRequest.sakenGjelder.name }
                        }
                        p {
                            classes = setOf("label-content")
                            span {
                                classes = setOf("label")
                                +"Fødselsnummer: "
                            }
                            span { +svarbrevRequest.sakenGjelder.fnr.toFnrView() }
                        }
                        if (svarbrevRequest.klager != null && svarbrevRequest.klager.fnr != svarbrevRequest.sakenGjelder.fnr) {
                            p {
                                classes = setOf("label-content")
                                span {
                                    classes = setOf("label")
                                    +"Klager: "
                                }
                                span { +svarbrevRequest.klager.name }
                            }
                        }
                        if (!svarbrevRequest.fullmektigFritekst.isNullOrBlank()) {
                            p {
                                classes = setOf("label-content")
                                span {
                                    classes = setOf("label")
                                    +"Fullmektig: "
                                }
                                span { +svarbrevRequest.fullmektigFritekst }
                            }
                        }
                        div {
                            id = "current-date"
                            classes = setOf("current-date")
                            + getFormattedDate(currentDate)
                        }
                    }
                    h1 { +"Klageinstansen orienterer om saksbehandlingen av klagen din" }
                    p {
                        +"Vi skal behandle klagen din som gjelder ${
                            getYtelseDisplayText(
                                ytelseId = svarbrevRequest.ytelseId,
                            )
                        }, som vi har fått oversendt ${
                            getFormattedDate(
                                svarbrevRequest.receivedDate!!
                            )
                        }."
                    }

                    if (!svarbrevRequest.initialCustomText.isNullOrBlank()) {
                        p {
                            +svarbrevRequest.initialCustomText
                        }
                    }

                    h2 { +"Klageinstansens saksbehandlingstid" }
                    p {
                        +"Saksbehandlingstiden vår er vanligvis "
                        span {
                            +getBehandlingstidText(
                                behandlingstidUnitTypeId = svarbrevRequest.behandlingstidUnitTypeId,
                                behandlingstidUnits = svarbrevRequest.behandlingstidUnits,
                                behandlingstidDate = null
                            )
                        }
                        +" fra vi mottok klagen, men dette kan variere avhengig av hvor mange klagesaker vi har til behandling. ${svarbrevRequest.customText ?: ""}"
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

    private fun getHTMLDocumentAnke(
        svarbrevRequest: SvarbrevRequest,
        currentDate: LocalDate,
    ): Document {
        return createHTMLDocument()
            .html {
                head {
                    style {
                        unsafe {
                            raw(
                                getCss()
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
                            span { +svarbrevRequest.sakenGjelder.name }
                        }
                        p {
                            classes = setOf("label-content")
                            span {
                                classes = setOf("label")
                                +"Fødselsnummer: "
                            }
                            span { +svarbrevRequest.sakenGjelder.fnr.toFnrView() }
                        }


                        if (svarbrevRequest.klager != null && svarbrevRequest.klager.fnr != svarbrevRequest.sakenGjelder.fnr) {
                            p {
                                classes = setOf("label-content")
                                span {
                                    classes = setOf("label")
                                    +"Den ankende part: "
                                }
                                span { +svarbrevRequest.klager.name }
                            }
                        }
                        if (!svarbrevRequest.fullmektigFritekst.isNullOrBlank()) {
                            p {
                                classes = setOf("label-content")
                                span {
                                    classes = setOf("label")
                                    +"Fullmektig: "
                                }
                                span { +svarbrevRequest.fullmektigFritekst }
                            }
                        }
                        div {
                            id = "current-date"
                            classes = setOf("current-date")
                            + getFormattedDate(currentDate)
                        }
                    }
                    h1 {
                        +"Nav orienterer om saksbehandlingen av anken din som gjelder ${
                            getYtelseDisplayText(
                                ytelseId = svarbrevRequest.ytelseId
                            )
                        }"
                    }
                    p {
                        +"Vi viser til anken din, som vi mottok ${getFormattedDate(svarbrevRequest.ankeReceivedDate ?: svarbrevRequest.receivedDate!!)}."
                    }

                    if (!svarbrevRequest.initialCustomText.isNullOrBlank()) {
                        p {
                            +svarbrevRequest.initialCustomText
                        }
                    }

                    h2 { +"Behandlingen av ankesaken" }
                    p {
                        +"Saksbehandlingstiden vår er nå "
                        span {
                            +getBehandlingstidText(
                                behandlingstidUnitTypeId = svarbrevRequest.behandlingstidUnitTypeId,
                                behandlingstidUnits = svarbrevRequest.behandlingstidUnits,
                                behandlingstidDate = null
                            )
                        }
                        +" fra vi mottok anken. Du finner oversikt over saksbehandlingstidene våre på www.nav.no/saksbehandlingstid."
                    }
                    if (!svarbrevRequest.customText.isNullOrBlank()) {
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

    private fun getHTMLDocumentOmgjoeringskrav(
        svarbrevRequest: SvarbrevRequest,
        currentDate: LocalDate,
    ): Document {
        return createHTMLDocument()
            .html {
                head {
                    style {
                        unsafe {
                            raw(
                                getCss()
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
                            span { +svarbrevRequest.sakenGjelder.name }
                        }
                        if (svarbrevRequest.klager != null && svarbrevRequest.klager.fnr != svarbrevRequest.sakenGjelder.fnr) {
                            p {
                                classes = setOf("label-content")
                                span {
                                    classes = setOf("label")
                                    +"Den som krever omgjøring: "
                                }
                                span { +svarbrevRequest.klager.name }
                            }
                        }
                        p {
                            classes = setOf("label-content")
                            span {
                                classes = setOf("label")
                                +"Fødselsnummer: "
                            }
                            span { +svarbrevRequest.sakenGjelder.fnr.toFnrView() }
                        }
                        if (!svarbrevRequest.fullmektigFritekst.isNullOrBlank()) {
                            p {
                                classes = setOf("label-content")
                                span {
                                    classes = setOf("label")
                                    +"Fullmektig: "
                                }
                                span { +svarbrevRequest.fullmektigFritekst }
                            }
                        }
                        div {
                            id = "current-date"
                            classes = setOf("current-date")
                            + getFormattedDate(currentDate)
                        }
                    }
                    h1 { +"Klageinstans har mottatt kravet ditt om omgjøring" }
                    p {
                        +"Vi viser til kravet ditt om omgjøring av vedtak som gjelder ${
                            getYtelseDisplayText(
                                ytelseId = svarbrevRequest.ytelseId
                            )
                        }, som vi mottok ${
                            getFormattedDate(
                                svarbrevRequest.receivedDate!!
                            )
                        }."
                    }

                    if (!svarbrevRequest.initialCustomText.isNullOrBlank()) {
                        p {
                            +svarbrevRequest.initialCustomText
                        }
                    }

                    h2 { +"Behandling av kravet om omgjøring" }
                    p {
                        +"Saksbehandlingstiden vår er vanligvis "
                        span {
                            +getBehandlingstidText(
                                behandlingstidUnitTypeId = svarbrevRequest.behandlingstidUnitTypeId,
                                behandlingstidUnits = svarbrevRequest.behandlingstidUnits,
                                behandlingstidDate = null
                            )
                        }
                        +" fra vi mottok kravet om omgjøring, men dette kan variere avhengig av hvor mange klagesaker vi har til behandling. ${svarbrevRequest.customText ?: ""}"
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

    private fun getHTMLDocumentBegjaeringOmGjenopptak(
        svarbrevRequest: SvarbrevRequest,
        currentDate: LocalDate,
    ): Document {
        return createHTMLDocument()
            .html {
                head {
                    style {
                        unsafe {
                            raw(
                                getCss()
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
                            span { +svarbrevRequest.sakenGjelder.name }
                        }
                        if (svarbrevRequest.klager != null && svarbrevRequest.klager.fnr != svarbrevRequest.sakenGjelder.fnr) {
                            p {
                                classes = setOf("label-content")
                                span {
                                    classes = setOf("label")
                                    +"Den som begjærer gjenopptak: "
                                }
                                span { +svarbrevRequest.klager.name }
                            }
                        }
                        p {
                            classes = setOf("label-content")
                            span {
                                classes = setOf("label")
                                +"Fødselsnummer: "
                            }
                            span { +svarbrevRequest.sakenGjelder.fnr.toFnrView() }
                        }
                        if (!svarbrevRequest.fullmektigFritekst.isNullOrBlank()) {
                            p {
                                classes = setOf("label-content")
                                span {
                                    classes = setOf("label")
                                    +"Fullmektig: "
                                }
                                span { +svarbrevRequest.fullmektigFritekst }
                            }
                        }
                        div {
                            id = "current-date"
                            classes = setOf("current-date")
                            + getFormattedDate(currentDate)
                        }
                    }
                    h1 { +"Klageinstans orienterer om saksbehandlingen ved begjæring om gjenopptak" }
                    p {
                        +"Vi viser til begjæringen din om gjenopptak av Trygderettens kjennelse som gjelder ${
                            getYtelseDisplayText(
                                ytelseId = svarbrevRequest.ytelseId
                            )
                        }, som vi mottok ${
                            getFormattedDate(
                                svarbrevRequest.receivedDate!!
                            )
                        }."
                    }

                    if (!svarbrevRequest.initialCustomText.isNullOrBlank()) {
                        p {
                            +svarbrevRequest.initialCustomText
                        }
                    }

                    h2 { +"Behandling av krav om gjenopptak" }
                    p {
                        +"Saksbehandlingstiden vår er vanligvis "
                        span {
                            +getBehandlingstidText(
                                behandlingstidUnitTypeId = svarbrevRequest.behandlingstidUnitTypeId,
                                behandlingstidUnits = svarbrevRequest.behandlingstidUnits,
                                behandlingstidDate = null
                            )
                        }
                        +" fra vi mottok begjæringen om gjenopptak, men dette kan variere avhengig av hvor mange saker vi har til behandling. ${svarbrevRequest.customText ?: ""}"
                    }
                    p {
                        div {
                            +"Du finner en oppdatert oversikt over saksbehandlingstiden vår på"
                        }
                        div {
                            +"www.nav.no/saksbehandlingstid."
                        }
                    }
                    p {
                        +"Vi skal ta vedtaket vårt, som ble vurdert i kjennelsen, opp til ny vurdering. Dersom vi ikke endrer det, sender vi saken din til Trygderetten."
                    }
                    h2 { +"Dersom saken går til Trygderetten" }
                    p {
                        +"Hvis saken din går videre til Trygderetten, vil du få kopi av oversendelsesbrevet, der vi forklarer saken og gir vår vurdering av begjæringen din om gjenopptak."
                    }
                    p {
                        +"Du får da mulighet til å komme med merknader, som vil følge saken til Trygderetten."
                    }
                    h2 { +"Du må melde fra om endringer" }
                    p {
                        +"Skjer det endringer du mener er viktig for saken din, må du orientere oss. Dette kan for eksempel være medisinske forhold, arbeid, inntekt og sivilstand. "
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
}