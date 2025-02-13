package no.nav.klage.pdfgen.util

import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.kodeverk.ytelse.ytelseToDisplayName

fun String.toFnrView() = this.substring(0, 6) + " " + this.substring(6)

fun String.decapitalize(): String {
    return if (!this.startsWith("NAV")) {
        this.replaceFirstChar(Char::lowercase)
    } else this
}

fun getYtelseDisplayText(ytelseId: String?, ytelsenavn: String?): String {
    return if (ytelseId != null) {
        val ytelse = Ytelse.of(ytelseId)
        ytelseToDisplayName[ytelse]!!.nb.decapitalize()
    } else {
        ytelsenavn!!.toSpecialCase()
    }
}

private fun String.toSpecialCase(): String {
    val strings = this.split(" - ")
    return when (strings.size) {
        1 -> {
            this.decapitalize()
        }

        2 -> {
            if (strings[0].equals(other = strings[1], ignoreCase = true)) {
                strings[0].decapitalize()
            } else {
                strings[0].decapitalize() + " - " + strings[1].decapitalize()
            }
        }

        else -> this
    }
}