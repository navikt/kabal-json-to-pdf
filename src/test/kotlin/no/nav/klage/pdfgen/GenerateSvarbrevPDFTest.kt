package no.nav.klage.pdfgen

import no.nav.klage.pdfgen.api.view.SvarbrevRequest
import no.nav.klage.pdfgen.service.SvarbrevService
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

class GenerateSvarbrevPDFTest {

    @Test
    fun `generate pdf from full input`() {
        val data = SvarbrevService().getSvarbrevAsByteArray(
            SvarbrevRequest(
                title = "Svarbrev",
                sakenGjelder = SvarbrevRequest.Part(name = "First Last", fnr = "12345678910"),
                klager = SvarbrevRequest.Part(name = "Second Last", fnr = "23456789120"),
                ytelsenavn = "Grunn- og hjelpestønad - Grunnstønad",
                fullmektigFritekst = "Fullmektig Fritekst",
                ankeReceivedDate = LocalDate.now(),
                receivedDate = LocalDate.now(),
                behandlingstidInWeeks = 12,
                avsenderEnhetId = "4291",
                type = null,
                customText = null,
            )
        )
        Files.write(Path.of("svarbrev.pdf"), data)
    }

    @Test
    fun `generate pdf from full klage input`() {
        val data = SvarbrevService().getSvarbrevAsByteArray(
            SvarbrevRequest(
                title = "Svarbrev",
                sakenGjelder = SvarbrevRequest.Part(name = "First Last", fnr = "12345678910"),
                klager = null,
                ytelsenavn = "Sykdom i familien - Pleiepenger sykt barn",
                fullmektigFritekst = null,
                ankeReceivedDate = null,
                receivedDate = LocalDate.now(),
                behandlingstidInWeeks = 14,
                avsenderEnhetId = "4291",
                type = SvarbrevRequest.Type.KLAGE,
                customText = "Dette er fritekst hentet fra innstillinger.",
            )
        )
        Files.write(Path.of("svarbrev.pdf"), data)
    }

}
