package no.nav.klage.pdfgen

import no.nav.klage.kodeverk.TimeUnitType
import no.nav.klage.pdfgen.api.view.ForlengetBehandlingstidRequest
import no.nav.klage.pdfgen.service.ForlengetBehandlingstidService
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GenerateForlengetBehandlingstidPDFTest {

    @BeforeAll
    fun emptyFileDiffFolder() {
        cleanOutputFolder()
    }

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
                behandlingstidDate = LocalDate.now(),
                avsenderEnhetId = "4291",
                type = ForlengetBehandlingstidRequest.Type.ANKE,
                previousBehandlingstidInfo = " Hei og hei . ",
                reason = " og her da  ",
                customText = " og her også . ",
                
            )
        )
        comparePdf("forlengetbehandlingstid", data)
    }

}
