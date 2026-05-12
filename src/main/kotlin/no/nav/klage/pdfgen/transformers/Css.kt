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
      font-size: 11px;
      color: black;
    }
    .column {
      display: inline-block;
      width: 50%;
    }
    /* Fjerner spacing mellom inline-blockene */
    .wrapper {
      font-size: 0;
      page-break-before: avoid;
    }
    
    /* Forskjøvet skalaen med 1 ift from https://aksel.nav.no/god-praksis/artikler/visuelle-retningslinjer-for-brev 
       Vår h1 = deres h2, vår h2 = deres h3, vår h3 = deres h4    
    */
    h1 {
        font-size: 13px;
        letter-spacing: 25%;
    }
    h2 {
        font-size: 12px;
        letter-spacing: 20%;
    }
    h3 {
        font-size: 11px;
        letter-spacing: 10%;
    }
    h1, h2, h3 {
        margin-bottom: 6px;
        line-height: 16px;
    }
    h1, h2, h3, h4, h5, h6 {
        font-weight: bold;
        page-break-after: avoid;
    }
    
    /* 26 px mellom brødtekst og H2, H3 eller H4 */
    p + h1, p + h2, p + h3, p + h4,
    ol + h1, ol + h2, ol + h3, ol + h4,
    ul + h1, ul + h2, ul + h3, ul + h4 {
        margin-top: 26px;
    }
    
    header {
       margin-bottom: 48px;
    }
    
    #header_text {
       float: left;
       font-size: 9px;   
       /* line-height: 16px; */
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
    
   #current-date {
        white-space: nowrap;
        text-align: right;
        font-size: 9px;
        float: right;
    }

    p {
        font-size: 11px;
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
        margin-top: 12pt;
        margin-bottom: 12pt;
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
      margin-bottom: 6pt;
    }
    
    td > *:last-child {
      margin-bottom: 0;
    }
    
    td > span {
      margin-bottom: 0;
    }
    
    ol, ul {
      padding-left: 9px;
      margin: 0;
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
      margin-top: 0;
    }

    .signature {
        margin-top: 32px;
        margin-bottom: 40px;
    }
    
    @page {
        margin: 64px;
        margin-bottom: 42px;
        width: 595px;
        height: 842px;
        
        padding: 0;
        padding-bottom: 74px;
        @bottom-left {
            content: "";
        }
        
        @bottom-right {
            font-family: "Source Sans Pro" !important;
            font-size: 9px;
            content: "Side " counter(page) " av " counter(pages);
            vertical-align: top;
        }
    }
    
    @page :first {
    
        @bottom-left {
            font-family: "Source Sans Pro" !important;
            font-size: 9px;
            content: "$footer";
            white-space: pre-wrap;
            vertical-align: top;
        }
        
        @bottom-right {
            content: "";
        }
    }
    """.trimIndent()
