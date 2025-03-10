package no.nav.klage.pdfgen.util

import no.nav.klage.kodeverk.TimeUnitType
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.kodeverk.ytelse.ytelseToDisplayName
import java.time.LocalDate

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

fun getBehandlingstidText(behandlingstidDate: LocalDate?, behandlingstidUnitTypeId: String?, behandlingstidUnits: Int?): String {
    if (behandlingstidDate != null) {
        return getFormattedDate(behandlingstidDate)
    } else if (behandlingstidUnits != null && behandlingstidUnitTypeId != null) {
        return behandlingstidUnits.toString() + when (TimeUnitType.of(
            behandlingstidUnitTypeId
        )) {
            TimeUnitType.WEEKS -> {
                if (behandlingstidUnits == 1) {
                    " uke"
                } else {
                    " uker"
                }
            }

            TimeUnitType.MONTHS -> {
                if (behandlingstidUnits == 1) {
                    " måned"
                } else {
                    " måneder"
                }
            }
        } + " fra du mottok dette brevet"
    } else {
        throw Exception("Needs date or units and unit type")
    }
}