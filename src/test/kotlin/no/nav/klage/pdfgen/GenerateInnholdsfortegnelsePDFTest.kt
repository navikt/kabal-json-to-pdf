package no.nav.klage.pdfgen

import no.nav.klage.pdfgen.api.view.InnholdsfortegnelseRequest
import no.nav.klage.pdfgen.api.view.InnholdsfortegnelseRequest.Document.Type
import no.nav.klage.pdfgen.service.InnholdsfortegnelseService
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

class GenerateInnholdsfortegnelsePDFTest {

    @Test
    fun `generate pdf from full input`() {
        val data = InnholdsfortegnelseService().getInnholdsfortegnelsePDFAsByteArray(InnholdsfortegnelseRequest(
            documents = listOf(
                InnholdsfortegnelseRequest.Document(
                    tittel = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et",
                    tema = "Kompensasjon for selvstendig næringsdrivende/frilansere",
                    dato = LocalDate.now(),
                    avsenderMottaker = "Kalle Anka",
                    saksnummer = "123456",
                    type = Type.U
                ),
                InnholdsfortegnelseRequest.Document(
                    tittel = "ROL notat 2023",
                    tema = "Arbeidsrådgivning - psykologtjenester",
                    dato = LocalDate.now(),
                    avsenderMottaker = "Kajsa Anka",
                    saksnummer = "123456",
                    type = Type.I
                ),
                InnholdsfortegnelseRequest.Document(
                    tittel = "Vedtak 2022",
                    tema = "Helsetjenester og ortopediske hjelpemidler",
                    dato = LocalDate.now().minusMonths(4),
                    avsenderMottaker = "Mette Wendy Lindberg Gulbrandsen",
                    saksnummer = "123456",
                    type = Type.N
                ),
                InnholdsfortegnelseRequest.Document(
                    tittel = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et",
                    tema = "Foreldrepenger",
                    dato = LocalDate.now().minusMonths(5),
                    avsenderMottaker = "Knatte Anka",
                    saksnummer = "123456", 
                    type = Type.U
                ),
            ),
        ))
        Files.write(Path.of("vedleggsoversikt.pdf"), data)
    }

}
