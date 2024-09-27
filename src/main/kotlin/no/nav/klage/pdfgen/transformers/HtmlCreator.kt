package no.nav.klage.pdfgen.transformers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import no.nav.klage.pdfgen.api.view.DocumentToPdfRequest
import no.nav.klage.pdfgen.exception.EmptyPlaceholderException
import no.nav.klage.pdfgen.exception.EmptyRegelverkException
import no.nav.klage.pdfgen.exception.InvalidMaltekstseksjonException
import no.nav.klage.pdfgen.exception.InvalidRegelverkException
import no.nav.klage.pdfgen.util.getLogger
import no.nav.klage.pdfgen.util.getSecureLogger
import no.nav.klage.pdfgen.util.getFormattedDate
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.time.LocalDate

@Suppress("UNCHECKED_CAST")
class HtmlCreator(val data: DocumentToPdfRequest, val validationMode: Boolean = false) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    private val document: Document = createHTMLDocument()
        .html {
            body {
                id = "body"
                header {
                    div {
                        id = "header_text"
                    }
                    div {
                        id = "logo"
                        img { src = "nav_logo.png" }
                    }
                }
            }
        }

    private var footer = ""

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
            } else if ((map["align"] == "left")) {
                mutableSetOf("alignLeft")
            } else mutableSetOf()

        val inlineStyles = mutableSetOf<String>()

        if (map.containsKey("indent")) {
            val indent = map["indent"] as Int
            val alignment = if (map["align"] == "right") "right" else "left"
            inlineStyles += "margin-$alignment: ${24 * indent}pt"
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
                                    (colSizeInPx.coerceAtLeast(48) * pxToPtRatio).toString() + "pt"
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
                    inlineStyles += "height: ${(heightInPx * pxToPtRatio)}pt;"
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

            "maltekst", "redigerbar-maltekst" -> {
                return loopOverChildren(children)
            }

            "regelverk" -> {
                val query = map["query"]

                if (query !is String || query.isEmpty()) {
                    if (validationMode) {
                        throw InvalidRegelverkException("Missing query in regelverk")
                    }

                    return listOf(document.create.div {
                        classes = setOf("invalid")
                        loopOverChildren(children)
                    })
                }

                val queryMap = jacksonObjectMapper().readValue(query, Map::class.java)

                if (isEqualToBehandling(queryMap)) {
                    return loopOverChildren(children)
                }

                if (validationMode) {
                    throw InvalidRegelverkException("Mismatched query in regelverk")
                }

                return listOf(document.create.div {
                    classes = setOf("invalid")
                    loopOverChildren(children)
                })

            }

            "maltekstseksjon" -> {
                val language = map["language"]?.toString()

                if (language != data.language) {
                    if (validationMode) {
                        throw InvalidMaltekstseksjonException("Mismatched language in maltekstseksjon")
                    } else {
                        return listOf(document.create.div {
                            classes = setOf("invalid")
                            loopOverChildren(children)
                        })
                    }
                }

                val query = map["query"]

                if (query !is String || query.isEmpty()) {
                    if (validationMode) {
                        throw InvalidMaltekstseksjonException("Missing query in maltekstseksjon")
                    }

                    return listOf(document.create.div {
                        classes = setOf("invalid")
                        loopOverChildren(children)
                    })
                }

                val queryMap = jacksonObjectMapper().readValue(query, Map::class.java)

                if (isEqualToBehandling(queryMap)) {
                    return loopOverChildren(children)
                }

                if (validationMode) {
                    throw InvalidMaltekstseksjonException("Mismatched query in maltekstseksjon")
                }

                return listOf(document.create.div {
                    classes = setOf("maltekstseksjon", "invalid")
                    loopOverChildren(children)
                })
            }

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
                val label = map["label"] ?: "Ukjent etikett"

                if (result == null || result.toString().isEmpty()) {
                    return emptyList()
                }

                return listOf(document.create.span {
                    b {
                        +"$label"
                        +": "
                    }
                    +"$result"
                })
            }

            "signature" -> {
                return listOf(document.create.div {
                    style = "margin-top: 24pt;"
                    classes = setOf("wrapper")
                    if (map.containsKey("medunderskriver")) {
                        val medunderskriver = map["medunderskriver"] as Map<String, Map<String, *>>
                        div {
                            classes = setOf("column")
                            div { +medunderskriver["name"].toString() }
                            if (medunderskriver["title"] != null) {
                                div { +medunderskriver["title"]!!.toString() }
                            }
                        }
                    }
                    if (map.containsKey("saksbehandler")) {
                        val saksbehandler = map["saksbehandler"] as Map<String, Map<String, *>>
                        div {
                            classes = setOf("column")
                            div { +saksbehandler["name"].toString() }
                            if (saksbehandler["title"] != null) {
                                div { +saksbehandler["title"]!!.toString() }
                            }
                        }
                    }
                })
            }

            "current-date" -> {
                return listOf(document.create.div {
                    classes = setOf("current-date")
                    +"Dato: ${getFormattedDate(LocalDate.now())}"
                })
            }

            "empty-void" -> document.create.div()

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

    private fun isEqualToBehandling(map: Map<*, *>): Boolean {
        if (map.isEmpty()) {
            return false
        }

        map.forEach { (key, value) ->
            if (key == "utfallIdList") {
                if (value != data.utfallIdList) {
                    return false
                }
            }

            if (key == "ytelseHjemmelIdList") {
                if (value !is List<*> || data.ytelseHjemmelIdList.size == value.size || data.ytelseHjemmelIdList.containsAll(value)) {
                    return false
                }
            }

            if (key == "templateSectionIdList") {
                if (value !is List<*> || data.templateSectionIdList.size != value.size || !data.templateSectionIdList.containsAll(value)) {
                    return false
                }
            }

            if (key == "language") {
                if (value != data.language) {
                    return false
                }
            }
        }

        return true
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
        var text = map["text"] ?: throw RuntimeException("no content here")
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
        (jacksonObjectMapper().readValue(data.json, List::class.java) as List<Map<String, *>>).forEach {
            processElement(it)
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

        document.childNodes.item(0).insertBefore(head, document.childNodes.item(0).firstChild)

        secureLogger.debug(document.serialize())
        return document
    }

    private fun processElement(map: Map<String, *>) {
        when (map["type"]) {
            "header" -> setHeaderText(map)
            "footer" -> setFooter(map)
            else -> addElementWithPossiblyChildren(map)
        }
    }

    private fun placeholderTextMissingInChildren(map: Map<String, *>): Boolean {
        val children = map["children"] as List<Map<String, *>>
        return children.any { it["text"] == null || it["text"].toString().trim('​').trim().isEmpty() }
    }

    private fun setHeaderText(map: Map<String, *>) {
        val span = document.getElementById("header_text")
        span.textContent = map["content"]?.toString() ?: " "
    }

    private fun setFooter(map: Map<String, *>) {
        footer = map["content"]?.toString()?.replace("\n", "\\A") ?: ""
    }

    private fun isElement(node: Map<String, *>): Boolean {
        //and not currentDate
        return node.containsKey("type")
    }
}

const val pxToPtRatio = 0.75
