package no.nav.klage.pdfgen.transformers

import org.intellij.lang.annotations.Language

@Language("css")
fun getVedleggsoversiktCss() = """
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

    h1 {
        font-weight: 600;
        font-size: 16pt;
    }

    table {
        margin-top: 24pt;
        border-collapse: collapse;
        width: 100%;
        font-size: 12pt;
    }

    thead {
        background-color: #E0E3E6;
    }

    tr, td {
        page-break-inside: avoid;
    }

    .extra-row {
        page-break-before: avoid;
    }

    td, th {
        padding: 6pt;
        word-break: normal;
        white-space: normal;
    }
    
    td {
        padding-bottom: 0;
    }

    .odd {
        background-color: #ECEEF0;
    }

    .white-space-no-wrap {
        white-space: nowrap;
    }

    label {
        font-weight: 600;
        margin-left: 8pt;
    }

    label:first-child {
        margin-left: 0;
    }

    .extra-row {
        font-size: 10pt;
    }

    .extra-row td {
        padding-top: 6pt;
        padding-bottom: 6pt;
    }

    .bold {
        font-weight: 600;
    }

    .combined-row-item {
        margin-right: 6pt;
    }

    .combined-row-item:last-child {
        margin-right: 0;
    }
    """.trimIndent() 