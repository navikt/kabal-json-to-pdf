package no.nav.klage.pdfgen.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import com.openhtmltopdf.pdfboxout.PDFontSupplier
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import no.nav.klage.pdfgen.Application
import no.nav.klage.pdfgen.service.PDFGenService
import org.apache.fontbox.ttf.TTFParser
import org.apache.pdfbox.io.IOUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.springframework.core.io.ClassPathResource
import org.w3c.dom.Document
import java.io.ByteArrayOutputStream

val objectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()

val colorProfile: ByteArray = IOUtils.toByteArray(Application::class.java.getResourceAsStream("/sRGB2014.icc"))

val fonts: Array<FontMetadata> =
    objectMapper.readValue(ClassPathResource("/fonts/config.json").inputStream)

data class FontMetadata(
    val family: String,
    val path: String,
    val weight: Int,
    val style: BaseRendererBuilder.FontStyle,
    val subset: Boolean
)

fun createPDFA(w3doc: Document): ByteArray {
    ByteArrayOutputStream().use {
        PdfRendererBuilder()
            .apply {
                for (font in fonts) {
                    val ttf = TTFParser().parseEmbedded(
                        ClassPathResource("/fonts/${font.path}").inputStream
                    )
                    ttf.isEnableGsub = false

                    useFont(
                        PDFontSupplier(PDType0Font.load(PDDocument(), ttf, font.subset)),
                        font.family,
                        font.weight,
                        font.style,
                        font.subset
                    )
                }
            }
            // .useFastMode() wait with fast mode until it doesn't print a bunch of errors
            .useColorProfile(colorProfile)
            .useSVGDrawer(BatikSVGDrawer())
            .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_2_U)
            .withW3cDocument(w3doc, PDFGenService::javaClass.javaClass.getResource("/dummy.html").toExternalForm())
            .toStream(it)
            .run()

        return it.toByteArray()
    }
}