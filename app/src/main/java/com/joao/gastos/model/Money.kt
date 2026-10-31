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
            val amount = normalize(raw) ?: return null
            if (amount.signum() <= 0) return null
            val cents = try {
                amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact()
            } catch (_: ArithmeticException) {
                return null
            }
            if (cents <= 0L || cents > MAX_CENTS) return null
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
            val separator = text.lastIndexOfAny(DECIMAL_MARKS)
            if (separator < 0) return false
            val fractionLength = text.length - separator - 1
            val onlyOneMark = text.count { it == ',' || it == '.' } == 1
            if (fractionLength == 3 && onlyOneMark) return false
            return fractionLength > 2
        }

        private fun normalize(raw: String): BigDecimal? {
            val text = cleaned(raw)
            if (text.isEmpty() || text.any { it != ',' && it != '.' && !it.isDigit() }) return null
            val lastComma = text.lastIndexOf(',')
            val lastDot = text.lastIndexOf('.')
            val decimalIndex = when {
                lastComma >= 0 && lastDot >= 0 -> maxOf(lastComma, lastDot)
                lastComma >= 0 || lastDot >= 0 -> {
                    val index = maxOf(lastComma, lastDot)
                    val fractionLength = text.length - index - 1
                    val onlyOneMark = text.count { it == ',' || it == '.' } == 1
                    if (fractionLength == 3 && onlyOneMark) -1 else index
                }
                else -> -1
            }
            val normalized = if (decimalIndex >= 0) {
                val fraction = text.substring(decimalIndex + 1)
                if (fraction.length > 2 || fraction.any { !it.isDigit() }) return null
                val integer = text.substring(0, decimalIndex).filter { it.isDigit() }
                if (integer.isEmpty() && fraction.isEmpty()) return null
                if (fraction.isEmpty()) integer.ifEmpty { "0" } else "${integer.ifEmpty { "0" }}.$fraction"
            } else {
                if ((lastComma >= 0 || lastDot >= 0) && !isThousands(text)) return null
                text.filter { it.isDigit() }.ifEmpty { return null }
            }
            return try {
                BigDecimal(normalized)
            } catch (_: NumberFormatException) {
                null
            }
        }

        private fun isThousands(text: String): Boolean {
            val parts = text.split(',', '.')
            if (parts.size < 2) return true
            if (parts.first().isEmpty() || parts.first().length > 3 || parts.first().any { !it.isDigit() }) return false
            return parts.drop(1).all { part -> part.length == 3 && part.all { it.isDigit() } }
        }

        private fun cleaned(raw: String): String = raw.trim().replace(" ", "").replace("\u00a0", "")

        private val DECIMAL_MARKS = charArrayOf(',', '.')
    }
}
