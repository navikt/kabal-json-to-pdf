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
                sakenGjelder = SvarbrevRequest.SakenGjelder(name = "First Last", fnr = "12345678910"),
                enhetsnavn = "NAV Klageinstans Oslo",
                fullmektigFritekst = "Fullmektig Fritekst",
                ankeReceivedDate = LocalDate.now(),
                behandlingstidInWeeks = 12
            )
        )
        Files.write(Path.of("svarbrev.pdf"), data)
    }

}
