package no.nav.klage.pdfgen.transformers

import org.intellij.lang.annotations.Language

const val DIGIT_WIDTH_PX = 20.0 / 3.0 // px width at 10pt font size

/**
 * Takes a string of digits and returns the width in pixels.
 */
private fun calculateNumberWidth(number: String): Double {
    return number.length * DIGIT_WIDTH_PX
}

const val PX_TO_PT_RATIO = 0.75
const val TEXT_WIDTH_PX = 18.2833 // Width of " av " at 10pt in px
const val TEXT_WIDTH_PT = TEXT_WIDTH_PX * PX_TO_PT_RATIO // Convert px to pt
const val PADDING = 12.0 // Padding in pt
const val SPACING = 10.0 // Extra spacing in pt

@Language("css")
fun getVedleggsoversiktCss(totalCount: Int): String {
    // Calculate width needed for the last counter text like "999 av 999"
    val lastCounterWidth = calculateNumberWidth(totalCount.toString(10)) * PX_TO_PT_RATIO * 2 + TEXT_WIDTH_PT + PADDING + SPACING
    val marginLeft = lastCounterWidth + 2

    return """
    html {
        font-family: "Source Sans Pro" !important;
        box-sizing: border-box;
    }

    *, ::before, ::after {
      box-sizing: inherit;
      padding: 0;
      margin: 0;
    }

    @page { size: A4; }

    .overview-date {
        position: absolute;
        top: 0;
        right: 0;
        font-size: 10pt;
        font-weight: 600;
    }

    h1 {
        font-weight: 600;
        font-size: 16pt;
        text-align: left;
        margin-top: 16pt;
    }

    .document-list {
        margin-top: 16pt;
        list-style: none;
        font-size: 12pt;
    }

    .document-item {
        padding-left: 8pt;
        margin-left: ${marginLeft}pt;
        margin-top: 12pt;
        page-break-inside: avoid;
        position: relative;
        border-left: 2pt solid #262626;
    }

    .document-item:first-child {
        margin-top: 0;
    }

    .document-item::before {
        content: attr(data-count);
        background-color: #262626;
        color: #ffffff;
        font-weight: 600;
        font-size: 10pt;
        padding: 2pt 6pt;
        border-radius: 4pt;
        position: absolute;
        left: -${marginLeft}pt;
        white-space: nowrap;
        top: 0;
    }

    .document-title {
        font-size: inherit;
        font-weight: 600;
        margin: 0;
    }


    .journalpost-metadata-list {
        list-style: none;
        padding: 0;
        margin: 0;
        margin-top: 8pt;
        font-size: 10pt;
    }

    .journalpost-metadata-item {
        position: relative;
        margin-top: 12pt;
        padding-left: 78pt;
    }

    .journalpost-metadata-item:first-child {
        margin-top: 0;
    }

    .journalpost-metadata-item::before {
        content: attr(data-date);
        color: #262626;
        border: 2pt solid #595959;
        font-weight: 600;
        font-size: 10pt;
        padding: 2pt 6pt;
        border-radius: 4pt;
        position: absolute;
        left: 0;
    }

    .metadata-table {
        border-collapse: collapse;
        border-spacing: 0;
        padding: 0;
        margin: 0;
        line-height: 1;
    }

    .metadata-table td {
        padding-top: 2pt;
        padding-bottom: 2pt;
        padding-left: 0;
        padding-right: 0;
        vertical-align: top;
        line-height: 1;
    }

    .metadata-table tr:first-child td {
        padding-top: 0;
    }

    .metadata-table td:first-child {
        font-weight: 600;
        padding-right: 8pt;
    }
    """.trimIndent()
}
