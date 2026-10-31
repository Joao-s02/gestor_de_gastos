package com.joao.gastos.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Locale

@JvmInline
value class Money(val cents: Long) {
    init {
        require(cents > 0) { "El importe debe ser mayor que 0" }
        require(cents <= MAX_CENTS) { "El importe máximo es 999.999,99" }
    }

    companion object {
        const val MAX_CENTS = 99_999_999L

        fun validationError(raw: String, locale: Locale = Locale.getDefault()): String? {
            if (raw.isBlank()) return "El importe es obligatorio"
            if (hasTooManyDecimals(raw, locale)) return "Máximo 2 decimales"
            val money = parse(raw, locale) ?: return "El importe debe ser mayor que 0 y como máximo 999.999,99"
            return if (money.cents > MAX_CENTS) "El importe máximo es 999.999,99" else null
        }

        fun parse(raw: String, locale: Locale = Locale.getDefault()): Money? {
            val text = raw.trim()
            if (text.isEmpty() || hasTooManyDecimals(text, locale)) return null
            val format = NumberFormat.getNumberInstance(locale)
            if (format is DecimalFormat) format.isParseBigDecimal = true
            val position = ParsePosition(0)
            val parsed = format.parse(text, position) ?: return null
            if (position.index != text.length) return null
            val amount = parsed as? BigDecimal ?: BigDecimal(parsed.toString())
            if (amount.signum() <= 0) return null
            val cents = try {
                amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact()
            } catch (_: ArithmeticException) {
                return null
            }
            if (cents <= 0 || cents > MAX_CENTS) return null
            return Money(cents)
        }

        fun format(cents: Long, locale: Locale = Locale.getDefault()): String {
            return NumberFormat.getCurrencyInstance(locale).format(BigDecimal.valueOf(cents, 2))
        }

        fun formatInput(cents: Long, locale: Locale = Locale.getDefault()): String {
            val format = NumberFormat.getNumberInstance(locale).apply {
                isGroupingUsed = false
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            return format.format(BigDecimal.valueOf(cents, 2))
        }

        private fun hasTooManyDecimals(raw: String, locale: Locale): Boolean {
            val symbols = (NumberFormat.getNumberInstance(locale) as DecimalFormat).decimalFormatSymbols
            val withoutGrouping = raw.trim().replace(symbols.groupingSeparator.toString(), "")
            val decimalIndex = withoutGrouping.indexOf(symbols.decimalSeparator)
            if (decimalIndex < 0) return false
            if (withoutGrouping.indexOf(symbols.decimalSeparator, decimalIndex + 1) >= 0) return true
            return withoutGrouping.length - decimalIndex - 1 > 2
        }
    }
}
