package no.nav.klage.pdfgen

import no.nav.klage.pdfgen.api.view.InnholdsfortegnelseRequest
import no.nav.klage.pdfgen.api.view.InnholdsfortegnelseRequest.Document.Type
import no.nav.klage.pdfgen.service.InnholdsfortegnelseService
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GenerateInnholdsfortegnelsePDFTest {

    @BeforeAll
    fun emptyFileDiffFolder() {
        cleanOutputFolder()
    }

    @Test
    fun `generate pdf from full input`() {
        val data = InnholdsfortegnelseService().getInnholdsfortegnelsePDFAsByteArray(InnholdsfortegnelseRequest(
            parentTitle = "Svar på innsynsbegjæring",
            formattedParentDate = LocalDate.of(2025, 6, 11),
            documents = listOf(
                InnholdsfortegnelseRequest.Document(
                    tittel = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et",
                    tema = "Kompensasjon for selvstendig næringsdrivende/frilansere",
                    dato = TEST_DATE,
                    avsenderMottaker = "Kalle Anka",
                    saksnummer = "123456",
                    type = Type.U
                ),
                InnholdsfortegnelseRequest.Document(
                    tittel = "ROL notat 2023",
                    tema = "Arbeidsrådgivning - psykologtjenester",
                    dato = TEST_DATE,
                    avsenderMottaker = "Kajsa Anka",
                    saksnummer = "123456",
                    type = Type.I
                ),
                InnholdsfortegnelseRequest.Document(
                    tittel = "Vedtak 2022",
                    tema = "Helsetjenester og ortopediske hjelpemidler",
                    dato = TEST_DATE.minusMonths(4),
                    avsenderMottaker = "Mette Wendy Lindberg Gulbrandsen",
                    saksnummer = "123456",
                    type = Type.N
                ),
                InnholdsfortegnelseRequest.Document(
                    tittel = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et",
                    tema = "Foreldrepenger",
                    dato = TEST_DATE.minusMonths(5),
                    avsenderMottaker = "Knatte Anka",
                    saksnummer = "123456", 
                    type = Type.U
                ),
            ),
        ))
        comparePdf("vedleggsoversikt", data)
    }

}
