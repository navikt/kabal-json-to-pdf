package no.nav.klage.pdfgen

import no.nav.klage.pdfgen.api.view.DocumentValidationResponse.DocumentValidationError
import no.nav.klage.pdfgen.service.PDFGenService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import java.io.File

/**
 * Test cases are driven entirely by the JSON files found under
 * `src/test/resources/json-test-data`. Every file is both:
 *
 * 1. validated with `validateDocumentContent`, and
 * 2. rendered with `getPDFAsByteArray` and compared against its expected snapshot under
 *    `expected-pdf/`, which mirrors this folder structure.
 *
 * Files are split into folders based on the validation outcome:
 *
 * - `validation-error/<ERROR_CODE>/` -> `validateDocumentContent` must report `<ERROR_CODE>`.
 * - `validation-success/`            -> `validateDocumentContent` must report no errors.
 *
 * To add a new test case, just drop a JSON file into the relevant folder, with a matching
 * `expected-pdf/<same folder>/<name>.pdf` - no new test method needed.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GeneratePDFTest {
    private val validationErrorDir = File("$TEST_JSON_TEST_DATA_PATH/validation-error")
    private val validationSuccessDir = File("$TEST_JSON_TEST_DATA_PATH/validation-success")

    private val outputSubfolder = javaClass.simpleName

    @BeforeAll
    fun emptyFileDiffFolder() {
        cleanOutputFolder(outputSubfolder)
    }

    @TestFactory
    fun `json files that fail validation`(): List<DynamicTest> =
        requireDirectory(validationErrorDir)
            .listFiles { it.isDirectory }!!
            .sortedBy { it.name }
            .flatMap { errorCodeDir ->
                val expectedErrorCode = errorCodeDir.name
                jsonFilesIn(errorCodeDir).map { file ->
                    dynamicTest(file.nameWithoutExtension) {
                        val jsonData = file.readText()

                        val errors = PDFGenService().validateDocumentContent(jsonData)
                        assertTrue(
                            errors.contains(DocumentValidationError.valueOf(expectedErrorCode)),
                            "Expected validation errors to contain '$expectedErrorCode', but got $errors",
                        )

                        val data = PDFGenService().getPDFAsByteArray(json = jsonData, currentDate = TEST_DATE)
                        comparePdf(
                            resource = "validation-error/${errorCodeDir.name}/${file.nameWithoutExtension}",
                            actualPdfBytes = data,
                            outputSubfolder = outputSubfolder,
                        )
                    }
                }
            }

    @Test
    fun `document with both an empty placeholder and an empty regelverk reports both errors`() {
        val jsonData =
            File("$TEST_JSON_TEST_DATA_PATH/validation-error-multiple/empty-placeholder-and-empty-regelverk.json")
                .readText()

        val errors = PDFGenService().validateDocumentContent(jsonData)

        assertEquals(
            setOf(DocumentValidationError.EMPTY_PLACEHOLDER, DocumentValidationError.EMPTY_REGELVERK),
            errors,
            "Expected both EMPTY_PLACEHOLDER and EMPTY_REGELVERK to be reported",
        )
    }

    @TestFactory
    fun `json files that pass validation`(): List<DynamicTest> =
        jsonFilesIn(requireDirectory(validationSuccessDir)).map { file ->
            dynamicTest(file.nameWithoutExtension) {
                val jsonData = file.readText()

                val errors = PDFGenService().validateDocumentContent(jsonData)
                assertTrue(
                    errors.isEmpty(),
                    "Expected no validation errors, but got $errors",
                )

                val data = PDFGenService().getPDFAsByteArray(json = jsonData, currentDate = TEST_DATE)
                comparePdf(
                    resource = "validation-success/${file.nameWithoutExtension}",
                    actualPdfBytes = data,
                    outputSubfolder = outputSubfolder,
                )
            }
        }

    /**
     * Fails loudly if [dir] doesn't exist or isn't a directory, instead of letting
     * `File.listFiles()` silently return null and the test factory silently produce 0 tests.
     */
    private fun requireDirectory(dir: File): File {
        check(dir.isDirectory) {
            "Expected test data directory does not exist: '${dir.path}'. " +
                "If it was renamed or moved, update ${this::class.simpleName} accordingly."
        }
        return dir
    }

    private fun jsonFilesIn(dir: File): List<File> =
        (dir.listFiles { f -> f.isFile && f.extension == "json" } ?: emptyArray())
            .sortedBy { it.name }
}
