package no.nav.klage.pdfgen.util

fun String.toFnrView() = this.substring(0, 6) + " " + this.substring(6)