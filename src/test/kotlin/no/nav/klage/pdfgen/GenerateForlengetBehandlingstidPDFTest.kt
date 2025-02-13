package no.nav.klage.pdfgen

import no.nav.klage.kodeverk.TimeUnitType
import no.nav.klage.pdfgen.api.view.ForlengetBehandlingstidRequest
import no.nav.klage.pdfgen.service.ForlengetBehandlingstidService
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

class GenerateForlengetBehandlingstidPDFTest {

    @Test
    fun `generate pdf from full input`() {
        val data = ForlengetBehandlingstidService().getForlengetBehandlingstidAsByteArray(
            ForlengetBehandlingstidRequest(
                title = "Forlenget behandlingstid",
                sakenGjelder = ForlengetBehandlingstidRequest.Part(name = "First Last", fnr = "12345678910"),
                klager = ForlengetBehandlingstidRequest.Part(name = "Second Last", fnr = "23456789120"),
                fullmektigFritekst = "Fullmektig Fritekst",
                ytelseId = "14",
                mottattKlageinstans = LocalDate.now().minusMonths(1),
                behandlingstidUnits = 12,
                behandlingstidUnitTypeId = TimeUnitType.MONTHS.id,
                avsenderEnhetId = "4291",
                type = ForlengetBehandlingstidRequest.Type.OMGJOERINGSKRAV,
                previousBehandlingstidInfo = "I brev fra Nav sendt 16. januar 2025 fikk du informasjon om at forventet behandlingstid var 6 uker.",
                reason = "Årsaken til det er lavere tilgang på kulepenner i forvaltningen.",
                behandlingstidDate = null,
                
            )
        )
        Files.write(Path.of("forlengetbehandlingstid.pdf"), data)
    }

}
