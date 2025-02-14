package no.nav.klage.pdfgen.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.pdfgen.api.view.DocumentValidationResponse
import no.nav.klage.pdfgen.api.view.ForlengetBehandlingstidRequest
import no.nav.klage.pdfgen.api.view.InnholdsfortegnelseRequest
import no.nav.klage.pdfgen.api.view.SvarbrevRequest
import no.nav.klage.pdfgen.exception.EmptyPlaceholderException
import no.nav.klage.pdfgen.exception.EmptyRegelverkException
import no.nav.klage.pdfgen.service.ForlengetBehandlingstidService
import no.nav.klage.pdfgen.service.InnholdsfortegnelseService
import no.nav.klage.pdfgen.service.PDFGenService
import no.nav.klage.pdfgen.service.SvarbrevService
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

@RestController
@Tag(name = "kabal-json-to-pdf", description = "Create PDF from JSON")
class PDFGenController(
    private val pdfGenService: PDFGenService,
    private val innholdsfortegnelseService: InnholdsfortegnelseService,
    private val svarbrevService: SvarbrevService,
    private val forlengetBehandlingstidService: ForlengetBehandlingstidService
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
        summary = "Generate pdf from json",
        description = "Generate pdf from json"
    )
    @ResponseBody
    @PostMapping("/toinnholdsfortegnelse")
    fun toInnholdsfortegnelsePDF(
        @RequestBody input: InnholdsfortegnelseRequest,
    ): ResponseEntity<ByteArray> {
        logger.debug("toInnholdsfortegnelsePDF() called. See body in secure logs")
        secureLogger.debug("toInnholdsfortegnelsePDF() called. Received input: {}", input)

        val data = innholdsfortegnelseService.getInnholdsfortegnelsePDFAsByteArray(input)

        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = MediaType.APPLICATION_PDF
        responseHeaders.add("Content-Disposition", "inline; filename=vedleggsoversikt.pdf")
        return ResponseEntity(
            data,
            responseHeaders,
            HttpStatus.OK
        )
    }

    @Operation(
        summary = "Generate svarbrev",
        description = "Generate svarbrev"
    )
    @ResponseBody
    @PostMapping("/svarbrev")
    fun generateSvarbrev(
        @RequestBody input: SvarbrevRequest,
    ): ResponseEntity<ByteArray> {
        logger.debug("generateSvarbrev() called. See body in secure logs")
        secureLogger.debug("generateSvarbrev() called. Received input: {}", input)

        val data = svarbrevService.getSvarbrevAsByteArray(input)

        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = MediaType.APPLICATION_PDF
        responseHeaders.add("Content-Disposition", "inline; filename=svarbrev.pdf")
        return ResponseEntity(
            data,
            responseHeaders,
            HttpStatus.OK
        )
    }

    @Operation(
        summary = "Generate forlenget behandlingstid letter",
        description = "Generate forlenget behandlingstid letter"
    )
    @ResponseBody
    @PostMapping("/forlengetbehandlingstid")
    fun generateForlengetBehandlingstid(
        @RequestBody input: ForlengetBehandlingstidRequest,
    ): ResponseEntity<ByteArray> {
        logger.debug("generateForlengetBehandlingstid() called. See body in secure logs")
        secureLogger.debug("generateForlengetBehandlingstid() called. Received input: {}", input)

        val data = forlengetBehandlingstidService.getForlengetBehandlingstidAsByteArray(input)

        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = MediaType.APPLICATION_PDF
        responseHeaders.add("Content-Disposition", "inline; filename=forlengetbehandlingstid.pdf")
        return ResponseEntity(
            data,
            responseHeaders,
            HttpStatus.OK
        )
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