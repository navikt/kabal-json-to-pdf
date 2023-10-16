package no.nav.klage.pdfgen.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.html.dom.serialize
import no.nav.klage.pdfgen.api.view.DocumentValidationResponse
import no.nav.klage.pdfgen.exception.EmptyPlaceholderException
import no.nav.klage.pdfgen.exception.EmptyRegelverkException
import no.nav.klage.pdfgen.service.PDFGenService
import no.nav.klage.pdfgen.util.getLogger
import no.nav.klage.pdfgen.util.getSecureLogger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.w3c.dom.Document

@RestController
@Tag(name = "kabal-json-to-pdf", description = "Create PDF from JSON")
class PDFGenController(
    private val pdfGenService: PDFGenService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Operation(
        summary = "Generate pdf from json",
        description = "Generate pdf from json"
    )
    @ResponseBody
    @PostMapping("/topdf")
    fun toPDF(
        @RequestBody json: String
    ): ResponseEntity<ByteArray> {
        logger.debug("toPDF() called. See body in secure logs")
        secureLogger.debug("toPDF() called. Received json: {}", json)

        val data = pdfGenService.getPDFAsByteArray(json)

        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = MediaType.APPLICATION_PDF
        responseHeaders.add("Content-Disposition", "inline; filename=file.pdf")
        return ResponseEntity(
            data,
            responseHeaders,
            HttpStatus.OK
        )
    }

    @Operation(
        summary = "Generate html from json",
        description = "Generate html from json"
    )
    @ResponseBody
    @PostMapping("/tohtml")
    fun toHtml(
        @RequestBody json: String
    ): String {
        logger.debug("toHtml() called. See body in secure logs")
        secureLogger.debug("toHtml() called. Received json: {}", json)

        return pdfGenService.getHTMLDocument(json).serialize(prettyPrint = false)
    }

    @Operation(
        summary = "Generate pdf then html from json",
        description = "Generate pdf then html from json"
    )
    @ResponseBody
    @PostMapping("/topdftohtml")
    fun toPdfToHtml(
        @RequestBody json: String
    ): String {
        logger.debug("toPdfToHtml() called. See body in secure logs")
        secureLogger.debug("toPdfToHtml() called. Received json: {}", json)

        return pdfGenService.getPdfToHTMLDocument(json)
    }

    @Operation(
        summary = "Validate pdf input",
        description = "Validate pdf input"
    )
    @PostMapping("/validate")
    fun validate(
        @RequestBody json: String
    ): DocumentValidationResponse {
        logger.debug("${::validate.name} called. See body in secure logs")
        secureLogger.debug("validate() called. Received json: {}", json)

        return try {
            pdfGenService.validateDocumentContent(json)
            DocumentValidationResponse()
        } catch (epe: EmptyPlaceholderException) {
            DocumentValidationResponse(
                errors = listOf(
                    DocumentValidationResponse.DocumentValidationError(
                        type = "EMPTY_PLACEHOLDERS"
                    )
                )
            )
        } catch (ere: EmptyRegelverkException) {
            DocumentValidationResponse(
                errors = listOf(
                    DocumentValidationResponse.DocumentValidationError(
                        type = "EMPTY_REGELVERK"
                    )
                )
            )
        }
    }
}