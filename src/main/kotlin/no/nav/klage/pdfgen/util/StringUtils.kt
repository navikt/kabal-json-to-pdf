package no.nav.klage.pdfgen.util

import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.kodeverk.ytelse.ytelseToDisplayName

fun String.toFnrView() = this.substring(0, 6) + " " + this.substring(6)

fun String.decapitalize(): String {
    return if (!this.startsWith("NAV")) {
        this.replaceFirstChar(Char::lowercase)
    } else this
}

fun getYtelseDisplayText(ytelseId: String): String {
    val ytelse = Ytelse.of(ytelseId)
    return ytelseToDisplayName[ytelse]!!.nb.decapitalize()
}