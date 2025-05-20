package no.nav.klage.pdfgen.config

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.spi.FilterReply
import org.slf4j.Marker

class FontLogFilter : TurboFilter() {

    override fun decide(
        marker: Marker?,
        logger: Logger?,
        level: Level?,
        format: String?,
        params: Array<out Any>?,
        throwable: Throwable?
    ): FilterReply {
        if (level == Level.WARN && format != null &&
            (format.contains("6 new fonts found, font cache will be re-built") ||
                    format.contains("Building on-disk font cache, this may take a while") ||
                    format.contains("Finished building on-disk font cache, found 6 fonts") ||
                    format.contains("Using fallback font LiberationSans for base font Helvetica") ||
                    format.contains("Using fallback font LiberationSans for base font ZapfDingbats")
                    )
        ) {
            return FilterReply.DENY
        }

        return FilterReply.NEUTRAL
    }
}