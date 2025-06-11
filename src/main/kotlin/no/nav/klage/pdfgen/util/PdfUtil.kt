package no.nav.klage.pdfgen.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import com.openhtmltopdf.pdfboxout.PDFontSupplier
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import org.apache.fontbox.ttf.TTFParser
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.springframework.core.io.ClassPathResource
import org.w3c.dom.Document
import java.io.ByteArrayOutputStream

private val colorprofile = ClassPathResource("/sRGB2014.icc").contentAsByteArray
private val baseUri = ClassPathResource("/dummy.html").uri.toString()

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun createPDFA(w3doc: Document): ByteArray {
    ByteArrayOutputStream().use { outputStream ->
        PdfRendererBuilder()
            .apply {
                for (font in fontsWithTTF()) {
                    useFont(
                        font.pdFontSupplier,
                        font.family,
                        font.weight,
                        font.style,
                        font.subset
                    )
                }
            }
            .useColorProfile(colorprofile)
            .useSVGDrawer(BatikSVGDrawer())
            .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_2_U)
            .withW3cDocument(w3doc, baseUri)
            .toStream(outputStream)
            .run()

        return outputStream.toByteArray()
    }
}

private fun fontsWithTTF(): List<FontWithSupplier> {
    val fonts: Array<FontMetadata> =
        jacksonObjectMapper().readValue(ClassPathResource("/fonts/config.json").contentAsByteArray)
    return fonts.map { font ->
        getFontWithTTF(font = font)
    }
}

private fun getFontWithTTF(font: FontMetadata): FontWithSupplier {
    val ttf = TTFParser().parseEmbedded(
        ClassPathResource("/fonts/${font.path}").inputStream
    )
    ttf.isEnableGsub = false

    return FontWithSupplier(
        family = font.family,
        weight = font.weight,
        style = font.style,
        subset = font.subset,
        pdFontSupplier = PDFontSupplier(PDType0Font.load(PDDocument(), ttf, font.subset)),
    )
}

private data class FontWithSupplier(
    val family: String,
    val weight: Int,
    val style: BaseRendererBuilder.FontStyle,
    val subset: Boolean,
    val pdFontSupplier: PDFontSupplier,
)

private data class FontMetadata(
    val family: String,
    val path: String,
    val weight: Int,
    val style: BaseRendererBuilder.FontStyle,
    val subset: Boolean,
)