package no.nav.klage.pdfgen.transformers

import kotlinx.html.body
import kotlinx.html.classes
import kotlinx.html.col
import kotlinx.html.colGroup
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.li
import kotlinx.html.ol
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.tr
import kotlinx.html.ul
import kotlinx.html.unsafe
import no.nav.klage.pdfgen.api.view.DocumentValidationResponse.DocumentValidationError
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

    private val validationErrors = mutableSetOf<DocumentValidationError>()

    private val document: Document =
        createHTMLDocument()
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
            } else {
                mutableSetOf()
            }

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

        val element =
            when (elementType) {
                "h1" -> {
                    document.create.h1()
                }

                "h2" -> {
                    document.create.h2()
                }

                "h3" -> {
                    document.create.h3()
                }

                "p" -> {
                    document.create.p()
                }

                "ul" -> {
                    document.create.ul()
                }

                "ol" -> {
                    document.create.ol()
                }

                "li" -> {
                    document.create.li()
                }

                "table" -> {
                    val table =
                        document.create.table {
                            classes = applyClasses
                            style = inlineStyles.joinToString(";")
                            if (map.containsKey("colSizes")) {
                                val colSizesInPx = map["colSizes"] as List<Int>
                                colGroup {
                                    colSizesInPx.forEach { colSizeInPx ->
                                        val width =
                                            if (colSizeInPx == 0) {
                                                "auto"
                                            } else {
                                                (colSizeInPx.coerceAtLeast(48) * EDITOR_PX_TO_PDF_PT_RATIO).toString() + "pt"
                                            }
                                        col {
                                            style = "width: $width;"
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
                        inlineStyles += "height: ${heightInPx * EDITOR_PX_TO_PDF_PT_RATIO}pt;"
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

                "page-break", "lic" -> {
                    document.create.div()
                }

                "placeholder" -> {
                    if (placeholderTextMissingInChildren(map)) {
                        if (validationMode) {
                            validationErrors.add(DocumentValidationError.EMPTY_PLACEHOLDER)
                        }
                        val text = map["placeholder"]
                        return listOf(createLeafElement(map = mapOf("text" to text), inputClasses = mutableSetOf("placeholder-text")))
                    } else {
                        return loopOverChildren(children)
                    }
                }

                // Create space between saksinfo and next paragraph in legacy templates
                "maltekstseksjon" -> {
                    return if (map["section"] == "section-rev-v2") {
                        val wrapper =
                            document.create.div {
                                this.classes = setOf("after-saksinfo")
                            }
                        loopOverChildren(children).forEach { wrapper.appendChild(it) }
                        listOf(wrapper)
                    } else {
                        loopOverChildren(children)
                    }
                }

                "maltekst", "redigerbar-maltekst", "regelverk" -> {
                    return loopOverChildren(children)
                }

                "regelverk-container" -> {
                    if (validationMode && (children.isEmpty() || getTexts(map).isEmpty())) {
                        validationErrors.add(DocumentValidationError.EMPTY_REGELVERK)
                    }

                    return loopOverChildren(children)
                }

                "label-content" -> {
                    val result = map["result"]
                    val label = map["label"] ?: throw RuntimeException("label mangler i label-content")

                    if (result == null || result.toString().isEmpty()) {
                        return emptyList()
                    }

                    return listOf(
                        document.create.p {
                            classes = setOf("label-content")
                            span {
                                classes = setOf("label")
                                +"$label"
                                +": "
                            }
                            span { +"$result" }
                        },
                    )
                }

                "signature" -> {
                    return if ((map["enabled"] as? Boolean) == false) {
                        emptyList()
                    } else {
                        listOf(
                            document.create.div {
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
                            },
                        )
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

                    val p =
                        document.create.p {
                            classes = setOf("label-content")
                        }

                    val labelMap =
                        key[0].toMutableMap().apply {
                            this["text"] = "${this["text"]}:"
                        }

                    p.appendChild(createLeafElement(map = labelMap, inputClasses = mutableSetOf("label")))
                    p.appendChild(createLeafElement(map = value[0]))

                    elements += p

                    return elements
                }

                "saksnummer" -> {
                    return createSaksnummerElement(children = children, label = "Saksnummer: ")
                }

                "arena-saksnummer" -> {
                    return createSaksnummerElement(
                        children = children,
                        label = "Saksnummer fra Arena: ",
                    )
                }

                "saksinfo" -> {
                    val saksinfo = document.create.div { classes = setOf("saksinfo") }

                    val saksinfoChildren = map["children"] as List<Map<String, *>>
                    loopOverChildren(saksinfoChildren).forEach { saksinfo.appendChild(it) }

                    saksinfo.appendChild(
                        document.create.div {
                            classes = setOf("current-date")
                            +getFormattedDate(currentDate)
                        },
                    )

                    return listOf(saksinfo)
                }

                "empty-void" -> {
                    return emptyList()
                }

                // Legacy topptekst/bunntekst - content was already never rendered (only "content"
                // field, which is never read). Must return emptyList() rather than an empty div:
                // even an empty div ends up with a leftover FEFF-only text node from
                // createLeafElement, which is enough "real" content to block margin collapsing
                // between whatever precedes and follows it.
                "header" -> {
                    logger.info("legacy element type: header")
                    return emptyList()
                }

                "footer" -> {
                    logger.info("legacy element type: footer")
                    return emptyList()
                }

                else -> {
                    logger.warn("unknown element type: $elementType")
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

    private fun createSaksnummerElement(
        children: List<Map<String, *>>,
        label: String,
    ): List<Element> {
        val elements = mutableListOf<Element>()

        // The placeholder is the actual saksnummer element. It may be surrounded by empty
        // Slate cursor-anchor text nodes (e.g. {"text": ""}), so we find it rather than assuming
        // a fixed position.
        val placeholderMap = children.first { isElement(it) }

        val placeholderElements = createElementsWithPossiblyChildren(map = placeholderMap)

        val p =
            document.create.p {
                classes = setOf("label-content")
                span {
                    classes = setOf("label")
                    +label
                }
            }

        placeholderElements.forEach {
            p.appendChild(it)
        }

        elements += p

        return elements
    }

    private fun loopOverChildren(children: List<Map<String, *>>): List<Element> =
        children.flatMap {
            if (isElement(it)) {
                createElementsWithPossiblyChildren(map = it)
            } else {
                listOf(createLeafElement(it))
            }
        }

    private fun createLeafElement(
        map: Map<String, *>,
        inputClasses: MutableSet<String> = mutableSetOf(),
    ): Element {
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

    fun getValidationErrors(): Set<DocumentValidationError> {
        dataList.forEach {
            processElement(it)
        }
        return validationErrors
    }

    fun getDoc(): Document {
        dataList.forEach {
            processElement(it)
        }

        val head =
            document.create.head {
                style {
                    unsafe {
                        raw(
                            getCss(),
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
        body.appendChild(
            document.create.div {
                classes = setOf("saksinfo")
                div {
                    classes = setOf("current-date")
                    +getFormattedDate(currentDate)
                }
            },
        )
    }

    private fun placeholderTextMissingInChildren(map: Map<String, *>): Boolean {
        val children = map["children"] as List<Map<String, *>>

        val combinedText =
            children.joinToString(separator = "") {
                it["text"]?.toString() ?: ""
            }

        return combinedText.trim('​').trim().isEmpty()
    }

    private fun isElement(node: Map<String, *>): Boolean {
        // and not currentDate
        return node.containsKey("type")
    }
}

// Ratio for converting a smart editor measurement (browser px) into the equivalent PDF measurement (pt).
//
// The smart editor renders its sheet at genuine 96dpi browser px (see SHEET_WIDTH_PT/ptToPx in the
// frontend's get-scaled-em.ts), while openhtmltopdf's CSS "px" is a 96dpi web pixel too, but we emit
// "pt" units directly, and openhtmltopdf's pt is the standard PDF point (1/72in). Since both sides
// derive their pixel sizes from the same pt-based sheet dimensions (595pt wide, 64pt inline padding),
// converting a browser px back to pt is just the fixed 96dpi -> 72dpi factor: 1px = 72/96 pt = 0.75pt.
const val EDITOR_PX_TO_PDF_PT_RATIO = 0.75
