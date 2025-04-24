package no.nav.klage.pdfgen.transformers

import org.intellij.lang.annotations.Language

@Language("css")
fun getCss(footer: String = "") = """
    html {
        font-family: "Source Sans Pro" !important;
        box-sizing: border-box;
        font-weight: 400;
        letter-spacing: 0;
        white-space: pre-wrap;
    }
    *, ::before, ::after {
      box-sizing: inherit;
      word-wrap: break-word;
      padding: 0;
      margin: 0;
    }
    .column {
      font-size: 12pt;
      display: inline-block;
      width: 50%;
    }
    /* Fjerner spacing mellom inline-blockene */
    .wrapper {
      font-size: 0;
      page-break-before: avoid;
    }
    h1 {
        font-size: 16pt;
    }
    .svarbrev h1 {
        font-size: 14pt;
    }
    h2 {
        font-size: 14pt;
    }
    .svarbrev h2 {
        font-size: 12pt;
    }
    h3 {
        font-size: 12pt;
    }
    h1, h2, h3, h4, h5, h6 {
        font-weight: 600;
        margin-top: 1em;
        margin-bottom: 0;
        page-break-after: avoid;
    }
    #header_text {
        float: left;
        width: 60%;        
    }
    header {
        margin-bottom: 6pt;
    }
    /* Clearfix */
    header:after{
        clear: both;
        content: "";
        display: block;
    }
    #logo {
        width: 30%;
        float: right;
    }
    .current-date {
        white-space: nowrap;
        text-align: right;
        margin-bottom: 24pt;
    }
    #logo img {
        display: block;
        height: 60pt;
        float: right;
    }
    p {
        font-size: 12pt;
        margin-top: 1em;
    }
    .placeholder-text {
        background-color: #EFA89D;
        border-radius: 3pt;
    }
    b, .bold {
        font-weight: 600;
    }
    .underline {
        text-decoration: underline;
    }
    .italic {
        font-style: italic;
    }
    .alignRight {
        text-align: right;
    }
    .alignLeft {
        text-align: left;
    }
    
    .alignCenter {
        text-align: center;
    }
    
    .pageBreak {
        page-break-after: always;
    }
    table {
        border-spacing: 0;
        border-collapse: collapse;
        max-width: 100%;
        margin-top: 12pt;
        margin-bottom: 12pt;
        page-break-inside: avoid;
        -fs-border-rendering: no-bevel;
    }
    td {
        border: 1pt solid #8F8F8F;
        word-wrap: break-word;
        vertical-align: top;
        text-align: left;
        background-color: transparent;
        padding: 4pt;
        padding-left: 3pt;
        padding-right: 3pt;
        min-width: 36pt;
    }
    tr:nth-child(odd) {
      background-color: rgb(247, 247, 247);
    }
    tr:nth-child(even) {
      background-color: #fff;
    }
    td > ul, td > ol {
      margin-top: 0;
    }
    td > * {
      margin-top: 0;
      margin-bottom: 6pt;
    }
    
    td > *:last-child {
      margin-bottom: 0;
    }
    
    td > span {
      margin-bottom: 0;
    }
    
    ol, ul {
      padding-left: 12pt;
      margin: 0;
      margin-top: 12pt;
    }
    
    li > ul, li > ol {
      margin-top: 0;
    }

    .signature {
        margin-top: 24pt;
    }
    
    @page {
        margin: 20mm;
        margin-top: 15mm;
        size: a4;
        padding: 0;
        @bottom-left {
            content: "";
        }
        @bottom-right {
            font-family: "Source Sans Pro" !important;
            font-size: 10pt;
            content: "Side " counter(page) " av " counter(pages);
        }
    }
    
    @page :first {
        margin-bottom: 30mm;
        @bottom-left {
            font-family: "Source Sans Pro" !important;
            font-size: 10pt;
            content: "$footer";
            white-space: pre-wrap;
        }
        @bottom-right {
            content: "";
        }
    }
    """.trimIndent()