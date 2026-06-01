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
        font-size: 11px;
    }
    *, ::before, ::after {
      box-sizing: inherit;
      word-wrap: break-word;
      padding: 0;
      margin: 0;
      color: black;
    }
    .signature-column {
      font-size: 11px;
      display: inline-block;
      width: 50%;
    }
    .signature {
      margin-top: 32px;
      margin-bottom: 40px;
      page-break-before: avoid;
    }
    
    h1 {
       font-size: 16px;
       letter-spacing: 0.3px;
       line-height: 20px;
       margin-top: 48px;
       margin-bottom: 26px;
    }
    h2 {
        font-size: 13px;
        letter-spacing: 0.25px;
    }
    h3 {
        font-size: 12px;
        letter-spacing: 0.2px;
    }
    h4 {
        font-size: 11px;
        letter-spacing: 0.1px;
    }
    h2, h3, h4 {
        margin-bottom: 1em;
        line-height: 16px;
        margin-top: 26px;
    }
    h1, h2, h3, h4, h5, h6 {
        font-weight: 600;
        page-break-after: avoid;
    }
    
    header {
        margin-bottom: 48px;
    }
    
    /* Clearfix */
    header:after{
        clear: both;
        content: "";
        display: block;
    }
    
    #logo img {
        height: 16px;
        width: 50px;
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
       min-height: 16px;
       position: relative;
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
       width: 150px;
    }
    
    /* Create space between saksinfo and next paragraph in legacy templates */
    .after-saksinfo {
        margin-top: 26px;
    }
    
    p {
        margin-bottom: 1em;
        line-height: 16px;
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
        border: 1px solid #c7cbd1;
        word-wrap: break-word;
        max-width: 100%;
        vertical-align: top;
        text-align: left;
        background-color: transparent;
        padding-top: 4px;
        padding-bottom: 4px;
        padding-left: 8px;
        padding-right: 8px;
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
      line-height: 16px;
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
        size: 595px 842px;
        margin: 64px;
        margin-bottom: 42px;
        padding: 0;
        padding-bottom: 74px;

        @bottom-right {
            font-family: "Source Sans Pro" !important;
            font-size: 9px;
            content: "Side " counter(page) " av " counter(pages);
            vertical-align: top;
        }
    }   
    """.trimIndent()

