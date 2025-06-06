package no.nav.klage.pdfgen.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.klage.pdfgen.transformers.HtmlCreator
import no.nav.klage.pdfgen.util.createPDFA
import org.springframework.stereotype.Service
import org.w3c.dom.Document

@Service
class PDFGenService {

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

}