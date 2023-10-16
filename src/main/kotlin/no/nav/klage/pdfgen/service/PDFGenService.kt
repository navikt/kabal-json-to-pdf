package no.nav.klage.pdfgen.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.openhtmltopdf.extend.FSSupplier
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import no.nav.klage.pdfgen.Application
import no.nav.klage.pdfgen.transformers.HtmlCreator
import org.apache.pdfbox.io.IOUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.fit.pdfdom.PDFDomTree
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import java.io.*
import java.nio.charset.StandardCharsets


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

@Service
class PDFGenService {

    fun getPDFAsByteArray(json: String): ByteArray {
        val doc = getHTMLDocument(jacksonObjectMapper().readValue(json, List::class.java) as List<Map<String, *>>)
        val os = ByteArrayOutputStream()
        createPDFA(doc, os)
        return os.toByteArray()
    }

    fun getHTMLDocument(json: String): Document {
        return getHTMLDocument(jacksonObjectMapper().readValue(json, List::class.java) as List<Map<String, *>>)
    }

    fun getPdfToHTMLDocument(json: String): String {
        val doc = getHTMLDocument(jacksonObjectMapper().readValue(json, List::class.java) as List<Map<String, *>>)
        val os = ByteArrayOutputStream()
        createPDFA(doc, os)
        val outputStream = os.toByteArray()

        val pdf: PDDocument = PDDocument.load(outputStream)
        val parser = PDFDomTree()
        val baos = ByteArrayOutputStream()
        val output: Writer = PrintWriter(baos, true, StandardCharsets.UTF_8)
        parser.writeText(pdf, output)
        output.close()
        pdf.close()
        return String(baos.toByteArray(), StandardCharsets.UTF_8)
    }

    fun validateDocumentContent(json: String) {
        getHTMLDocument(jacksonObjectMapper().readValue(json, List::class.java) as List<Map<String, *>>, true)
    }

    private fun getHTMLDocument(list: List<Map<String, *>>, validationMode: Boolean = false): Document {
        validateHeaderFooter(list)
        val c = HtmlCreator(list, validationMode)
        return c.getDoc()
    }

    private fun validateHeaderFooter(list: List<Map<String, *>>) {
        if (list.any { it["type"] == "header" }.xor(list.any { it["type"] == "footer" })) {
            throw RuntimeException("Both a header and a footer must be defined.")
        }
    }

    private fun createPDFA(w3doc: Document, outputStream: OutputStream) = PdfRendererBuilder()
        .apply {
            for (font in fonts) {
                useFont(FSSupplier(getIs(font.path)), font.family, font.weight, font.style, font.subset)
            }
        }
        // .useFastMode() wait with fast mode until it doesn't print a bunch of errors
        .useColorProfile(colorProfile)
        .useSVGDrawer(BatikSVGDrawer())
        .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_2_U)
        .withW3cDocument(w3doc, PDFGenService::javaClass.javaClass.getResource("/dummy.html").toExternalForm())
        .toStream(outputStream)
        .buildPdfRenderer()
        .createPDF()

    private fun getIs(path: String): () -> InputStream { return { ClassPathResource("/fonts/$path").inputStream } }

}