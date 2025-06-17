package no.nav.klage.pdfgen

import no.nav.klage.pdfgen.exception.EmptyPlaceholderException
import no.nav.klage.pdfgen.exception.EmptyRegelverkException
import no.nav.klage.pdfgen.service.PDFGenService
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.io.File


@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GeneratePDF {

    @BeforeAll
    fun emptyFileDiffFolder() {
        cleanOutputFolder()
    }

    @Test
    fun `generate pdf from full input`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/full-document.json").readText()
        val data = PDFGenService(currentDate = TEST_DATE).getPDFAsByteArray(jsonData)
        comparePdf("full-document", data)
    }

    @Test
    fun `generate pdf with center-align`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/center-align.json").readText()
        val data = PDFGenService(currentDate = TEST_DATE).getPDFAsByteArray(jsonData)
        comparePdf("center-align", data)
    }

    @Test
    fun `generate pdf from fullmektig input`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/fullmektig.json").readText()
        val data = PDFGenService(currentDate = TEST_DATE).getPDFAsByteArray(jsonData)
        comparePdf("fullmektig", data)
    }

    @Test
    fun `generate pdf from table input`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/tables.json").readText()
        val data = PDFGenService(currentDate = TEST_DATE).getPDFAsByteArray(jsonData)
        comparePdf("tables", data)
    }

    @Test
    fun `generate pdf from minimal input`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/minimal.json").readText()
        val data = PDFGenService(currentDate = TEST_DATE).getPDFAsByteArray(jsonData)
        comparePdf("minimal", data)
    }

    @Test
    fun `generate pdf with placeholder examples`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/incomplete-placeholder-example.json").readText()
        val data = PDFGenService(currentDate = TEST_DATE).getPDFAsByteArray(jsonData)
        comparePdf("incomplete-placeholder-example", data)
    }

    @Test
    fun `generate pdf with null in header`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/null-in-header.json").readText()
        val data = PDFGenService(currentDate = TEST_DATE).getPDFAsByteArray(jsonData)
        comparePdf("null-in-header", data)
    }

    @Test
    fun `generate pdf with regelverk type`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/complete-with-regelverk.json").readText()
        val data = PDFGenService(currentDate = TEST_DATE).getPDFAsByteArray(jsonData)
        comparePdf("complete-with-regelverk", data)
    }

    @Test
    fun `generate pdf with redigerbar maltekst`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/redigerbar-maltekst.json").readText()
        val data = PDFGenService(currentDate = TEST_DATE).getPDFAsByteArray(jsonData)
        comparePdf("redigerbar-maltekst", data)
    }

    @Test
    fun `input without header throws error`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/no-header.json").readText()
        assertThrows<RuntimeException> { PDFGenService().getPDFAsByteArray(jsonData) }
    }

    @Test
    fun `validate pdf with incomplete placeholder throws exception`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/incomplete-placeholder-example.json").readText()
        assertThrows<EmptyPlaceholderException> { PDFGenService().validateDocumentContent(jsonData) }
    }

    @Test
    fun `validate pdf with complete placeholders passes`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/complete-placeholder-example.json").readText()
        PDFGenService().validateDocumentContent(jsonData)
    }

    @Test
    fun `validate pdf with complete regelverk passes`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/complete-with-regelverk.json").readText()
        PDFGenService().validateDocumentContent(jsonData)
    }

    @Test
    fun `validate pdf with incomplete regelverk throws exception`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/empty-regelverk-container.json").readText()
        assertThrows<EmptyRegelverkException> { PDFGenService().validateDocumentContent(jsonData) }
    }

    @Test
    fun `validate pdf with text somewhere in regelverk passes`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/not-all-empty-text-regelverk.json").readText()
        PDFGenService().validateDocumentContent(jsonData)
    }

    @Test
    fun `validate pdf with only empty texts in regelverk throws exception`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/all-empty-text-regelverk.json").readText()
        assertThrows<EmptyRegelverkException> { PDFGenService().validateDocumentContent(jsonData) }
    }

    @Test
    fun `validate pdf passes`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/minimal.json").readText()
        PDFGenService().validateDocumentContent(jsonData)
    }

    @Test
    fun `generate pdf with current date on later page`() {
        val jsonData = File("$TEST_JSON_TEST_DATA_PATH/tilsvarsbrev-med-oversendelsesbrev.json").readText()
        val data = PDFGenService(currentDate = TEST_DATE).getPDFAsByteArray(jsonData)
        comparePdf("tilsvarsbrev-med-oversendelsesbrev", data)
    }
}