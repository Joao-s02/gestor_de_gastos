package com.joao.gastos.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
    @Test
    fun commaWithTwoDigitsIsDecimalNotThousands() {
        assertEquals(1_250L, Money.parse("12,50")?.cents)
    }

    @Test
    fun dotWithTwoDigitsIsDecimal() {
        assertEquals(1_250L, Money.parse("12.50")?.cents)
    }

    @Test
    fun keepsRealThousandsSeparators() {
        assertEquals(125_000L, Money.parse("1,250")?.cents)
        assertEquals(125_050L, Money.parse("1.250,50")?.cents)
        assertEquals(125_000L, Money.parse("1,250.00")?.cents)
    }

    @Test
    fun rejectsEmptyZeroAndExtraDecimals() {
        assertNull(Money.parse(""))
        assertNull(Money.parse("0"))
        assertNull(Money.parse("0,00"))
        assertNull(Money.parse("12,5099"))
        assertEquals("Máximo 2 decimales", Money.validationError("12,5099"))
    }
}
