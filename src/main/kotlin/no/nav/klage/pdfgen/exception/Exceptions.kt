package no.nav.klage.pdfgen.exception

class ValidationException(msg: String) : RuntimeException(msg)
class EmptyPlaceholderException(msg: String) : RuntimeException(msg)
class EmptyRegelverkException(msg: String) : RuntimeException(msg)