package no.nav.klage.pdfgen.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import no.nav.klage.pdfgen.transformers.HtmlCreator
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import java.io.ByteArrayOutputStream

@Service
class PDFGenService(
    private val pdfRendererBuilder: PdfRendererBuilder
) {

    fun getPDFAsByteArray(json: String): ByteArray {
        val doc = getHTMLDocument(jacksonObjectMapper().readValue(json, List::class.java) as List<Map<String, *>>)
        return createPDFA(doc)
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

    fun createPDFA(w3doc: Document): ByteArray {
        val os = ByteArrayOutputStream()
        pdfRendererBuilder
            .withW3cDocument(w3doc, this::javaClass.javaClass.getResource("/dummy.html")!!.toExternalForm())
            .toStream(os)
            .buildPdfRenderer()
            .createPDF()
        return os.toByteArray()
    }

}