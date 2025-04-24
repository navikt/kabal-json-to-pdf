package no.nav.klage.pdfgen

import no.nav.klage.pdfgen.exception.EmptyPlaceholderException
import no.nav.klage.pdfgen.exception.EmptyRegelverkException
import no.nav.klage.pdfgen.service.PDFGenService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.nio.file.Files
import java.nio.file.Path


class GeneratePDF {

    val path = "src/test/resources/"

    @Test
    fun `generate pdf from full input`() {
        val jsonData = File(path + "full-document.json").readText()
        val data = PDFGenService().getPDFAsByteArray(jsonData)
        Files.write(Path.of("test.pdf"), data)
    }

    @Test
    fun `generate pdf with center-align`() {
        val jsonData = File(path + "center-align.json").readText()
        val data = PDFGenService().getPDFAsByteArray(jsonData)
        Files.write(Path.of("center.pdf"), data)
    }

    @Test
    fun `generate pdf from fullmektig input`() {
        val jsonData = File(path + "fullmektig.json").readText()
        val data = PDFGenService().getPDFAsByteArray(jsonData)
        Files.write(Path.of("test.pdf"), data)
    }

    @Test
    fun `generate pdf from table input`() {
        val jsonData = File(path + "tables.json").readText()
        val data = PDFGenService().getPDFAsByteArray(jsonData)
        Files.write(Path.of("test.pdf"), data)
    }

    @Test
    fun `generate pdf from minimal input`() {
        val jsonData = File(path + "minimal.json").readText()
        val data = PDFGenService().getPDFAsByteArray(jsonData)
        Files.write(Path.of("test.pdf"), data)
    }

    @Test
    fun `generate pdf with placeholder examples`() {
        val jsonData = File(path + "incomplete-placeholder-example.json").readText()
        val data = PDFGenService().getPDFAsByteArray(jsonData)
        Files.write(Path.of("test.pdf"), data)
    }

    @Test
    fun `generate pdf with null in header`() {
        val jsonData = File(path + "null-in-header.json").readText()
        val data = PDFGenService().getPDFAsByteArray(jsonData)
        Files.write(Path.of("test.pdf"), data)
    }

    @Test
    fun `generate pdf with regelverk type`() {
        val jsonData = File(path + "complete-with-regelverk.json").readText()
        val data = PDFGenService().getPDFAsByteArray(jsonData)
        Files.write(Path.of("test.pdf"), data)
    }

    @Test
    fun `generate pdf with redigerbar maltekst`() {
        val jsonData = File(path + "redigerbar-maltekst.json").readText()
        val data = PDFGenService().getPDFAsByteArray(jsonData)
        Files.write(Path.of("test.pdf"), data)
    }

    @Test
    fun `input without header throws error`(){
        val jsonData = File(path + "no-header.json").readText()
        assertThrows<RuntimeException> { PDFGenService().getPDFAsByteArray(jsonData) }
    }

    @Test
    fun `validate pdf with incomplete placeholder throws exception`() {
        val jsonData = File(path + "incomplete-placeholder-example.json").readText()
        assertThrows<EmptyPlaceholderException> { PDFGenService().validateDocumentContent(jsonData) }
    }

    @Test
    fun `validate pdf with complete placeholders passes`() {
        val jsonData = File(path + "complete-placeholder-example.json").readText()
        PDFGenService().validateDocumentContent(jsonData)
    }

    @Test
    fun `validate pdf with complete regelverk passes`() {
        val jsonData = File(path + "complete-with-regelverk.json").readText()
        PDFGenService().validateDocumentContent(jsonData)
    }

    @Test
    fun `validate pdf with incomplete regelverk throws exception`() {
        val jsonData = File(path + "empty-regelverk-container.json").readText()
        assertThrows<EmptyRegelverkException> { PDFGenService().validateDocumentContent(jsonData) }
    }

    @Test
    fun `validate pdf with text somewhere in regelverk passes`() {
        val jsonData = File(path + "not-all-empty-text-regelverk.json").readText()
        PDFGenService().validateDocumentContent(jsonData)
    }

    @Test
    fun `validate pdf with only empty texts in regelverk throws exception`() {
        val jsonData = File(path + "all-empty-text-regelverk.json").readText()
        assertThrows<EmptyRegelverkException> { PDFGenService().validateDocumentContent(jsonData) }
    }

    @Test
    fun `validate pdf passes`() {
        val jsonData = File(path + "minimal.json").readText()
        PDFGenService().validateDocumentContent(jsonData)
    }

    @Test
    fun `generate pdf with current date on later page`() {
        val jsonData = File(path + "tilsvarsbrev-med-oversendelsesbrev.json").readText()
        val data = PDFGenService().getPDFAsByteArray(jsonData)
        Files.write(Path.of("test.pdf"), data)
    }
}
