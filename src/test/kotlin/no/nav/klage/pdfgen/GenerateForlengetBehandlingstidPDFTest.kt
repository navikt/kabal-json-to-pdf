package no.nav.klage.pdfgen

import no.nav.klage.kodeverk.TimeUnitType
import no.nav.klage.pdfgen.api.view.ForlengetBehandlingstidRequest
import no.nav.klage.pdfgen.config.PdfRendererBuilderConfig
import no.nav.klage.pdfgen.service.ForlengetBehandlingstidService
import no.nav.klage.pdfgen.service.PDFGenService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

@ActiveProfiles("local")
@SpringBootTest(
    classes = [
        PDFGenService::class,
        PdfRendererBuilderConfig::class,
        ForlengetBehandlingstidService::class,
    ]
)
class GenerateForlengetBehandlingstidPDFTest {

    @Autowired
    lateinit var forlengetBehandlingstidService: ForlengetBehandlingstidService

    @Test
    fun `generate pdf from full input`() {
        val data = forlengetBehandlingstidService.getForlengetBehandlingstidAsByteArray(
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
        Files.write(Path.of("forlengetbehandlingstid.pdf"), data)
    }

}
