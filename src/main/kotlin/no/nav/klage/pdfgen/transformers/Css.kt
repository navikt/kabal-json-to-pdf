package no.nav.klage.pdfgen.transformers

import org.intellij.lang.annotations.Language
@Language("css")
fun getCss() = """
    html {
        font-family: "Source Sans Pro" !important;
        box-sizing: border-box;
        font-weight: 400;
        letter-spacing: 0;
        white-space: pre-wrap;
        font-size: 11pt;
    }
    *, ::before, ::after {
      box-sizing: inherit;
      word-wrap: break-word;
      padding: 0;
      margin: 0;
      color: black;
    }
    /* The last top-level element in the document (whatever it is - a paragraph, a
       heading, the signature, etc.) must not carry a trailing margin-bottom. Any
       such margin still counts toward the page's content height, so if the content
       lands close enough to the page boundary, that trailing margin alone can spill
       onto an otherwise completely empty extra page. #body > *:last-child has higher
       specificity than the plain element/class selectors below, so this wins
       regardless of source order. */
    #body > *:last-child {
      margin-bottom: 0;
    }
    .signature-column {
      font-size: 11pt;
      display: inline-block;
      width: 49%;
    }
    .signature {
      margin-top: 32pt;
      margin-bottom: 40pt;
      page-break-before: avoid;
    }

    h1 {
       font-size: 16pt;
       letter-spacing: 0.3pt;
       line-height: 20pt;
       margin-top: 48pt;
       margin-bottom: 26pt;
    }
    h2 {
        font-size: 13pt;
        letter-spacing: 0.25pt;
    }
    h3 {
        font-size: 12pt;
        letter-spacing: 0.2pt;
    }
    h4 {
        font-size: 11pt;
        letter-spacing: 0.1pt;
    }
    h2, h3, h4 {
        margin-bottom: 6pt;
        line-height: 16pt;
        margin-top: 26pt;
    }
    h1, h2, h3, h4, h5, h6 {
        font-weight: 600;
        page-break-after: avoid;
    }

    header {
        margin-bottom: 48pt;
    }

    /* Clearfix */
    header:after{
        clear: both;
        content: "";
        display: block;
    }

    #logo img {
        height: 16pt;
        width: 50pt;
    }

   .current-date {
        white-space: nowrap;
        text-align: right;
        position: absolute;
        bottom: 0;
        right: 0;
    }

    .saksinfo {
       /* For legacy cases where saksinfo has no other children than the absolutely positioned current-date */
       min-height: 16pt;
       position: relative;
       margin-bottom: 48pt;
    }

    /* Override bold items in legacy saksinfo - text should never be bold in saksinfo */
    .label-content .bold {
       font-weight: normal;
    }

    .label-content {
       margin: 0;
    }

    .label-content .label {
       display: inline-block;
       width: 150pt;
    }

    /* Create space between saksinfo and next paragraph in legacy templates */
    .after-saksinfo {
        margin-top: 26pt;
    }

    p {
        margin-bottom: 1em;
        line-height: 16pt;
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
        margin-bottom: 1em;
        page-break-inside: avoid;
        -fs-border-rendering: no-bevel;
    }
    td {
        border: 1pt solid #c7cbd1;
        word-wrap: break-word;
        max-width: 100%;
        vertical-align: top;
        text-align: left;
        background-color: transparent;
        padding-top: 4pt;
        padding-bottom: 4pt;
        padding-left: 8pt;
        padding-right: 8pt;
    }
    tr:nth-child(odd) {
      background-color: #f5f6f7
    }
    tr:nth-child(even) {
      background-color: #fff;
    }
    td > ul, td > ol {
      margin-top: 0;
    }
    td > * {
      margin-top: 0;
    }

    td > *:last-child {
      margin-bottom: 0;
    }

    td > span {
      margin-bottom: 0;
    }

    ol, ul {
      margin-bottom: 1em;
      margin-left: 2em;
      line-height: 16pt;
    }

    ul {
        list-style-type: disc;
    }

    ul ul {
        list-style-type: circle;
    }

    ul ul ul {
        list-style-type: square;
    }

    ul ul ul ul {
        list-style-type: disc;
    }

    ul ul ul ul ul {
        list-style-type: circle;
    }

    ul ul ul ul ul ul {
        list-style-type: square;
    }

    ul ul ul ul ul ul ul {
        list-style-type: disc;
    }

    ul ul ul ul ul ul ul ul {
        list-style-type: circle;
    }

    ul ul ul ul ul ul ul ul ul {
        list-style-type: square;
    }

    li > ul, li > ol {
      margin-left: 1em;
    }

    @page {
        size: 595pt 842pt;
        margin: 64pt;
        margin-bottom: 42pt;
        padding: 0;
        padding-bottom: 74pt;

        @bottom-right {
            font-family: "Source Sans Pro" !important;
            font-size: 9pt;
            content: "Side " counter(page) " av " counter(pages);
            vertical-align: top;
        }
    }
    """.trimIndent()
