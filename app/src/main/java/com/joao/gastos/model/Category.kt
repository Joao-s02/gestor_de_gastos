package com.joao.gastos.model

enum class Category(
    val label: String,
    private val allowedTypes: Set<MovementType>,
) {
    Comida("Comida", setOf(MovementType.GASTO)),
    Transporte("Transporte", setOf(MovementType.GASTO)),
    Hogar("Hogar", setOf(MovementType.GASTO)),
    Ocio("Ocio", setOf(MovementType.GASTO)),
    Salud("Salud", setOf(MovementType.GASTO)),
    Compras("Compras", setOf(MovementType.GASTO)),
    Servicios("Servicios", setOf(MovementType.GASTO)),
    Salario("Salario", setOf(MovementType.INGRESO)),
    Freelance("Freelance", setOf(MovementType.INGRESO)),
    Ventas("Ventas", setOf(MovementType.INGRESO)),
    Regalo("Regalo", setOf(MovementType.INGRESO)),
    Reembolso("Reembolso", setOf(MovementType.INGRESO)),
    Otros("Otros", setOf(MovementType.GASTO, MovementType.INGRESO)),
    ;

    fun allows(type: MovementType): Boolean = type in allowedTypes

    companion object {
        fun fromStored(name: String): Category? = entries.find { it.name == name }

        fun forType(type: MovementType): List<Category> = entries.filter { it.allows(type) }
    }
}
