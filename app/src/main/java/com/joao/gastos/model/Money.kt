package com.joao.gastos.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

@JvmInline
value class Money(val cents: Long) {
    init {
        require(cents > 0) { "El importe debe ser mayor que 0" }
        require(cents <= MAX_CENTS) { "El importe máximo es 999.999,99" }
    }

    companion object {
        const val MAX_CENTS = 99_999_999L

        fun validationError(raw: String): String? {
            if (raw.isBlank()) return "El importe es obligatorio"
            if (hasTooManyDecimals(raw)) return "Máximo 2 decimales"
            return if (parse(raw) == null) "El importe debe ser mayor que 0 y como máximo 999.999,99" else null
        }

        fun parse(raw: String): Money? {
            val amount = toAmount(raw) ?: return null
            if (amount.signum() <= 0) return null
            val cents = amount.toCents() ?: return null
            if (cents > MAX_CENTS) return null
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

        private fun hasTooManyDecimals(raw: String): Boolean {
            val text = cleaned(raw)
            val decimalAt = decimalSeparatorIndex(text)
            return decimalAt >= 0 && fractionOf(text, decimalAt).length > 2
        }

        private fun toAmount(raw: String): BigDecimal? {
            val text = cleaned(raw)
            if (text.isEmpty() || text.any { !it.isDigit() && it != ',' && it != '.' }) return null
            val decimalAt = decimalSeparatorIndex(text)
            val plain = if (decimalAt >= 0) {
                val fraction = fractionOf(text, decimalAt)
                if (fraction.length > 2 || fraction.any { !it.isDigit() }) return null
                val integer = digitsBefore(text, decimalAt)
                if (integer.isEmpty() && fraction.isEmpty()) return null
                if (fraction.isEmpty()) integer.ifEmpty { "0" } else "${integer.ifEmpty { "0" }}.$fraction"
            } else {
                if (text.any { it == ',' || it == '.' } && !isThousandsGrouped(text)) return null
                text.filter { it.isDigit() }.ifEmpty { return null }
            }
            return plain.toBigDecimalOrNull()
        }

        private fun decimalSeparatorIndex(text: String): Int {
            val lastComma = text.lastIndexOf(',')
            val lastDot = text.lastIndexOf('.')
            if (lastComma < 0 && lastDot < 0) return -1
            if (lastComma >= 0 && lastDot >= 0) return maxOf(lastComma, lastDot)
            val onlyMark = maxOf(lastComma, lastDot)
            val looksLikeThousands = fractionOf(text, onlyMark).length == 3 && text.count { it == ',' || it == '.' } == 1
            return if (looksLikeThousands) -1 else onlyMark
        }

        private fun isThousandsGrouped(text: String): Boolean {
            val parts = text.split(',', '.')
            if (parts.size < 2) return true
            if (parts.first().isEmpty() || parts.first().length > 3 || parts.first().any { !it.isDigit() }) return false
            return parts.drop(1).all { part -> part.length == 3 && part.all { it.isDigit() } }
        }

        private fun BigDecimal.toCents(): Long? {
            return try {
                movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact()
            } catch (_: ArithmeticException) {
                null
            }
        }

        private fun fractionOf(text: String, separatorAt: Int): String = text.substring(separatorAt + 1)

        private fun digitsBefore(text: String, separatorAt: Int): String = text.substring(0, separatorAt).filter { it.isDigit() }

        private fun cleaned(raw: String): String = raw.trim().replace(" ", "").replace("\u00a0", "")
    }
}
