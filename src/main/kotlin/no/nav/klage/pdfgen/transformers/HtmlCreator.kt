package no.nav.klage.pdfgen.transformers

import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.dom.createHTMLDocument
import no.nav.klage.pdfgen.exception.EmptyPlaceholderException
import no.nav.klage.pdfgen.exception.EmptyRegelverkException
import no.nav.klage.pdfgen.util.getFormattedDate
import no.nav.klage.pdfgen.util.getLogger
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.time.LocalDate

@Suppress("UNCHECKED_CAST")
class HtmlCreator(
    val dataList: List<Map<String, *>>,
    val validationMode: Boolean = false,
    val currentDate: LocalDate,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    private val document: Document = createHTMLDocument()
        .html {
            body {
                id = "body"
                header {
                    div {
                        id = "logo"
                        img { src = "nav_logo.svg" }
                    }
                }
            }
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

    private fun addElementWithPossiblyChildren(map: Map<String, *>) {
        val elements = createElementsWithPossiblyChildren(map = map)
        val body = document.getElementById("body")
        elements.forEach {
            body.appendChild(it)
        }
    }

    private fun createElementsWithPossiblyChildren(map: Map<String, *>): List<Element> {
        val elementType = map["type"]
        var children = emptyList<Map<String, *>>()

        val applyClasses =
            if ((map["textAlign"] == "text-align-right") || (map["align"] == "right")) {
                mutableSetOf("alignRight")
            } else if ((map["textAlign"] == "text-align-center") || (map["align"] == "center")) {
                mutableSetOf("alignCenter")
            } else if ((map["align"] == "left")) {
                mutableSetOf("alignLeft")
            } else mutableSetOf()

        val inlineStyles = mutableSetOf<String>()

        if (map.containsKey("indent")) {
            val indent = map["indent"] as Int
            val alignment = if (map["align"] == "right") "right" else "left"
            inlineStyles += "margin-$alignment: ${20 * indent}pt"
        }

        if (elementType != "page-break") {
            children = map["children"] as List<Map<String, *>>
        } else {
            applyClasses.add("pageBreak")
        }

        val element = when (elementType) {
            "h1" -> document.create.h1()
            "h2" -> document.create.h2()
            "h3" -> document.create.h3()
            "p" -> document.create.p()
            "ul" -> document.create.ul()
            "ol" -> document.create.ol()
            "li" -> document.create.li()
            "table" -> {
                val table = document.create.table {
                    classes = applyClasses
                    style = inlineStyles.joinToString(";")
                    if (map.containsKey("colSizes")) {
                        val colSizesInPx = map["colSizes"] as List<Int>
                        colGroup {
                            colSizesInPx.forEach { colSizeInPx ->
                                val width = if (colSizeInPx == 0) {
                                    "auto"
                                } else {
                                    (colSizeInPx.coerceAtLeast(48) * editorPxToPdfPtRatio).toString() + "pt"
                                }
                                col {
                                    style = "width: ${width};"
                                }
                            }
                        }
                    }
                }

                val tbody = document.create.tbody()

                loopOverChildren(children).forEach {
                    tbody.appendChild(it)
                }

                table.appendChild(tbody)
                return listOf(table)
            }

            "tr" -> {
                if (map.containsKey("size")) {
                    val heightInPx = map["size"] as Int
                    inlineStyles += "height: ${heightInPx * editorPxToPdfPtRatio}pt;"
                }
                document.create.tr()
            }

            "td" -> {
                document.create.td {
                    if (map.containsKey("colSpan")) {
                        attributes["colspan"] = map["colSpan"].toString()
                    }
                }
            }

            "page-break", "lic" -> document.create.div()

            "placeholder" -> {
                if (placeholderTextMissingInChildren(map)) {
                    if (validationMode) {
                        throw EmptyPlaceholderException("Placeholder error")
                    } else {
                        val text = map["placeholder"]
                        return listOf(createLeafElement(mapOf("text" to text), mutableSetOf("placeholder-text")))
                    }
                } else {
                    return loopOverChildren(children)
                }
            }

            // Create space between saksinfo and next paragraph in legacy templates
            "maltekstseksjon" -> {
                return if (map["section"] == "section-rev-v2") {
                    val wrapper = document.create.div {
                        this.classes = setOf("after-saksinfo")
                    }
                    loopOverChildren(children).forEach { wrapper.appendChild(it) }
                    listOf(wrapper)
                } else {
                    loopOverChildren(children)
                }
            }

            "maltekst", "redigerbar-maltekst", "regelverk" -> return loopOverChildren(children)

            "regelverk-container" -> {
                if (validationMode) {
                    if (children.isEmpty()) {
                        throw EmptyRegelverkException("Empty regelverk")
                    } else if (getTexts(map).isEmpty()) {
                        throw EmptyRegelverkException("Empty regelverk")
                    }
                }

                return loopOverChildren(children)
            }

            "label-content" -> {
                val result = map["result"]
                val label = map["label"] ?: throw RuntimeException("label mangler i label-content")

                if (result == null || result.toString().isEmpty()) {
                    return emptyList()
                }

                return listOf(document.create.p {
                    classes = setOf("label-content")
                    span {
                        classes = setOf("label")
                        +"$label"
                        +": "
                    }
                    span { +"$result" }
                })
            }

            "signature" -> {
                return if ((map["enabled"] as? Boolean) == false) {
                    emptyList()
                } else {
                    listOf(document.create.div {
                        classes = setOf("signature")
                        if (map.containsKey("medunderskriver")) {
                            val medunderskriver = map["medunderskriver"] as Map<String, Map<String, *>>
                            div {
                                classes = setOf("signature-column")
                                div { +medunderskriver["name"].toString() }
                                if (medunderskriver["title"] != null) {
                                    div { +medunderskriver["title"]!!.toString() }
                                }
                            }
                        }
                        if (map.containsKey("saksbehandler")) {
                            val saksbehandler = map["saksbehandler"] as Map<String, Map<String, *>>
                            div {
                                classes = setOf("signature-column")
                                div { +saksbehandler["name"].toString() }
                                if (saksbehandler["title"] != null) {
                                    div { +saksbehandler["title"]!!.toString() }
                                }
                            }
                        }
                    })
                }
            }

            "fullmektig" -> {
                val show = map["show"] as Boolean
                if (!show) {
                    return emptyList()
                }

                val key = children[0]["children"] as List<Map<String, *>>
                val value = children[1]["children"] as List<Map<String, *>>

                val elements = mutableListOf<Element>()

                val p = document.create.p {
                    classes = setOf("label-content")
                }

                val labelMap = key[0].toMutableMap().apply {
                    this["text"] = "${this["text"]}:"
                }

                p.appendChild(createLeafElement(labelMap, mutableSetOf("label")))
                p.appendChild(createLeafElement(map = value[0]))

                elements += p

                return elements
            }

            "saksnummer" -> return createSaksnummerElement(childMap = children[0], label = "Saksnummer: ")

            "arena-saksnummer" -> return createSaksnummerElement(
                childMap = children[0],
                label = "Saksnummer fra Arena: "
            )

            "saksinfo" -> {
                val saksinfo = document.create.div { classes = setOf("saksinfo") }

                val saksinfoChildren = map["children"] as List<Map<String, *>>
                loopOverChildren(saksinfoChildren).forEach { saksinfo.appendChild(it) }

                saksinfo.appendChild(document.create.div {
                    classes = setOf("current-date")
                    +getFormattedDate(currentDate)
                })

                return listOf(saksinfo)
            }

            "empty-void" -> return emptyList()

            else -> {
                when (elementType) {
                    "header" -> logger.info("legacy element type: header")
                    "footer" -> logger.info("legacy element type: footer")
                    else -> logger.warn("unknown element type: $elementType")
                }

                document.create.div()
            }
        }

        if (applyClasses.isNotEmpty()) {
            element.setAttribute("class", applyClasses.joinToString(" "))
        }
        if (inlineStyles.isNotEmpty()) {
            element.setAttribute("style", inlineStyles.joinToString(";"))
        }

        loopOverChildren(children).forEach {
            element.appendChild(it)
        }

        return listOf(element)
    }

    private fun createSaksnummerElement(childMap: Map<String, *>, label: String): List<Element> {
        val elements = mutableListOf<Element>()

        val children = createElementsWithPossiblyChildren(map = childMap)

        val p = document.create.p {
            classes = setOf("label-content")
            span {
                classes = setOf("label")
                +label
            }
        }

        children.forEach {
            p.appendChild(it)
        }

        elements += p

        return elements
    }

    private fun loopOverChildren(
        children: List<Map<String, *>>,
    ): List<Element> {
        return children.flatMap {
            if (isElement(it)) {
                createElementsWithPossiblyChildren(map = it)
            } else {
                listOf(createLeafElement(it))
            }
        }
    }

    private fun createLeafElement(map: Map<String, *>, inputClasses: MutableSet<String> = mutableSetOf()): Element {
        var text = map["text"]
        if (text == null) {
            throw RuntimeException("no content here")
        }
        text as String
        if (text.isEmpty()) {
            text = "\uFEFF"
        }

        if (map["bold"] == true) {
            inputClasses += "bold"
        }
        if (map["underline"] == true) {
            inputClasses += "underline"
        }
        if (map["italic"] == true) {
            inputClasses += "italic"
        }

        return document.create.span {
            classes = inputClasses
            +text
        }
    }

    fun getDoc(): Document {
        dataList.forEach {
            processElement(it)
        }

        val head = document.create.head {
            style {
                unsafe {
                    raw(
                        getCss()
                    )
                }
            }
        }

        document.childNodes.item(0).insertBefore(head, document.childNodes.item(0).firstChild)
        return document
    }

    private fun processElement(map: Map<String, *>) {
        when (map["type"]) {
            "current-date" -> setCurrentDate()
            else -> addElementWithPossiblyChildren(map)
        }
    }

    private fun setCurrentDate() {
        val body = document.getElementById("body")
        body.appendChild(document.create.div {
            classes = setOf("saksinfo")
            div {
                classes = setOf("current-date")
                +getFormattedDate(currentDate)
            }
        })
    }

    private fun placeholderTextMissingInChildren(map: Map<String, *>): Boolean {
        val children = map["children"] as List<Map<String, *>>

        val combinedText = children.joinToString(separator = "") {
            it["text"]?.toString() ?: ""
        }

        return combinedText.trim('​').trim().isEmpty()
    }


    private fun isElement(node: Map<String, *>): Boolean {
        //and not currentDate
        return node.containsKey("type")
    }
}

// PDF page content width in pt (A4 width 595pt - 64pt padding on each side, matching @page in Css.kt).
// Note: this is pt, not px - openhtmltopdf's CSS "px" is a 96dpi web pixel (1px = 0.75pt), while the
// visuelle retningslinjer for brev design guideline's "px" values are Figma pixels. Figma locks PDF
// export to a 1x scale (its "72dpi" default for asset exports), so 1 Figma pixel = 1/72in. openhtmltopdf
// specifies 1 pt as exactly 1/72in too (the standard PDF point), so 1 Figma pixel = 1 openhtmltopdf pt.
// Mixing up Figma px with openhtmltopdf's 96dpi px is what caused pages to render at 75% of true A4 size.
const val pdfPaddingInlinePt = 64.0
const val pdfContentWidthPt = 595.0 - pdfPaddingInlinePt * 2

// Content width in smart editor (MAX_TABLE_WIDTH = SHEET_WIDTH_PX - PADDING_INLINE_PX * 2 = 800 - 64 * 2)
// This one genuinely is browser px (96dpi), since the smart editor renders in an actual browser.
// It's a coincidence that the numeral (64) matches pdfPaddingInlinePt above - they are not the same
// unit, and could diverge independently (e.g. if the smart editor's on-screen padding ever changes).
const val smartEditorPaddingInlinePx = 64.0
const val smartEditorContentWidthPx = 800.0 - smartEditorPaddingInlinePx * 2

// Ratio for converting a smart editor measurement (browser px) into the equivalent PDF measurement (pt).
const val editorPxToPdfPtRatio = pdfContentWidthPt / smartEditorContentWidthPx
