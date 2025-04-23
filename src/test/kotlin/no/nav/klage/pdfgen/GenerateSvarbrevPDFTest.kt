package no.nav.klage.pdfgen

import no.nav.klage.kodeverk.TimeUnitType
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
                ytelseId = "31",
                fullmektigFritekst = "Fullmektig Fritekst",
                ankeReceivedDate = LocalDate.now(),
                receivedDate = LocalDate.now(),
                behandlingstidUnits = 12,
                behandlingstidUnitTypeId = TimeUnitType.WEEKS.id,
                avsenderEnhetId = "4291",
                type = SvarbrevRequest.Type.KLAGE,
                initialCustomText = null,
                customText = "Litt ekstra fritekst.",
            )
        )
        Files.write(Path.of("svarbrev.pdf"), data)
    }

    @Test
    fun `generate pdf from full anke input`() {
        val data = SvarbrevService().getSvarbrevAsByteArray(
            SvarbrevRequest(
                title = "Svarbrev og hei og hei",
                sakenGjelder = SvarbrevRequest.Part(name = "First Last", fnr = "12345678910"),
                klager = SvarbrevRequest.Part(name = "Second Last", fnr = "23456789120"),
                ytelseId = "3",
                fullmektigFritekst = "Fullmektig fritekst",
                ankeReceivedDate = null,
                receivedDate = LocalDate.now(),
                behandlingstidUnits = 12,
                behandlingstidUnitTypeId = TimeUnitType.WEEKS.id,
                avsenderEnhetId = "4291",
                type = SvarbrevRequest.Type.ANKE,
                initialCustomText = "Her har vi lagt inn litt ekstra informasjon.",
                customText = null,
            )
        )
        Files.write(Path.of("svarbrev.pdf"), data)
    }

}
