package no.nav.klage.pdfgen

import no.nav.klage.pdfgen.exception.EmptyPlaceholderException
import no.nav.klage.pdfgen.exception.EmptyRegelverkException
import no.nav.klage.pdfgen.service.PDFGenService
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
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
 * Files are split into two folders based on the validation outcome:
 *
 * - `validation-error/<Exception>/` -> `validateDocumentContent` must throw `<Exception>`.
 * - `validation-success/`           -> `validateDocumentContent` must complete without throwing.
 *
 * To add a new test case, just drop a JSON file into the relevant folder, with a matching
 * `expected-pdf/<same folder>/<name>.pdf` - no new test method needed.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GeneratePDFTest {

    private val validationErrorDir = File("$TEST_JSON_TEST_DATA_PATH/validation-error")
    private val validationSuccessDir = File("$TEST_JSON_TEST_DATA_PATH/validation-success")

    // Exceptions expected under validation-error/<folder name>/*.json.
    // Add new entries here when introducing a new exception type as a subfolder.
    private val exceptionsByName = mapOf(
        EmptyPlaceholderException::class.simpleName to EmptyPlaceholderException::class.java,
        EmptyRegelverkException::class.simpleName to EmptyRegelverkException::class.java,
    )

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
            .flatMap { exceptionDir ->
                val exceptionClass = exceptionsByName[exceptionDir.name]
                    ?: error(
                        "No exception class registered for folder '${exceptionDir.name}'. " +
                            "Add it to `exceptionsByName` in ${this::class.simpleName}."
                    )
                jsonFilesIn(exceptionDir).map { file ->
                    dynamicTest(file.nameWithoutExtension) {
                        val jsonData = file.readText()

                        assertThrows(exceptionClass) { PDFGenService().validateDocumentContent(jsonData) }

                        val data = PDFGenService().getPDFAsByteArray(json = jsonData, currentDate = TEST_DATE)
                        comparePdf("validation-error/${exceptionDir.name}/${file.nameWithoutExtension}", data, outputSubfolder)
                    }
                }
            }

    @TestFactory
    fun `json files that pass validation`(): List<DynamicTest> =
        jsonFilesIn(requireDirectory(validationSuccessDir)).map { file ->
            dynamicTest(file.nameWithoutExtension) {
                val jsonData = file.readText()

                PDFGenService().validateDocumentContent(jsonData)

                val data = PDFGenService().getPDFAsByteArray(json = jsonData, currentDate = TEST_DATE)
                comparePdf("validation-success/${file.nameWithoutExtension}", data, outputSubfolder)
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
