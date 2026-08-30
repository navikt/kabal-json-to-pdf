package no.nav.klage.pdfgen

import com.openhtmltopdf.pdfboxout.visualtester.PdfVisualTester
import com.openhtmltopdf.pdfboxout.visualtester.PdfVisualTester.PdfCompareResult
import org.junit.jupiter.api.fail
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.util.Comparator
import java.util.stream.Collectors
import javax.imageio.ImageIO

const val TEST_OUTPUT_PATH = "test-output"
const val TEST_RESOURCES_PATH = "src/test/resources"
const val TEST_JSON_TEST_DATA_PATH = "$TEST_RESOURCES_PATH/json-test-data"

val TEST_DATE: LocalDate = LocalDate.of(2025, 6, 11)

/**
 * Compares a rendered PDF against its expected snapshot.
 *
 * [resource] may be a plain name (e.g. "tables") or a relative path with subfolders
 * (e.g. "validation-success/tables" or "validation-error/EmptyRegelverkException/full-klagevedtak").
 * The path segments are mirrored under `generated-pdfs/` (the actual PDF, always written),
 * `expected-pdf/` (the golden snapshot), and `test-output/<outputSubfolder>/` (failure artefacts),
 * so the expected file for "validation-success/tables" is looked up at
 * "expected-pdf/validation-success/tables.pdf", and its failure artefacts (if any) are written
 * under "test-output/<outputSubfolder>/validation-success/tables/".
 */
fun comparePdf(
    resource: String,
    actualPdfBytes: ByteArray,
    outputSubfolder: String,
) {
    // NIO's Path parser treats "/" as a valid separator on every platform (including Windows),
    // so [resource] can be resolved directly against a base path without manually splitting it
    // into a directory and a file name first.
    val outputFolder = Path.of(TEST_OUTPUT_PATH, outputSubfolder, resource)

    // Always write actual PDF
    val generatedPdfPath = Path.of("generated-pdfs", "$resource.pdf")
    Files.createDirectories(generatedPdfPath.parent)
    Files.write(generatedPdfPath, actualPdfBytes)

    val pathToFile = Path.of(TEST_RESOURCES_PATH, "expected-pdf", "$resource.pdf")

    if (!Files.exists(pathToFile)) {
        fail(
            "No expected PDF found for resource '$resource'.\n" +
                "Expected file:  $pathToFile\n" +
                "To create it, review the generated PDF and, if it looks correct, copy it into place:\n" +
                "  cp $generatedPdfPath $pathToFile",
        )
    }

    val expectedPdfBytes = Files.readAllBytes(pathToFile)

    val problems =
        PdfVisualTester.comparePdfDocuments(
            expectedPdfBytes,
            actualPdfBytes,
            resource,
            false,
        )

    if (!problems.isEmpty()) {
        System.err.println("Found problems with test case ($resource):")
        System.err.println(
            problems
                .stream()
                .map { p: PdfCompareResult? -> p!!.logMessage }
                .collect(Collectors.joining("\n    ", "[\n    ", "\n]")),
        )

        System.err.println("For test case ($resource) writing failure artefacts to '$outputFolder'")
        Files.createDirectories(outputFolder)
        Files.write(outputFolder.resolve("actual.pdf"), actualPdfBytes)
        Files.write(outputFolder.resolve("expected.pdf"), expectedPdfBytes)
    }

    for (result in problems) {
        if (result.testImages != null) {
            ImageIO.write(
                result.testImages.createDiff(),
                "png",
                outputFolder.resolve("${result.pageNumber}-diff.png").toFile(),
            )
            ImageIO.write(
                result.testImages.actual,
                "png",
                outputFolder.resolve("${result.pageNumber}-actual.png").toFile(),
            )
            ImageIO.write(
                result.testImages.expected,
                "png",
                outputFolder.resolve("${result.pageNumber}-expected.png").toFile(),
            )
        }
    }
    if (problems.isNotEmpty()) {
        fail("Test failed for resource: $resource. See output in '$outputFolder' for more details.")
    }
}

fun cleanOutputFolder(outputSubfolder: String) {
    val folder = Path.of(TEST_OUTPUT_PATH, outputSubfolder)
    if (Files.exists(folder)) {
        // Failure artefacts now live in per-resource subfolders (rather than as flat,
        // uniquely-named files), so deleting only files would leave behind an ever-growing pile
        // of now-empty directories across test runs. Walk deepest-first (including OS-generated
        // dotfiles such as .DS_Store, which would otherwise make a directory look non-empty and
        // block its removal) so each directory is already empty - and therefore deletable - by
        // the time we reach it.
        Files.walk(folder).use { stream ->
            stream
                .sorted(Comparator.reverseOrder())
                .filter { it != folder }
                .forEach { path ->
                    if (Files.isDirectory(path)) {
                        if (Files.list(path).use { it.findAny().isEmpty }) {
                            Files.delete(path)
                        }
                    } else {
                        Files.delete(path)
                    }
                }
        }
    }
}
