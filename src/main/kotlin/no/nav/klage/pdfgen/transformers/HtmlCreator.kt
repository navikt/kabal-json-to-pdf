package no.nav.klage.pdfgen.transformers

import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import no.nav.klage.pdfgen.exception.EmptyPlaceholderException
import no.nav.klage.pdfgen.exception.EmptyRegelverkException
import no.nav.klage.pdfgen.transformers.ElementType.*
import no.nav.klage.pdfgen.transformers.ElementType.FOOTER
import no.nav.klage.pdfgen.transformers.ElementType.HEADER
import no.nav.klage.pdfgen.util.getLogger
import no.nav.klage.pdfgen.util.getSecureLogger
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

@Suppress("UNCHECKED_CAST")
class HtmlCreator(val dataList: List<Map<String, *>>, val validationMode: Boolean = false) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    private fun getCss(footer: String) =
        """
                                html {
                                    white-space: pre-wrap;
                                    font-family: "Source Sans Pro" !important;
                                    box-sizing: border-box;
                                }
                                *, ::before, ::after {
                                  box-sizing: inherit;
                                }
                                .column {
                                  font-size: 16px;
                                  display: inline-block;
                                  width: 50%;
                                }
                                /* Fjerner spacing mellom inline-blockene */
                                .wrapper {
                                  font-size: 0;
                                }
                                h1 {
                                    font-size: 16pt;
                                }
                                h2 {
                                    font-size: 14pt;
                                }
                                h3 {
                                    font-size: 12pt;
                                }
                                .indent {
                                    padding-left: 24pt;
                                }
                                #header span {
                                    font-size: 10pt;
                                    white-space: pre;
                                }
                                img {
                                    display: block;
                                    width: 100pt;
                                    float: right;
                                },
                                p, span {
                                    font-size: 12pt;
                                }
                                .placeholder-text {
                                    background-color: #EFA89D;
                                    border-radius: 3pt;
                                }
                                .bold {
                                    font-weight: bold;
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
                                .pageBreak {
                                    page-break-after: always;
                                }
                                table {
                                    border-spacing: 0;
                                    border-collapse: collapse;
                                    max-width: 100%;
                                    margin-top: 12pt;
                                    margin-bottom: 12pt;
                                }
                                tr {
                                    min-height: 24pt;
                                }
                                td {
                                    border: 1pt solid rgb(143, 143, 143);
                                    min-width: 36pt;
                                    word-break: break-word;
                                    white-space: pre-wrap;
                                    vertical-align: top;
                                    text-align: left;
                                    background-color: transparent;
                                    padding: 4pt;
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
                                
                                @page {
                                    margin: 15mm 20mm 20mm 20mm;
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
                                    margin: 15mm 20mm 30mm 20mm;
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


    private val document: Document = createHTMLDocument()
        .html {
            body {
                div {
                    id = "header"
                    span {
                        id = "header_text"
                        +"Returadresse:\nNAV Klageinstans"
                    }
                    img { src = "nav_logo.png" }
                }
                br { }
                br { }
                br { }
                br { }

                div { id = "div_content_id" }
            }
        }

    private var footer =
        "NAV Klageinstans\\Anav.no"

    private fun addLabelContentElement(map: Map<String, *>) {
        val result = map["result"] ?: return

        val divElement = document.getElementById("div_content_id") as Node
        divElement.append {
            div {
                p { +"$result" }
            }
        }
    }

    private fun addMaltekst(map: Map<String, *>) {
        addElements(map)
    }

    private fun addRegelverkContainer(map: Map<String, *>) {
        if (validationMode) {
            if (map["children"] == null || (map["children"] as List<Map<String, *>>).isEmpty()) {
                throw EmptyRegelverkException("Empty regelverk")
            } else if (getTexts(map).isEmpty()) {
                throw EmptyRegelverkException("Empty regelverk")
            }
        }

        addElements(map)
    }

    private fun getTexts(map: Map<String, *>): List<String> {
        val texts = mutableListOf<String>()

        if (map["text"] != null && (map["text"] as String).isNotBlank()) {
            texts += (map["text"] as String)
        }

        return if (map["children"] == null || (map["children"] as List<Map<String, *>>).isEmpty()) {
            texts
        } else {
            val children = (map["children"] as List<Map<String, *>>)
            children.forEach {
                texts.addAll(getTexts(it))
            }
            texts
        }
    }

    private fun addElements(map: Map<String, *>) {
        val elementList = map["children"]
        if (elementList != null) {
            elementList as List<Map<String, *>>
            elementList.forEach {
                val div = document.create.div {
                    this.addElementWithPossiblyChildren(map = it)
                }
                val divElement = document.getElementById("div_content_id") as Node
                divElement.appendChild(div)
            }
        } else {
            logger.error("No children element.")
            return
        }
    }

    private fun addRegelverk(map: Map<String, *>) {
        val elementList = map["children"]
        if (elementList != null) {
            elementList as List<Map<String, *>>
            elementList.forEach {
                processElement(it)
            }
        } else {
            logger.error("No children element.")
            return
        }
    }


//TODO: Finn ut behovet for validering.
    private fun addRedigerbarMaltekst(map: Map<String, *>) {
        val elementList = map["children"]
        if (elementList != null) {
            elementList as List<Map<String, *>>
            elementList.forEach {
                processElement(it)
            }
        } else {
            logger.error("No children element.")
            return
        }
    }

    private fun addElementWithPossiblyChildren(map: Map<String, *>) {
        val div = document.create.div {
            this.addElementWithPossiblyChildren(map = map)
        }
        val divElement = document.getElementById("div_content_id") as Node
        divElement.appendChild(div)
    }

    private fun Tag.addElementWithPossiblyChildren(map: Map<String, *>) {
        val elementType = map["type"]
        var children = emptyList<Map<String, *>>()

        val applyClasses =
            if ((map["textAlign"] == "text-align-right") || (map["align"] == "right")) mutableSetOf("alignRight")
            else mutableSetOf()
        if (elementType == "indent") {
            applyClasses += "indent"
        }

        val inlineStyles = mutableSetOf<String>()

        if (map.containsKey("indent")) {
            val indent = map["indent"] as Int
            if (elementType in listOf("paragraph", "p")) {
                inlineStyles += "padding-left: ${24 * indent}pt"
            } else if (elementType in listOf("bullet-list", "numbered-list", "ul", "ol")) {
                inlineStyles += "padding-left: ${(24 * indent) + 12}pt"
            }
        }

        if (elementType == "placeholder") {
            if (placeholderTextMissingInChildren(map)) {
                if (validationMode) {
                    throw EmptyPlaceholderException("Placeholder error")
                } else {
                    val text = map["placeholder"]
                    addLeafElement(mapOf("text" to text), mutableSetOf("placeholder-text"))
                }
                return
            }
        }

        if (elementType != "page-break") {
            children = map["children"] as List<Map<String, *>>
        } else {
            applyClasses.add("pageBreak")
        }

        val element = when (elementType) {
            "standard-text", "placeholder" -> SPAN(initialAttributes = emptyMap(), consumer = this.consumer)
            "heading-one", "h1" -> H1(initialAttributes = emptyMap(), consumer = this.consumer)
            "heading-two", "h2" -> H2(initialAttributes = emptyMap(), consumer = this.consumer)
            "heading-three", "h3" -> H3(initialAttributes = emptyMap(), consumer = this.consumer)
            "blockquote" -> BLOCKQUOTE(initialAttributes = emptyMap(), consumer = this.consumer)
            "paragraph", "p" -> P(initialAttributes = emptyMap(), consumer = this.consumer)
            "bullet-list", "ul" -> UL(initialAttributes = emptyMap(), consumer = this.consumer)
            "numbered-list", "ol" -> OL(initialAttributes = emptyMap(), consumer = this.consumer)
            "list-item", "li" -> LI(initialAttributes = emptyMap(), consumer = this.consumer)
            "table" -> TABLE(initialAttributes = emptyMap(), consumer = this.consumer)
            "tr" -> {
                if (map.containsKey("size")) {
                    val heightInPx = map["size"] as Int
                    inlineStyles += "height: ${(heightInPx * pxToPtRatio).roundToInt()}pt;"
                }
                TR(initialAttributes = emptyMap(), consumer = this.consumer)
            }
            "td" -> {
                if (map.containsKey("colSpan")) {
                    TD(initialAttributes = mapOf("colspan" to map["colSpan"].toString()), consumer = this.consumer)
                } else {
                    TD(initialAttributes = emptyMap(), consumer = this.consumer)
                }
            }
            "page-break", "list-item-container", "indent", "lic" -> DIV(
                initialAttributes = emptyMap(),
                consumer = this.consumer
            )
            "empty-void" -> return //ignore
            else -> {
                logger.warn("unknown element type: $elementType")
                return
            }
        }

        element.visit {
            classes = applyClasses
            style = inlineStyles.joinToString(";")

            //special handling for tables
            if (this is TABLE) {
                if (map.containsKey("colSizes")) {
                    val colSizesInPx = map["colSizes"] as List<Int>
                    colGroup {
                        style = "width: 100%;"
                        colSizesInPx.forEach { colSizeInPx ->
                            col {
                                style = "width: ${(colSizeInPx * pxToPtRatio).roundToInt()}pt;"
                            }
                        }
                    }
                }

                //wrap in tbody
                tbody {
                    loopOverChildren(children)
                }
            } else {
                loopOverChildren(children)
            }
        }
    }

    private fun HTMLTag.loopOverChildren(
        children: List<Map<String, *>>,
    ) {
        children.forEach {
            when (it.getType()) {
                LEAF -> this.addLeafElement(it)
                ELEMENT -> this.addElementWithPossiblyChildren(map = it)
                else -> {}
            }
        }
    }

    private fun addDocumentList(map: Map<String, *>) {
        val children = map["documents"] as List<Map<String, String>>
        val dElement = document.create.div {
            ul {
                children.forEach {
                    li { +it["title"].toString() }
                }
            }
        }
        val divElement = document.getElementById("div_content_id") as Node
        divElement.appendChild(dElement)
    }

    private fun addSignatureElement(map: Map<String, *>) {
        val dElement = document.create.div {
            classes = setOf("wrapper")
            if (map.containsKey("medunderskriver")) {
                val medunderskriver = map["medunderskriver"] as Map<String, Map<String, *>>
                div {
                    classes = setOf("column")
                    div { +medunderskriver["name"].toString() }
                    div { +medunderskriver["title"].toString() }
                }
            }
            if (map.containsKey("saksbehandler")) {
                val saksbehandler = map["saksbehandler"] as Map<String, Map<String, *>>
                div {
                    classes = setOf("column")
                    div { +saksbehandler["name"].toString() }
                    div { +saksbehandler["title"].toString() }
                }
            }
        }
        val divElement = document.getElementById("div_content_id") as Node
        divElement.appendChild(dElement)
    }

    private fun Tag.addLeafElement(map: Map<String, *>, inputClasses: MutableSet<String> = mutableSetOf()) {
        val text = map["text"] ?: throw RuntimeException("no content here")

        if (map["bold"] == true) {
            inputClasses += "bold"
        }
        if (map["underline"] == true) {
            inputClasses += "underline"
        }
        if (map["italic"] == true) {
            inputClasses += "italic"
        }

        this.consumer.span {
            classes = inputClasses
            +text.toString()
        }
    }

    private fun addCurrentDate() {
        val formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.forLanguageTag("no"))
        val dateAsText = ZonedDateTime.now(ZoneId.of("Europe/Oslo")).format(formatter)

        val div = document.create.div {
            classes = setOf("alignRight")
            +"Dato: $dateAsText"
        }
        val divElement = document.getElementById("div_content_id") as Node
        divElement.appendChild(div)
    }

    fun getDoc(): Document {
        dataList.forEach {
            processElement(it)
        }

        //defaults for now
        if (!headerAndFooterExists(dataList)) {
            val span = document.getElementById("header_text")
            span.textContent = "Returadresse,\nNAV Klageinstans Midt-Norge, Postboks 2914 Torgarden, 7438 Trondheim"

            footer =
                "Postadresse: NAV Klageinstans Midt-Norge // Postboks 2914 Torgarden // 7438 Trondheim\\ATelefon: 21 07 17 30\\Anav.no"
        }

        //add css when we have a footer set
        val head = document.create.head {
            style {
                unsafe {
                    raw(
                        getCss(footer)
                    )
                }
            }
        }

        document.childNodes.item(0).appendChild(head)

        println(document.serialize())
        secureLogger.debug(document.serialize())
        return document
    }

    private fun headerAndFooterExists(list: List<Map<String, *>>) =
        list.any { it["type"] == "header" } && list.any { it["type"] == "footer" }

    private fun processElement(map: Map<String, *>) {
        when (map.getType()) {
            REGELVERK -> addRegelverk(map)
            REGELVERK_CONTAINER -> addRegelverkContainer(map)
            REDIGERBAR_MALTEKST -> addRedigerbarMaltekst(map)
            LABEL_CONTENT_ELEMENT -> addLabelContentElement(map)
            SIGNATURE_ELEMENT -> addSignatureElement(map)
            ELEMENT, INDENT -> addElementWithPossiblyChildren(map)
            DOCUMENT_LIST -> addDocumentList(map)
            MALTEKST -> addMaltekst(map)
            CURRENT_DATE -> addCurrentDate()
            HEADER -> addHeader(map)
            FOOTER -> setFooter(map)
            LEAF -> {}
            IGNORED -> {}
        }
    }

    private fun placeholderTextMissingInChildren(map: Map<String, *>): Boolean {
        val children = map["children"] as List<Map<String, *>>
        return children.any { it["text"] == null || it["text"].toString().trim('​').trim().isEmpty() }
    }

    private fun addHeader(map: Map<String, *>) {
        val span = document.getElementById("header_text")
        span.textContent = map["content"]?.toString() ?: " "
    }

    private fun setFooter(map: Map<String, *>) {
        footer = map["content"]?.toString()?.replace("\n", "\\A") ?: ""
    }

    private fun Map<String, *>.getType(): ElementType {
        val type = this["type"]
        if (type != null) {
            return when (type) {
                "label-content" -> LABEL_CONTENT_ELEMENT
                "signature" -> SIGNATURE_ELEMENT
                "document-list" -> DOCUMENT_LIST
                "maltekst" -> MALTEKST
                "current-date" -> CURRENT_DATE
                "header" -> HEADER
                "footer" -> FOOTER
                "redigerbar-maltekst" -> REDIGERBAR_MALTEKST
                "regelverkstekst" -> IGNORED
                "regelverk" -> REGELVERK
                "regelverk-container" -> REGELVERK_CONTAINER
                else -> ELEMENT
            }
        }
        return LEAF
    }
}

enum class ElementType {
    LABEL_CONTENT_ELEMENT,
    SIGNATURE_ELEMENT,
    ELEMENT,
    LEAF,
    DOCUMENT_LIST,
    MALTEKST,
    REGELVERK,
    REGELVERK_CONTAINER,
    REDIGERBAR_MALTEKST,
    CURRENT_DATE,
    HEADER,
    FOOTER,
    INDENT,
    IGNORED,
}

const val pxToPtRatio = 0.75