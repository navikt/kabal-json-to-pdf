package no.nav.klage.pdfgen.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun getFormattedDate(localDate: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.forLanguageTag("no"))
    return localDate.format(formatter)
}
