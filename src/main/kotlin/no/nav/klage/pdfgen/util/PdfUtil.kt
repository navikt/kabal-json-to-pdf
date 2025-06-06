package no.nav.klage.pdfgen.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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

fun createPDFA(w3doc: Document): ByteArray {
    ByteArrayOutputStream().use { outputStream ->
        PdfRendererBuilder()
            .apply {
                for (font in FontConfig.fontsWithTTF) {
                    useFont(
                        font.pdFontSupplier,
                        font.family,
                        font.weight,
                        font.style,
                        font.subset
                    )
                }
            }
            .useColorProfile(colorProfile)
            .useSVGDrawer(BatikSVGDrawer())
            .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_2_U)
            .withW3cDocument(w3doc, FontConfig.baseUri)
            .toStream(outputStream)
            .run()

        return outputStream.toByteArray()
    }
}

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
object FontConfig {

    val baseUri: String = PDFGenService::javaClass.javaClass.getResource("/dummy.html").toExternalForm()

    val fontsWithTTF: List<FontWithSupplier> by lazy {
        val fonts: Array<FontMetadata> =
            jacksonObjectMapper().readValue(ClassPathResource("/fonts/config.json").inputStream)
        val doc = PDDocument()
        fonts.map {
            getFontWithTTF(font = it, doc = doc)
        }
    }

    fun getFontWithTTF(font: FontMetadata, doc: PDDocument): FontWithSupplier {
        val ttf = TTFParser().parseEmbedded(
            ClassPathResource("/fonts/${font.path}").inputStream
        )
        ttf.isEnableGsub = false

        return FontWithSupplier(
            family = font.family,
            weight = font.weight,
            style = font.style,
            subset = font.subset,
            pdFontSupplier = PDFontSupplier(PDType0Font.load(doc, ttf, font.subset)),
        )
    }
}

data class FontWithSupplier(
    val family: String,
    val weight: Int,
    val style: BaseRendererBuilder.FontStyle,
    val subset: Boolean,
    val pdFontSupplier: PDFontSupplier,
)

val colorProfile: ByteArray = IOUtils.toByteArray(Application::class.java.getResourceAsStream("/sRGB2014.icc"))

data class FontMetadata(
    val family: String,
    val path: String,
    val weight: Int,
    val style: BaseRendererBuilder.FontStyle,
    val subset: Boolean,
)