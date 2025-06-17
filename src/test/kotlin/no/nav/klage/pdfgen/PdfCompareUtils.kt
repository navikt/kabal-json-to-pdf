package no.nav.klage.pdfgen

import com.openhtmltopdf.pdfboxout.visualtester.PdfVisualTester
import com.openhtmltopdf.pdfboxout.visualtester.PdfVisualTester.PdfCompareResult
import org.junit.jupiter.api.fail
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.util.stream.Collectors
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.name

const val TEST_OUTPUT_PATH = "test-output"
const val TEST_RESOURCES_PATH = "src/test/resources"
const val TEST_JSON_TEST_DATA_PATH = "$TEST_RESOURCES_PATH/json-test-data"

val TEST_DATE: LocalDate = LocalDate.of(2025, 6, 11)

fun comparePdf(resource: String, actualPdfBytes: ByteArray) {
    //always write actual PDF
    Files.write(Path.of("generated-pdfs/$resource.pdf"), actualPdfBytes)

    val pathToFile = "$TEST_RESOURCES_PATH/expected-pdf/test_$resource.pdf"

    val expectedPdfBytes = Files.readAllBytes(Path(pathToFile))

    val problems = PdfVisualTester.comparePdfDocuments(
        expectedPdfBytes,
        actualPdfBytes,
        resource,
        false
    )

    if (!problems.isEmpty()) {
        System.err.println("Found problems with test case ($resource):")
        System.err.println(problems.stream().map { p: PdfCompareResult? -> p!!.logMessage }
            .collect(Collectors.joining("\n    ", "[\n    ", "\n]")))

        System.err.println("For test case ($resource) writing failure artefacts to '$TEST_OUTPUT_PATH'")
        val outPdf = File(TEST_OUTPUT_PATH, "$resource---actual.pdf")
        Files.write(outPdf.toPath(), actualPdfBytes)
    }

    for (result in problems) {
        if (result.testImages != null) {
            var output = File(TEST_OUTPUT_PATH, resource + "---" + result.pageNumber + "---diff.png")
            ImageIO.write(result.testImages.createDiff(), "png", output)

            output = File(TEST_OUTPUT_PATH, resource + "---" + result.pageNumber + "---actual.png")
            ImageIO.write(result.testImages.actual, "png", output)

            output = File(TEST_OUTPUT_PATH, resource + "---" + result.pageNumber + "---expected.png")
            ImageIO.write(result.testImages.expected, "png", output)
        }
    }
    if (problems.isNotEmpty()) {
        fail("Test failed for resource: $resource. See output in '$TEST_OUTPUT_PATH' for more details.")
    }
}

fun cleanOutputFolder() {
    val folder = Path.of(TEST_OUTPUT_PATH)
    if (Files.exists(folder)) {
        Files.walk(folder)
            .filter { Files.isRegularFile(it) && !it.name.startsWith(".") }
            .forEach(Files::delete)
    }
}