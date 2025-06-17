package no.nav.klage.pdfgen

import no.nav.klage.kodeverk.TimeUnitType
import no.nav.klage.pdfgen.api.view.SvarbrevRequest
import no.nav.klage.pdfgen.service.SvarbrevService
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GenerateSvarbrevPDFTest {

    @BeforeAll
    fun emptyFileDiffFolder() {
        cleanOutputFolder()
    }

    @Test
    fun `generate pdf from full input`() {
        val data = SvarbrevService(currentDate = TEST_DATE).getSvarbrevAsByteArray(
            SvarbrevRequest(
                title = "Svarbrev",
                sakenGjelder = SvarbrevRequest.Part(name = "First Last", fnr = "12345678910"),
                klager = SvarbrevRequest.Part(name = "Second Last", fnr = "23456789120"),
                ytelseId = "31",
                fullmektigFritekst = "Fullmektig Fritekst",
                ankeReceivedDate = TEST_DATE,
                receivedDate = TEST_DATE,
                behandlingstidUnits = 12,
                behandlingstidUnitTypeId = TimeUnitType.WEEKS.id,
                avsenderEnhetId = "4291",
                type = SvarbrevRequest.Type.KLAGE,
                initialCustomText = null,
                customText = "Litt ekstra fritekst.",
            )
        )
        comparePdf("svarbrev_klage_full", data)
    }

    @Test
    fun `generate pdf from full anke input`() {
        val data = SvarbrevService(currentDate = TEST_DATE).getSvarbrevAsByteArray(
            SvarbrevRequest(
                title = "Svarbrev og hei og hei",
                sakenGjelder = SvarbrevRequest.Part(name = "First Last", fnr = "12345678910"),
                klager = SvarbrevRequest.Part(name = "Second Last", fnr = "23456789120"),
                ytelseId = "3",
                fullmektigFritekst = "Fullmektig fritekst",
                ankeReceivedDate = null,
                receivedDate = TEST_DATE,
                behandlingstidUnits = 12,
                behandlingstidUnitTypeId = TimeUnitType.WEEKS.id,
                avsenderEnhetId = "4291",
                type = SvarbrevRequest.Type.ANKE,
                initialCustomText = "Her har vi lagt inn litt ekstra informasjon.",
                customText = null,
            )
        )
        comparePdf("svarbrev_anke_full", data)
    }

}
